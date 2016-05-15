package managers;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import events.TempChangeEvent;
import listeners.TempChangeListener;

/**
 * Created by mdochita on 13/04/2016.
 */
public class TempChangeManager {
    private EventListenerList listenerList = new EventListenerList();
    private static TempChangeManager instance;

    private TempChangeManager() {
    }

    public static TempChangeManager instance() {
        if (instance == null) {
            instance = new TempChangeManager();
        }
        return instance;
    }

    public void addTempChangeListener(TempChangeListener listener) {
        listenerList.add(TempChangeListener.class, listener);
    }

    public void removeTempChangeListener(TempChangeListener listener) {
        listenerList.remove(TempChangeListener.class, listener);
    }

    public void processEvent(TempChangeEvent event) {
        EventListener[] listeners = listenerList.getListeners(TempChangeListener.class);
        for (EventListener listener : listeners) {
            ((TempChangeListener) listener).tempChanged(event);
        }
    }
}
