package buttons;
import events.DoorCloseEvent;
import main.RefrigeratorDisplay;
import managers.DoorCloseManager;

public class DoorCloseButton extends GUIButton {
    private static final long serialVersionUID = 10L;
    private int type;

    public DoorCloseButton(String string, int type) {
        super(string);
        this.type = type;
    }

    @Override
    public void inform(RefrigeratorDisplay source) {
        DoorCloseManager.instance().processEvent(new DoorCloseEvent(source, type));
    }
}