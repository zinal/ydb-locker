package tech.ydb.locker;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author mzinal
 */
public class YdbLockOwner implements Serializable {

    private String typeId;
    private String instanceId;

    public YdbLockOwner() {
    }

    public YdbLockOwner(String typeId) {
        this.typeId = typeId;
    }

    public YdbLockOwner(String typeId, String instanceId) {
        this.typeId = typeId;
        this.instanceId = instanceId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.typeId);
        hash = 13 * hash + Objects.hashCode(this.instanceId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final YdbLockOwner other = (YdbLockOwner) obj;
        if (!Objects.equals(this.typeId, other.typeId)) {
            return false;
        }
        return Objects.equals(this.instanceId, other.instanceId);
    }

}
