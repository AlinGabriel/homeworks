package main;
import java.util.List;
import java.util.Observable;

public abstract class RefrigeratorDisplay extends Observable {
    protected static RefrigeratorDisplay instance;

    public void initialize(List<Integer> configs) {
        instance = this;
        RefrigeratorContext.initialiseContext(configs);
    }

    public static RefrigeratorDisplay instance() {
        return instance;
    }

    public abstract void turnLightOn(int type);
    public abstract void turnLightOff(int type);
    public abstract void setCurrentTemp(int type);
    public abstract void setDesiredTemp(int type);
    public abstract void setRoomDesiredTemp();
    public abstract String getDesiredTemp(int type);
    public abstract String getRoomDesiredTemp();
    public abstract void startCooling(int type);
    public abstract void stopCooling(int type);
    public abstract void showRoomTempError();
    public abstract void hideRoomTempError();
    public abstract void showError(int type);
    public abstract void hideError(int type);
}