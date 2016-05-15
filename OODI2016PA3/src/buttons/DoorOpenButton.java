package buttons;

import events.DoorOpenEvent;
import main.RefrigeratorDisplay;
import managers.DoorOpenManager;

public class DoorOpenButton extends GUIButton {
    private static final long serialVersionUID = 10L;
    private int type;

    public DoorOpenButton(String string, int type) {
        super(string);
        this.type = type;
    }

    @Override
    public void inform(RefrigeratorDisplay source) {
        DoorOpenManager.instance().processEvent(new DoorOpenEvent(source, type));
    }
}