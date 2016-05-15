package states;

import events.DoorOpenEvent;
import events.TimerTickedEvent;
import listeners.DoorOpenListener;
import main.RefrigeratorContext;
import managers.DoorOpenManager;

public class IdleDoorClosedState extends RefrigeratorState implements DoorOpenListener {

    public IdleDoorClosedState(RefrigeratorContext context) {
        super(context);
    }

    @Override
    public void run() {
        super.run();
        DoorOpenManager.instance().addDoorOpenListener(this);
        display.turnLightOff(context.getType());
        display.stopCooling(context.getType());
    }

    @Override
    public void leave() {
        super.leave();
        DoorOpenManager.instance().removeDoorOpenListener(this);
    }

    @Override
    public void timerTicked(TimerTickedEvent event) {
        if (context.getTempDifference() <= context.getCurrentTemp() - context.getSelectedTemp()) {
            context.changeCurrentState(new CoolingDoorClosedState(context));
            return;
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
        if (context.getType() == event.getType()) {
            context.changeCurrentState(new IdleDoorOpenState(context));
        }
    }

}