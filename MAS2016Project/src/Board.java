
import java.awt.Point;
import java.util.Random;

public class Board {
	private static Board instance;
	String[][] m;
	private Point basePos;

	/* singleton - exists only to defeat instantiation */
	private Board() {
		instance = this;
		basePos = new Point();
		m = new String[10][10];
	}
	
	public static Board getInstance() {
		if (instance == null) {
			instance = new Board();
		}
		return instance;
	}
	
	public void setAgentPos(Agent agent) {
		m[agent.getCurrPos().x][agent.getCurrPos().y] = agent.getName();
	}
	
	public void setBasePos(Point basePos) {
		this.basePos = basePos;
		m[basePos.x][basePos.y] = "B";
	}
	
	public Point getBasePos() {
		return basePos;
	}
		
	public void createBoard() {
		Random r;
		int i, j, obs, rocks;

		for (i = 0; i < m.length; i++)
			for (j = 0; j < m.length; j++)
				m[i][j] = "o";
		/* generate from 1 to 10 obstacles */
		r = new Random();
		obs = r.nextInt(m.length) + 1;
		for (i = 0; i < obs; i++)
			m[r.nextInt(m.length)][r.nextInt(m.length)] = "#";
		/* generate from 1 to 10*10/3 rocks */
		rocks = r.nextInt((int) Math.pow(m.length, 2) / 3) + 1;
		for (i = 0; i < rocks; i++)
			m[r.nextInt(m.length)][r.nextInt(m.length)] = "*";
	}

	public void printBoard() {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m.length; j++)
				System.out.print(m[i][j] + " ");
			System.out.println();
		}
		System.out.println("Remaining rocks: " + this.countRemainingRocks("*"));
		System.out.println("===================");
	}
	
	public boolean hasRocks(){
		if (countRemainingRocks("*") != 0)
			return true;
		return false;
	}
	
	public boolean hasRocksDug() {
		if (countRemainingRocks("b") != 0)
			return true;
		return false;
	}
	
	public int countRemainingRocks(String rock) {
		int rocks = 0;
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m.length; j++)
				if (m[i][j] == rock)
					rocks++;
		return rocks;
	}
}