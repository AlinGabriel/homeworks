import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class CognitiveAgent extends Agent {
	ArrayList<Point> rocksCoord; 
	static Board board;

	public CognitiveAgent(String name) {
		super(name);
		rocksCoord = new ArrayList<Point>();
		board = Board.getInstance();
	}
	
	public boolean isAgentAtTheBase() {
		if (board.getBasePos().equals(this.getCurrPos()))
			return true;
		return false;
	}

	public boolean isCarrierIdle(){
		return rocksCoord.isEmpty();
	}
	
	@Override
	void move() {
		// TODO Auto-generated method stub

	}

	@Override
	void moveToBase() {
		boolean foundValidMove = false;
		Point basePos = board.getBasePos();
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;
		Random rand = new Random();
		int direction;
		System.out.println("Carrier " + this.getName() + " is returning to base.");
		for (int i = 0 ; i < 10 ; i++) {
			direction = rand.nextInt(4);
			if (direction == 0 && x + 1 <= basePos.x) {
				if (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "B") // down
					this.setNewPos(new Point(x + 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length && (board.m[x][y + 1] == "o"  || board.m[x][y + 1] == "B")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o"  || board.m[x][y - 1] == "B")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			}
			else if (direction == 1 && x - 1 >= basePos.x) {
				if (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "B") // up
					this.setNewPos(new Point(x - 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length && (board.m[x][y + 1] == "o"  || board.m[x][y + 1] == "B")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o"  || board.m[x][y - 1] == "B")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			}
			else if (direction == 2 && y + 1 <= basePos.y) {
				if (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "B") // right
					this.setNewPos(new Point(x, y + 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length && (board.m[x + 1][y] == "o"  || board.m[x + 1][y] == "B")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o"  || board.m[x - 1][y] == "B")) // up
						this.setNewPos(new Point(x - 1, y));
				}
			}
			else if (direction == 3 && y - 1 >= basePos.y) {
				if(board.m[x][y - 1] == "o" || board.m[x][y-1] == "B") // left
					this.setNewPos(new Point(x, y - 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length && (board.m[x + 1][y] == "o"  || board.m[x + 1][y] == "B")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "B")) // up
						this.setNewPos(new Point(x - 1, y));
					}
			}
				
			// if newPos the same as currPos try again
			if (this.getNewPos().x != x || this.getNewPos().y != y) {
				foundValidMove = true;
				break;
			}
		}
		if (foundValidMove) {
			/* upon returning to base to unload rock, any other rock found cannot be taken */
			if (board.m[x][y] != "B" && board.m[x][y] != "*")
				board.m[x][y] = "o";
			this.setCurrPos(this.getNewPos());
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] == "B") {
				System.out.println("Carrier " + this.getName() + " has found base. Now unloading rocks..");
			} 
			else if (board.m[this.getCurrPos().x][this.getCurrPos().y] != "*") {
				board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
			}
		}
	}

	public void collectRock() {
		boolean foundValidMove = false;
		Point rockPos = rocksCoord.get(0);
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;
		Random rand = new Random();
		int direction;
		System.out.println("Carrier " + this.getName() + " is going to collect the rock.");
		for (int i = 0 ; i < 10 ; i++) {
			direction = rand.nextInt(4);
			if (direction == 0 && x + 1 <= rockPos.x) {
				if (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "b") // down
					this.setNewPos(new Point(x + 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length && (board.m[x][y + 1] == "o"  || board.m[x][y + 1] == "b")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o"  || board.m[x][y - 1] == "b")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			}
			else if (direction == 1 && x - 1 >= rockPos.x) {
				if (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "b") // up
					this.setNewPos(new Point(x - 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length && (board.m[x][y + 1] == "o"  || board.m[x][y + 1] == "b")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o"  || board.m[x][y - 1] == "b")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			}
			else if (direction == 2 && y + 1 <= rockPos.y) {
				if (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "b") // right
					this.setNewPos(new Point(x, y + 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length && (board.m[x + 1][y] == "o"  || board.m[x + 1][y] == "b")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o"  || board.m[x - 1][y] == "b")) // up
						this.setNewPos(new Point(x - 1, y));
				}
			}
			else if (direction == 3 && y - 1 >= rockPos.y) {
				if(board.m[x][y - 1] == "o" || board.m[x][y-1] == "b") // left
					this.setNewPos(new Point(x, y - 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length && (board.m[x + 1][y] == "o"  || board.m[x + 1][y] == "b")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "b")) // up
						this.setNewPos(new Point(x - 1, y));
					}
			}
				
			// if newPos the same as currPos try again
			if (this.getNewPos().x != x || this.getNewPos().y != y) {
				foundValidMove = true;
				break;
			}
		}
		if (foundValidMove) {
			/* upon returning to base to unload rock, any other rock found cannot be taken */
			if (board.m[x][y] != "B" && board.m[x][y] != "*")
				board.m[x][y] = "o";
			this.setCurrPos(this.getNewPos());
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] == "b") {
//				hasFoundRock = false;
				System.out.println("Carrier " + this.getName() + " has collected rock. Waiting for next rock coords..");
				for (Point rockcoord : rocksCoord)
					if (rockcoord.x == this.getCurrPos().x && rockcoord.y == this.getCurrPos().y) {
						rocksCoord.remove(rockcoord);
						break;
					}
			} 
			board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
		}
	}

}