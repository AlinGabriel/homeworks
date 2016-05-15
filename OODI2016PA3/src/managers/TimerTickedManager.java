package managers;

import java.util.EventListener;

import javax.swing.event.EventListenerList;

import events.TimerTickedEvent;
import listeners.TimerTickedListener;

public class TimerTickedManager {
	private EventListenerList listenerList = new EventListenerList();
	private static TimerTickedManager instance;

	private TimerTickedManager() {
	}

	public static TimerTickedManager instance() {
		if (instance == null) {
			instance = new TimerTickedManager();
		}
		return instance;
	}

	public void addTimerTickedListener(TimerTickedListener listener) {
		listenerList.add(TimerTickedListener.class, listener);
	}

	public void removeTimerTickedListener(TimerTickedListener listener) {
		listenerList.remove(TimerTickedListener.class, listener);
	}

	public void processEvent(TimerTickedEvent event) {
		EventListener[] listeners = listenerList
				.getListeners(TimerTickedListener.class);
		for (EventListener listener : listeners)
			((TimerTickedListener) listener).timerTicked(event);
	}
}