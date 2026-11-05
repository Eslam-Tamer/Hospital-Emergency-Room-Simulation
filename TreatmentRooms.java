
public class TreatmentRooms {
    private final Semaphore rooms;

    public TreatmentRooms(int count) {
        rooms = new Semaphore(count);
    }

    // true if a room was acquired within timeoutMs
    public boolean tryAcquire(long timeoutMs) throws InterruptedException {
        return rooms.tryP(timeoutMs);
    }

    public void release() {
        rooms.V();
    }
}
