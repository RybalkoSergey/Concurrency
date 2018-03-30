import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by SRYBALKO on 2/21/2018.
 */
public class TestCyclicBarrier {

    @Test
    public void test() {
        Runnable barrier1Action = () -> System.out.println("BarrierAction 1 executed ");
        Runnable barrier2Action = () -> System.out.println("BarrierAction 2 executed ");

        CyclicBarrier barrier1 = new CyclicBarrier(3, barrier1Action);
        CyclicBarrier barrier2 = new CyclicBarrier(3, barrier2Action);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(new CyclicBarrierRunnable(barrier1, barrier2));
        executor.execute(new CyclicBarrierRunnable(barrier1, barrier2));

        ConcurrentUtils.stop(executor);
    }

    private class CyclicBarrierRunnable implements Runnable{

        CyclicBarrier barrier1 = null;
        CyclicBarrier barrier2 = null;

        public CyclicBarrierRunnable(CyclicBarrier barrier1, CyclicBarrier barrier2) {
            this.barrier1 = barrier1;
            this.barrier2 = barrier2;
        }

        public void run() {
            try {
                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName() + " waiting at barrier 1");
                this.barrier1.await();

                Thread.sleep(2000);
                System.out.println(Thread.currentThread().getName() + " waiting at barrier 2");
                this.barrier2.await();

                System.out.println(Thread.currentThread().getName() + " done!");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }
}
