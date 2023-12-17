package tech.ydb.locker;

import java.util.Collection;

/**
 *
 * @author zinal
 */
public interface PessimisticLocker {

    YdbLockResponse lock(YdbLockOwner owner, Collection<YdbLockItem> items);

    void unlock(YdbLockOwner owner);

}
