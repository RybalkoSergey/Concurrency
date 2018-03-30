import org.testng.annotations.Test;
import utils.ConcurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Created by SRYBALKO on 2/21/2018.
 */
public class TestSemaphore {

    @Test
    public void test1() {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        Semaphore semaphore = new Semaphore(5);

        Runnable longRunningTask = () -> {
            boolean permit = false;
            try {
                semaphore.acquire();
                //permit = semaphore.tryAcquire(1, TimeUnit.SECONDS);
                //if (permit) {
                    System.out.println(Thread.currentThread().getName() + ": Semaphore acquired");
                    System.out.println("Available permits: " + semaphore.availablePermits());
                    ConcurrentUtils.sleep(5);

                //} else {
                //    System.out.println(Thread.currentThread().getName() + ": Could not acquire semaphore");
                //}
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            } finally {
                //if (permit) {
                    System.out.println(Thread.currentThread().getName() + ": Semaphore released");
                    semaphore.release();
                //}

            }
        };

        IntStream.range(0, 10)
                .forEach(i -> executor.submit(longRunningTask));



        ConcurrentUtils.stop(executor);
    }

    // ---------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------------------

    @Test
    public void test2() {
        int threadCount = 6;
        ExecutorService exService = Executors.newFixedThreadPool(threadCount);
        Library library = new Library();
        for(int i=0; i < threadCount; i++) {
            Reader reader = new Reader(library);
            exService.execute(reader);
        }
        ConcurrentUtils.stop(exService);
    }

    public class Book {
        private String bookName;
        public Book(String bookName) {
            this.bookName = bookName;
        }
        public void read() {
            System.out.println(bookName + " is being read......");
            try {
                Thread.sleep(2000);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        public String getBookName() {
            return bookName;
        }
    }

    private class Library {
        private static final int MAX_PERMIT = 3;
        private final Semaphore availableBook = new Semaphore(MAX_PERMIT, true);
        private Book[] books = {new Book("Book #1"), new Book("Book #2"), new Book("Book #3")};
        private boolean[] beingRead = new boolean[MAX_PERMIT];

        //Book is being issued to reader
        public Object issueBook() throws InterruptedException {
            availableBook.acquire();
            return getNextAvailableBook();
        }
        private synchronized Book getNextAvailableBook() {
            Book book = null;
            for (int i = 0; i < MAX_PERMIT; ++i) {
                if (!beingRead[i]) {
                    beingRead[i] = true;
                    book = books[i];
                    System.out.println(book.getBookName()+" has been taken.");
                    break;
                }
            }
            return book;
        }

        //Book is being returned to library
        public void returnBook(Book book) {
            if (markAsAvailableBook(book))
                availableBook.release();
        }
        private synchronized boolean markAsAvailableBook(Book book) {
            boolean flag = false;
            for (int i = 0; i < MAX_PERMIT; ++i) {
                if (book == books[i]) {
                    if (beingRead[i]) {
                        beingRead[i] = false;
                        flag = true;
                        System.out.println(book.getBookName()+" has been returned.");
                    }
                    break;
                }
            }
            return flag;
        }
    }

    public class Reader implements Runnable {
        private Library library;
        public Reader(Library library) {
            this.library = library;
        }
        @Override
        public void run() {
            try {
                Book book = (Book)library.issueBook();
                book.read();
                library.returnBook(book);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
