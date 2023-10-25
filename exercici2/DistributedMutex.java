import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DistributedMutex {

    /**
     * Lamport's logical clock, initialized to 0
     * <br>
     * Atomic so that it can be incremented atomically, on different threads
     */
    public final AtomicInteger lamportClock = new AtomicInteger(0);

    /**
     * This map stores the acknowledgments received for the current request
     * <br>
     * It is synchronized too so that it can be accessed by different threads
     */
    public final ConcurrentHashMap<Integer, Boolean> acksReceived = new ConcurrentHashMap<>();

    public final int myID;
    private final ServerSocket serverSocket;
    public final List<String> clientSockets;


    public DistributedMutex(int myID, List<String> clientSockets, ServerSocket serverSocket) {
        this.myID = myID;
        this.serverSocket = serverSocket;
        this.clientSockets = clientSockets; // Our socket is null, so we don't send messages to ourselves

        // Finally, Start a thread to listen for incoming messages
        Thread t = new Thread(this::listenForMessages);
        t.start();
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
    abstract void handleMessage(String message);


    /**
     * This method requests access to the critical section
     * <br>
     * It blocks until the process has access to the critical section
     */
    public abstract void requestCS();


    /**
     * This method releases access to the critical section
     * <br>
     * It notifies all processes that the critical section is free
     */
    public abstract void releaseCS();



    public void sendRequestToAllProcesses(Request request) {
        sendMessageToAllProcesses("REQUEST:" + request.processID + ":" + request.timestamp);
    }

    public void sendReleaseToAllProcesses() {
        sendMessageToAllProcesses("RELEASE:" + myID + ":" + lamportClock.get());
    }

    public void sendAcknowledgment(int processID) {
        sendMessage("ACK:" + myID + ":" + lamportClock.get(), clientSockets.get(processID - 1));
    }

    public void sendMessageToAllProcesses(String message) {
        for (String socket : clientSockets) {
            if (socket == null) continue; //Don't send messages to ourselves (our socket is null
            sendMessage(message, socket);
        }
    }


    public void sendMessage(String message, String socket) {
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
