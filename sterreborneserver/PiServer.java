package sterreborneserver;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ServerThread extends Thread {

    private Socket socket = null;
    private ServerEngine serverEngine;

    public ServerThread(ServerEngine serverEngine,Socket socket) {
        super("piServer Thread");
        this.socket = socket;
        this.serverEngine = serverEngine;
    }

    public void run() {

        try (
                BufferedReader inFromClient
                = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());) {

            // wait for client to send a command, followed by text, terminated with "."
            String command;
            ArrayList<String> text = new ArrayList<>();
            ArrayList<String> reply = new ArrayList<>();

            command = inFromClient.readLine();

            String line = inFromClient.readLine();
            while (!line.equals(".")) {
                text.add(line);                
                line = inFromClient.readLine();
            }

            if (command.equals("newSchedule")) {
                reply = serverEngine.newSchedule(text);
            } else if (command.equals("getSchedule")) {
                reply = serverEngine.getSchedule(text);
            } else if (command.equals("getStatus")) {
                reply = serverEngine.getStatus();
            } else if (command.equals("saveSchedule")) {
                reply = serverEngine.saveSchedule();
            } else if (command.equals("restartScheduler")) {
                reply = serverEngine.restart();
            } else {
                System.err.println("unknown command from client : <" + command + ">");
            }

            for (String l : reply) {
                outToClient.writeBytes(l + "\n");
            }
            outToClient.writeBytes(".\n");
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class PiServer extends Thread{

    private ServerEngine serverEngine;
    private int portNumber;

       public PiServer(ServerEngine serverEngine) {
        super();
        this.serverEngine=serverEngine;
        portNumber=serverEngine.portNumber;
    }

    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            SchedulerPanel.serverMessage(portNumber,1, "Listening on port " + portNumber);
            while (true) {
                new ServerThread(serverEngine,serverSocket.accept()).start();
            }
        } catch (IOException e) {
            SchedulerPanel.serverMessage(portNumber,1, "Could not listen on port " + portNumber+". Exiting...");
            System.exit(-1);
        }
    }
}
