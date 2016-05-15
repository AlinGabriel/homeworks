package events;
import java.util.EventObject;

public class DoorCloseEvent extends EventObject {
    private static final long serialVersionUID = 10L;
    private int type;

    public DoorCloseEvent(Object source, int type) {
        super(source);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}