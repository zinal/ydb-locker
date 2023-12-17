package tech.ydb.locker;

import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zinal
 */
public class InMemoryLockerTest {

    @Test
    public void test1() {
        final PessimisticLocker locker = makeLocker();
        final YdbLockOwner owner1 = new YdbLockOwner("test", "instance1");
        final YdbLockOwner owner2 = new YdbLockOwner("test", "instance2");
        locker.unlock(owner1);
        locker.unlock(owner2);

        final Collection<YdbLockItem> items1 = Arrays.asList(
            new YdbLockItem("1", "2"),
            new YdbLockItem("3", "4"),
            new YdbLockItem("5", "6")
        );
        final YdbLockResponse response1 = locker.lock(owner1, items1);
        Assert.assertEquals(response1.getRemaining().isEmpty(), true);
        Assert.assertEquals(
                new TreeSet<>(response1.getLocked()),
                new TreeSet<>(Arrays.asList("1", "2", "3", "4", "5", "6")));

        final Collection<YdbLockItem> items2 = Arrays.asList(
            new YdbLockItem("2", "3"),
            new YdbLockItem("7", "8"),
            new YdbLockItem("9", "10")
        );
        final YdbLockResponse response2 = locker.lock(owner2, items2);
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("2", "3")),
                new TreeSet<>(response2.getRemaining()));
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("7", "8", "9", "10")),
                new TreeSet<>(response2.getLocked()));

        final Collection<YdbLockItem> items3 = Arrays.asList(
            new YdbLockItem("1", "3"),
            new YdbLockItem("2", "4"),
            new YdbLockItem("6", "8"),
            new YdbLockItem("7", "11")
        );
        final YdbLockResponse response3 = locker.lock(owner1, items3);
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("6", "8", "7", "11")),
                new TreeSet<>(response3.getRemaining()));
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("1", "2", "3", "4")),
                new TreeSet<>(response3.getLocked()));

        locker.unlock(owner1);

        final YdbLockResponse response4 = locker.lock(owner2, items2);
        Assert.assertEquals(
                new TreeSet<String>(),
                new TreeSet<>(response4.getRemaining()));
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("2", "3", "7", "8", "9", "10")),
                new TreeSet<>(response4.getLocked()));
    }

    private PessimisticLocker makeLocker() {
        return new InMemoryLocker();
    }

}
