package tech.ydb.locker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import tech.ydb.table.transaction.TxControl;

/**
 *
 * @author mzinal
 */
public class YdbLocker {

    private final YdbConnector connector;
    private final String tableName;

    private final String sqlLock;
    private final String sqlUnlock;

    public YdbLocker(YdbConnector connector) {
        this(connector, grabTableName(connector));
    }

    public YdbLocker(YdbConnector connector, String tableName) {
        this.connector = connector;
        this.tableName = (tableName == null) ? "ydb_locker" : tableName;
        sqlLock = resource("query-lock.sql", this.tableName);
        sqlUnlock = resource("query-unlock.sql", this.tableName);
        ensureTableExists(connector, this.tableName);
    }

    public YdbLockResponse lock(YdbLockRequest req) {
        return null;
    }

    public void unlock(YdbUnlockRequest req) {

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
        YdbConfig config = connector.getConfig();
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
