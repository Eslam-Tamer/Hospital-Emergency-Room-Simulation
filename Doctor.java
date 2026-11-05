import java.util.Random;

public class Doctor extends Thread {
    private static final long ROOM_TIMEOUT_MS = 3000;   // 3 seconds

    private final int id;
    private final WaitingRoom waitingRoom;
    private final TreatmentRooms treatmentRooms;
    private final Random random = new Random();

    public Doctor(int id, WaitingRoom waitingRoom, TreatmentRooms treatmentRooms) {
        this.id = id;
        this.waitingRoom = waitingRoom;
        this.treatmentRooms = treatmentRooms;
    }

    private String me() {
        return "Doctor " + id;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // 1. idle until a patient is available, then take him
                Patient p = waitingRoom.take();
                System.out.println(me() + " is assigned to " + p.getPatientName() + ".");

                // 2. try to get a treatment room for 3 seconds
                System.out.println(me() + " is trying to acquire a treatment room.");
                if (treatmentRooms.tryAcquire(ROOM_TIMEOUT_MS)) {
                    // 3a. treat the patient
                    System.out.println(me() + " acquired a treatment room.");
                    System.out.println(me() + " begins treating " + p.getPatientName() + ".");
                    Thread.sleep(2000 + random.nextInt(3000));   // 2-5 seconds
                    System.out.println(me() + " finished treating " + p.getPatientName() + ".");
                    treatmentRooms.release();
                    System.out.println(me() + " released the treatment room.");
                    System.out.println(me() + " is now available.");
                    p.leave();
                } else {
                    // 3b. no room in time: send the patient back
                    System.out.println(me() + " could not acquire a treatment room.");
                    System.out.println(me() + " is sending " + p.getPatientName()
                            + " back to the waiting room.");
                    p.returnToWaitingRoom();
                    System.out.println(me() + " is now available.");
                }
            }
        } catch (InterruptedException e) {
            // Hospital interrupts doctors when all patients are treated
        }
    }
}
