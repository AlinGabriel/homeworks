package managers;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import events.DoorOpenEvent;
import listeners.DoorOpenListener;

public class DoorOpenManager {
    private EventListenerList listenerList = new EventListenerList();
    private static DoorOpenManager instance;

    private DoorOpenManager() {
    }

    public static DoorOpenManager instance() {
        if (instance == null) {
            instance = new DoorOpenManager();
        }
        return instance;
    }

    public void addDoorOpenListener(DoorOpenListener listener) {
        listenerList.add(DoorOpenListener.class, listener);
    }

    public void removeDoorOpenListener(DoorOpenListener listener) {
        listenerList.remove(DoorOpenListener.class, listener);
    }

    public void processEvent(DoorOpenEvent event) {
        EventListener[] listeners = listenerList.getListeners(DoorOpenListener.class);
        for (EventListener listener : listeners) {
            ((DoorOpenListener) listener).doorOpened(event);
        }
    }
}
