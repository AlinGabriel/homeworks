package main;

import java.util.ArrayList;
import java.util.List;

import states.IdleDoorClosedState;
import states.RefrigeratorState;

public class RefrigeratorContext {
	public static final int ROOM = -1;
	public static final int FRIDGE = 0;
	public static final int FREEZER = 1;

	private static final List<RefrigeratorContext> instances = new ArrayList<>();

	private static int roomTemp = 20;
	private static int highRoomTemp;
	private static int lowRoomTemp;

	private RefrigeratorState currentState;
	private static RefrigeratorDisplay display;

	private int currentTemp;
	private int lowTemp;
	private int highTemp;
	private int riseDoorOpened;
	private int riseDoorClosed;
	private int cool;
	private int tempDifference;
	private int type;

	private int selectedTemp = 20;
	private int minLeftRiseDoorOpened = 0;
	private int minLeftRiseDoorClosed = 0;
	private int minLeftCool = 0;

	public static void initialiseContext(List<Integer> configs) {
		display = RefrigeratorDisplay.instance();
		lowRoomTemp = configs.get(4);
		highRoomTemp = configs.get(5);
		RefrigeratorContext fridgeInstance = new RefrigeratorContext(roomTemp, configs.get(0), configs.get(1),
				configs.get(6), configs.get(7), configs.get(10), configs.get(12), FRIDGE);
		RefrigeratorContext freezerInstance = new RefrigeratorContext(roomTemp, configs.get(2), configs.get(3),
				configs.get(8), configs.get(9), configs.get(11), configs.get(13), FREEZER);
		instances.add(FRIDGE, fridgeInstance);
		instances.add(FREEZER, freezerInstance);
		Timer.instance();
		for (RefrigeratorContext refrigeratorContext : instances)
			refrigeratorContext.changeCurrentState(new IdleDoorClosedState(refrigeratorContext));
	}

	private RefrigeratorContext(int currentTemp, int lowTemp, int highTemp, int riseDoorClosed, int riseDoorOpened,
			int tempDifference, int cool, int type) {
		this.currentTemp = currentTemp;
		this.lowTemp = lowTemp;
		this.highTemp = highTemp;
		this.riseDoorOpened = riseDoorOpened;
		this.riseDoorClosed = riseDoorClosed;
		this.tempDifference = tempDifference;
		this.cool = cool;
		this.type = type;
		this.currentState = new IdleDoorClosedState(this);
	}

	public void changeCurrentState(RefrigeratorState nextState) {
		currentState.leave();
		currentState = nextState;
		nextState.run();
	}

	public static List<RefrigeratorContext> instances() {
		return instances;
	}

	public static int getRoomTemp() {
		return roomTemp;
	}

	public static int getHighRoomTemp() {
		return highRoomTemp;
	}

	public static int getLowRoomTemp() {
		return lowRoomTemp;
	}

	public static void setRoomTemp(int temp) {
		roomTemp = temp;
	}

	public RefrigeratorDisplay getDisplay() {
		return display;
	}

	public int getCurrentTemp() {
		return currentTemp;
	}

	public void setCurrentTemp(int currentTemp) {
		this.currentTemp = currentTemp;
	}

	public int getLowTemp() {
		return lowTemp;
	}

	public int getHighTemp() {
		return highTemp;
	}

	public int getRiseDoorOpened() {
		return riseDoorOpened;
	}

	public int getRiseDoorClosed() {
		return riseDoorClosed;
	}

	public int getCool() {
		return cool;
	}

	public int getTempDifference() {
		return tempDifference;
	}

	public int getMinLeftRiseDoorOpened() {
		return minLeftRiseDoorOpened;
	}

	public void setMinLeftRiseDoorOpened(int minLeftRiseDoorOpened) {
		this.minLeftRiseDoorOpened = minLeftRiseDoorOpened;
	}

	public int getMinLeftRiseDoorClosed() {
		return minLeftRiseDoorClosed;
	}

	public void setMinLeftRiseDoorClosed(int minLeftRiseDoorClosed) {
		this.minLeftRiseDoorClosed = minLeftRiseDoorClosed;
	}

	public int getMinLeftCool() {
		return minLeftCool;
	}

	public void setMinLeftCool(int minLeftCool) {
		this.minLeftCool = minLeftCool;
	}

	public int getSelectedTemp() {
		return selectedTemp;
	}

	public void setSelectedTemp(int selectedTemp) {
		this.selectedTemp = selectedTemp;
	}

	public int getType() {
		return type;
	}
}