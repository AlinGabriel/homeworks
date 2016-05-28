import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class CognitiveAgent extends Agent {
	private ArrayList<Point> rockCoordsQueue;
	private Point otherCarrierRockCoords;
	private static Board board;

	public CognitiveAgent(String name) {
		super(name);
		rockCoordsQueue = new ArrayList<Point>();
		board = Board.getInstance();
		otherCarrierRockCoords = null;
		this.setCurrPos(new Point(board.m.length / 2, board.m.length / 2));
	}

	public ArrayList<Point> getRockCoordsQueue() {
		return rockCoordsQueue;
	}

	public boolean isAgentAtTheBase() {
		if (board.getBasePos().equals(this.getCurrPos()))
			return true;
		return false;
	}

	public boolean isCarrierIdle() {
		return rockCoordsQueue.isEmpty();
	}

	@Override
	void moveToRock() {
		boolean foundValidMove = false;
		Point rockPos = rockCoordsQueue.get(0);
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;
		Random rand = new Random();
		int direction;
		System.out.println("Carrier " + this.getName() + " is going to collect the rock.");
		for (int i = 0; i < 10; i++) {
			direction = rand.nextInt(4);
			if (direction == 0 && x + 1 <= rockPos.x) {
				if (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "b") // down
					this.setNewPos(new Point(x + 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length
							&& (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "b")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "b")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			} else if (direction == 1 && x - 1 >= rockPos.x) {
				if (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "b") // up
					this.setNewPos(new Point(x - 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length
							&& (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "b")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "b")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			} else if (direction == 2 && y + 1 <= rockPos.y) {
				if (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "b") // right
					this.setNewPos(new Point(x, y + 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length
							&& (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "b")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "b")) // up
						this.setNewPos(new Point(x - 1, y));
				}
			} else if (direction == 3 && y - 1 >= rockPos.y) {
				if (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "b") // left
					this.setNewPos(new Point(x, y - 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length
							&& (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "b")) // down
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
			if (board.m[x][y] != "B" && board.m[x][y] != "*")
				board.m[x][y] = "o";
			if (otherCarrierRockCoords != null) {
				board.m[otherCarrierRockCoords.x][otherCarrierRockCoords.y] = "b";
				otherCarrierRockCoords = null;
			}
			this.setCurrPos(this.getNewPos());
			/*
			 * since every search agent has its own carrier, if any carrier
			 * finds a rock in its path, if will collect it if and only if that
			 * rock was discovered by his search agent, else it will pass on it.
			 */
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] == "b") {
				if (rockCoordsQueue.contains(this.getCurrPos())) {
					rockCoordsQueue.remove(this.getCurrPos());
					System.out.println("Carrier " + this.getName() + " has collected rock. Waiting for next rock coords..");
				} else {
					//found a rock that must be collected by other carrier; retain its coordinates so on the next,
					// move we put it back on the map. the carrier just passes over it.
					otherCarrierRockCoords = new Point(this.getCurrPos().x, this.getCurrPos().y);
				}
			}
			board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
		}
	}

	@Override
	void moveToBase() {
		boolean foundValidMove = false;
		Point basePos = board.getBasePos();
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;
		Random rand = new Random();
		int direction;
		/*
		 * upon returning to base to unload rock, any other rock found
		 * cannot be taken
		 */
		System.out.println("Carrier " + this.getName() + " is returning to base.");
		for (int i = 0; i < 10; i++) {
			direction = rand.nextInt(4);
			if (direction == 0 && x + 1 <= basePos.x) {
				if (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "B") // down
					this.setNewPos(new Point(x + 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length
							&& (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "B")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "B")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			} else if (direction == 1 && x - 1 >= basePos.x) {
				if (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "B") // up
					this.setNewPos(new Point(x - 1, y));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && y + 1 < board.m.length
							&& (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "B")) // right
						this.setNewPos(new Point(x, y + 1));
					else if (direction == 1 && y - 1 >= 0 && (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "B")) // left
						this.setNewPos(new Point(x, y - 1));
				}
			} else if (direction == 2 && y + 1 <= basePos.y) {
				if (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "B") // right
					this.setNewPos(new Point(x, y + 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length
							&& (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "B")) // down
						this.setNewPos(new Point(x + 1, y));
					else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "B")) // up
						this.setNewPos(new Point(x - 1, y));
				}
			} else if (direction == 3 && y - 1 >= basePos.y) {
				if (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "B") // left
					this.setNewPos(new Point(x, y - 1));
				else {
					direction = rand.nextInt(2);
					if (direction == 0 && x + 1 < board.m.length
							&& (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "B")) // down
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
			/* upon returning to base to unload rock, any other rock found
			 * cannot be taken */
			if (board.m[x][y] != "B" && board.m[x][y] != "*")
				board.m[x][y] = "o";
			this.setCurrPos(this.getNewPos());
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] == "B") {
				System.out.println("Carrier " + this.getName() + " has found base. Now unloading rocks..");
			} else if (board.m[this.getCurrPos().x][this.getCurrPos().y] != "*") {
				board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
			}
		}
	}
}