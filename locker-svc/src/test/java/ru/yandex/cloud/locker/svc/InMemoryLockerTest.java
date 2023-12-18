package ru.yandex.cloud.locker.svc;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import org.junit.Assert;
import org.junit.Test;
import ru.yandex.cloud.locker.LockerItem;

import ru.yandex.cloud.locker.LockerOwner;
import ru.yandex.cloud.locker.LockerResponse;
import ru.yandex.cloud.locker.PessimisticLocker;

/**
 *
 * @author zinal
 */
public class InMemoryLockerTest {

    @Test
    public void test1() {
        final PessimisticLocker locker = makeLocker();
        final LockerOwner owner1 = new LockerOwner("test", "instance1");
        final LockerOwner owner2 = new LockerOwner("test", "instance2");
        locker.unlock(owner1);
        locker.unlock(owner2);

        final List<LockerItem> items1 = Arrays.asList(
            new LockerItem("1", "2"),
            new LockerItem("3", "4"),
            new LockerItem("5", "6")
        );
        final LockerResponse response1 = locker.lock(owner1, items1);
        Assert.assertEquals(response1.getRemaining().isEmpty(), true);
        Assert.assertEquals(
                new TreeSet<>(response1.getLocked()),
                new TreeSet<>(Arrays.asList("1", "2", "3", "4", "5", "6")));

        final List<LockerItem> items2 = Arrays.asList(
            new LockerItem("2", "3"),
            new LockerItem("7", "8"),
            new LockerItem("9", "10")
        );
        final LockerResponse response2 = locker.lock(owner2, items2);
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("2", "3")),
                new TreeSet<>(response2.getRemaining()));
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("7", "8", "9", "10")),
                new TreeSet<>(response2.getLocked()));

        final List<LockerItem> items3 = Arrays.asList(
            new LockerItem("1", "3"),
            new LockerItem("2", "4"),
            new LockerItem("6", "8"),
            new LockerItem("7", "11")
        );
        final LockerResponse response3 = locker.lock(owner1, items3);
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("6", "8", "7", "11")),
                new TreeSet<>(response3.getRemaining()));
        Assert.assertEquals(
                new TreeSet<>(Arrays.asList("1", "2", "3", "4")),
                new TreeSet<>(response3.getLocked()));

        locker.unlock(owner1);

        final LockerResponse response4 = locker.lock(owner2, items2);
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
