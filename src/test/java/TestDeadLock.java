import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static utils.ConcurrentUtils.sleep;
import static utils.ConcurrentUtils.stop;

/**
 * Created by SRYBALKO on 2/24/2018.
 */
public class TestDeadLock {

    @Test
    public void test1() {
        Fox fox1 = new Fox();
        Fox fox2 = new Fox();
        Food food= new Food();
        Water water= new Water();

        ExecutorService service= null;
        try {
            service = Executors.newScheduledThreadPool(10);
            service.submit(() -> fox1.eatAndDrink(food,water));
            service.submit(() -> fox2.drinkAndEat(food,water));
        } finally {
            if(service != null) {
                stop(service);
            }
        }
    }

    class Fox {
        public void eatAndDrink(Food food, Water water) {

            synchronized(food) {
                System.out.println("Got Food!");
                move();
                synchronized(water) {
                    System.out.println("Got Water!");
                }
            }
        }

        public void drinkAndEat(Food food, Water water) {
            synchronized(water) {
                System.out.println("Got Water!");
                move();
                synchronized(food) {
                    System.out.println("Got Food!");
                }
            }
        }
        public void move() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("InterruptedException");
            }
        }
    }

    class Food{}
    class Water{}

    @Test
    public void test2() {
        Bank bank = new Bank();
        final Account a = new Account(1000);
        final Account b = new Account(2000);

        new Thread(() -> bank.transfer(a, b, 500)).start();
        new Thread(() -> bank.transfer(b, a, 300)).start();

        sleep(60);
    }

    class Account {
        private int balance;
        public Lock lock = new ReentrantLock();

        public int getBalance() {
            return balance;
        }

        public Account(int initialBalance) {
            this.balance = initialBalance;
        }

        public void withdraw(int amount) {
            balance -= amount;
        }

        public void deposit(int amount) {
            balance += amount;
        }
    }

    class Bank {
        void transfer(Account ac1, Account ac2, int amount) {
            synchronized (ac1) {
                System.out.println(Thread.currentThread().getName() + " Get lock ac1");
                sleep(1);

                synchronized (ac2) {
                    System.out.println(Thread.currentThread().getName() + " Get lock ac2");
                    if (ac1.getBalance() < amount) {
                        throw new RuntimeException("Not enough money for " + ac1);
                    }
                    ac1.withdraw(amount);
                    ac2.deposit(amount);
                    System.out.println("Transfer successful");
                }
            }
        }

//        void transfer1(Account ac1, Account ac2, int amount) {
//            try {
//                if (ac1.lock.tryLock(1, TimeUnit.SECONDS)) {
//                    System.out.println(Thread.currentThread().getName() + " Get lock ac1");
//                    try {
//                        if (ac2.lock.tryLock(1, TimeUnit.SECONDS)) {
//                            System.out.println(Thread.currentThread().getName() + " Get lock ac2");
//                            if (ac1.getBalance() < amount) {
//                                throw new RuntimeException("Not enough money for " + ac1);
//                            }
//                            ac1.withdraw(amount);
//                            ac2.deposit(amount);
//                            System.out.println("Transfer successful");
//                        }
//                    } finally {
//                        ac1.lock.unlock();
//                    }
//                }
//            } catch (InterruptedException e) {
//                ac1.lock.unlock();
//                ac2.lock.unlock();
//            }
//        }
    }

    //C:\Program Files\Java\jdk1.8.0_121\bin>jps
    //C:\Program Files\Java\jdk1.8.0_121\bin>jstack 9688
    //C:\Program Files\Java\jdk1.8.0_121\bin>jconsole
}
