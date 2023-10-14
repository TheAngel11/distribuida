import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class LightweightProcess {

    public static void main(String[] args) {

        String myID = args[0];
        ArrayList<String> sockets = stringToArrayList(args[1]);
        String hwIp = args[2];
        String hwPort = args[3];

        while (true) {
            waitHeavyWeight(sockets.get(Integer.parseInt(myID.charAt(1) + "") - 1).split(":")[1]);
            requestCS();
            for (int i = 0; i < 10; i++) {
                System.out.printf("I'm lightweight process %s\n", myID);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }

            releaseCS();
            notifyHeavyWeight(hwIp, hwPort);
        }

    }

    private static void waitHeavyWeight(String port) {
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

    private static void releaseCS() {
    }

    private static void requestCS() {
    }


    public static ArrayList<String> stringToArrayList(String s) {
        s = s.substring(1, s.length() - 1); //Remove the square brackets
        String[] items = s.split(", ");  //Split by comma followed by space
        return new ArrayList<>(Arrays.asList(items));
    }
}
