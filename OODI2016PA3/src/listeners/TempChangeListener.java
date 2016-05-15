package listeners;
import java.util.EventListener;

import events.TempChangeEvent;

public interface TempChangeListener extends EventListener {
    void tempChanged(TempChangeEvent event);
}