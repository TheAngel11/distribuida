import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RicartAgrawalaMutex extends DistributedMutex {

    // This queue stores the requests to enter the critical section
    private final ArrayList<Request> pendingQueue = new ArrayList<>();

    private AtomicInteger myTS = new AtomicInteger();


    public RicartAgrawalaMutex(int myID, List<String> clientSockets, ServerSocket serverSocket) {
        super(myID, clientSockets, serverSocket);
    }

    @Override
    void handleMessage(String message) {
        // Message format: "TYPE:SENDER_ID:TIMESTAMP"
        String[] parts = message.split(":");
        String type = parts[0];
        int senderID = Integer.parseInt(parts[1]);
        int timestamp = Integer.parseInt(parts[2]);

        switch (type) {
            case "REQUEST":
                // Update the Lamport clock (the max between the current value and the timestamp of the message + 1)
                lamportClock.updateAndGet(current -> Math.max(current, timestamp) + 1);

                if(myTS.get() == Integer.MAX_VALUE || (myTS.get() > timestamp || (myTS.get() == timestamp && myID > senderID))){
                    sendAcknowledgment(senderID);
                }
                else{
                    synchronized (pendingQueue){
                        pendingQueue.add(new Request(senderID, timestamp));
                    }
                }
                break;
            case "ACK":
                // If it's an acknowledgment, add it to the map
                acksReceived.put(senderID, true);
                break;
        }
    }

    @Override
    public void requestCS() {
        int timestamp = lamportClock.incrementAndGet();
        myTS.set(timestamp);

        // Send it to all processes
        Request myRequest = new Request(myID, timestamp);
        sendRequestToAllProcesses(myRequest);

        // Wait for all acknowledgments
        while (acksReceived.size() < clientSockets.size() - 1) {
            try {
                Thread.sleep(10); // Could be adjusted depending on the network, to avoid busy waiting
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }

    }

    @Override
    public void releaseCS() {
        myTS.set(Integer.MAX_VALUE);

        synchronized (pendingQueue){
            for (Request request : pendingQueue) {
                sendAcknowledgment(request.processID);
            }

            pendingQueue.clear();
        }

        acksReceived.clear();
    }
}
