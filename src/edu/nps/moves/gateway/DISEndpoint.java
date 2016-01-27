
package edu.nps.moves.gateway;

/**
 * interface for things that can read or send binary or JSON format DIS packets. This
 * may be a web page connected via a web socket or, on the server side, a 
 * native socket that reads and writes bcast or mcast DIS.
 * 
 * @author DMcG
 */
public interface DISEndpoint 
{
    /** Send binary format DIS to an endpoint. */
   public void sendBinaryToClient(byte[] buf);
   
   
   
   /** Send JSON format DIS to a client */
   public void sendToClient(String aMessage);

}
