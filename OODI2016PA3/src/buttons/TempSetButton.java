package buttons;
import events.TempChangeEvent;
import main.RefrigeratorContext;
import main.RefrigeratorDisplay;
import managers.TempChangeManager;

public class TempSetButton extends GUIButton {
	private static final long serialVersionUID = 1L;
	int type;

    public TempSetButton(String string, int type) {
        super(string);
        this.type = type;
    }

    @Override
    public void inform(RefrigeratorDisplay display) {
        if (type == RefrigeratorContext.ROOM) {
            int temp = 0;
            try {
                temp = Integer.parseInt(display.getRoomDesiredTemp());
            } catch (Exception e) {
                display.showRoomTempError();
                return;
            }
            if (temp > RefrigeratorContext.getHighRoomTemp() || temp < RefrigeratorContext.getLowRoomTemp()) {
                display.showRoomTempError();
            } else {
                RefrigeratorContext.setRoomTemp(temp);
                display.hideRoomTempError();
            }
            display.setRoomDesiredTemp();
        } else {
            TempChangeManager.instance().processEvent(new TempChangeEvent(this, type, display.getDesiredTemp(type)));
        }
    }
}