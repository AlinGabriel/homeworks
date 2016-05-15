package main;
import java.util.Observable;

public class Clock extends Observable implements Runnable {
    private Thread thread = new Thread(this);
    private static Clock instance;

    public enum Events {
        CLOCK_TICKED_EVENT
    }

    private Clock() {
        thread.start();
    }

    public static Clock instance() {
        if (instance == null) {
            instance = new Clock();
        }
        return instance;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000);
                setChanged();
                notifyObservers(Events.CLOCK_TICKED_EVENT);
            }
        } catch (InterruptedException ie) {
        }
    }
}