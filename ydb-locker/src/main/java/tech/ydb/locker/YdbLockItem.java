package tech.ydb.locker;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author mzinal
 */
public class YdbLockItem implements Serializable {

    private final Set<String> points = new TreeSet<>();

    public YdbLockItem(String point) {
        points.add(point);
    }

    public YdbLockItem(String first, String second) {
        points.add(first);
        points.add(second);
    }

    public YdbLockItem(Collection<String> points) {
        this.points.addAll(points);
    }

    public Set<String> getPoints() {
        return points;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.points);
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
        final YdbLockItem other = (YdbLockItem) obj;
        return Objects.equals(this.points, other.points);
    }

}
