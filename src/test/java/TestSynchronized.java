import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.stream.IntStream;

import static utils.ConcurrentUtils.sleep;

/**
 * Created by SRYBALKO on 2/16/2018.
 */
public class TestSynchronized {

    @Test
    public void test1() {
        Counter counter = new Counter();

        IntStream.range(0, 10000).forEach(i ->
                new Thread(() -> {
                    counter.inc1();
                    counter.inc2();
                }).start()
        );

        sleep(2);
        System.out.println(counter.c1);
        System.out.println(counter.c2);


    }

    private class Counter {
        private long c1 = 0;
        private long c2 = 0;

//        private Object lock1 = new Object();
//        private Object lock2 = new Object();

        public void inc1() {
            //synchronized(lock1) {
                c1++;
            //}
        }

        public void inc2() {
            //synchronized(lock2) {
                c2++;
            //}
        }
    }
}
