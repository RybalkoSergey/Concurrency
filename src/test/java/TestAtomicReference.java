import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by SRYBALKO on 2/24/2018.
 */
public class TestAtomicReference {

    @Test
    public void test1() throws InterruptedException {
        AtomicReference<Person> p  = new AtomicReference<>(new Person(20));

        Thread t1 = new Thread(() -> {
            for(int i=1 ; i<=3 ; i++){
                p.set(new Person(p.get().age+10));

                System.out.println(Thread.currentThread().getName() + " Atomic Check by first thread: is "+p.get().age);
            }
        });

        Thread t2 = new Thread(() -> {
            Person per = p.get();
            for(int i=1 ; i<=3 ; i++){
                System.out.println(Thread.currentThread().getName() + " equals: " + p.get().equals(per) + " " + per.age + " " + p.get().age);

                p.compareAndSet(per, new Person(p.get().age+10));

                System.out.println(Thread.currentThread().getName() + " Atomic Check by second thread : is "+p.get().age);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    class Person {
        int age;
        public Person(int i) {
            age=i;
        }
    }
}
