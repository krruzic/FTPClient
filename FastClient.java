/**
 * FastClient Class
 * 
 * FastClient implements a basic reliable FTP client application based on UDP data transmission and selective repeat protocol
 * @author Kristopher Ruzic
 * @version 0.9, 19 Mar 2017
 */

import java.net.Socket;
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
    private int serverPort;
    private int timeout;

    private String serverName;
    private Socket tcpSock;
    private DatagramSocket udpSock;
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
            this.udpSock = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Could not create FastClient, exiting");
            System.exit(-1);
        }
        this.serverName = server_name; 
        this.serverPort = server_port;
        this.timeout = timeout;

        this.queue = new TxQueue(window);
    }

    /** 
     * send file 
     * @param file_name      file to be transfered
     */
    public void send(String file_name) { 
        try {
            doHandshake(file_name);
        } catch (Exception e) {
            System.out.println("Connection to Server could not be established");
            System.exit(-1);
        }
        // thread to handle acks (removing segments from queue)
        Thread qt = new Thread(new QueueHandler(this.queue, udpSock));
        qt.start();
        try {
            fis = new FileInputStream(new File(file_name));
        } catch (Exception e) {
            System.out.println("File could not be read or does not exist");
            System.exit(-1);
        }
        Segment s = new Segment();
        outgoing = new byte[s.MAX_PAYLOAD_SIZE];
        int seq = 0;
        int a = 0;
        System.out.println("Reading in file and creating packets");
        while (a != -1) {
            try {
                a = fis.read(outgoing,0,outgoing.length);
            } catch (Exception e) {
                System.out.println("File could not be read!");
                System.exit(-1);
            }
            if (a != -1) {
                try {
                    if (a == outgoing.length) s = new Segment(seq, outgoing);
                    else s = new Segment(seq, Arrays.copyOf(outgoing, a));

                    queue.add(s);
                    new Thread(new SegmentHandler(s, queue, serverName, udpSock, timeout, serverPort)).start();
                    seq++;
                } catch (Exception e) {
                    System.out.println("Problem creating packets...");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }
        while(!queue.isEmpty()) {} // wait for all segments to be ACKed
        qt.interrupt();
        try {
            fis.close();
            tcpOut.writeByte(0); // tell server we're done

            // clean up
            tcpSock.close();
            udpSock.close();
        } catch (Exception e) {
            System.out.println("Problem cleaning up after sending file. Exiting anyway");
            System.exit(0);
        }
    }

    /** 
     * Do handshake with the server
     * @param file_name file to do handshake for 
     */
    public void doHandshake(String file_name) throws FTPServerException, java.io.IOException {
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
