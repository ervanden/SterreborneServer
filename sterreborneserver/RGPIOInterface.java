package sterreborneserver;

import rgpio.*;

class RGPIOInterface implements VInputListener, MessageListener {

    VDigitalInput button;
    VDigitalOutput heating;
    VDigitalOutput boiler;
    VAnalogInput[] tmp;
    VAnalogInput[] hum;
    VAnalogOutput[] tmpOffset;

    VDevice sensor1;
    VDevice sensor2;
    VDevice sensor3;

    final static int nrSensors = 3;

    public void onInputEvent(VInput vinput) {
    }

    public void onMessage(MessageEvent e) throws Exception {
        if (e.type != MessageType.UpdateRRDB) {
            System.out.println(e.toString());
        }
    }

    public void initialize() {

        RGPIO.addMessageListener(this);
        RGPIO.initialize();
        button = RGPIO.VDigitalInput("button");
        heating = RGPIO.VDigitalOutput("heating");
        boiler = RGPIO.VDigitalOutput("boiler");
        button.addVinputListener(this);

        sensor1 = RGPIO.VDevice("DHT22-1");
        sensor2 = RGPIO.VDevice("DHT22-2");
        sensor3 = RGPIO.VDevice("DHT22-3");

        tmp = new VAnalogInput[nrSensors];
        hum = new VAnalogInput[nrSensors];
        tmpOffset = new VAnalogOutput[nrSensors];

        for (int i = 0; i < nrSensors; i++) {
            tmp[i] = RGPIO.VAnalogInput("T" + (i + 1));
            hum[i] = RGPIO.VAnalogInput("H" + (i + 1));
            tmpOffset[i] = RGPIO.VAnalogOutput("OffsetT" + (i + 1));
        }

        RGPIO.createRRD(5);

        new ReadSensorThread(15).start();
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

                    sensor1.send("HAHA 1");
                    sensor2.send("HAHA 2");
                    sensor3.send("HAHA 3");

                    Thread.sleep(step * 1000);

                    tmpOffset[0].set("-81"); // T1
                    tmpOffset[1].set("-90"); // T2
                    tmpOffset[2].set("-84"); // T3

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
