package ru.yandex.cloud.locker;

import java.io.Closeable;
import java.util.List;

/**
 * Интерфейс для установки и снятия пессимистических блокировок.
 *
 * @author zinal
 */
public interface PessimisticLocker extends Closeable {

    LockerResponse lock(LockerRequest request);

    void unlock(LockerOwner owner);

    default LockerResponse lock(LockerOwner owner, List<LockerItem> items) {
        return lock(new LockerRequest(owner, items));
    }

    @Override
    default void close() {}

}
