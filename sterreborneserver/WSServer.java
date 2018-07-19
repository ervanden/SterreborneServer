package sterreborneserver;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;


public class WSServer extends WebSocketServer {

    public WSServer(int port) { // throws UnknownHostException {

        super(new InetSocketAddress(port));

    }

    // the reply to a client request is generated by a listener object that has access to the application
    // data. By using a listener the WSServer code can stay generic

    private WSServerListener listener = null;

    public void addListener(WSServerListener l) {
        if (listener != null) {
            System.out.println("WSServer can only have 1 listener");
        } else {
            listener = l;
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.sendToAll("new connection (resource) : " + handshake.getResourceDescriptor());
        this.sendToAll("new connection (address) : " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        this.sendToAll(conn + " has closed the connection!");
    }

    @Override
    public void onMessage(WebSocket clientConnection, String request) {

        ArrayList<String> reply;
//        System.out.println("MESSAGE FROM CLIENT " + clientConnection + " : " + request);
        reply = listener.onClientRequest(clientConnection.toString(), request);
        for (String r : reply) {
//            System.out.println(" reply : "+r);
            clientConnection.send(r);
        }

//        System.out.println("DONE WITH CLIENT " + clientConnection);

    }

    @Override
    public void onFragment(WebSocket conn, Framedata fragment) {
        System.out.println("received fragment: " + fragment);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Websocket server started!");
    }

    public void sendToAll(String text) {
        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}

