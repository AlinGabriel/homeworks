package listeners;
import java.util.EventListener;

import events.TimerTickedEvent;

public interface TimerTickedListener extends EventListener {
	void timerTicked(TimerTickedEvent event);
}