package sterreborneserver;

import java.util.ArrayList;
import javax.swing.JFrame;

public class SterreborneServer {

    static int server_verbosity;
    static boolean server_controlActive;
    static int server_port;

    static PiClient piClient;
    public static RGPIOInterface rgpioInterface;
    static String client_target_host;
    static int client_target_port;
    static String client_command;
    static int client_verbosity;

    static MatrixTableModel tm;

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Active hours");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        SchedulerPanel newContentPane = new SchedulerPanel(tm);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        frame.pack();
        frame.setVisible(true);
    }

    private static void usage() {
        System.out.println("Usage :");
        System.out.println(" Scheduler.jar client server_ip server_port [verbosity]");
        System.out.println(" Scheduler.jar server [schedule_filename] [verbosity]");
        System.out.println();
    }

    public static void main(String[] args) {

        if (args.length == 0) { // run as server
           server_verbosity = 1;
            server_port = 6789;
            server_controlActive = true;

            TimeValue now = new TimeValue();
            System.out.println("Scheduler starts at " + now.dateName());
            System.out.println("verbosity=" + server_verbosity);
            System.out.println("controlActive=" + server_controlActive);
            System.out.println();

            rgpioInterface = new RGPIOInterface();
            rgpioInterface.initialize();
            new ServerEngine(6789, 5).start();
            new ServerEngine(6790, 6).start();
//            new PiButton(2);
            
        } else if (args[0].equals("client")) {
            client_target_host = "localhost";
            client_target_port = 6789;
            client_command = "gui";
            client_verbosity = 0;

            for (int arg = 2; arg <= args.length; arg++) {
                String[] s = args[arg - 1].split("=");
                if (s[0].equals("server")) {
                    client_target_host = s[1];
                } else if (s[0].equals("port")) {
                    client_target_port = Integer.parseInt(s[1]);
                } else if (s[0].equals("verbosity")) {
                    client_verbosity = Integer.parseInt(s[1]);
                } else if (s[0].equals("command")) {
                    client_command = s[1];
                }
            }

            tm = new MatrixTableModel();
            // client can not get schedule from server now because if verbose>0
            // messages go to the GUI which is not started yet

            piClient = new PiClient();
            PiClient.setServerAddress(client_target_host, client_target_port);

            if (client_command.equals("gui")) {
                TimeValue now = new TimeValue();
                System.out.println("Client starts at " + now.dateName());
                System.out.println("server=" + client_target_host);
                System.out.println("port=" + client_target_port);
                System.out.println("verbosity=" + client_verbosity);
                System.out.println();
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createAndShowGUI();
                        tm.getScheduleFromServer();
                    }
                });
            } else if (client_command.equals("SchedulerStatus")) {
                String reply = tm.getStatusFromServer();
                System.out.println(reply);
            } else if (client_command.equals("SchedulerTable")) {
                tm.getScheduleFromServer();
                tm.phpPrintSchedule();
            } else if (client_command.equals("SchedulerCommit")) {
                tm.readScheduleFromFile();
                tm.sendScheduleToServer();
            } else if (true) { // command for PhpServer to execute, if port=6788
                ArrayList<String> msg = new ArrayList<>();
                ArrayList<String> reply;
                msg.add(client_command);
                reply = PiClient.send(msg);
                for (String line : reply) {
                    System.out.println(line);
                }
            } else {
                System.out.println("unknown client command: " + client_command);
            }

        } else  {
 usage();
        }
    }
}
