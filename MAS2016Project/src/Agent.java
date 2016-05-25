import java.awt.Point;

abstract class Agent {
	private String name;
	private Point currPos;
	private Point newPos;

	public Agent(String string) {
		currPos = newPos = new Point();
		name = string;
	}
	
	public String getName(){
		return name;
	}
	
	public void setCurrPos(Point pos) {
		currPos.setLocation(pos);
	}
	
	public Point getCurrPos() {
		return currPos;
	}
	
	public void setNewPos(Point pos){
		newPos.setLocation(pos);
	}
	
	public Point getNewPos() {
		return newPos;
	}

	abstract void move();
	abstract void moveToBase();
}