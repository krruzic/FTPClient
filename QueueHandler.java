import java.net.DatagramPacket;
import java.net.DatagramSocket;

import java.util.Arrays;
public class QueueHandler implements Runnable {
    protected TxQueue queue = null;
    protected DatagramSocket udpSock = null;

    public QueueHandler(TxQueue q, DatagramSocket u) {
        this.queue = q;
        this.udpSock = u;
    }

    public void run() {
        while (!udpSock.isClosed()) {
            byte[] incoming = new byte[4];
            int ack = -1;
            try {
                DatagramPacket resp = new DatagramPacket(incoming, incoming.length);
                udpSock.receive(resp);
                
                // convert byte array read in to an int
                ack = java.nio.ByteBuffer.wrap(Arrays.copyOf(resp.getData(), incoming.length)).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
                System.out.format("ACK received for Seq #%d\n", ack);
                TxQueueNode cur = queue.getNode(ack);
                if (cur != null)
                    cur.setStatus(1);
                while (!queue.isEmpty() && queue.getHeadNode().getStatus() == 1) {
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
