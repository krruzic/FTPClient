
import java.net.*;
/**
 * TxQueueNode Class
 * 
 * TxQueueNode implements each node in the TxQueue
 * 
 * @author      Cyriac James
 * @version     3.1, Jan 01, 2017
 *
 */
public class TxQueueNode {

	private int segmentStatus = -1; // Status of segment stored in the node; 0 - Sent by client , 1 - Acknowledged by server

        public Segment seg = null;
	public TxQueueNode next = null;
	public final static int SENT = 0; 
        public final static int ACKNOWLEDGED = 1; 

	 /**
        * Constructor - create a new queue node
        * @param seg              Segment
        */

	public TxQueueNode(Segment seg)
	{
		this.seg = seg;
	}
	
	/**
        * Set the status of the node
        * @param status         Segment status
        */

	public void setStatus(int status)
	{
		segmentStatus = status;
	}

	/**
        * return the segment status
        */


	public int getStatus()
        {
                return segmentStatus;
        }

}
