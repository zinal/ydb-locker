package ru.yandex.cloud.locker;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author mzinal
 */
public class LockerOwner implements Serializable {

    protected final String typeId;
    protected final String instanceId;

    public LockerOwner() {
        this.typeId = "-";
        this.instanceId = "-";
    }

    public LockerOwner(LockerOwner ref) {
        this.typeId = ref.typeId;
        this.instanceId = ref.instanceId;
    }

    public LockerOwner(String typeId) {
        this.typeId = (typeId==null) ? "-" : typeId;
        this.instanceId = "-";
    }

    public LockerOwner(String typeId, String instanceId) {
        this.typeId = (typeId==null) ? "-" : typeId;
        this.instanceId = (instanceId==null) ? "-" : instanceId;
    }

    public String getTypeId() {
        return typeId;
    }

    public String getInstanceId() {
        return instanceId;
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
        if (!(obj instanceof LockerOwner)) {
            return false;
        }
        final LockerOwner other = (LockerOwner) obj;
        if (!Objects.equals(this.typeId, other.typeId)) {
            return false;
        }
        return Objects.equals(this.instanceId, other.instanceId);
    }

}
