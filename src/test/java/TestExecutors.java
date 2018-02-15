import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static utils.ConcurrentUtils.sleep;

/**
 * Created by SRYBALKO on 2/15/2018.
 */
public class TestExecutors {

    @Test
    public void test1() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
//        executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
        });
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test2() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            sleep(10);
            String threadName = Thread.currentThread().getName();
            System.out.println("Hello " + threadName);
        });

        try {
            System.out.println("attempt to shutdown executor");
            executor.shutdown();
            executor.awaitTermination(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!executor.isTerminated()) {
                System.out.println("cancel non-finished tasks");
            }
            //List<Runnable> list = executor.shutdownNow();
            System.out.println("shutdown finished");
            System.out.println("executor.isShutdown = " + executor.isShutdown());
            //System.out.println("list.size = " + list.size());
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test3(){
        Callable<Integer> task = () -> {
            sleep(2);
            System.out.println("Executing task...");
            return 123;
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(task);

        System.out.println("future done - " + future.isDone());

        //executor.shutdownNow();
        Integer result = null;
        try {
            result = future.get();
        } catch (InterruptedException e) {
            System.out.println("future.get() returns InterruptedException");
        } catch (ExecutionException e) {
            System.out.println("future.get() returns ExecutionException");
        }

        System.out.println("future done - " + future.isDone());
        System.out.print("result: " + result);
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test4() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        //executor.awaitTermination(3, TimeUnit.SECONDS);

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Start executing task");
            sleep(5);
            return 123;
        });

        future.get(1, TimeUnit.SECONDS);
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test5() throws InterruptedException, ExecutionException, TimeoutException {
        ExecutorService executor = Executors.newFixedThreadPool(1);

        Future<Integer> future = executor.submit(() -> {
            System.out.println("Start executing task");
            sleep(5);
            return 123;
        });

//        new Thread(() -> {
//            while (true) {
//                sleep(1);
//                System.out.println("future.isCancelled() - " + future.isCancelled());
//                System.out.println("future.isDone() - " + future.isDone());
//            }
//        }).start();

//        sleep(3);
//        future.cancel(true);
        future.get();

        System.out.println("Task is completed");
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test6() throws InterruptedException {
        ExecutorService executor = Executors.newWorkStealingPool();

        List<Callable<String>> callables = Arrays.asList(() -> "task1", () -> "task2", () -> "task3");

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(System.out::println);
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test7() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newWorkStealingPool();

        List<Callable<String>> callables = Arrays.asList(
                callable("task1", 6),
                callable("task2", 5),
                callable("task3", 4));

        String result = executor.invokeAny(callables);
        System.out.println(result);
        System.out.println("Task is completed");
    }

    Callable<String> callable(String result, long sleepSeconds) {
        return () -> {
            TimeUnit.SECONDS.sleep(sleepSeconds);
            return result;
        };
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test8() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        ScheduledFuture<?> future = executor.schedule(
                () -> System.out.println("Executing task ... "),
                3,
                TimeUnit.SECONDS);

        sleep(2);


        System.out.println("future.isDone() - " + future.isDone());
        System.out.println("Remaining Delay:"  + future.getDelay(TimeUnit.MILLISECONDS));

        sleep(2);

        System.out.println("future.isDone() - " + future.isDone());
//        System.out.println("Remaining Delay:" +  future.getDelay(TimeUnit.MILLISECONDS));
        executor.shutdown();
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void test9() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> System.out.println("Executing task ... ");

        int initialDelay = 0;
        int period = 1;
        executor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);

        sleep(10);
        executor.shutdown();
    }

    @Test
    public void test10() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                System.out.println("Executing task ... ");
            }
            catch (InterruptedException e) {
                System.err.println("task interrupted");
            }
        };

        executor.scheduleWithFixedDelay(task, 0, 1, TimeUnit.SECONDS);

        sleep(10);
        executor.shutdown();
    }

}

// pitfalls

// Keeping an unused ExecutorService alive
// Wrong thread-pool capacity while using fixed length thread-pool
// Calling a Future‘s get() method after task cancellation
// Unexpectedly-long blocking with Future‘s get() method