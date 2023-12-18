package ru.yandex.cloud.locker.svc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Objects;

import ru.yandex.cloud.locker.LockerItem;
import ru.yandex.cloud.locker.LockerOwner;
import ru.yandex.cloud.locker.LockerRequest;
import ru.yandex.cloud.locker.LockerResponse;
import ru.yandex.cloud.locker.PessimisticLocker;

/**
 * A very straightforward and dumb in-memory pessimistic locks implementation.
 *
 * @author zinal
 */
public class InMemoryLocker implements PessimisticLocker {

    private final Map<String, Entry> forwardMap = new HashMap<>();
    private final Map<LockerOwner, Set<Entry>> reverseMap = new HashMap<>();

    @Override
    public LockerResponse lock(LockerRequest request) {
        if (request==null || request.getOwner()==null) {
            return null;
        }
        if (request.getItems()==null || request.getItems().isEmpty())
            return new LockerResponse();
        LockerResponse response = new LockerResponse();
        synchronized(this) {
            for ( LockerItem item : request.getItems() ) {
                int badPoints = 0;
                for (String point : item.getPoints()) {
                    Entry e = forwardMap.get(point);
                    if (e != null && !request.getOwner().equals(e) ) {
                        ++badPoints;
                    }
                }
                if (badPoints > 0) {
                    response.getRemaining().addAll(item.getPoints());
                } else {
                    for (String point : item.getPoints()) {
                        final Entry e = new Entry(request.getOwner(), point);
                        forwardMap.put(point, e);
                        Set<Entry> entries = reverseMap.get(request.getOwner());
                        if (entries==null) {
                            entries = new HashSet<>();
                            reverseMap.put(request.getOwner(), entries);
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
    public void unlock(LockerOwner owner) {
        if (owner==null) {
            return;
        }
        String typeId = owner.getTypeId();
        String instanceId = owner.getInstanceId();
        if (instanceId==null) {
            instanceId = "-";
        }
        owner = new LockerOwner(typeId, instanceId);
        synchronized(this) {
            Set<Entry> entries = reverseMap.remove(owner);
            if (entries != null) {
                for (Entry e : entries) {
                    forwardMap.remove(e.objectId);
                }
            }
        }
    }

    private static final class Entry extends LockerOwner {
        final String objectId;

        Entry(LockerOwner owner, String objectId) {
            super(owner);
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
