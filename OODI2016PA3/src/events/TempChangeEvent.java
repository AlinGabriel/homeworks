package events;
import java.util.EventObject;

public class TempChangeEvent extends EventObject {
    private static final long serialVersionUID = 10L;
    private int type;
    private String temp;

    public TempChangeEvent(Object source, int type, String temp) {
        super(source);
        this.type = type;
        this.temp = temp;
    }

    public int getType() {
        return type;
    }

    public String getTemp() {
        return temp;
    }
}

