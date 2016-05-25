import java.awt.Point;

public class Main {

	public static void main(String args[]) {

		Board board = Board.getInstance();
		board.createBoard();
		ReactiveAgent agent = new ReactiveAgent("R");
		ReactiveAgent agent2 = new ReactiveAgent("T");
		ReactiveAgent agent3 = new ReactiveAgent("U");
		ReactiveAgent agent4 = new ReactiveAgent("V");
//		ReactiveAgent agent5 = new ReactiveAgent("W");
//		ReactiveAgent agent6 = new ReactiveAgent("X");
//		ReactiveAgent agent7 = new ReactiveAgent("Y");
//		ReactiveAgent agent8 = new ReactiveAgent("Z");
		agent.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 - 1));
		agent.carrier.setCurrPos(new Point(board.m.length / 2, board.m.length / 2));
		agent2.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 + 1));
		agent2.carrier.setCurrPos(new Point(board.m.length / 2, board.m.length / 2));
		agent3.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 + 1));
		agent3.carrier.setCurrPos(new Point(board.m.length / 2, board.m.length / 2));
		agent4.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 - 1));
		agent4.carrier.setCurrPos(new Point(board.m.length / 2, board.m.length / 2));
//		agent5.setCurrPos(new Point(board.m.length / 2 , board.m.length / 2 + 1));
//		agent6.setCurrPos(new Point(board.m.length / 2 , board.m.length / 2 - 1));
//		agent7.setCurrPos(new Point(board.m.length / 2 - 1, board.m.length / 2 ));
//		agent8.setCurrPos(new Point(board.m.length / 2 + 1, board.m.length / 2 ));
		board.setAgentPos(agent);
		board.setAgentPos(agent.carrier);
		board.setAgentPos(agent2);
		board.setAgentPos(agent2.carrier);
		board.setAgentPos(agent3);
		board.setAgentPos(agent3.carrier);
		board.setAgentPos(agent4);
		board.setAgentPos(agent4.carrier);
//		board.setAgentPos(agent5);
//		board.setAgentPos(agent6);
//		board.setAgentPos(agent7);
//		board.setAgentPos(agent8);
		board.setBasePos(new Point(board.m.length / 2, board.m.length / 2));
		board.printBoard();

		while (board.hasRocks() || board.hasRocksDug()) { // || !agent2.isAgentAtTheBase() || !agent3.isAgentAtTheBase() || !agent4.isAgentAtTheBase()) {
			if (!agent.carrier.isCarrierIdle()) {
				agent.carrier.collectRock();
			}
			else if (!agent.carrier.isAgentAtTheBase()){
				agent.carrier.moveToBase();
			}
			if (board.hasRocks())
				agent.move();
			else 
				agent.moveToBase();
			if (!board.hasRocks() && !board.hasRocksDug())
				break;
			if (!agent2.carrier.isCarrierIdle()) {
				agent2.carrier.collectRock();
			}
			else if (!agent2.carrier.isAgentAtTheBase()){
				agent2.carrier.moveToBase();
			}
			if (board.hasRocks())
				agent2.move();
			else 
				agent2.moveToBase();
			if (!board.hasRocks() && !board.hasRocksDug())
				break;
			if (!agent3.carrier.isCarrierIdle()) {
				agent3.carrier.collectRock();
			}
			else if (!agent3.carrier.isAgentAtTheBase()){
				agent3.carrier.moveToBase();
			}
			if (board.hasRocks())
				agent3.move();
			else 
				agent3.moveToBase();
			if (!board.hasRocks() && !board.hasRocksDug())
				break;
			if (!agent4.carrier.isCarrierIdle()) {
				agent4.carrier.collectRock();
			}
			else if (!agent4.carrier.isAgentAtTheBase()){
				agent4.carrier.moveToBase();
			}
			if (board.hasRocks())
				agent4.move();
			else 
				agent4.moveToBase();
			
//			if (!board.hasRocks())
//				break;
//			if (agent3.hasFoundRock()){
//				agent3.moveToBase();
//			}
//			else {
//				agent3.move();
//			}
//			if (!board.hasRocks())
//				break;
//			if (agent4.hasFoundRock()){
//				agent4.moveToBase();
//			}
//			else {
//				agent4.move();
//			}
//			if (!board.hasRocks())
//				break;
//			if (agent5.hasFoundRock()){
//				agent5.moveToBase();
//			}
//			else {
//				agent5.move();
//			}
//			if (!board.hasRocks())
//				break;
//			if (agent6.hasFoundRock()){
//				agent6.moveToBase();
//			}
//			else {
//				agent6.move();
//			}
//			if (!board.hasRocks())
//				break;
//			if (agent7.hasFoundRock()){
//				agent7.moveToBase();
//			}
//			else {
//				agent7.move();
//			}
//			if (!board.hasRocks())
//				break;
//			if (agent8.hasFoundRock()){
//				agent8.moveToBase();
//			}
//			else {
//				agent8.move();
//			}
			board.printBoard();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("All rocks found! Well done robots!");
	}
}