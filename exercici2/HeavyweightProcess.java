import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HeavyweightProcess {

    private static final int NUM_LIGHTWEIGHT_PROCESSES = 3;
    private static boolean token = false;

    public static void main(String[] args) {

        String myIp = args[1].split(":")[0];
        String myPort = args[1].split(":")[1];
        String senderIp = args[2].split(":")[0];
        String senderPort = args[2].split(":")[1];
        String myId = args[3];

        //If I am the first heavyweight process, I have the token
        if (myId.equals("A")) token = true;

        System.out.println("Heavyweight process " + myId + " started");

        List<Process> processes = new ArrayList<>();
        ArrayList<String> lightweightSockets = new ArrayList<>();
        for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
            lightweightSockets.add(myIp + ":" + (Integer.parseInt(myPort) + i));
        }

        try {
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                ProcessBuilder light = new ProcessBuilder("java", "LightweightProcess", myId + (i + 1), lightweightSockets.toString());
                light.inheritIO();
                processes.add(light.start());
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                for (Process proc : processes) {
                    proc.destroy();
                }
            }));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        while (true) {
            //If I don't have the token, I listen for it
            while (!token) listenHeavyweight(myPort);

            //When I get the token, I send the action to all the lightweight processes (they can start now)
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                sendActionToLightweight(lightweightSockets.get(i).split(":")[0], lightweightSockets.get(i).split(":")[1]);
            }

            //I listen for the lightweight processes to finish
            for (int i = 0; i < NUM_LIGHTWEIGHT_PROCESSES; i++) {
                listenLightweight(myPort);
            }

            token = false;
            sendTokenToHeavyweight(senderIp, senderPort);
        }
    }

    private static void listenHeavyweight(String port) {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            token = in.readBoolean();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

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

    private static void sendActionToLightweight(String ip, String port) {
        try {
            Socket socket = new Socket(ip, Integer.parseInt(port));
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());
            // Send action
            os.writeBoolean(true);
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void listenLightweight(String port) {
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(port));
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            in.readBoolean();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
