package main;

import java.util.Observable;
import java.util.Observer;

import events.TimerTickedEvent;
import managers.TimerTickedManager;

public class Timer implements Observer {
	private static Timer instance;

	private Timer() {
		instance = this;
		Clock.instance().addObserver(instance);
	}

	public static Timer instance() {
		if (instance == null) {
			instance = new Timer();
		}
		return instance;
	}

	@Override
	public void update(Observable clock, Object value) {
		TimerTickedManager.instance().processEvent(new TimerTickedEvent(instance));
	}
}