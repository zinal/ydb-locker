package ru.yandex.cloud.locker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mzinal
 */
public class LockerRequest implements Serializable {

    private final LockerOwner owner;
    private final List<LockerItem> items;

    public LockerRequest(LockerOwner owner, List<LockerItem> items) {
        this.owner = owner;
        this.items = items;
    }

    public LockerRequest(LockerOwner owner) {
        this(owner, new ArrayList<>());
    }

    public LockerOwner getOwner() {
        return owner;
    }

    public List<LockerItem> getItems() {
        return items;
    }

}
