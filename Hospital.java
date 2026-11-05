import java.util.Random;
import java.util.Scanner;

public class Hospital {
    private static final int TREATMENT_ROOMS = 2;

    private static int totalPatients;
    private static int treatedPatients = 0;

    // Called by a patient when he leaves; wakes main when everyone is done
    public static synchronized void patientLeft() {
        treatedPatients++;
        if (treatedPatients == totalPatients)
            Hospital.class.notifyAll();
    }

    private static synchronized void waitUntilAllTreated() throws InterruptedException {
        while (treatedPatients < totalPatients)
            Hospital.class.wait();
    }

    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);
        System.out.print("Waiting room capacity: ");
        int capacity = in.nextInt();
        System.out.print("Number of doctors: ");
        int numDoctors = in.nextInt();
        System.out.print("Number of patients: ");
        totalPatients = in.nextInt();
        System.out.println();

        WaitingRoom waitingRoom = new WaitingRoom(capacity);
        TreatmentRooms treatmentRooms = new TreatmentRooms(TREATMENT_ROOMS);

        // create doctors
        Doctor[] doctors = new Doctor[numDoctors];
        for (int i = 0; i < numDoctors; i++) {
            doctors[i] = new Doctor(i + 1, waitingRoom, treatmentRooms);
            doctors[i].start();
        }

        // create patients
        Patient[] patients = new Patient[totalPatients];
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < totalPatients; i++) {
            patients[i] = new Patient(i + 1, waitingRoom);
            names.append(patients[i].getPatientName()).append(" ");
        }
        System.out.println("Patients arriving:");
        System.out.println(names.toString().trim());

        Random random = new Random();
        for (Patient p : patients) {
            p.start();
            Thread.sleep(random.nextInt(500));   // patients arrive at slightly different times
        }

        // wait until every patient is treated, then stop the idle doctors
        waitUntilAllTreated();
        for (Doctor d : doctors)
            d.interrupt();
        for (Doctor d : doctors)
            d.join();

        System.out.println("All patients have been treated.");
        System.out.println("Simulation completed.");
    }
}
