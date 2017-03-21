import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class QueueHandler implements Runnable {
    protected TxQueue queue = null;
    protected DatagramSocket udpSock = null;

    public QueueHandler(TxQueue q, DatagramSocket u) {
        this.queue = q;
        this.udpSock = u;
    }

    public void run() {
        byte[] incoming = new byte[1];
        while (!Thread.currentThread().isInterrupted()) {
            try {
                DatagramPacket resp = new DatagramPacket(incoming, incoming.length);
                udpSock.receive(resp);
                System.out.format("ACK received for Seq #%d\n", incoming[0]);
                if (queue.getNode(incoming[0]) != null)
                    queue.getNode(incoming[0]).setStatus(1);
                while (!queue.isEmpty() && queue.getHeadNode().getStatus() == 1) {
                    System.out.format("Removing segment at head: %d\n", queue.getHeadSegment().getSeqNum());
                    queue.remove();
                }
            } catch (java.net.SocketException e) {
                System.exit(0); // this is wrong and bad but works
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
