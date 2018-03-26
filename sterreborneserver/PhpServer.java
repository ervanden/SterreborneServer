package sterreborneserver;

import java.io.*;
import java.net.*;

class PhpServerThread extends Thread {

    private Socket socket = null;

    public PhpServerThread(Socket socket) {
        super("piServer Thread");
        this.socket = socket;

    }

    public void run() {

        try (BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());) {

            String command;
            command = inFromClient.readLine();
            if (!inFromClient.readLine().equals(".")) {
                System.out.println("server received " + command + " not followed by . ");
            }
            System.out.println("PhpServer received " + command);

            Process p;
            try {
                p = Runtime.getRuntime().exec(command);
                p.waitFor();
                BufferedReader reader
                        = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line = "";
                while ((line = reader.readLine()) != null) {
                    outToClient.writeBytes(line + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            outToClient.writeBytes(".\n");
            System.out.println("PhpServer replied to " + command);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class PhpServer extends Thread {

    private int portNumber;

    public PhpServer(int portNumber) {
        super();
        this.portNumber = portNumber;
    }

    public void run() {
        System.out.println("PhpServer starts listening on port " + portNumber);
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                new PhpServerThread(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            System.out.println("Could not listen on port " + portNumber + ". Exiting...");
            System.exit(-1);
        }
    }
}
