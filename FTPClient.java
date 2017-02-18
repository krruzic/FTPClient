/**
 * FastFTP Class
 * FastFtp implements a basic FTP application based on UDP data transmission and 
 * alternating-bit stop-and-wait concept
 * @author      Kristopher Ruzic
 * @version     1.5, 16 Feb 2017
 *
 
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
public class FTPClient {

    private String file_name;
    private Socket tcpSock; 
    private DatagramSocket udpSock;
    private DataInputStream tcpIn;
    private DataOutputStream tcpOut;
    private byte[] outgoing;    
    private FileInputStream fis;
    private int server_port;
    /**
     * Constructor to initialize the program 
     * 
     * @param serverName    server name
     * @param server_port   server port
     * @param file_name     name of file to transfer * @param timeout       Time out value (in milli-seconds).
     */

    // sets up all the attributes and calls the handshake routine
    public FTPClient(String server_name, int server_port, String file_name, int timeout) { 
        try {
            this.tcpSock = new Socket(server_name, server_port); 
            this.tcpSock.setReuseAddress(true);
            this.file_name = file_name; 
            doHandshake();
            this.udpSock = new DatagramSocket();
            this.udpSock.setSoTimeout(timeout);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (FTPServerException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        this.server_port = server_port;
    }

    /*
     * method that does the sending. Reads file chunk by chunk,
     * creates Segments then sends them in a datagram packet
     * calls waitForACK to ensure it was sent
     * also does the closing handshake
     */
    public void send() throws Exception { 
        InetAddress hostAddress = InetAddress.getByName("localhost");
        fis = new FileInputStream(new File(file_name)); 
        Segment s = new Segment();
        this.outgoing = new byte[s.MAX_PAYLOAD_SIZE];
        int count = 0;
        int a;
        int seq = 1;
        while((a = fis.read(outgoing,0,outgoing.length)) != -1) {
            seq = (seq == 1) ? 0 : 1;
            if (a == outgoing.length) s = new Segment(seq, outgoing);
            else s = new Segment(seq, Arrays.copyOf(outgoing, a));
            DatagramPacket dp = new DatagramPacket(s.getBytes(), s.getLength(), hostAddress, this.server_port);
            this.udpSock.send(dp); 
            waitForACK(dp,count++,seq);
        }
        fis.close();
        this.tcpOut.writeByte(0);
       
        // clean up
        this.tcpSock.close();
        this.udpSock.close();
    }

    // loops infinitely until it recieves an ACK for the packet
    public void waitForACK(DatagramPacket dp, int count, int expectedSeq) throws Exception {
        byte[] incoming = new byte[1];
        while (true) {
            DatagramPacket resp = new DatagramPacket(incoming, incoming.length);
            try {
                udpSock.receive(resp);
                if (incoming[0] != expectedSeq)
                    System.out.println("duplicate ACK, ignoring");
            } catch (SocketTimeoutException e) {
                System.out.format("resending packet %d!\n", count);
                this.udpSock.send(dp);
                continue;
            }
            System.out.format("packet %d sent and ACKed, moving to next segment\n", count);
            break;
        }
    }

    // writes one byte on the tcp socket and checks if it got a response
    public void doHandshake() throws FTPServerException, java.io.IOException {
        this.tcpOut = new DataOutputStream(this.tcpSock.getOutputStream());
        this.tcpOut.writeUTF(this.file_name);
        this.tcpOut.flush();
        this.tcpIn = new DataInputStream(this.tcpSock.getInputStream());
        byte resp = this.tcpIn.readByte();
        
        if (resp == 0) System.out.println("Handshake done!");
        else throw new FTPServerException("Could not complete handshake with server");
    }

    public static void main(String[] args) {
        
        String server = "localhost";
        String file_name = "";
        int server_port = 8888;
        int timeout = 50; // milli-seconds (this value should not be changed)

        
        // check for command line arguments
        if (args.length == 3) {
            // either provide 3 parameters
            server = args[0];
            server_port = Integer.parseInt(args[1]);
            file_name = args[2];
        }
        else {
            System.out.println("wrong number of arguments, try again.");
            System.out.println("usage: java FTPClient server port file");
            System.exit(0);
        }

        
        FTPClient ftp = new FTPClient(server, server_port, file_name, timeout);
        
        System.out.printf("sending file \'%s\' to server...\n", file_name);
        try {
            ftp.send();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        System.out.println("file transfer completed.");
    }

}
