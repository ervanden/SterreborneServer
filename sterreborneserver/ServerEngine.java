package sterreborneserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class ServerEngine implements WSServerListener {

    public int portNumber;
    public int outputPin;

    public boolean STATE = false;
    public WSServer webSocketServer;

    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    String scheduleFileName;

    ArrayList<String> weekdays = new ArrayList<>();
    ServerEngineThread serverEngineThread = new ServerEngineThread();


    class ServerEngineThread extends Thread {

        private boolean stop = false;
        private boolean fastforward = false;

        public ServerEngineThread() {
            super("ServerEngineThread");
        }

        public void run() {
            if (true) {
                while (true) {
                    stop = false;
                    startScheduling();
                }
            }
        }


        public void restart() {
            SterreborneServer.message(portNumber, 1, "serverEngineThread is asked to restart");
            stop = true;
            // startScheduling() will now terminate and will be called again in run()
        }

        private void stoppableSleep(int seconds) {

            for (int s = 1; s <= seconds; s++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {

                }
                if (stop) {
                    SterreborneServer.message(portNumber, 1, "sleep is interrupted");
                    return;
                }
            }
        }


        private void changeState(boolean newState, TimeValue tnow, boolean showMessage) {
            if (newState) {
                STATE = SterreborneServer.rgpioInterface.switchOn(outputPin);
            } else {
                STATE = SterreborneServer.rgpioInterface.switchOff(outputPin);
            }

            JSONStatusToAll();

            if (showMessage) {
                SterreborneServer.message(portNumber, 1, printState(tnow) + "  <-----------");
            }
        }

        private String printState(TimeValue tnow) {
            String s = "STATE=";
            if (STATE) {
                s = s + "ON";
            } else {
                s = s + "OFF";
            }
            return s;
        }

        private void startScheduling() {
            SterreborneServer.message(portNumber, 1, "Restart scheduling");

            TimeValue tnow;
            TimeValue tprev;
            TimeValue tnext;

            boolean currentState, nextState;
            boolean firstIteration = true;

            if (!scheduleHasData()) {
                SterreborneServer.message(portNumber, 1, "Schedule has no data. Waiting...");
                stoppableSleep(60);
            } else {

            /* PSEUDO CODE,DO NOT REMOVE
             while (true)
             {
             t=now;
             STATUS := tprev.on
             sleep(tnext-t)
             STATUS := tnext.on
             sleep(5 min)
             }
             */

                tnow = new TimeValue(); //compiler needs initialization

                SterreborneServer.message(portNumber, 1, printState(tnow) + "  <----------- Pin STATE");

                while (!stop) {

                /* if fastforward, sleeps are replaced by increasing the time of tnow
                 and tnow is not syncronized with the real time after every loop
                 */
                    if (fastforward) {  // not too fast!
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                        }
                    }

                    if (!fastforward) {
                        tnow = new TimeValue();   // synchronize
                    }

                    tprev = previousEvent(tnow.dayName(), tnow.hour(), tnow.minute());
                    tnext = nextEvent(tnow.dayName(), tnow.hour(), tnow.minute());

                    SterreborneServer.message(portNumber, 2, tprev.dateName() + " < " + tnow.dateName() + "  < " + tnext.dateName());

                    /* tprev is always on the same day as tnow */
                    currentState = tprev.on;
                    SterreborneServer.message(portNumber, 2, "current state according to schedule (tprev)  = " + currentState);

                    if (stop) {
                        return;
                    }

                    changeState(currentState, tnow, firstIteration);

                    nextState = tnext.on;
                    SterreborneServer.message(portNumber, 2, "next state according to schedule (tnext) = " + nextState);
                    int secondsToNextEvent = tnext.isSecondsLaterThan(tnow);
                    if (secondsToNextEvent < 0) {
                        // we are in the last timeslot of the day, tnext is next day
                        secondsToNextEvent = 24 * 3600 - (tnow.hour() * 3600 + tnow.minute() * 60);
                    }
                    SterreborneServer.message(portNumber, 2, "seconds to next event = " + secondsToNextEvent);
                    SterreborneServer.message(portNumber, 2, "Sleeping " + secondsToNextEvent);

                    if (!fastforward) {
                        stoppableSleep(secondsToNextEvent);
                    }
                    // roll time forward instead of creating a new tnow
                    tnow.add(TimeValue.SECOND, secondsToNextEvent);

                    if (stop) {
                        return;
                    }

                    changeState(nextState, tnow, true);

                    SterreborneServer.message(portNumber, 2, "Sleeping " + 5 * 60);

                    if (!fastforward) {
                        stoppableSleep(5 * 60);
                    }

                    // we are now in the middle of the next time slot.
                    if (tprev.once) {
                        System.out.println(" tprev once = true");
                        System.out.println(tprev.asString());
                        tprev.once = false;
                        tprev.on = !tprev.on;
                        JSONTimeValueToAll(tprev);
                        saveSchedule();
                    }

                    tnow.add(TimeValue.SECOND, 5 * 60);
                    firstIteration = false;

                }
            }

        }
    }




    public ServerEngine(int portNumber, int outputPin) {
        this.portNumber = portNumber;
        this.outputPin = outputPin;
        SterreborneServer.rgpioInterface.initOutputPin(outputPin);
        scheduleFileName = "/home/pi/Scheduler/Schedule" + portNumber + ".txt";
        {
            for (int col = 0; col < columnCount; col++) {
                String dayName="";
                for (int h= 0; h < 24; h++) {
                    for (int q = 0; q < 4; q++) {
                        TimeValue tv = new TimeValue();
                        tv.set(Calendar.MINUTE, q*15);
                        tv.set(Calendar.HOUR_OF_DAY, h);
                        tv.set(Calendar.DAY_OF_WEEK,col+1); // col is 0-6, day of week is 1-7
                        dayName=tv.dayName();
                        tv.on=false;
                        tv.once=false;
                        tableData[h * 4 + q][col] = tv;
                        //System.out.println("col="+col+" row="+(h*4+q)+"  "+ tv.asString());

                    }
                }
                //System.out.println("column "+col+" is "+dayName);
                weekdays.add(dayName);
            }
        }
        this.STATE = false;  // sure ??
        restoreSchedule();

    }


    public boolean scheduleHasData() {
        return tableData[0][0] != null;

    }

    public void start() {
        webSocketServer = new WSServer(portNumber);
        webSocketServer.addListener(this);
        System.out.println("Starting Websocket Server on port " + portNumber);
        webSocketServer.start();
        serverEngineThread.start();
    }


    // WSServer calls onClientRequest when receiving a request

    public ArrayList<String> onClientRequest(String clientID, String request) {

        SterreborneServer.message(portNumber,1,"Client request : " + request);

        boolean invalidMessage = false;
        String[] tokens = request.split(":");

        if (tokens.length == 1) {

            if (tokens[0].equals("GETSTATUS")) {
                JSONStatusToAll();  // does not generate a reply
            } else if (tokens[0].equals("GETSCHEDULE")) {  // Get Schedule
                JSONScheduleToAll();
            } else if (tokens[0].equals("NSDONE")) {  // New Schedule complete
                saveSchedule();
                restart();
            } else {
                invalidMessage = true;
            }

        } else if (tokens.length == 5) {

            if (tokens[0].equals("NS")) {
                String day = tokens[1];
                int hour = Integer.parseInt(tokens[2]);
                int minute = Integer.parseInt(tokens[3]);
                String color = tokens[4];

                for (int col = 0; col < columnCount; col++) {
                    if (tableData[0][col].dayName().equals(day)) {
                        int row = hour * 4 + (minute / 15);
                        TimeValue tv = tableData[row][col];
                        if (tv==null) {  // happens when there is no data e.g. when the schedule file was deleted
                            tv=new TimeValue();
                        }
//System.out.println("OS " + day + ":" + tv.hour() + ":" + tv.minute() + "=" + tv.color() + "  row=" + row + " col=" + col);
//System.out.println("NS " + day + ":" + hour + ":" + minute + "=" + color + "  row=" + row + " col=" + col);
                        tv.on = (color.equals("red") || color.equals("darkred"));
                        tv.once = (color.equals("darkred") || color.equals("darkblue"));
                    }
                }
            } else { // invalid request
                invalidMessage = true;
            }

        } else {  // invalid number of tokens
            invalidMessage = true;
        }

        if (invalidMessage) SterreborneServer.message(portNumber,1, "invalid message: <" + request + ">");

        return null;
    }


    public ArrayList<String> restart() {
        SterreborneServer.message(portNumber,1, "serverEngine is asked to restart");
        serverEngineThread.restart();
        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    public int dayToColumn(String day) {
        return weekdays.indexOf(day);
    }

    public ArrayList<String> newSchedule(ArrayList<String> timeValueList) {
        SterreborneServer.message(portNumber,1, "Receiving schedule update");
        int col = 0;
        int row = 0;
        for (String line : timeValueList) {
            TimeValue timeValue = TimeValue.stringToTimeValue(line);
            weekdays.set(col, timeValue.dayName());
            tableData[row][col] = timeValue;
            row = (row + 1) % rowCount;
            if (row == 0) {
                col++;
            }
        }

        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }


    public void JSONStatusToAll() {
        SterreborneServer.message(portNumber,1,"Sending state update (STATE="+STATE+")");
        if (STATE) {
            webSocketServer.sendToAll("{\"messageID\":\"STATUS\", \"status\":\"ON\", \"port\":"+portNumber+"}");
        } else {
            webSocketServer.sendToAll("{\"messageID\":\"STATUS\", \"status\":\"OFF\", \"port\":"+portNumber+"}");
        }
    }

    public void JSONTimeValueToAll(TimeValue tv) {
        webSocketServer.sendToAll("{" +
                "\"messageID\":\"CS\", " +
                "\"day\":\"" + tv.dayName() + "\"," +
                "\"hour\":\"" + tv.hour() + "\"," +
                "\"minute\":\"" + tv.minute() + "\"," +
                "\"color\":\"" + tv.color() + "\"," +
                "\"port\":\"" + portNumber + "\"" +
                "}");
    }

    public void  JSONScheduleToAll() {

        // if tableData has no values (first start of pi) send nothing to the client
        if (tableData[0][0] == null) {
            SterreborneServer.message(portNumber,1, "No data to send");
            return;
        } else {
            for (int col = 0; col < columnCount; col++) {
                for (int row = 0; row < rowCount; row++) {
                    JSONTimeValueToAll(tableData[row][col]);
                }
            }
        webSocketServer.sendToAll( "{\"messageID\":\"CSDONE\"}");
        }
    }

    public ArrayList<String> saveSchedule() {
        ArrayList<String> reply = new ArrayList<>();

        // Stores the schedule in a file to restore after pi boot
        // This procedure is called after every update of the schedule

        SterreborneServer.message(portNumber,1, "Saving the schedule to " + scheduleFileName);
        try {
            File initialFile = new File(scheduleFileName);
            OutputStream is = new FileOutputStream(initialFile);
            OutputStreamWriter isr = new OutputStreamWriter(is, "UTF-8");
            BufferedWriter outputStream = new BufferedWriter(isr);

            // tableData[][] is always populated since saveSchedule() is called after an 
            // update from the client
            for (String day : weekdays) {
                int col = dayToColumn(day);
                for (int row = 0; row < rowCount; row++) {
                    outputStream.write(tableData[row][col].asString());
                    outputStream.newLine();
                }
            }
            outputStream.close();
            reply.add("ok");

        } catch (IOException io) {
            SterreborneServer.message(portNumber,1, "io exception while writing to " + scheduleFileName);
            reply.add("io exception while writing to " + scheduleFileName);
        }
        return reply;
    }

    public void restoreSchedule() {  // called when ServerEngine starts
        // we read the schedule file and put the entries in an  arraylist 
        // so that the schedule is in the same format as when
        // it was updated from the client.
        ArrayList<String> msg;
        BufferedReader inputStream = null;

        SterreborneServer.message(portNumber,1, "Restoring the schedule from " + scheduleFileName);
        try {
            File initialFile = new File(scheduleFileName);
            InputStream is = new FileInputStream(initialFile);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            inputStream = new BufferedReader(isr);

            msg = new ArrayList<>();
            for (int col = 0; col < columnCount; col++) {
                for (int row = 0; row < rowCount; row++) {
                    msg.add(inputStream.readLine());
                }
            }
            newSchedule(msg);

            inputStream.close();

        } catch (IOException io) {
            System.err.println(" io exception while reading from " + scheduleFileName);
        }
    }

    public TimeValue previousEvent(String dayName, int hour, int minute) {
        int row = hour * 4 + minute / 15;
        int col = dayToColumn(dayName);
        return tableData[row][col];
    }

    public TimeValue nextEvent(String dayName, int hour, int minute) {
        int row = hour * 4 + minute / 15;
        int col = dayToColumn(dayName);
        row = (row + 1) % rowCount;
        if (row == 0) { // overflow to next day
            col = (col + 1) % 7;
        }
        return tableData[row][col];
    }


}
