package sterreborneserver;

/*
package scheduler;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class PiButton {

    long p_mil; // time of last pin state change
    long pic_mil; // time when last picture was taken

    public PiButton(int pin) {

        final GpioPinDigitalInput myButton = Pi4j.initInputPin(pin);

        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                Calendar now = new GregorianCalendar();
                long mil = now.getTimeInMillis();
                long delta = mil-p_mil;
                p_mil = mil;

                System.out.println(delta + " msec  "
                        + "--> GPIO PIN " + pin
                        + " STATE CHANGE: " + event.getPin()
                        + " = " + event.getState());

                long pic_delta = mil-pic_mil;

                if (pic_delta > 2000) {

                    pic_mil = mil;

                    // do not take pictures faster than one per 2 seconds
                    Process p;
                    try {
                System.out.println("("+pic_delta + "msec  = picture delta) /home/pi/Scheduler/takePicture");
                        p = Runtime.getRuntime().exec("/home/pi/Scheduler/takePicture");
                        p.waitFor();
                        BufferedReader reader
                                = new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line + "\n");
                        }

                    } catch (Exception e) {
                        System.out.println("ERROR : could not execute command");
                    }
                }
            }

        });

        System.out.println(" ... Listening on GPIO #02.");

        try {
            while (true) {
                Thread.sleep(5000);
            }
        } catch (InterruptedException ie) {
            System.out.println("PiButton sleep interrupted exception");
        };

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}

*/
