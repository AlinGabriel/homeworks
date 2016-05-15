package states;

import events.DoorCloseEvent;
import events.TimerTickedEvent;
import listeners.DoorCloseListener;
import main.RefrigeratorContext;
import managers.DoorCloseManager;

public class CoolingDoorOpenedState extends RefrigeratorState implements DoorCloseListener {

    public CoolingDoorOpenedState(RefrigeratorContext context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        DoorCloseManager.instance().addDoorCloseListener(this);
        display.startCooling(context.getType());
        display.turnLightOn(context.getType());
    }

    @Override
    public void leave() {
        super.leave();
        DoorCloseManager.instance().removeDoorCloseListener(this);
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
            if (context.getRiseDoorOpened() == context.getMinLeftRiseDoorOpened()) {
                context.setCurrentTemp(context.getCurrentTemp() + 1);
                context.setMinLeftRiseDoorOpened(0);
            } else {
                context.setMinLeftRiseDoorOpened(context.getMinLeftRiseDoorOpened() + 1);
            }
        }
        display.setCurrentTemp(context.getType());
    }

    @Override
    public void doorClosed(DoorCloseEvent event) {
        if (event.getType() == context.getType()) {
            context.changeCurrentState(new CoolingDoorClosedState(context));
        }
    }
}