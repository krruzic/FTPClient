


/**
 * FastClient Class
 * 
 * FastClient implements a basic reliable FTP client application based on UDP data transmission and selective repeat protocol
 * @author Kristopher Ruzic
 * @version 0.1, 19 Mar 2017
 */

import java.net.Socket;
import java.net.DatagramPacket;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.net.InetAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Arrays;


public class FastClient {


    private InetAddress hostAddr;
    private int serverPort;
    private int timeout;

    private Socket tcpSock;
    private DataInputStream tcpIn;
    private DataOutputStream tcpOut;
    private byte[] outgoing;
    private FileInputStream fis;
    private TxQueue queue;

    /**
     * Constructor to initialize the program 
     * 
     * @param server_name    server name or IP
     * @param server_port    server port
     * @param window         window size
     * @param timeout        time out value
     */
    public FastClient(String server_name, int server_port, int window, int timeout) {
        /* initialize */	
        try {
            this.tcpSock = new Socket(server_name, server_port);
            this.tcpSock.setReuseAddress(true);
        } catch (Exception e) {
        }
        this.hostAddr = InetAddress.getByName(server_name);
        this.serverPort = server_port;
        this.timeout = timeout;
        this.queue = new TxQueue(window);

    }

    /** 
     * send file 
     * @param file_name      file to be transfered
     */
    public void send(String file_name) throws Exception {
        doHandshake(file_name);
        // thread to handle acks (removing segments from queue)
        new Thread(new ACKHandler(this.queue)).start();

        fis = new FileInputStream(new File(file_name));
        Segment s = new Segment();
        outgoing = new byte[s.MAX_PAYLOAD_SIZE];
        if ((a = fis.read(outgoing,0,outgoing.length)) == -1)
            break; // file is blank

        int seq = 0;
        while (true) {
            if (a == outgoing.length) s = new Segment(seq, outgoing);
            else s = new Segment(seq, Arrays.copyOf(outgoing, a));
            queue.add(s);
            new Thread(new SeqmentHandler(s));
            if ((a = fis.read(outgoing,0,outgoing.length)) == -1)
                break; // whole file read
            seq++;
        }
//        while ((a = fis.read(outgoing,0,outgoing.length)) != -1) {
//            if (a == outgoing.length) s = new Segment(count, outgoing);
//            else s = new Segment(count, Arrays.copyOf(outgoing, a));
//            while (!queue.isFull()) {
//                // read in bytes, create segment and add to queue
//                DatagramPacket dp = new DatagramPacket(s.getBytes(), s.getLength(), hostAddr, this.server_port);
//                
//            }
//        }
        fis.close();
        tcpOut.writeByte(0);

        // clean up
        tcpSock.close();
        udpSock.close();
    }

    /** 
     * Do handshake with the server
     * @param file_name file to do handshake for 
     */
    public void doHandshake(string file_name) throws FTPServerException, java.io.IOException {
        tcpOut = new DataOutputStream(tcpSock.getOutputStream());
        tcpOut.writeUTF(file_name);
        tcpOut.flush();
        tcpIn = new DataInputStream(tcpSock.getInputStream());
        byte resp = tcpIn.readByte();

        if (resp == 0) System.out.println("Handshake done!");
        else throw new FTPServerException("Could not complete handshake with server");
    }

    /**
     * A simple test driver
     * 
     */
    public static void main(String[] args) {
        int window = 10; //segments
        int timeout = 100; // milli-seconds (don't change this value)

        String server = "localhost";
        String file_name = "";
        int server_port = 0;

        // check for command line arguments
        if (args.length == 4) {
            // either provide 3 parameters
            server = args[0];
            server_port = Integer.parseInt(args[1]);
            file_name = args[2];
            window = Integer.parseInt(args[3]);
        }
        else {
            System.out.println("wrong number of arguments, try again.");
            System.out.println("usage: java FastClient server port file windowsize");
            System.exit(0);
        }


        FastClient fc = new FastClient(server, server_port, window, timeout);

        System.out.printf("sending file \'%s\' to server...\n", file_name);
        fc.send(file_name);
        System.out.println("file transfer completed.");
    }

}
