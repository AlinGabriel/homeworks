package states;

import events.DoorOpenEvent;
import events.TimerTickedEvent;
import listeners.DoorOpenListener;
import main.RefrigeratorContext;
import managers.DoorOpenManager;

public class CoolingDoorClosedState extends RefrigeratorState implements DoorOpenListener {

    public CoolingDoorClosedState(RefrigeratorContext context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        DoorOpenManager.instance().addDoorOpenListener(this);
        display.startCooling(context.getType());
        display.turnLightOff(context.getType());
    }

    @Override
    public void leave() {
        super.leave();
        DoorOpenManager.instance().removeDoorOpenListener(this);
    }

    @Override
    public void timerTicked(TimerTickedEvent event) {
        if (context.getCurrentTemp() <= context.getSelectedTemp()) {
            context.changeCurrentState(new IdleDoorClosedState(context));
            return;
        }
        if (context.getCool() == context.getMinLeftCool()) {
            context.setCurrentTemp(context.getCurrentTemp() - 1);
            context.setMinLeftCool(0);
        } else {
            context.setMinLeftCool(context.getMinLeftCool() + 1);
        }
        if (context.getCurrentTemp() < RefrigeratorContext.getRoomTemp()) {
            if (context.getRiseDoorClosed() == context.getMinLeftRiseDoorClosed()) {
                context.setCurrentTemp(context.getCurrentTemp() + 1);
                context.setMinLeftRiseDoorClosed(0);
            } else {
                context.setMinLeftRiseDoorClosed(context.getMinLeftRiseDoorClosed() + 1);
            }
        }
        display.setCurrentTemp(context.getType());
    }

    @Override
    public void doorOpened(DoorOpenEvent event) {
        if (event.getType() == context.getType()) {
            context.changeCurrentState(new CoolingDoorOpenedState(context));
        }
    }
}