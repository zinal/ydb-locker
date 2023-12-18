package tech.ydb.locker;

import java.util.ArrayList;
import java.util.List;
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
            ACCOUNTS[i] = String.format("%09d%09d", i, ACCOUNTS.length - i);
        }
    }

    private static final int THREAD_STEPS = 10;
    private static final int THREAD_COUNT = 100;
    private static final AtomicLong SLEEP_TIME = new AtomicLong(0L);
    private static final AtomicLong WORK_TIME = new AtomicLong(0L);
    private static final AtomicLong TOTAL_TIME = new AtomicLong(0L);
    private static final AtomicLong LOCK_REQUESTS = new AtomicLong(0L);
    private static final AtomicLong LOCK_SUCCESSES = new AtomicLong(0L);
    private static final AtomicLong LOCK_FAILURES = new AtomicLong(0L);

    private final PessimisticLocker locker;
    private final int number;
    private final Random random;

    public YdbLockerPerfDemo(PessimisticLocker locker, int number) {
        this.locker = locker;
        this.number = number;
        this.random = new Random();
    }

    @Override
    public void run() {
        final YdbLockOwner owner = new YdbLockOwner("perf-demo", String.valueOf(number));
        final List<YdbLockItem> items = new ArrayList<>();
        locker.unlock(owner);
        final long startFull = System.currentTimeMillis();
        for (int step = 0; step < THREAD_STEPS; ++step) {
            final int itemCount = 10 + random.nextInt(490);
            items.clear();
            for (int itemNo = 0; itemNo < itemCount; ++itemNo) {
                int posDt = selectAccount(-1);
                int posKt = selectAccount(posDt);
                String accountDt = ACCOUNTS[posDt];
                String accountKt = ACCOUNTS[posKt];
                items.add(new YdbLockItem(accountDt, accountKt));
            }
            YdbLockResponse response;
            do {
                response = locker.lock(owner, items);
                LOCK_FAILURES.addAndGet(response.getRemaining().size());
                LOCK_REQUESTS.addAndGet(1L);
                if (response.getLocked().isEmpty()) {
                    final long startSleep = System.currentTimeMillis();
                    try {
                        Thread.sleep(100L + random.nextInt(900));
                    } catch(InterruptedException ix) {}
                    SLEEP_TIME.addAndGet(System.currentTimeMillis() - startSleep);
                }
            } while (response.getLocked().isEmpty());

            LOCK_SUCCESSES.addAndGet(response.getLocked().size());

            final long startWork = System.currentTimeMillis();
            try {
                Thread.sleep(100L + 50L * response.getLocked().size());
            } catch(InterruptedException ix) {}
            WORK_TIME.addAndGet(System.currentTimeMillis() - startWork);

            locker.unlock(owner);
        }
        TOTAL_TIME.addAndGet(System.currentTimeMillis() - startFull);
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
        /*
        if (args.length != 1) {
            System.err.println("USAGE: java -jar ydb-locker.jar connection-props.xml");
            System.exit(1);
        }
        YdbConfig config = YdbConfig.fromFile(args[0]);
        PessimisticLocker locker = new InMemoryLocker()
        PessimisticLocker locker = new YdbLocker(config)
        */
        try (PessimisticLocker locker = new InMemoryLocker()) {
            System.out.println("Connected!");
            ArrayList<Thread> threads = new ArrayList<>(THREAD_COUNT);
            for (int threadNum = 0; threadNum < THREAD_COUNT; ++threadNum) {
                Thread t = new Thread(new YdbLockerPerfDemo(locker, threadNum));
                t.setDaemon(true);
                t.setName("locker-test-" + String.valueOf(threadNum));
                threads.add(t);
            }
            System.out.println("Threads created...");
            for (int threadNum = 0; threadNum < THREAD_COUNT; ++threadNum) {
                threads.get(threadNum).start();
            }
            System.out.println("Threads started, please stand by...");
            for (int threadNum = 0; threadNum < THREAD_COUNT; ++threadNum) {
                threads.get(threadNum).join();
            }
            long totalTime = TOTAL_TIME.get();
            long waitTime = SLEEP_TIME.get();
            long workTime = WORK_TIME.get();
            long lockTime = totalTime - (waitTime + workTime);
            long lockSuccesses = LOCK_SUCCESSES.get();
            long lockFailures = LOCK_FAILURES.get();
            long lockRequests = LOCK_REQUESTS.get();
            double lockPerThread = ((double)lockTime) / ((double)THREAD_COUNT);
            double lockPerStep = lockPerThread / ((double)THREAD_STEPS);
            System.out.println("Completed!");
            System.out.println("... total time " + String.valueOf(totalTime));
            System.out.println("... work time " + String.valueOf(workTime));
            System.out.println("... wait time " + String.valueOf(waitTime));
            System.out.println("... lock time " + String.valueOf(lockTime));
            System.out.println("... lock time per thread " + String.valueOf(lockPerThread));
            System.out.println("... lock time per step " + String.valueOf(lockPerStep));
            System.out.println("... total lock requests " + String.valueOf(lockRequests));
            System.out.println("... total lock successes " + String.valueOf(lockSuccesses));
            System.out.println("... total lock failures " + String.valueOf(lockFailures));
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.exit(2);
        }
    }

}
