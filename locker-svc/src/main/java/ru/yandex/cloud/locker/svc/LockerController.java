package ru.yandex.cloud.locker.svc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ru.yandex.cloud.locker.LockerOwner;
import ru.yandex.cloud.locker.LockerRequest;
import ru.yandex.cloud.locker.LockerResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("v1")
public class LockerController {

    private final LockerService lockerService;

    @RequestMapping(value = "/lock", method = RequestMethod.POST, produces = { "application/json"})
    public LockerResponse lock(@RequestBody LockerRequest request) {
        return lockerService.lock(request);
    }

    @RequestMapping(value = "/unlock", method = RequestMethod.POST)
    public void unlock(@RequestBody LockerOwner owner) {
        lockerService.unlock(owner);
    }

}
