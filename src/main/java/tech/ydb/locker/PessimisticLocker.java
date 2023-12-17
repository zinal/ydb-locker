package tech.ydb.locker;

import java.io.Closeable;
import java.util.Collection;

/**
 *
 * @author zinal
 */
public interface PessimisticLocker extends Closeable {

    YdbLockResponse lock(YdbLockOwner owner, Collection<YdbLockItem> items);

    void unlock(YdbLockOwner owner);

    @Override
    default void close() {}

}
