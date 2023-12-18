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
        boolean comma0 = false;
        if (owner != null) {
            sb.append("\"owner\": ");
            sb.append("{");
            sb.append("\"typeId\": ");
            sb.append("\"").append(safe(owner.getTypeId())).append("\", ");
            sb.append("\"instanceId\": ");
            sb.append("\"").append(safe(owner.getInstanceId())).append("\"");
            sb.append("}");
            comma0 = true;
        }
        if (items!=null) {
            if (comma0) sb.append(", "); else comma0 = true;
            sb.append("\"items\": ");
            sb.append("[");
            boolean comma1 = false;
            for (LockerItem item : items) {
                if (comma1) sb.append(", "); else comma1 = true;
                sb.append("{");
                if (item.getPoints() != null) {
                    sb.append("\"points\": ");
                    sb.append("[");
                    boolean comma2 = false;
                    for (String point : item.getPoints()) {
                        if (comma2) sb.append(", "); else comma2 = true;
                        sb.append("\"").append(safe(point)).append("\"");
                    }
                    sb.append("]");
                }
                sb.append("}");
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
