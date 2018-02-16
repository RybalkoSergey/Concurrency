import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
                    sharedQueue.remove();
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

        public Object remove() throws Exception {
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
