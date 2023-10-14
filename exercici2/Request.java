public class Request implements Comparable<Request> {
    final String address;
    final int timestamp;

    Request(String address, int timestamp) {
        this.address = address;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(Request o) {
        if (this.timestamp == o.timestamp) {
            return this.address.compareTo(o.address);
        }
        return Integer.compare(this.timestamp, o.timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Request) {
            Request other = (Request) obj;
            return address.equals(other.address) && timestamp == other.timestamp;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return address.hashCode() + timestamp;
    }
}