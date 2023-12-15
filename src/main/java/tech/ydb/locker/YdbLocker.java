package tech.ydb.locker;

/**
 *
 * @author mzinal
 */
public class YdbLocker {

    private final YdbConnector connector;

    public YdbLocker(YdbConnector connector) {
        this.connector = connector;
    }

    public YdbLockResponse lock(YdbLockRequest req) {
        return null;
    }

    public void unlock(YdbUnlockRequest req) {
        
    }

}
