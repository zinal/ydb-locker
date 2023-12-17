package tech.ydb.locker;

/**
 *
 * @author zinal
 */
public interface PessimisticLocker {

    YdbLockResponse lock(YdbLockRequest req);

    void unlock(YdbUnlockRequest req);

}
