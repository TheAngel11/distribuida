import java.net.ServerSocket;
import java.util.List;

public class RicartAgrawalaMutex extends DistributedMutex {

    public RicartAgrawalaMutex(int myID, List<String> clientSockets, ServerSocket serverSocket) {
        super(myID, clientSockets, serverSocket);
    }

    @Override
    void handleMessage(String message) {

    }

    @Override
    public void requestCS() {

    }

    @Override
    public void releaseCS() {

    }
}
