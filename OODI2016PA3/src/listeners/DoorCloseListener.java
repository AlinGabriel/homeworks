package listeners;

import java.util.EventListener;

import events.DoorCloseEvent;

public interface DoorCloseListener extends EventListener {
    void doorClosed(DoorCloseEvent event);
}