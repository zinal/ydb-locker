package ru.yandex.cloud.locker.svc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ru.yandex.cloud.locker.LockerOwner;
import ru.yandex.cloud.locker.LockerRequest;
import ru.yandex.cloud.locker.LockerResponse;
import ru.yandex.cloud.locker.PessimisticLocker;

/**
 * Locker Service.
 *
 */
@Service
@Slf4j
public class LockerService {

    private final PessimisticLocker locker = new InMemoryLocker();

    public LockerResponse lock(LockerRequest request) {
        return locker.lock(request);
    }

    public void unlock(LockerOwner owner) {
        locker.unlock(owner);
    }

}
