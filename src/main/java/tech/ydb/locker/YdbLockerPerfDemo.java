package tech.ydb.locker;

import java.util.Arrays;

/**
 *
 * @author mzinal
 */
public class YdbLockerPerfDemo {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YdbLockerPerfDemo.class);

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USAGE: java -jar ydb-locker.jar connection-props.xml");
            System.exit(1);
        }
        YdbConfig config = YdbConfig.fromFile(args[0]);
        try (YdbConnector yc = new YdbConnector(config)) {
            LOG.info("Connected!");
            YdbLockRequest req = new YdbLockRequest(new YdbLockOwner("aaa", "bbb"));
            req.setItems(Arrays.asList(
                    new YdbLockItem("acc1", "acc2"),
                    new YdbLockItem("acc2", "acc1"),
                    new YdbLockItem("acc3", "acc4"),
                    new YdbLockItem("acc3", "acc4"),
                    new YdbLockItem("acc3", "acc1")));
            new YdbLocker(yc).lock(req);
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(2);
        }
    }

}
