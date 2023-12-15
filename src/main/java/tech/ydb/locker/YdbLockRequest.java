package tech.ydb.locker;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mzinal
 */
public class YdbLockRequest implements Serializable {

    private YdbLockOwner owner;
    private List<YdbLockItem> items;

    public YdbLockRequest() {
    }

    public YdbLockRequest(YdbLockOwner owner) {
        this.owner = owner;
    }

    public YdbLockRequest(YdbLockOwner owner, List<YdbLockItem> items) {
        this.owner = owner;
        this.items = items;
    }

    public YdbLockOwner getOwner() {
        return owner;
    }

    public void setOwner(YdbLockOwner owner) {
        this.owner = owner;
    }

    public List<YdbLockItem> getItems() {
        return items;
    }

    public void setItems(List<YdbLockItem> items) {
        this.items = items;
    }

}
