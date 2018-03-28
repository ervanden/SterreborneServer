package sterreborneserver;

import rgpio.*;

class RGPIOInterface implements VInputListener, MessageListener {

    VDigitalInput button;
    VDigitalOutput heating;
    VDigitalOutput boiler;
    VAnalogInput[] tmp;
    VAnalogInput[] hum;
    VAnalogOutput[] tmpOffset;

    final static int nrSensors = 3;

    public void onInputEvent(VInput vinput) {
    }

    public void onMessage(MessageEvent e) throws Exception {
        System.out.println(e.toString());

    }

    public void initialize() {

        RGPIO.addMessageListener(this);
        RGPIO.initialize();
        button = RGPIO.VDigitalInput("button");
        heating = RGPIO.VDigitalOutput("heating");
        boiler = RGPIO.VDigitalOutput("boiler");
        button.addVinputListener(this);

        tmp = new VAnalogInput[nrSensors];
        hum = new VAnalogInput[nrSensors];
        tmpOffset = new VAnalogOutput[nrSensors];
        
        for (int i = 0; i < nrSensors; i++) {
            tmp[i] = RGPIO.VAnalogInput("T" + (i + 1));
            hum[i] = RGPIO.VAnalogInput("H" + (i + 1));
          tmpOffset[i] = RGPIO.VAnalogOutput("OffsetT" + (i + 1));
        }

        RGPIO.createRRD(5);

        new ReadSensorThread(5).start();
    }

    public boolean switchOn(String output) {

        if (SterreborneServer.server_controlActive) {
            System.out.println("RGPIO Interface switch " + output + " On");

            if (output.equals("heating")) {
                heating.set("High");
            }
            if (output.equals("boiler")) {
                boiler.set("High");
            }
        }
        return true;
    }

    public boolean switchOff(String output) {

        if (SterreborneServer.server_controlActive) {
            System.out.println("RGPIO Interface switch " + output + " Off");

            if (output.equals("heating")) {
                heating.set("Low");
            }
            if (output.equals("boiler")) {
                boiler.set("Low");
            }
        }
        return false;
    }

    class ReadSensorThread extends Thread {

        int step;

        public ReadSensorThread(int step) {
            super();
            this.step = step;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(step * 1000);
                    
                    for (int i = 0; i < nrSensors; i++) {
                        tmpOffset[i].set("100");
                    }
                    
                    for (int i = 0; i < nrSensors; i++) {
                        tmp[i].get();
                        hum[i].get();
                    }

                } catch (InterruptedException ie) {
                }
            }
        }
    }
}
