public class SegmentHandler implements Runnable {

    protected Segment seg = null;

    public SegmentHandler(segment) {
        this.seg = segment;
    }

    public void run() {
        byte[] incoming = new byte[1];
        while (true) {
            DatagramPacket resp = new DatagramPacket(incoming, incoming.length);
        }
    }

}
