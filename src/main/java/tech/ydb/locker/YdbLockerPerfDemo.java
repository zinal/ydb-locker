package tech.ydb.locker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author mzinal
 */
public class YdbLockerPerfDemo implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(YdbLockerPerfDemo.class);

    private static final String[] ACCOUNTS = new String[10000];
    static {
        for (int i=0; i<ACCOUNTS.length; ++i) {
            ACCOUNTS[i] = String.format("%10d%10d", i, 10000-i);
        }
    }

    private static AtomicLong WAIT_TIME = new AtomicLong(0L);
    private static AtomicLong TOTAL_TIME = new AtomicLong(0L);

    private final YdbConnector yc;
    private final YdbLocker locker;
    private final int number;
    private final Random random;

    public YdbLockerPerfDemo(YdbConnector yc, int number) {
        this.yc = yc;
        this.locker = new YdbLocker(yc);
        this.number = number;
        this.random = new Random();
    }

    @Override
    public void run() {
        final long startWork = System.currentTimeMillis();
        final YdbLockOwner owner = new YdbLockOwner("perf-demo", String.valueOf(number));
        final YdbUnlockRequest unlockRequest = new YdbUnlockRequest(owner);
        locker.unlock(unlockRequest);
        System.out.println("Started thread #" + String.valueOf(number));
        for (int step = 0; step < 100; ++step) {
            final YdbLockRequest request = new YdbLockRequest(owner);
            final int itemCount = 100 + random.nextInt(401);
            request.setItems(new ArrayList<>(itemCount));
            for (int itemNo = 0; itemNo < itemCount; ++itemNo) {
                int posDt = selectAccount(-1);
                int posKt = selectAccount(posDt);
                String accountDt = ACCOUNTS[posDt];
                String accountKt = ACCOUNTS[posKt];
                request.getItems().add(new YdbLockItem(accountDt, accountKt));
            }
            locker.lock(request);
            final long startSleep = System.currentTimeMillis();
            try {
                Thread.sleep(100L + random.nextInt(4900));
            } catch(InterruptedException ix) {}
            WAIT_TIME.addAndGet(System.currentTimeMillis() - startSleep);
            locker.unlock(unlockRequest);
        }
        TOTAL_TIME.addAndGet(System.currentTimeMillis() - startWork);
    }

    private int selectAccount(int avoid) {
        int select = random.nextInt(ACCOUNTS.length);
        if (select == avoid) {
            if (select > 0) {
                select = select - 1;
            } else {
                select = 1;
            }
        }
        return select;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("USAGE: java -jar ydb-locker.jar connection-props.xml");
            System.exit(1);
        }
        YdbConfig config = YdbConfig.fromFile(args[0]);
        try (YdbConnector yc = new YdbConnector(config)) {
            System.out.println("Connected!");
            final int threadsTotal = 10;
            ArrayList<Thread> threads = new ArrayList<>(threadsTotal);
            for (int threadNum = 0; threadNum < threadsTotal; ++threadNum) {
                Thread t = new Thread(new YdbLockerPerfDemo(yc, threadNum));
                t.setDaemon(true);
                t.setName("locker-test-" + String.valueOf(threadNum));
                threads.add(t);
            }
            System.out.println("Threads created...");
            for (int threadNum = 0; threadNum < threadsTotal; ++threadNum) {
                threads.get(threadNum).start();
            }
            System.out.println("Threads started, please stand by...");
            for (int threadNum = 0; threadNum < threadsTotal; ++threadNum) {
                threads.get(threadNum).join();
            }
            long totalTime = TOTAL_TIME.get();
            long waitTime = WAIT_TIME.get();
            long lockTime = totalTime - waitTime;
            double lockPerThread = ((double)lockTime) / ((double)threadsTotal);
            System.out.println("Completed, total time " + String.valueOf(totalTime));
            System.out.println("... wait time " + String.valueOf(waitTime));
            System.out.println("... lock time " + String.valueOf(lockTime));
            System.out.println("... lock time per thread " + String.valueOf(lockPerThread));
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(2);
        }
    }

}
