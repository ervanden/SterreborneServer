package sterreborneserver;

import com.pi4j.io.gpio.*;

public class RGPIOInterface {

    static GpioController gpio;

    static GpioPinDigitalOutput[] initializedOutputPins = new GpioPinDigitalOutput[20];
    static GpioPinDigitalInput[] initializedInputPins = new GpioPinDigitalInput[20];

    {
        for (int i = 0; i < 20; i++) {
            initializedOutputPins[i] = null;
            initializedInputPins[i] = null;
        }
    }

    static Pin intToPin(int i) {
        Pin pin = null;
        switch (i) {
            case 0:
                return RaspiPin.GPIO_00;
            case 1:
                return RaspiPin.GPIO_01;
            case 2:
                return RaspiPin.GPIO_02;
            case 3:
                return RaspiPin.GPIO_03;
            case 4:
                return RaspiPin.GPIO_04;
            case 5:
                return RaspiPin.GPIO_05;
            case 6:
                return RaspiPin.GPIO_06;
            case 7:
                return RaspiPin.GPIO_07;
            case 8:
                return RaspiPin.GPIO_08;
            case 9:
                return RaspiPin.GPIO_09;
            case 10:
                return RaspiPin.GPIO_10;
            case 11:
                return RaspiPin.GPIO_11;
            case 12:
                return RaspiPin.GPIO_12;
            case 13:
                return RaspiPin.GPIO_13;
            case 14:
                return RaspiPin.GPIO_14;
            case 15:
                return RaspiPin.GPIO_15;
            case 16:
                return RaspiPin.GPIO_16;
            case 17:
                return RaspiPin.GPIO_17;
            case 18:
                return RaspiPin.GPIO_18;
            case 19:
                return RaspiPin.GPIO_19;
            case 20:
                return RaspiPin.GPIO_20;
            default:
                System.out.println("non existing raspi pin " + i);

        }
        return pin;
    }

    static public boolean switchOn(int n) {
        System.out.println("switchOn(" + n + ")");
        GpioPinDigitalOutput pin = initializedOutputPins[n];
        if (SterreborneServer.server_controlActive) {
            System.out.println("Pi4J Pin " + n + " On");
            pin.high();
        }
        return true;
    }

    static public boolean switchOff(int n) {
        System.out.println("switchOff(" + n + ")");
        GpioPinDigitalOutput pin = initializedOutputPins[n];
        if (SterreborneServer.server_controlActive) {
            System.out.println("Pi4J Pin " + n + " Off");
            pin.low();
        }
        return false;
    }

    static public boolean readPin() {
        return false;
    }

    static public boolean initOutputPin(int n) {
        GpioPinDigitalOutput op = null;
        if (SterreborneServer.server_controlActive) {
            op = gpio.provisionDigitalOutputPin(intToPin(n), "LED", PinState.LOW);
            initializedOutputPins[n] = op;
        }
        return true;
    }

    static public GpioPinDigitalInput initInputPin(int n) {
        // return the GpioPin so that a listener can be added to it
        GpioPinDigitalInput ip = null;
        if (SterreborneServer.server_controlActive) {
            ip = gpio.provisionDigitalInputPin(intToPin(n), PinPullResistance.PULL_DOWN);
            ip.setShutdownOptions(true);
            initializedInputPins[n] = ip;
        }
        return ip;
    }

    public static void initialize() {
        if (SterreborneServer.server_controlActive) {
            gpio = GpioFactory.getInstance();
        }
    }
}
