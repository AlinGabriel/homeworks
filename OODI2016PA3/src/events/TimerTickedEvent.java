package events;
import java.util.EventObject;

public class TimerTickedEvent extends EventObject {
	private static final long serialVersionUID = 10L;
	
	public TimerTickedEvent(Object source) {
		super(source);
	}
}