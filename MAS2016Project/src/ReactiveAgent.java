import java.awt.Point;
import java.util.Random;

public class ReactiveAgent extends Agent {
	Point lastRockCord;
	static Board board;
	CognitiveAgent carrier;

	public ReactiveAgent(String name) {
		super(name);
		board = Board.getInstance();
		carrier = new CognitiveAgent(name.toLowerCase());
		lastRockCord = null;
	}

	public boolean isAgentAtTheBase() {
		if (board.getBasePos().equals(this.getCurrPos()))
			return true;
		return false;
	}

	public void move() {
		boolean foundValidMove = false;
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;

		/* prioritize if we are near a rock, go in the direction of the rock */
		System.out.println("Agent " + this.getName() + " is searching for rock.");
		for (int i = 0 ; i < 10 ; i++) {
			int direction = detectNearObject(x, y, "*");

			if (direction == 0 && x + 1 < board.m.length && (board.m[x + 1][y] == "o" || board.m[x + 1][y] == "*")) // down
				this.setNewPos(new Point(x + 1, y));
			else if (direction == 1 && x - 1 >= 0 && (board.m[x - 1][y] == "o" || board.m[x - 1][y] == "*")) // up
				this.setNewPos(new Point(x - 1, y));
			else if (direction == 2 && y + 1 < board.m.length && (board.m[x][y + 1] == "o" || board.m[x][y + 1] == "*")) // right
				this.setNewPos(new Point(x, y + 1));
			else if (direction == 3 && y - 1 >= 0 && (board.m[x][y - 1] == "o" || board.m[x][y - 1] == "*")) // left
				this.setNewPos(new Point(x, y - 1));
			// if newPos the same as currPos try again
			if (this.getNewPos().x != x || this.getNewPos().y != y) {
				foundValidMove = true;
				break;
			}
		}
		if (foundValidMove) {
			if (board.m[x][y] != "B" && board.m[x][y] !="*" && board.m[x][y] != "b")
				board.m[x][y] = "o";
			if (lastRockCord != null) {
				board.m[lastRockCord.x][lastRockCord.y] = "b";
				lastRockCord = null;
			}
			this.setCurrPos(this.getNewPos());
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] == "*") {
				lastRockCord = new Point(this.getCurrPos().x,this.getCurrPos().y);
				System.out.println("Agent " + this.getName() + " has found rock. Sending the coords to its carrier");
				sendCoord(this.getCurrPos());
			}
			if (board.m[this.getCurrPos().x][this.getCurrPos().y] != "B" && 
					board.m[this.getCurrPos().x][this.getCurrPos().y] != "b") {
				board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
			}
		}
	}

	private void sendCoord(Point coords) {
		carrier.rocksCoord.add(new Point(coords.x,coords.y));
	}

	/*
	 * search near positions and return the direction to any found rock or a
	 * random one if none found
	 */
	public int detectNearObject(int x, int y, String object) {
		Random random = new Random();
		int dir;

		if (x + 1 < board.m.length && board.m[x + 1][y] == object)
			return 0;
		else if (x - 1 >= 0 && board.m[x - 1][y] == object)
			return 1;
		else if (y + 1 < board.m.length && board.m[x][y + 1] == object)
			return 2;
		else if (y - 1 >= 0 && board.m[x][y - 1] == object)
			return 3;
		else if (x + 1 < board.m.length && y + 1 < board.m.length && board.m[x + 1][y + 1] == object){
			dir = random.nextInt(2);
			if (dir == 0 && board.m[x + 1][y] != "#")
				return 0;
			else if (dir == 1 && board.m[x][y+1] != "#")
				return 2;
		}
		else if (x + 1 < board.m.length && y - 1 >= 0 && board.m[x + 1][y - 1] == object){
			dir = random.nextInt(2);
			if (dir == 0 && board.m[x + 1][y] != "#")
				return 0;
			else if (dir == 1 && board.m[x][y-1] != "#")
				return 3;
		}
		else if (x - 1 >= 0 && y + 1 < board.m.length && board.m[x - 1][y + 1] == object){
			dir = random.nextInt(2);
			if (dir == 0 && board.m[x - 1][y] != "#")
				return 1;
			else if (dir == 1 && board.m[x][y+1] != "#")
				return 2;
		}
		else if (x - 1 >= 0 && y - 1 >= 0 && board.m[x - 1][y - 1] == object){
			dir = random.nextInt(2);
			if (dir == 0 && board.m[x - 1][y] != "#")
				return 1;
			else if (dir == 1 && board.m[x][y-1] != "#")
				return 3;
		}

		return random.nextInt(4);
	}

	@Override
	void moveToBase() {
		boolean foundValidMove = false;
		Point basePos = board.getBasePos();
		int x = this.getCurrPos().x;
		int y = this.getCurrPos().y;
		Random rand = new Random();
		int direction;
		System.out.println("Agent " + this.getName() + " is returning to base.");
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
				System.out.println("Agent " + this.getName() + " has found base. Now unloading rock..");
			} 
			else if (board.m[this.getCurrPos().x][this.getCurrPos().y] != "*") {
				board.m[this.getCurrPos().x][this.getCurrPos().y] = this.getName();
			}
		}
	}
}