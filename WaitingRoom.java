
public class WaitingRoom {
    private final Patient[] store;
    private final int size;
    private int inptr = 0;
    private int outptr = 0;

    private final Semaphore spaces;            // free seats
    private final Semaphore elements = new Semaphore(0); // waiting patients
    private final Semaphore mutex = new Semaphore(1);    // protects store/inptr/outptr
                                                         // (many patients and doctors use them)

    public WaitingRoom(int capacity) {
        this.size = capacity;
        this.store = new Patient[capacity];
        this.spaces = new Semaphore(capacity);
    }

    // Patient: enter only if a seat is free right now. false => room is full
    public boolean tryAdd(Patient p) throws InterruptedException {
        if (!spaces.tryP(0))
            return false;
        insert(p);
        return true;
    }

    // Patient: wait (outside) until a seat becomes free, then enter
    public void add(Patient p) throws InterruptedException {
        spaces.P();
        insert(p);
    }

    private void insert(Patient p) throws InterruptedException {
        mutex.P();
        store[inptr] = p;
        inptr = (inptr + 1) % size;
        mutex.V();
        elements.V();          // tell doctors a patient is available
    }

    // Doctor: stay idle until a patient is available, then take him
    public Patient take() throws InterruptedException {
        elements.P();
        mutex.P();
        Patient p = store[outptr];
        outptr = (outptr + 1) % size;
        mutex.V();
        spaces.V();            // a seat became free
        return p;
    }
}
