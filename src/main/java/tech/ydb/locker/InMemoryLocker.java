package tech.ydb.locker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author zinal
 */
public class InMemoryLocker implements PessimisticLocker {

    private final Map<String, Entry> forwardMap = new HashMap<>();
    private final Map<YdbLockOwner, Set<Entry>> reverseMap = new HashMap<>();

    @Override
    public YdbLockResponse lock(YdbLockRequest request) {
        String typeId = request.getOwner().getTypeId();
        String instanceId = request.getOwner().getInstanceId();
        if (instanceId==null) {
            instanceId = "-";
        }
        YdbLockOwner owner = new YdbLockOwner(typeId, instanceId);
        YdbLockResponse response = new YdbLockResponse();
        synchronized(this) {
            for ( YdbLockItem item : request.getItems() ) {
                int badPoints = 0;
                for (String point : item.getPoints()) {
                    Entry e = forwardMap.get(point);
                    if (e != null && !(e.typeId.equals(typeId) && e.instanceId.equals(instanceId)) ) {
                        response.getRemaining().add(point);
                        ++badPoints;
                    }
                }
                if (badPoints==0) {
                    for (String point : item.getPoints()) {
                        final Entry e = new Entry(owner, point);
                        forwardMap.put(point, e);
                        Set<Entry> entries = reverseMap.get(owner);
                        if (entries==null) {
                            entries = new HashSet<>();
                            reverseMap.put(owner, entries);
                        }
                        entries.add(e);
                        response.getLocked().add(point);
                    }
                }
            }
        }
        response.setLocked(new ArrayList<>(new HashSet<>(response.getLocked())));
        response.setRemaining(new ArrayList<>(new HashSet<>(response.getRemaining())));
        return response;
    }

    @Override
    public void unlock(YdbUnlockRequest request) {
        String typeId = request.getOwner().getTypeId();
        String instanceId = request.getOwner().getInstanceId();
        if (instanceId==null) {
            instanceId = "-";
        }
        YdbLockOwner owner = new YdbLockOwner(typeId, instanceId);
        synchronized(this) {
            Set<Entry> entries = reverseMap.remove(owner);
            if (entries != null) {
                for (Entry e : entries) {
                    forwardMap.remove(e.objectId);
                }
            }
        }
    }

    private static final class Entry {
        final String typeId;
        final String instanceId;
        final String objectId;

        Entry(YdbLockOwner owner, String objectId) {
            this.typeId = owner.getTypeId();
            this.instanceId = owner.getInstanceId();
            this.objectId = objectId;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + Objects.hashCode(this.typeId);
            hash = 89 * hash + Objects.hashCode(this.instanceId);
            hash = 89 * hash + Objects.hashCode(this.objectId);
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
            final Entry other = (Entry) obj;
            if (!Objects.equals(this.typeId, other.typeId)) {
                return false;
            }
            if (!Objects.equals(this.instanceId, other.instanceId)) {
                return false;
            }
            return Objects.equals(this.objectId, other.objectId);
        }
    }

}
