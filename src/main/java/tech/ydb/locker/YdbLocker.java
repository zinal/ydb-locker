package tech.ydb.locker;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author mzinal
 */
public class YdbLocker {

    private final YdbConnector connector;
    private final String tableName;

    private final String sqlCreate;
    private final String sqlLock;
    private final String sqlUnlock;

    public YdbLocker(YdbConnector connector) {
        this(connector, grabTableName(connector));
    }

    public YdbLocker(YdbConnector connector, String tableName) {
        this.connector = connector;
        this.tableName = (tableName == null) ? "ydb_locker" : tableName;
        sqlCreate = resource("query-table.sql");
        sqlLock = resource("query-lock.sql");
        sqlUnlock = resource("query-unlock.sql");
    }

    public YdbLockResponse lock(YdbLockRequest req) {
        return null;
    }

    public void unlock(YdbUnlockRequest req) {

    }

    private static String grabTableName(YdbConnector connector) {
        YdbConfig config = connector.getConfig();
        String prefix = config.getPrefix();
        return config.getProperties().getProperty(prefix + "locker.table");
    }

    private static String resource(String name) {
        String path = YdbLocker.class.getPackage().getName().replace('.', '/') + "/" + name;
        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(path)) {
            if (is==null) {
                throw new IOException("No such resource");
            }
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int bytes;
            while ((bytes = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, bytes);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ix) {
            throw new RuntimeException("Cannot load resource " + name, ix);
        }
    }

}
