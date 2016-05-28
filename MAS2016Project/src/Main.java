import java.awt.Point;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String args[]) {

		Board board = Board.getInstance();
		board.createBoard();
		ReactiveAgent agent1 = new ReactiveAgent("R");
		ReactiveAgent agent2 = new ReactiveAgent("T");
		ReactiveAgent agent3 = new ReactiveAgent("U");
		ReactiveAgent agent4 = new ReactiveAgent("V");
		// ReactiveAgent agent5 = new ReactiveAgent("W");
		// ReactiveAgent agent6 = new ReactiveAgent("X");
		// ReactiveAgent agent7 = new ReactiveAgent("Y");
		// ReactiveAgent agent8 = new ReactiveAgent("Z");
		List<ReactiveAgent> agents = Arrays.asList(agent1, agent2,agent3,agent4);
		agent1.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 - 1));
		agent2.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 + 1));
		agent3.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 + 1));
		agent4.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 - 1));
		// agent5.setCurrPos(new Point(board.m.length / 2 , board.m.length / 2 + 1));
		// agent6.setCurrPos(new Point(board.m.length / 2 , board.m.length / 2 - 1));
		// agent7.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 ));
		// agent8.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 ));
		for (ReactiveAgent agent : agents) {
			board.setAgentPos(agent);
			board.setAgentPos(agent.getCarrier());
		}
		board.setBasePos(new Point(board.m.length / 2, board.m.length / 2));
		board.printBoard();

		while (!board.isEmpty()) {
			for (ReactiveAgent agent : agents) {
				if (board.hasRocks()) {
					agent.moveToRock();
				} else if (!agent.isAgentAtTheBase()){
					agent.moveToBase();
				}
				if (!agent.getCarrier().isCarrierIdle()) {
					agent.getCarrier().moveToRock();
				} else if (!agent.getCarrier().isAgentAtTheBase()) {
					agent.getCarrier().moveToBase();
				}
			}

			// if (!board.hasRocks())
			// break;
			// if (agent3.hasFoundRock()){
			// agent3.moveToBase();
			// }
			// else {
			// agent3.move();
			// }
			// if (!board.hasRocks())
			// break;
			board.printBoard();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("All precious rock collected! Well done agents!");
	}
}