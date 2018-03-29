import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import static utils.ConcurrentUtils.*;

/**
 * Created by SRYBALKO on 2/20/2018.
 */
public class TestReadWriteLock {

    @Test
    public void test1() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        Map<String, String> map = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        executor.submit(() -> {
            lock.writeLock().lock();
            System.out.println(Thread.currentThread().getName() + " lock.writeLock()");
            try {
                sleep(3);
                map.put("test", "test");
            } finally {
                //System.out.println(Thread.currentThread().getName() + " lock released");
                lock.writeLock().unlock();
            }
        });

        Runnable readTask = () -> {
            System.out.println(Thread.currentThread().getName() + " try lock.readLock()");
            lock.readLock().lock();
            try {
                System.out.println(Thread.currentThread().getName() + " get lock.readLock()");
                System.out.println(Thread.currentThread().getName() + " " + map.get("test"));
                sleep(1);
            } finally {
                lock.readLock().unlock();
            }
        };

        executor.submit(readTask);
        executor.submit(readTask);

        stop(executor);
    }


    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    @Test
    public void test2() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        SynchronizedHashMapWithReadWriteLock mapWithReadWriteLock = new SynchronizedHashMapWithReadWriteLock();

        executor.execute(() -> {
            IntStream.range(0, 10000).forEach(i -> {
                try {
                    mapWithReadWriteLock.put(String.valueOf(i), String.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        executor.execute(() -> {
            IntStream.range(10001, 20001).forEach(i -> {
                try {
                    mapWithReadWriteLock.put(String.valueOf(i), String.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        ConcurrentUtils.stop(executor);


        System.out.println("Count " + mapWithReadWriteLock.syncHashMap.size());
    }


    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    @Test
    public void test3() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        SynchronizedHashMapWithReadWriteLock mapWithReadWriteLock = new SynchronizedHashMapWithReadWriteLock();

        executor.execute(() -> {
            IntStream.range(0, 3).forEach(i -> {
                try {
                    mapWithReadWriteLock.put(String.valueOf(i), String.valueOf(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        executor.execute(() -> {
            IntStream.range(0, 50).forEach(i -> {
                try {
                    mapWithReadWriteLock.get(String.valueOf(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        executor.execute(() -> {
            IntStream.range(0, 40).forEach(i -> {
                try {
                    mapWithReadWriteLock.get(String.valueOf(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        executor.execute(() -> {
            IntStream.range(0, 41).forEach(i -> {
                try {
                    mapWithReadWriteLock.get(String.valueOf(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        stop(executor);

    }


    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------------------------


    private class SynchronizedHashMapWithReadWriteLock {

        Map<String,String> syncHashMap = new HashMap<>();
        ReadWriteLock lock = new ReentrantReadWriteLock();

        Lock writeLock = lock.writeLock();
        Lock readLock = lock.readLock();

        public Map<String, String> getSyncHashMap() {
            return syncHashMap;
        }

        public void put(String key, String value) {
            try {
//                writeLock.lock();
//                System.out.println(Thread.currentThread().getName() +  " - writeLock.lock() --- put " + key);
                syncHashMap.put(key, value);
//                sleep(1);
            } finally {
//                System.out.println(Thread.currentThread().getName() +  " - writeLock.unlock() --- putting done " + key);
//                writeLock.unlock();

            }
        }

        public String get(String key){
            try {
//                System.out.println(Thread.currentThread().getName() +  " - try readLock.lock() --- get " + key);
//                readLock.lock();
//                System.out.println(Thread.currentThread().getName() +  " - readLock.lock() --- get " + key);
                return syncHashMap.get(key);
            } finally {
//                System.out.println(Thread.currentThread().getName() +  " - readLock.unlock() --- get " + key);
//                readLock.unlock();
            }
        }

        public String remove(String key){
            try {
                writeLock.lock();
                return syncHashMap.remove(key);
            } finally {
                writeLock.unlock();
            }
        }

        public boolean containsKey(String key) {
            try {
                readLock.lock();
                return syncHashMap.containsKey(key);
            } finally {
                readLock.unlock();
            }
        }
    }
}
