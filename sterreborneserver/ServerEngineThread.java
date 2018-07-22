package sterreborneserver;

public class ServerEngineThread extends Thread {

    private boolean stop = false;
    private boolean fastforward = false;
    private ServerEngine serverEngine;

    public ServerEngineThread(ServerEngine serverEngine) {
        super("ServerEngineThread");
        this.serverEngine = serverEngine;
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
        SterreborneServer.message(serverEngine.portNumber, 1, "serverEngineThread is asked to restart");
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
                SterreborneServer.message(serverEngine.portNumber, 1, "sleep is interrupted");
                return;
            }
        }
    }


    private void changeState(boolean newState, TimeValue tnow, boolean showMessage) {
        if (newState) {
            serverEngine.STATE = SterreborneServer.rgpioInterface.switchOn(serverEngine.output);
        } else {
            serverEngine.STATE = SterreborneServer.rgpioInterface.switchOff(serverEngine.output);
        }

        serverEngine.JSONStatusToAll();

        if (showMessage) {
            SterreborneServer.message(serverEngine.portNumber, 1, printState(tnow) + "  <-----------");
        }
    }

    private String printState(TimeValue tnow) {
        String s = "STATE=";
        if (serverEngine.STATE) {
            s = s + "ON";
        } else {
            s = s + "OFF";
        }
        return s;
    }

    private void startScheduling() {
        SterreborneServer.message(serverEngine.portNumber, 1, "Restart scheduling");

        TimeValue tnow;
        TimeValue tprev;
        TimeValue tnext;

        boolean currentState, nextState;
        boolean firstIteration = true;

        if (!serverEngine.scheduleHasData()) {
            SterreborneServer.message(serverEngine.portNumber, 1, "Schedule has no data. Waiting...");
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

            SterreborneServer.message(serverEngine.portNumber, 1, printState(tnow) + "  <----------- Pin STATE");

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

                // expire if we are one week later than the same day in the schedule
                serverEngine.expireOnDate(tnow);

                tprev = serverEngine.previousEvent(tnow.dayName(), tnow.hour(), tnow.minute());
                tnext = serverEngine.nextEvent(tnow.dayName(), tnow.hour(), tnow.minute());

                SterreborneServer.message(serverEngine.portNumber, 2, tprev.dateName() + " < " + tnow.dateName() + "  < " + tnext.dateName());

                /* tprev is always on the same day as tnow */
                currentState = getState(tprev);
                SterreborneServer.message(serverEngine.portNumber, 2, "current state according to schedule (tprev)  = " + currentState);

                if (stop) {
                    return;
                }

                changeState(currentState, tnow, firstIteration);

                serverEngine.expireOnEndOfSchedule(tnow);
                // getState will from now on only be called for future events.
                // If tnow is in the last timeslot of the schedule, all future events expire

                nextState = getState(tnext);
                SterreborneServer.message(serverEngine.portNumber, 2, "next state according to schedule (tnext) = " + nextState);
                int secondsToNextEvent = tnext.isSecondsLaterThan(tnow);
                if (secondsToNextEvent < 0) {
                    // we are in the last timeslot of the day, tnext is next day
                    secondsToNextEvent = 24 * 3600 - (tnow.hour() * 3600 + tnow.minute() * 60);
                }
                SterreborneServer.message(serverEngine.portNumber, 2, "seconds to next event = " + secondsToNextEvent);
                SterreborneServer.message(serverEngine.portNumber, 2, "Sleeping " + secondsToNextEvent);

                if (!fastforward) {
                    stoppableSleep(secondsToNextEvent);
                }
                // roll time forward instead of creating a new tnow
                tnow.add(TimeValue.SECOND, secondsToNextEvent);

                if (stop) {
                    return;
                }

                changeState(nextState, tnow, true);

                SterreborneServer.message(serverEngine.portNumber, 2, "Sleeping " + 5 * 60);

                if (!fastforward) {
                    stoppableSleep(5 * 60);
                }
                tnow.add(TimeValue.SECOND, 5 * 60);
                firstIteration = false;

            }
        }

    }

    private boolean getState(TimeValue tschedule) {
        // get the state of the event in tschedule on the date of today.
        // it is assumed that tschedule and today are the same weekday.

        if (serverEngine.expired) {
            if (tschedule.once) {
                return !tschedule.on;
            } else {
                return tschedule.on;
            }
        } else { // not expired
            return tschedule.on;
        }
    }

}
