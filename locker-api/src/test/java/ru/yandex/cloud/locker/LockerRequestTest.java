package ru.yandex.cloud.locker;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author mzinal
 */
public class LockerRequestTest {

    @Test
    public void test1() {
        final LockerRequest req1 = new LockerRequest(new LockerOwner("aaa", "bbb"),
                Arrays.asList(new LockerItem("x", "y"), new LockerItem("000}xxx", "999{yyy")));
        Assert.assertEquals("{\"owner\": {\"typeId\": \"aaa\", \"instanceId\": \"bbb\"}, \"items\": [{\"points\": [\"x\", \"y\"]}, {\"points\": [\"000}xxx\", \"999{yyy\"]}]}",
                req1.toJson());
    }

}
