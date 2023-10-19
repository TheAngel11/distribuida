# Distributed Mutual Exclusion

We need to design and implement a distributed application. This application must have two heavyweight processes ProcessA and ProcessB. ProcessA must invoke 3 lightweight processes ProcessLWA1, ProcessLWA2 and ProcessLWA3. ProcessB, on the other hand, must invoke 3 processes ProcessLWB1, ProcessLWB2, ProcessLWB3. Each lightweight process must live in a infinity loop which will consist of showing your ID on the screen 10 times and while waiting 1 second.
<br>
<br>
<br>
Both heavyweight processes have to run on the same machine, so all lightweight processes will compete for the same shared resource: the screen. A token-based mutual exclusion policy will need to be implemented between the two heavyweight processes. Among the processes in- voked by ProcessA, a Lamport’s policy must be implemented for mutual exclusion. Among the processes invoked by ProcessB, Ricart and Agrawala’s policy will need to be implemented for mutual exclusion.

## How to run
Use the script provided to compile the code and run the program

```bash
$ chmod +x exercici2.sh
$ ./exercici2.sh
```

## Authors
Angel Garcia (angel.garcia@students.salle.url.edu)
<br>
Biel Carpi (biel.carpi@students.salle.url.edu)
