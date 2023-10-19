import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LamportMutex {

    /**
     * Lamport's logical clock, initialized to 0
     * <br>
     * Atomic so that it can be incremented atomically, on different threads
     */
    private final AtomicInteger lamportClock = new AtomicInteger(0);

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

    /**
     * This map stores the acknowledgments received for the current request
     * <br>
     * It is synchronized too so that it can be accessed by different threads
     */
    private final ConcurrentHashMap<Integer, Boolean> acksReceived = new ConcurrentHashMap<>();

    private final int myID;
    private final ServerSocket serverSocket;
    private final List<String> clientSockets;

    private final Thread t;

    public LamportMutex(int myID, List<String> clientSockets, ServerSocket serverSocket) {
        this.myID = myID;
        this.serverSocket = serverSocket;
        this.clientSockets = clientSockets; // Our socket is null, so we don't send messages to ourselves

        // Finally, Start a thread to listen for incoming messages
        t = new Thread(this::listenForMessages);
        t.start();
    }


    public void destroy() {
        t.interrupt();
    }


    /**
     * This method listens for incoming messages
     * It runs in a separate thread (invoked from the constructor,
     * so that it doesn't block the main thread
     */
    private void listenForMessages() {
        while (true) {
            try {
                // Await a connection
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = in.readLine();

                // Handle the message
                handleMessage(message);

                in.close();
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * This method handles an incoming message
     *
     * @param message The message to handle
     */
    private void handleMessage(String message) {
        // Message format: "TYPE:SENDER_ID:TIMESTAMP"
        String[] parts = message.split(":");
        String type = parts[0];
        int senderID = Integer.parseInt(parts[1]);
        int timestamp = Integer.parseInt(parts[2]);

        // Update the Lamport clock (the max between the current value and the timestamp of the message + 1)
        lamportClock.updateAndGet(current -> Math.max(current, timestamp) + 1);

        switch (type) {
            case "REQUEST":
                // If it's a request, add it to the queue and send an acknowledgment
                synchronized (queue) {
                    queue.add(new Request(senderID, timestamp));
                }
                sendAcknowledgment(senderID);
                break;
            case "ACK":
                // If it's an acknowledgment, add it to the map
                acksReceived.put(senderID, true);
                break;
            case "RELEASE":
                // If it's a release, remove the request from the queue
                synchronized (queue) {
                    queue.removeIf(req -> req.processID == senderID);
                }
                break;
        }
    }


    /**
     * This method requests access to the critical section
     * <br>
     * It blocks until the process has access to the critical section
     */
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

    private void sendRequestToAllProcesses(Request request) {
        sendMessageToAllProcesses("REQUEST:" + request.processID + ":" + request.timestamp);
    }

    private void sendReleaseToAllProcesses() {
        sendMessageToAllProcesses("RELEASE:" + myID + ":" + lamportClock.get());
    }

    private void sendAcknowledgment(int processID) {
        sendMessage("ACK:" + myID + ":" + lamportClock.get(), processID);
    }

    private void sendMessageToAllProcesses(String message) {
        for (String socket : clientSockets) {
            if (socket == null) continue; //Don't send messages to ourselves (our socket is null
            sendMessage(message, socket);
        }
    }

    private void sendMessage(String message, int processID) {
        sendMessage(message, clientSockets.get(processID - 1));
    }

    private void sendMessage(String message, String socket) {
        try {
            Socket s = new Socket(socket.split(":")[0], Integer.parseInt(socket.split(":")[1]));
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(message);
            out.flush();
            s.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
