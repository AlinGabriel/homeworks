package states;

import events.TempChangeEvent;
import listeners.TempChangeListener;
import listeners.TimerTickedListener;
import main.RefrigeratorContext;
import main.RefrigeratorDisplay;
import managers.TempChangeManager;
import managers.TimerTickedManager;

public abstract class RefrigeratorState implements TimerTickedListener, TempChangeListener {
    protected RefrigeratorContext context;
    protected RefrigeratorDisplay display;

    protected RefrigeratorState(RefrigeratorContext context) {
        this.context = context;
        display = RefrigeratorDisplay.instance();
    }

    public void run() {
        TempChangeManager.instance().addTempChangeListener(this);
        TimerTickedManager.instance().addTimerTickedListener(this);
    }

    public void leave() {
        TempChangeManager.instance().removeTempChangeListener(this);
        TimerTickedManager.instance().removeTimerTickedListener(this);
    }

    public void tempChanged(TempChangeEvent event) {
        int temp = 0;
        try {
            temp = Integer.parseInt(event.getTemp());
        } catch (Exception e) {
            display.showError(event.getType());
            display.setDesiredTemp(event.getType());
            return;
        }
        if (event.getType() == context.getType()) {
            if (temp > context.getHighTemp() || temp < context.getLowTemp()) {
                display.showError(event.getType());
            } else {
                context.setSelectedTemp(temp);
                display.hideError(event.getType());
            }
        }
        display.setDesiredTemp(event.getType());
    }
}