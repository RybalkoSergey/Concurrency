import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Created by SRYBALKO on 2/21/2018.
 */
public class TestAtomicInteger {

    @Test
    public void test1() {
        AtomicInteger ai= new AtomicInteger(0);

        System.out.println(ai.incrementAndGet());
        System.out.println(ai.getAndIncrement());
        System.out.println(ai.get());

        ai.set(10);
        System.out.println(ai.get());

//        ai.accumulateAndGet();
//        System.out.println(ai.get());

        ai.compareAndSet(10, 15);
        System.out.println(ai.get());

        ai.weakCompareAndSet(15, 20);
        System.out.println(ai.get());
    }

    @Test
    public void test2() {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> executor.submit(atomicInt::incrementAndGet));

        ConcurrentUtils.stop(executor);

        System.out.println(atomicInt.get());
    }

    @Test
    public void test3() {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> {
                    Runnable task = () ->
                            atomicInt.updateAndGet(n -> n + 2);
                    executor.submit(task);
                });

        ConcurrentUtils.stop(executor);

        System.out.println(atomicInt.get());
    }

    @Test
    public void test4() {
        AtomicInteger atomicInt = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        IntStream.range(0, 1000)
                .forEach(i -> {
                    Runnable task = () ->
                            atomicInt.accumulateAndGet(i, (n, m) -> n + m);
                    executor.submit(task);
                });

        ConcurrentUtils.stop(executor);

        System.out.println(atomicInt.get());
    }
}
