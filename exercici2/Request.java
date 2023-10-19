public class Request {

    int processID;
    int timestamp;

    public Request(int processID, int timestamp) {
        this.processID = processID;
        this.timestamp = timestamp;
    }


    @Override
    public String toString() {
        return "Request{" + "processID=" + processID + ", timestamp=" + timestamp + '}';
    }
}