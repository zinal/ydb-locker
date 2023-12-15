package tech.ydb.locker;

/**
 *
 * @author mzinal
 */
public class YdbUnlockRequest {

    private YdbLockOwner owner;

    public YdbUnlockRequest() {
    }

    public YdbUnlockRequest(YdbLockOwner owner) {
        this.owner = owner;
    }

    public YdbLockOwner getOwner() {
        return owner;
    }

    public void setOwner(YdbLockOwner owner) {
        this.owner = owner;
    }

}
