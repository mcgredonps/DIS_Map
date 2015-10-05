/**
 * Lifted from http://amilamanoj.blogspot.com/2013/06/secure-websockets-with-jetty.html
 */

package edu.nps.moves.gateway;

/**
 * A class that corresponds to one connection to one web page client. This is created when
 * a client connects a websocket to us, one instance per connection. Typically there's one
 * websocket opened from the web page to the server.
 * 
 * @author DMcG
 */

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import org.json.*;
import java.nio.*;

import edu.nps.moves.disutil.*;
import edu.nps.moves.dis.*;

import java.io.IOException;

@WebSocket
public class WebPageConnection implements DISEndpoint
{
    private static int count = 0;
    
    /** The far side of the connection */
    private RemoteEndpoint remote;
    int connectionNumber = 0;
    ConnectionManager connectionManager = ConnectionManager.getConnectionManager();
   

    /** Fired when the websocket from the client connects. Add it to the ConnectionManager's
     * list of connections.
     * 
     * @param session The session with the web client
     */
    @OnWebSocketConnect
    public void onConnect(Session session) 
    {
        System.out.println("WebSocket Opened");
        this.remote = session.getRemote();
        count++;
        this.connectionNumber = count;
        
        ConnectionManager.getConnectionManager().addConnection(this);
    }

    /** Fired when the client sends a message. Tell the connection manager to repeat the 
     * message to all other clients, but do not loop it back to the sender.
     * 
     * @param message the (text) message sent from the client to the server, typicall in JSON format
     */
    @OnWebSocketMessage
    public void onMessage(String message) 
    {
        //System.out.println("Message from Client: " + message);
        connectionManager.repeatMessage(message, this);
       
    }
    
    /** Fired when a client sends a binary message. 
     *
     * @param buf binary data
     * @param offset
     * @param length 
     */
    @OnWebSocketMessage
    public void onBinaryMessage(byte buf[], int offset, int length)
    {
       //PduFactory factory = new PduFactory();
       //Pdu aPdu = factory.createPdu(buf);
       //System.out.println("Got web pdu of type " + aPdu.getClass().getName() );
        
       // The distribution of a message to all the clients is handled in a separate
       // thread. This decouples handling the incoming traffic from the distribution,
       // which can be slow. The idea is that we want to get in an out fast in
       // this method.
       connectionManager.enqueueBinaryMessage(buf, this);
    }

    /** Fired when the web socket closes. The client informs us, and we remove
     * the client from the list of active clients in ConnectionManager
     * 
     * @param statusCode reason for closure
     * @param reason string reason for closure
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) 
    {
            System.out.println("WebSocket Closed. Code:" + statusCode + " Reason:" + reason);
            ConnectionManager.getConnectionManager().removeConnection(this);
    }
    
    /**
     * Sends data in string format to the client. This is typically in
     * JSON format. This is usually called by the ConnectionManager.
     * 
     * @param message data to be sent
     */
    public void sendToClient(String message)
    {
        try
        {
            remote.sendString(message);
        }
        catch(IOException e)
        {
            System.out.println(e);
        }
    }
    
    /**
     * Sends data in binary format to the client. The end client is typically
     * another web page. This is called typically from ConnectionManager.
     * 
     * @param buf 
     */
    public void sendBinaryToClient(byte[] buf)
    {
        try
        {
            remote.sendBytes(ByteBuffer.wrap(buf));
            remote.flush();
            //System.out.println("Sent to connection " + connectionNumber);
        }
        catch(IOException e)
        {
            System.out.println("In connection " + connectionNumber);
            System.out.println(e);
            e.printStackTrace();

        }
    }

}
