import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a heavyweight process.
 * <p>
 * It creates the lightweight processes and sends them the token to start.
 * It also listens for the lightweight processes to finish and sends the token to the next heavyweight process.
 */
public class HeavyweightProcess {

    /**
     * The number of lightweight processes
     */
    private static final int NUM_LIGHTWEIGHT_PROCESSES = 3;

    /**
     * This boolean indicates if this heavyweight process has the token
     */
    private static boolean token = false;

    /**
     * This socket is used to listen for the lightweight processes to notify that they have finished,
     *  and to listen for the token from the previous heavyweight process
     */
    private static ServerSocket serverSocket;


    public static void main(String[] args) {

        String myIp = args[0].split(":")[0];
        String myPort = args[0].split(":")[1];
        String senderIp = args[1].split(":")[0];
        String senderPort = args[1].split(":")[1];
        String myId = args[2];

        //If I am the first heavyweight process, I have the token
        if (myId.equals("A")) token = true;

        //Initialize the server socket
        try {
            serverSocket = new ServerSocket(Integer.parseInt(myPort));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Heavyweight process " + myId + " started");

        //Create a list of sockets for the lightweight processes
        ArrayList<String> lightweightSockets = new ArrayList<>();
        for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
            lightweightSockets.add(myIp + ":" + (Integer.parseInt(myPort) + i + 1));
        }
        List<Process> processes = new ArrayList<>();

        try {
            // Create the lightweight processes
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                ProcessBuilder light = new ProcessBuilder("java", "LightweightProcess.java", myId + (i + 1), lightweightSockets.toString(), myIp, myPort);
                light.inheritIO();
                processes.add(light.start());
            }

            // Add a shutdown hook to destroy the lightweight processes when this process is killed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Process proc : processes) {
                    proc.destroy();
                }
            }));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        //Wait for the lightweight processes to start properly
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        while (true) {
            //If I don't have the token, I listen for it
            while (!token) listenHeavyweight();

            //When I get the token, I send the action to all the lightweight processes (they can start now)
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                sendActionToLightweight(lightweightSockets.get(i).split(":")[0], lightweightSockets.get(i).split(":")[1]);
            }

            //I listen for the lightweight processes to finish
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                listenLightweight();
            }

            //When all the lightweight processes have finished, I send the token to the next heavyweight process
            token = false;
            sendTokenToHeavyweight(senderIp, senderPort);
        }
    }

    /**
     * Listens for the token from the previous heavyweight process
     */
    private static void listenHeavyweight() {
        try {
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            token = in.readBoolean();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Sends the token to the next heavyweight process
     * @param ip The ip of the next heavyweight process
     * @param port The port of the next heavyweight process
     */
    private static void sendTokenToHeavyweight(String ip, String port) {
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeBoolean(true);
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Tells a lightweight process to start
     * @param ip The ip of the lightweight process
     * @param port The port of the lightweight process
     */
    private static void sendActionToLightweight(String ip, String port) {
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            os.writeBoolean(true);
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Listens for a lightweight process to finish
     */
    private static void listenLightweight() {
        try {
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            in.readBoolean();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
