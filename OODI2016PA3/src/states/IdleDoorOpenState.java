package states;

import events.DoorCloseEvent;
import events.TimerTickedEvent;
import listeners.DoorCloseListener;
import main.RefrigeratorContext;
import managers.DoorCloseManager;

public class IdleDoorOpenState extends RefrigeratorState implements DoorCloseListener {
    public IdleDoorOpenState(RefrigeratorContext context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        DoorCloseManager.instance().addDoorCloseListener(this);
        display.turnLightOn(context.getType());
        display.stopCooling(context.getType());
    }

    @Override
    public void leave() {
        super.leave();
        DoorCloseManager.instance().removeDoorCloseListener(this);
    }

    @Override
    public void timerTicked(TimerTickedEvent event) {
        if (context.getTempDifference() <= context.getCurrentTemp() - context.getSelectedTemp()) {
            context.changeCurrentState(new CoolingDoorOpenedState(context));
            return;
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
        if (context.getType() == event.getType()) {
            context.changeCurrentState(new IdleDoorClosedState(context));
        }
    }

}

