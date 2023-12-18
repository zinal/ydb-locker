package ru.yandex.cloud.locker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;

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

    public String toJson() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (owner != null) {
            sb.append("\"owner\": ");
            sb.append("{");
            sb.append("\"typeId\": ");
            sb.append("\"").append(owner.getTypeId()).append("\",");
            sb.append("\"instanceId\": ");
            sb.append("\"").append(owner.getInstanceId()).append("\",");
            sb.append("}");
        }
        if (items!=null) {
            sb.append("\"items\": ");
            sb.append("[");
            for (LockerItem item : items) {
                
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String safe(String v) {
        return StringEscapeUtils.escapeJson(v);
    }

}
