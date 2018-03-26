package sterreborneserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;



public class PiClient {
    
    // send and receive messages to the server address/port
    // Server and client add a line with a single "." to indicate end of the message   

    static String piAddress = "192.168.0.2";
    static int piPort = 6789;

    static public void setServerAddress(String host, int port) {
        piAddress=host;
        piPort=port;
    }

    static public ArrayList<String> send(ArrayList<String> msg) {

        // send msg txt to server and return reply txt from server
        
        ArrayList<String> reply = new ArrayList<>();

        try {
            Socket clientSocket = new Socket();

            clientSocket.connect(new InetSocketAddress(piAddress, piPort), 3000);

            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            for (String line : msg) {
                outToServer.writeBytes(line + "\n");
            }
            outToServer.writeBytes(".\n");

            String line = inFromServer.readLine();
            while (!line.equals(".")) {
                reply.add(line);
                line = inFromServer.readLine();
            }

            return reply;
            
        } catch (Exception se) {
            System.out.println("PiClient: Pi does not respond");
            System.out.println(se.getMessage());
            reply.add(se.getMessage());
            return reply;
        }

    }

}
