/**
 * FastFTP Class
 * FastFtp implements a basic FTP application based on UDP data transmission and 
 * alternating-bit stop-and-wait concept
 * @author      Kristopher Ruzic
 * @version     1.0, 1 Feb 2017
 *
 */

import java.util.Timer;
import java.net.Socket;
import java.net.DatagramSocket;
import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
public class FTPClient {

    private String file_name;
    private Timer t; 
    private Socket tcpSock; 
    /**
     * Constructor to initialize the program 
     * 
     * @param serverName    server name
     * @param server_port   server port
     * @param file_name     name of file to transfer
     * @param timeout       Time out value (in milli-seconds).
     */
    
    public FTPClient(String server_name, int server_port, String file_name, int timeout) {
    
    /* Initialize values */
        this.tcpSock = new Socket(server_name, server_port);        
        this.file_name = file_name; 
        this.t = new Timer(timeout);
        doHandShake(); 
    }
    

    /**
     * Send file content as Segments
     * 
     */
    public void send() {
        
        /* send logic goes here. You may introduce addtional methods and classes
    }

    

/**
        * A simple test driver
         * 
        */
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
        ftp.send();
        System.out.println("file transfer completed.");
    }

}
