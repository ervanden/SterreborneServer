package sterreborneserver;

public class SterreborneServer {

    static int verbosity;
    public static RGPIOInterface rgpioInterface;


    public static void message(int portNumber, int level, String message) {
        if (level<=verbosity){
            TimeValue now=new TimeValue();
            System.out.println(now.dateName()+ " ["+portNumber+"] "+message);
        }
    }

    public static void main(String[] args) {

        verbosity = 1;
        TimeValue now = new TimeValue();
        System.out.println("Scheduler starts at " + now.dateName());
        System.out.println("verbosity=" + verbosity);
        System.out.println();

        rgpioInterface = new RGPIOInterface();
        rgpioInterface.initialize();
        new ServerEngine(6789, 5).start();
        new ServerEngine(6790, 6).start();
//            new PiButton(2);
    }
}
