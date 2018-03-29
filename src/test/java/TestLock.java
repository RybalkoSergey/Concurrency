import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import static utils.ConcurrentUtils.sleep;

/**
 * Created by SRYBALKO on 2/16/2018.
 */
public class TestLock {

    @Test
    public void test1() {
        Counter counter = new Counter();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        IntStream.range(0, 1000000)
                .forEach(i -> executor.submit(() -> counter.increment()));

        ConcurrentUtils.stop(executor);
        System.out.println(counter.count);

    }

    private class Counter {
//        ReentrantLock lock = new ReentrantLock();
        private long count = 0;

        void increment() {
//            lock.lock();
            try {
                count++;
            } finally {
//                lock.unlock();
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test2() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        ReentrantLock lock = new ReentrantLock();

        executor.submit(() -> {
            lock.lock();
            try {
//                test15(lock);
                System.out.println(Thread.currentThread().getName() + " Hold count : " + lock.getHoldCount());
                sleep(3);
            } finally {
                lock.unlock();
            }
        });

        executor.submit(() -> {
            System.out.println(Thread.currentThread().getName() + " Locked: " + lock.isLocked());
            System.out.println(Thread.currentThread().getName() + " Held by me: " + lock.isHeldByCurrentThread());
            System.out.println(Thread.currentThread().getName() + " Hold count : " + lock.getHoldCount());
            boolean locked = lock.tryLock();
            System.out.println(Thread.currentThread().getName() + " Lock acquired: " + locked);
        });

        ConcurrentUtils.stop(executor);
    }

//    public void test15(ReentrantLock lock) {
//        lock.lock();
//        try {
//            System.out.println(Thread.currentThread().getName() + " Hold count : " + lock.getHoldCount());
//        } finally {
//            lock.unlock();
//        }
//    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test3() {
        final int threadCount = 5;
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);
        final Task task = new LockUnlockDemo();
        for (int i=0; i< threadCount; i++) {
            service.execute(new Worker(task));
        }
        ConcurrentUtils.stop(service);
    }

    private class LockUnlockDemo implements Task {
        final ReentrantLock reentrantLock = new ReentrantLock();
        @Override
        public void performTask() {
            reentrantLock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + ": Lock acquired.");
                System.out.println("Processing...");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println(Thread.currentThread().getName() + ": Lock released.");
                reentrantLock.unlock();
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test4() {
        final int threadCount = 5;
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);
        final Task task = new TryLockDemo();
        for (int i=0; i< threadCount; i++) {
            service.execute(new Worker(task));
        }
        ConcurrentUtils.stop(service);
    }

    private class TryLockDemo implements Task {
        final ReentrantLock reentrantLock = new ReentrantLock();
        @Override
        public void performTask() {
            try {
                System.out.println(Thread.currentThread().getName() +": attemt to get lock");
                boolean flag = reentrantLock.tryLock(100, TimeUnit.MILLISECONDS);
                System.out.println(Thread.currentThread().getName() +": flag = " + flag);
                if (flag) {
                    try {
                        System.out.println(Thread.currentThread().getName() +": Lock acquired.");
                        System.out.println(Thread.currentThread().getName() + " Performing task...");
                        Thread.sleep(2000);
                    } finally {
                        System.out.println(Thread.currentThread().getName() +": Lock released.");
                        reentrantLock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    @Test
    public void test5() {
        final int threadCount = 5;
        final ExecutorService service = Executors.newFixedThreadPool(threadCount);
        final Task task = new LockInterruptiblyDemo();
        for (int i=0; i< threadCount; i++) {
            service.execute(new Worker(task));
        }
        ConcurrentUtils.stop(service);
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    @Test
    public void test6() throws InterruptedException {
        final Task task = new LockInterruptiblyDemo();
        Thread thread1 = new Thread(new Worker(task));
        thread1.start();

        Thread thread2 = new Thread(new Worker(task));
        thread2.start();

        Thread thread3 = new Thread(new Worker(task));
        thread3.start();

        sleep(1);

        thread2.interrupt();
        thread3.interrupt();

        thread1.join();
    }

    private class LockInterruptiblyDemo implements Task{
        final ReentrantLock reentrantLock = new ReentrantLock();
        @Override
        public void performTask() {
            try {
                reentrantLock.lockInterruptibly();
                //if it is not able to acquire lock because of other threads interrupts,
                //it will throw InterruptedException and control will go to catch block.
                try {
                    System.out.println(Thread.currentThread().getName() +": Lock acquired.");
                    System.out.println("Work on progress...");
                    Thread.sleep(2000);

                } finally {
                    System.out.println(Thread.currentThread().getName() +": Lock released.");
                    reentrantLock.unlock();
                }
            } catch (InterruptedException e) {
                System.err.println(Thread.currentThread().getName() + " - Interrupted wait");
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    public interface Task {
        public void performTask();
    }



    public class Worker implements Runnable {
        private Task task;
        public Worker(Task task) {
            this.task = task;
        }
        @Override
        public void run() {
            task.performTask();
        }
    }

    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test10() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        SharedQueue sharedQueue = new SharedQueue(1000);

        executor.submit(() -> {
            IntStream.range(0, 10000).forEach(i -> {
                try {
                    System.out.println("Adding item " + i);
                    sharedQueue.add(i);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });

        executor.submit(() -> {
            IntStream.range(0, 10000).forEach(i -> {
                try {
                    System.out.println("Removing item " + i);
                    sharedQueue.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        });

        sleep(10);
    }

    private class SharedQueue {
        private Object[] elems = null;
        private int itemsCount = 0;
        private int placeIndex = 0;
        private int removeIndex = 0;

        private final Lock lock = new ReentrantLock();
        private final Condition isEmpty = lock.newCondition();
        private final Condition isFull = lock.newCondition();


        public SharedQueue(int capacity) {
            this.elems = new Object[capacity];
        }

        public void add(Object elem) throws Exception {
            lock.lock();
            while(itemsCount >= elems.length) isFull.await();


            if(itemsCount >= elems.length) {
                throw new Exception("Index of bound exception");
            }

            elems[placeIndex] = elem;
            placeIndex = (placeIndex + 1) % elems.length;
            itemsCount = itemsCount + 1;

            System.out.println("------Item was Added");

            isEmpty.signal();
            lock.unlock();
        }

        public Object get() throws Exception {
            lock.lock();
            while(itemsCount <= 0) isEmpty.await();

            Object elem = null;

            while(itemsCount <= 0) {
                throw new Exception("Remove wrang items");
            }

            elem = elems[removeIndex];
            elems[removeIndex] = null;
            removeIndex = (removeIndex + 1) % elems.length;
            itemsCount = itemsCount - 1;

            System.out.println("------Item was removed");

            isFull.signal();
            lock.unlock();

            return elem;
        }
    }
}

//    The main differences between a Lock and a synchronized block are:
//
//        A synchronized block makes no guarantees about the sequence in which threads waiting to entering it are granted access.
//        You cannot pass any parameters to the entry of a synchronized block. Thus, having a timeout trying to get access to a synchronized block is not possible.
//        The synchronized block must be fully contained within a single method. A Lock can have it's calls to lock() and unlock() in separate methods.
