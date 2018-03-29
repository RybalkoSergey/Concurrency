import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

import static utils.ConcurrentUtils.sleep;

/**
 * Created by SRYBALKO on 2/20/2018.
 */
public class TestStampedLock {

    @Test
    public void test1() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Map<String, String> map = new HashMap<>();
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.writeLock();
            System.out.println(Thread.currentThread().getName() +  " - writeLock.lock() --- stamp -> " + stamp);
            try {
                sleep(1);
                map.put("test", "test");
            } finally {
                System.out.println(Thread.currentThread().getName() +  " - writeLock.unlockWrite() --- stamp -> " + stamp);
                lock.unlockWrite(stamp);
            }
        });

        Runnable readTask = () -> {
            long stamp = lock.readLock();
            System.out.println(Thread.currentThread().getName() +  " - readLock.lock() --- stamp -> " + stamp);
            try {
                System.out.println(map.get("test"));
                sleep(1);
            } finally {
                System.out.println(Thread.currentThread().getName() +  " - unlockRead.lock() --- stamp -> " + stamp);
                lock.unlockRead(stamp);
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        ConcurrentUtils.stop(executor);
    }

    @Test
    public void test2() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long stamp = lock.tryOptimisticRead();
            try {
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                sleep(1);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                sleep(2);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
                sleep(2);
                System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
            } finally {
                lock.unlock(stamp);
            }
        });

//        executor.execute(() ->
//            IntStream.range(0, 3).forEach(i -> {
//                long stamp = lock.tryOptimisticRead();
//                System.out.println(Thread.currentThread().getName() +  " - tryOptimisticRead.lock() --- stamp -> " + stamp);
//                try {
//                    System.out.println("Optimistic Lock Valid: " + lock.validate(stamp));
//                    sleep(1);
//                } catch (Exception e) {
//                    lock.unlock(stamp);
//                }
//        }));

        executor.submit(() -> {
            long stamp = lock.writeLock();
            System.out.println(Thread.currentThread().getName() +  " - writeLock.lock() --- stamp -> " + stamp);
            try {
                sleep(2);
            } finally {
                System.out.println(Thread.currentThread().getName() +  " - writeLock.unlock() --- stamp -> " + stamp);
                lock.unlock(stamp);
            }
        });

        ConcurrentUtils.stop(executor);
    }

    @Test
    public void test3() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        StampedLock lock = new StampedLock();

        executor.submit(() -> {
            long count = 0;
            long stamp = lock.readLock();
            try {
                if (count == 0) {
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0L) {
                        System.out.println("Could not convert to write lock");
                        stamp = lock.writeLock();
                    }
                    count = 23;
                }
                System.out.println(count);
            } finally {
                lock.unlock(stamp);
            }
        });

        ConcurrentUtils.stop(executor);
    }
}
