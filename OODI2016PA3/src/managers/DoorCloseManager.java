package managers;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import events.DoorCloseEvent;
import listeners.DoorCloseListener;

public class DoorCloseManager {
    private EventListenerList listenerList = new EventListenerList();
    private static DoorCloseManager instance;

    private DoorCloseManager() {
    }

    public static DoorCloseManager instance() {
        if (instance == null) {
            instance = new DoorCloseManager();
        }
        return instance;
    }

    public void addDoorCloseListener(DoorCloseListener listener) {
        listenerList.add(DoorCloseListener.class, listener);
    }

    public void removeDoorCloseListener(DoorCloseListener listener) {
        listenerList.remove(DoorCloseListener.class, listener);
    }

    public void processEvent(DoorCloseEvent event) {
        EventListener[] listeners = listenerList.getListeners(DoorCloseListener.class);
        for (EventListener listener : listeners)
            ((DoorCloseListener) listener).doorClosed(event);
    }
}