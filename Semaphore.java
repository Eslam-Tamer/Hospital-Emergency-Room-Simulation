
public class Semaphore {
    private int value;

    public Semaphore(int initial) {
        this.value = initial;
    }

    // wait(): take one resource, block if none is available
    public synchronized void P() throws InterruptedException {
        while (value <= 0)
            wait();
        value--;
    }

    // signal(): return one resource and wake up one waiting thread
    public synchronized void V() {
        value++;
        notify();
    }

    // Custom method: try to take a resource within timeoutMs milliseconds.
    // Returns false if no resource became available in time.
    public synchronized boolean tryP(long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (value <= 0) {
            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0)
                return false;      // time is over
            wait(remaining);       // wait until notified or time is over
        }
        value--;
        return true;
    }
}
