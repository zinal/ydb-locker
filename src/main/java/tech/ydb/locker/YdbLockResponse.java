package tech.ydb.locker;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author mzinal
 */
public class YdbLockResponse implements Serializable {

    private List<String> locked;
    private List<String> remaining;

    public YdbLockResponse() {
    }

    public YdbLockResponse(List<String> locked, List<String> remaining) {
        this.locked = locked;
        this.remaining = remaining;
    }

    public boolean isComplete() {
        return (remaining==null) || remaining.isEmpty();
    }

    public List<String> getLocked() {
        return locked;
    }

    public void setLocked(List<String> locked) {
        this.locked = locked;
    }

    public List<String> getRemaining() {
        return remaining;
    }

    public void setRemaining(List<String> remaining) {
        this.remaining = remaining;
    }

}
