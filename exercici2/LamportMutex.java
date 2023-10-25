import java.net.ServerSocket;
import java.util.List;
import java.util.PriorityQueue;

/**
 * This class represents a Lamport mutex
 * <p>
 * It is used to synchronize access to the critical section between multiple processes
 */
public class LamportMutex extends DistributedMutex {

    /**
     * This queue stores the requests to enter the critical section
     * It is ordered by timestamp. If the timestamp is the same, we order by process ID
     * <br><br>
     * To use this queue in a thread-safe way, <strong>we synchronize on it when accessing it</strong>
     */
    private final PriorityQueue<Request> queue = new PriorityQueue<>((r1, r2) -> {
        int compare = Integer.compare(r1.timestamp, r2.timestamp);
        if (compare == 0) {
            return Integer.compare(r1.processID, r2.processID);
        }
        return compare;
    });


    public LamportMutex(int myID, List<String> clientSockets, ServerSocket serverSocket) {
        super(myID, clientSockets, serverSocket);
    }

    public void handleMessage(String message) {
        // Message format: "TYPE:SENDER_ID:TIMESTAMP"
        String[] parts = message.split(":");
        String type = parts[0];
        int senderID = Integer.parseInt(parts[1]);
        int timestamp = Integer.parseInt(parts[2]);

        switch (type) {
            case "REQUEST":
                // Update the Lamport clock (the max between the current value and the timestamp of the message + 1)
                lamportClock.updateAndGet(current -> Math.max(current, timestamp) + 1);

                // If it's a request, add it to the queue and send an acknowledgment
                synchronized (queue) {
                    queue.add(new Request(senderID, timestamp));
                }
                sendAcknowledgment(senderID);
                break;
            case "ACK":
                // Update the Lamport clock (the max between the current value and the timestamp of the message + 1)
                lamportClock.updateAndGet(current -> Math.max(current, timestamp) + 1);

                // If it's an acknowledgment, add it to the map
                acksReceived.put(senderID, true);
                break;
            case "RELEASE":
                // If it's a release, remove the request from the queue
                // Note we're not updating the lamport clock in the release message.
                //  If we did, we could have a situation where the process that sent the release message
                //  has a lower timestamp than the process that sent the request message
                synchronized (queue) {
                    queue.removeIf(req -> req.processID == senderID);
                }
                break;
        }
    }


    public void requestCS() {
        int timestamp = lamportClock.incrementAndGet();

        // Add our request to the queue and send it to all processes
        Request myRequest = new Request(myID, timestamp);
        synchronized (queue) {
            queue.add(myRequest);
        }
        sendRequestToAllProcesses(myRequest);

        // Wait for all acknowledgments, and until our request is at the head of the queue
        while (acksReceived.size() < clientSockets.size() - 1 || !isMyRequestAtHeadOfQueue()) {
            try {
                Thread.sleep(10); // Could be adjusted depending on the network, to avoid busy waiting
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    /**
     * This method releases access to the critical section
     * <br>
     * It notifies all processes that the critical section is free
     */
    public void releaseCS() {
        // Remove our request from the queue and notify all processes
        synchronized (queue) {
            queue.removeIf(req -> req.processID == myID);
        }
        sendReleaseToAllProcesses();
        acksReceived.clear();
    }



    private boolean isMyRequestAtHeadOfQueue() {
        // Check if the first element is our request
        synchronized (queue) {
            return !queue.isEmpty() && queue.peek().processID == myID;
        }
    }

}