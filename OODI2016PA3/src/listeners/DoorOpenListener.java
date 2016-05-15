package listeners;
import java.util.EventListener;

import events.DoorOpenEvent;

public interface DoorOpenListener extends EventListener {
    void doorOpened(DoorOpenEvent event);
}