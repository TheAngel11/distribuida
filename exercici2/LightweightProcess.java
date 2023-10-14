import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class represents a lightweight process.
 * <p>
 * It requests access to the critical section, prints a message every second 10 times, and releases access to the critical section.
 * It also notifies the heavyweight process that it has finished.
 */
public class LightweightProcess {

    /**
     * This socket is used to listen for the heavyweight process to notify that the process can start
     */
    private static ServerSocket serverSocket;

    public static void main(String[] args) {

        String myID = args[0];
        ArrayList<String> sockets = stringToArrayList(args[1]);
        String hwIp = args[2];
        String hwPort = args[3];

        //Initialize the server socket
        try {
            serverSocket = new ServerSocket(Integer.parseInt(sockets.get(Integer.parseInt(myID.charAt(1) + "") - 1).split(":")[1]));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        System.out.printf("Lightweight process %s started\n", myID);

        while (true) {
            //Wait for the heavyweight process to notify that the process can start
            waitHeavyWeight();

            //Request access to the critical section
            requestCS();

            //Print a message every second 10 times
            for (int i = 0; i < 10; i++) {
                System.out.printf("I'm lightweight process %s\n", myID);
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            //Release access to the critical section
            releaseCS();

            //Notify the heavyweight process that this process has finished
            notifyHeavyWeight(hwIp, hwPort);
        }

    }

    /**
     * Waits for the heavyweight process to notify that the process can start
     */
    private static void waitHeavyWeight() {
        try {
            Socket socket = serverSocket.accept();
            DataInputStream in = new DataInputStream(socket.getInputStream());
            in.readBoolean();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Notifies the heavyweight process that this process has finished
     * @param ip The ip of the heavyweight process
     * @param port The port of the heavyweight process
     */
    private static void notifyHeavyWeight(String ip, String port) {
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
     * Releases access to the critical section
     */
    private static void releaseCS() {
    }


    /**
     * Requests access to the critical section
     */
    private static void requestCS() {
    }


    /**
     * Converts a string to an ArrayList
     * @param s The string to convert (format: "[item1, item2, item3]", as returned by Arrays.toString())
     * @return The ArrayList
     */
    private static ArrayList<String> stringToArrayList(String s) {
        s = s.substring(1, s.length() - 1); //Remove the square brackets
        String[] items = s.split(", ");  //Split by comma followed by space
        return new ArrayList<>(Arrays.asList(items));
    }
}
