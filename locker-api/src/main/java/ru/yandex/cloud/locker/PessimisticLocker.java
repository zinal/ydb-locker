package ru.yandex.cloud.locker;

import java.io.Closeable;

/**
 * Интерфейс для установки и снятия пессимистических блокировок.
 *
 * @author zinal
 */
public interface PessimisticLocker extends Closeable {

    LockerResponse lock(LockerRequest request);

    void unlock(LockerOwner owner);

    @Override
    default void close() {}

}
