
public class Patient extends Thread {
    private final int id;
    private final WaitingRoom waitingRoom;

    public Patient(int id, WaitingRoom waitingRoom) {
        this.id = id;
        this.waitingRoom = waitingRoom;
    }

    public String getPatientName() {
        return "P" + id;
    }

    @Override
    public void run() {
        System.out.println(getPatientName() + " arrived.");
        enterWaitingRoom();
    }

    // Used both for first arrival and when a doctor sends the patient back
    public void enterWaitingRoom() {
        try {
            if (!waitingRoom.tryAdd(this)) {
                System.out.println("The waiting room is full.");
                System.out.println(getPatientName() + " is waiting outside for an available seat.");
                waitingRoom.add(this);   // blocks until a seat is free
            }
            System.out.println(getPatientName() + " entered the waiting room.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Doctor sends the patient back: the PATIENT waits outside (not the doctor),
    // so doctors never get stuck => no deadlock.
    public void returnToWaitingRoom() {
        new Thread(this::enterWaitingRoom).start();
    }

    public void leave() {
        System.out.println(getPatientName() + " left.");
        Hospital.patientLeft();
    }
}
