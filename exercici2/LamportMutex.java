import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

public class LamportMutex {

    private final ArrayList<String> processes;
    private final ServerSocket serverSocket;
    private final AtomicInteger clock;
    private final PriorityBlockingQueue<Request> queue;
    private final ConcurrentHashMap<String, Boolean> replies;
    private volatile boolean inCriticalSection;
    private volatile boolean requestedCriticalSection;

    public LamportMutex(ArrayList<String> processes, ServerSocket serverSocket) {
        this.processes = processes;
        this.serverSocket = serverSocket;
        this.clock = new AtomicInteger(0);
        this.queue = new PriorityBlockingQueue<>();
        this.replies = new ConcurrentHashMap<>();
        this.inCriticalSection = false;
        this.requestedCriticalSection = false;

        for (String process : processes) {
            replies.put(process, false);
        }
    }

    public void requestCS() {
        int timestamp = clock.incrementAndGet();
        Request myRequest = new Request(serverSocket.getLocalSocketAddress().toString(), timestamp);
        queue.add(myRequest);
        requestedCriticalSection = true;

        // Send request to all processes
        for (String processAddress : processes) {
            sendMessage(processAddress, "request", timestamp);
        }

        // Wait for replies from all processes and for our request to be at the head of the queue
        while (queue.peek() != myRequest || replies.containsValue(false)) {
            // Busy wait
        }

        inCriticalSection = true;
    }

    public void releaseCS() {
        int timestamp = clock.incrementAndGet();
        queue.poll();  // Remove our request

        // Reset all replies
        for (String key : replies.keySet()) {
            replies.put(key, false);
        }

        requestedCriticalSection = false;
        inCriticalSection = false;

        // Notify all other processes
        for (String processAddress : processes) {
            sendMessage(processAddress, "release", timestamp);
        }
    }

    public void receiveMessage() {
        try {
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());

            String type = in.readUTF();
            String senderAddress = in.readUTF();
            int timestamp = in.readInt();

            clock.set(Math.max(clock.get(), timestamp) + 1);  // Update clock
            System.out.println("Received message from " + senderAddress + type + timestamp);

            switch (type) {
                case "request":
                    queue.add(new Request(senderAddress, timestamp));
                    sendMessage(senderAddress, "reply", clock.get());
                    break;
                case "reply":
                    replies.put(senderAddress, true);
                    break;
                case "release":
                    queue.remove(new Request(senderAddress, timestamp));
                    break;
            }

            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void sendMessage(String address, String type, int timestamp) {
        try {
            String[] parts = address.split(":");
            Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            // Here we'll get the address and port separately.
            String localAddress = socket.getLocalAddress().getHostAddress();
            int localPort = serverSocket.getLocalPort();

            out.writeUTF(type);
            out.writeUTF(localAddress + ":" + localPort);  // Use the combined address and port
            out.writeInt(timestamp);

            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
