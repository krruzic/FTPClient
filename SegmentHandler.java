import java.util.Timer;
import java.util.TimerTask;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SegmentHandler implements Runnable {
    protected Timer resendTimer = null;
    protected Segment segment = null;
    protected TxQueue queue = null;

    public SegmentHandler(Segment segment, TxQueue queue, String serverName, DatagramSocket udpSock, int timeout, int port) {
        this.segment = segment;
        this.queue = queue;
        resendTimer = new Timer();
        resendTimer.schedule(new ResendTask(segment, serverName, udpSock, port), 0, timeout);
    }

    class ResendTask extends TimerTask {
        protected Segment segment = null;
        protected InetAddress host = null;
        protected DatagramSocket udpSock = null;
        protected int port = -1;

        public ResendTask(Segment s, String n, DatagramSocket u, int p) {
            this.segment = s;
            try {
                this.host = InetAddress.getByName(n);
            } catch (Exception e) {
                System.out.println("Error creating segment thread, crashing!");
                System.exit(-1);
            }
            this.udpSock = u;
            this.port = p;
        }

        public void run() {
            TxQueueNode temp = queue.getNode(segment.getSeqNum());
            if (temp == null) return;
            if (temp.getStatus() != 1) {
                DatagramPacket dp = new DatagramPacket(segment.getBytes(), segment.getLength(), host, port);
                try {
                    udpSock.send(dp);
                } catch (Exception e) {
                    System.out.println("Problem inside thread sending Segment!");
                }
                return;
            }
            resendTimer.cancel();
        } 
    }

    @Override
    public void run() {
        System.out.format("Sending Segment #%d\n", segment.getSeqNum());
    }
}
