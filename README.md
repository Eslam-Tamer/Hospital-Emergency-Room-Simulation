# Assignment 1: Hospital Emergency Room Simulation

CS241 / SCS241: Operating Systems, Faculty of Computing and Artificial Intelligence, Cairo University


## Description

A multi-threaded Java simulation of a hospital emergency room, built on the
Producer-Consumer problem with a Bounded Buffer.

- **Patients** are the producers. They try to enter a waiting room with limited capacity.
- **Doctors** are the consumers. They take patients from the waiting room and treat them.
- There are only **2 treatment rooms** in the whole hospital. A doctor must acquire one before treating a patient.
- If a doctor cannot get a room within **3 seconds**, he sends the patient back to the waiting room.

All synchronization uses our own `Semaphore` class (`synchronized` + `wait/notify`), as in the labs.

## How to Compile and Run

```
javac *.java
java Hospital
```

The program asks for three inputs:

```
Waiting room capacity: 2
Number of doctors: 3
Number of patients: 5
```

The output order changes from run to run because threads are scheduled differently each time.

## Project Structure

| File | Role |
|---|---|
| `Hospital.java` | Reads the input, creates doctors and patients, prints the events, and ends the simulation when all patients are treated. |
| `Semaphore.java` | Counting semaphore with `P()`, `V()`, and the custom `tryP(timeoutMs)` that returns `false` if no resource is available in time. |
| `WaitingRoom.java` | Bounded buffer (circular array) protected by semaphores. Shared resource 1. |
| `TreatmentRooms.java` | The 2 treatment rooms, controlled by a semaphore. Shared resource 2. |
| `Patient.java` | Patient thread: arrives, enters the waiting room, or waits outside if it is full. |
| `Doctor.java` | Doctor thread: idle, takes a patient, tries to acquire a room, treats, releases the room. |

## Synchronization Design

**Waiting room (Bounded Buffer)** uses three semaphores:

- `spaces` (initial value = capacity): counts free seats. A patient does `P()` before entering.
- `elements` (initial value = 0): counts waiting patients. A doctor does `P()` before taking one, so he stays idle when the room is empty.
- `mutex` (initial value = 1): protects the buffer array and the `inptr` / `outptr` indexes, because many patients and many doctors use them at the same time.

**Treatment rooms** are a separate semaphore initialized to 2, so they are synchronized independently from the waiting room. A doctor calls `tryP(3000)`: if it returns `false`, the 3 seconds passed with no free room.

**Waiting outside when the room is full:** a patient first calls `tryAdd`, which uses `tryP(0)` to check for a free seat without waiting. If there is none, he prints that he is waiting outside and then blocks in `add` until a seat is free.

## Race Conditions and Deadlock

- No shared variable is touched without a semaphore or a `synchronized` method protecting it, so there are no race conditions.
- **Deadlock avoidance:** when a doctor sends a patient back, the *patient* waits outside in his own thread, not the doctor. If the doctor waited, all doctors could block on a full waiting room while nobody takes patients out.
- A doctor never holds a treatment room while waiting for a seat, so there is no circular wait.

## Ending the Simulation

Each patient calls `Hospital.patientLeft()` when he leaves. When the counter reaches the total number of patients, the main thread wakes up (`wait/notifyAll`), interrupts the idle doctor threads, joins them, and prints:

```
All patients have been treated.
Simulation completed.
```

## Events Printed

Patient arrives, enters the waiting room, waits outside (room full), is assigned to a doctor, begins treatment in a room, goes back to the waiting room (no room in time), finishes treatment, and leaves.
