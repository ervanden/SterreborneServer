package sterreborneserver;

import rgpio.*;

class RGPIOInterfaceNEW implements VInputListener, VDeviceListener, MessageListener {

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
             System.out.println("VInput "+vinput.name+" sent event ");
    }
    
    public void onDeviceMessage(VDevice vdevice, String message){
     System.out.println("VDevice "+vdevice.name+" sent message \""+message+"\"");
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
/*
        sensor1 = RGPIO.VDevice("DHT22-1");
        sensor2 = RGPIO.VDevice("DHT22-2");
        sensor3 = RGPIO.VDevice("DHT22-3");
        sensor1.addVDeviceListener(this);
        sensor2.addVDeviceListener(this);
        sensor3.addVDeviceListener(this); 
        
        tmp = new VAnalogInput[nrSensors];
        hum = new VAnalogInput[nrSensors];
        tmpOffset = new VAnalogOutput[nrSensors];

        for (int i = 0; i < nrSensors; i++) {
            tmp[i] = RGPIO.VAnalogInput("T" + (i + 1));
            hum[i] = RGPIO.VAnalogInput("H" + (i + 1));
            tmpOffset[i] = RGPIO.VAnalogOutput("OffsetT" + (i + 1));
        }
*/
        RGPIO.createRRD(5);

//        new ReadSensorThread(10).start();
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

                    sensor1.sendMessage("PING1");
                    sensor2.sendMessage("PING2");
                    sensor3.sendMessage("PING3");

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
