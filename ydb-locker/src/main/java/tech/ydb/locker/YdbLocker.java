package tech.ydb.locker;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import tech.ydb.core.Issue;
import tech.ydb.core.Result;
import tech.ydb.table.query.DataQueryResult;
import tech.ydb.table.query.Params;
import tech.ydb.table.transaction.TxControl;
import tech.ydb.table.values.ListType;
import tech.ydb.table.values.PrimitiveType;
import tech.ydb.table.values.PrimitiveValue;

/**
 * A very straightforward and dumb pessimistic locks implementation on top of YDB table.
 *
 * @author mzinal
 */
public class YdbLocker implements PessimisticLocker {

    private final boolean connectorOwner;
    private final YdbConnector connector;
    private final String tableName;

    private final String sqlLock;
    private final String sqlUnlock;

    private ListType typeListObjects;

    public YdbLocker(YdbConfig config) {
        this(new YdbConnector(config), grabTableName(config), true);
    }

    public YdbLocker(YdbConfig config, String tableName) {
        this(new YdbConnector(config), tableName, true);
    }

    public YdbLocker(YdbConnector connector, boolean connectorOwner) {
        this(connector, grabTableName(connector), connectorOwner);
    }

    public YdbLocker(YdbConnector connector, String tableName, boolean connectorOwner) {
        this.connectorOwner = connectorOwner;
        this.connector = connector;
        this.tableName = (tableName == null) ? "ydb_locker" : tableName;
        sqlLock = resource("query-lock.sql", this.tableName);
        sqlUnlock = resource("query-unlock.sql", this.tableName);
        typeListObjects = ListType.of(PrimitiveType.Text);
        ensureTableExists(connector, this.tableName);
    }

    @Override
    public void close() {
        if (connectorOwner) {
            connector.close();
        }
    }

    @Override
    public YdbLockResponse lock(YdbLockOwner owner, Collection<YdbLockItem> items) {
        if (owner==null || owner.getTypeId()==null) {
            return null;
        }
        if (items==null || items.isEmpty())
            return new YdbLockResponse();
        String typeId = owner.getTypeId();
        String instanceId = owner.getInstanceId();
        if (instanceId==null) {
            instanceId = "-";
        }
        List<YdbLockItem> lockItems = new ArrayList<>(new HashSet<>(items));
        List<CompletableFuture<Result<DataQueryResult>>> statements 
                = new ArrayList<>(lockItems.size());
        for (YdbLockItem item : lockItems) {
            Params params = Params.of(
                    "$h_type", PrimitiveValue.newText(typeId),
                    "$h_instance", PrimitiveValue.newText(instanceId),
                    "$object_ids", typeListObjects.newValueOwn(convert(item.getPoints())));
            CompletableFuture<Result<DataQueryResult>> statement =
                    connector.getRetryCtx().supplyResult(
                            session -> session.executeDataQuery(sqlLock,
                                    TxControl.serializableRw().setCommitTx(true), params));
            statements.add(statement);
        }
        final YdbLockResponse response = new YdbLockResponse();
        int position = 0;
        for (CompletableFuture<Result<DataQueryResult>> statement : statements) {
            YdbLockItem item = lockItems.get(position);
            ++position;
            Result<DataQueryResult> result = statement.join();
            if (result.isSuccess()) {
                if (response.getLocked() == null) {
                    response.setLocked(new ArrayList<>());
                }
                response.getLocked().addAll(item.getPoints());
            } else {
                boolean conflict = false;
                for (Issue issue : result.getStatus().getIssues()) {
                    if ( issue.getMessage().contains("LOCK_CONFLICT") )
                        conflict = true;
                }
                if (conflict) {
                    if (response.getRemaining() == null) {
                        response.setRemaining(new ArrayList<>());
                    }
                    response.getRemaining().addAll(item.getPoints());
                } else {
                    result.getStatus().expectSuccess();
                }
            }
        }
        if (response.getRemaining() != null) {
            response.getRemaining().removeAll(response.getLocked());
        }
        return response;
    }

    @Override
    public void unlock(YdbLockOwner owner) {
        if (owner==null || owner.getTypeId()==null) {
            return;
        }
        String typeId = owner.getTypeId();
        String instanceId = owner.getInstanceId();
        if (instanceId==null) {
            instanceId = "-";
        }

        Params params = Params.of(
            "$h_type", PrimitiveValue.newText(typeId),
            "$h_instance", PrimitiveValue.newText(instanceId)
        );
        connector.getRetryCtx().supplyResult(
                session -> session.executeDataQuery(sqlUnlock,
                        TxControl.serializableRw().setCommitTx(true), params))
                .join().getStatus().expectSuccess();
    }

    private static PrimitiveValue[] convert(Collection<String> lines) {
        if (lines==null) {
            return new PrimitiveValue[0];
        }
        PrimitiveValue[] ret = new PrimitiveValue[lines.size()];
        Iterator<String> it = lines.iterator();
        for (int i=0; i<ret.length; ++i) {
            ret[i] = PrimitiveValue.newText(it.next());
        }
        return ret;
    }

    private static void ensureTableExists(YdbConnector connector, String tableName) {
        String sqlSelect = "SELECT object_id FROM `" + tableName + "` ORDER BY object_id LIMIT 1";
        boolean tableExists = connector.getRetryCtx().supplyResult(
                session -> session.executeDataQuery(sqlSelect, TxControl.onlineRo()))
                .join().isSuccess();
        if (! tableExists) {
            String sqlCreate = resource("query-table.sql", tableName);
            connector.getRetryCtx().supplyStatus(
                    session -> session.executeSchemeQuery(sqlCreate)).join().expectSuccess();
        }
    }

    private static String grabTableName(YdbConnector connector) {
        return grabTableName(connector.getConfig());
    }

    private static String grabTableName(YdbConfig config) {
        String prefix = config.getPrefix();
        return config.getProperties().getProperty(prefix + "locker.table");
    }

    private static String resource(String name, String tableName) {
        try (InputStream is = YdbLocker.class.getResourceAsStream(name)) {
            if (is==null) {
                throw new IOException("No such resource");
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int bytes;
            while ((bytes = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, bytes);
            }
            String value = new String(baos.toByteArray(), StandardCharsets.UTF_8);
            return value.replace("{ydb_locker_table}", tableName);
        } catch (IOException ix) {
            throw new RuntimeException("Cannot load resource " + name, ix);
        }
    }

}
