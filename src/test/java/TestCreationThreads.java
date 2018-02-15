import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by SRYBALKO on 2/15/2018.
 */
public class TestCreationThreads {

    @Test
    public void test1() {
        MyThread myThread = new MyThread();
        myThread.start();
    }

    private class MyThread extends Thread {
        public void run(){
            String name = Thread.currentThread().getName();
            System.out.println("MyThread running " + name);
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test2() {
        Thread thread = new Thread(new MyRunnable());
        thread.start();
    }

    public class MyRunnable implements Runnable {
        public void run(){
            String name = Thread.currentThread().getName();
            System.out.println("MyRunnable running " + name);
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test3() throws InterruptedException {
        Runnable runnable = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("MyRunnable running " + name);
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }
}
