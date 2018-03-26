package scheduler;

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

public class ServerEngine {

    public int portNumber;
    public String output;
    
    public boolean STATE = false;
    public boolean expired = false;

    // expired=true  means that the current time is after the dates in the schedule
    // and the one time events are no longer to be executed.
    int columnCount = 7;
    int rowCount = 24 * 4;
    TimeValue[][] tableData = new TimeValue[rowCount][columnCount];

    String scheduleFileName;

    ArrayList<String> weekdays = new ArrayList<>();
    ServerEngineThread serverEngineThread = new ServerEngineThread(this);

    {
        weekdays.add("MONDAY");  // will all be overwritten when schedule is restored
        weekdays.add("TUESDAY");
        weekdays.add("WEDNESDAY");
        weekdays.add("THURSDAY");
        weekdays.add("FRIDAY");
        weekdays.add("SATURDAY");
        weekdays.add("SUNDAY");

        for (int col = 0; col < columnCount; col++) {
            for (int row = 0; row < rowCount; row++) {
                tableData[row][col] = null;
            }
        }
    }

    public ServerEngine(int portNumber, String output) {
        this.portNumber = portNumber;
        this.output = output;

        scheduleFileName = "/home/pi/Scheduler/Schedule"+portNumber+".txt";
        if (!Scheduler.server_controlActive) {
            scheduleFileName = "C:\\Users\\erikv\\Documents\\Schedule"+portNumber+".txt";
        }

        this.STATE = false;  // sure ??
        restoreSchedule();

    }
    
    private void msg(int verbosity, String message){
                SchedulerPanel.serverMessage(portNumber,verbosity,message);
    }

    public boolean scheduleHasData() {
        return tableData[0][0] != null;

    }

    public void start() {
        new PiServer(this).start();
        serverEngineThread.start();
    }

    public ArrayList<String> restart() {
        msg(1, "serverEngine is asked to restart");
        serverEngineThread.restart();
        ArrayList<String> reply = new ArrayList<>();
        reply.add("ok");
        return reply;
    }

    public int dayToColumn(String day) {
        return weekdays.indexOf(day);
    }

    public ArrayList<String> newSchedule(ArrayList<String> timeValueList) {
        msg(1, "Receiving schedule update");
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

    public ArrayList<String> getStatus() {
        ArrayList<String> reply = new ArrayList<>();
        if (STATE) {
            reply.add("ON");
        } else {
            reply.add("OFF");
        }
        return reply;
    }

    public ArrayList<String> getSchedule(ArrayList<String> day) {
        ArrayList<String> reply = new ArrayList<>();

        int col = dayToColumn(day.get(0));

        // if tableData has no values (first start of pi) return an empty list
        if (tableData[0][col] == null) {
            msg(1, "No data to send");
            return reply;
        } else {
            for (int row = 0; row < rowCount; row++) {
                reply.add(tableData[row][col].asString());
            }
        }
        return reply;
    }

    public ArrayList<String> saveSchedule() {
        ArrayList<String> reply = new ArrayList<>();

        // Stores the schedule in a file to restore after pi boot
        // This procedure is called after every update of the schedule
        msg(1, "Saving the schedule to " + scheduleFileName);
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
            msg(1, "io exception while writing to " + scheduleFileName);
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

        msg(1, "Restoring the schedule from " + scheduleFileName);
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

    public void expireOnDate(TimeValue tnow) {

        if (!expired) {
            if (!tnow.isSameDateAs(tableData[0][dayToColumn(tnow.dayName())])) {
                expired = true;
                msg(1, "expire on date ");
            }
        }
    }

    public void expireOnEndOfSchedule(TimeValue tnow) {

        if (!expired) {
            // check if tnow is in the last time slot of the schedule
            int row = tnow.hour() * 4 + tnow.minute() / 15;
            int col = dayToColumn(tnow.dayName());

            if ((row == (rowCount - 1)) && (col == (columnCount - 1))) {
                expired = true;
                msg(1, "expire on end of schedule ");
            }
        }
    }
}
