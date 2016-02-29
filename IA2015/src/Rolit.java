import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/*
 * Homework 3 Artificial Intelligence
 * @autor Arhip Alin-Gabriel, group 342C3
 * Rolit is the 4-players version of the game Othello/Reversi
 * The implementation uses a similar approach to the Monte-Carlo Search Tree Algorithm
 * for the decision of the best move: is selects a move then random plays the game
 * 100 times from that move and based on how many times it wins the game, it stores the score for that move.
 * The best move is then decided by comparing the scores of each move and finding the maximum score,
 * which represents the move for which the AI has won most of the games out of 100 tries.
 * The algorithm can be extended to more tries, but it costs more time.
 *  */

class Pair<A, B> {

	public A first;
	public B second;

	public Pair(A first, B second) {
		this.first = first;
		this.second = second;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}
}

public class Rolit {

	public static final String RED = "R", BLUE = "B", YELLOW = "Y", GREEN = "G", EMPTY = ".", ONGOING = "ongoing";
	static String board[][];
	static BufferedReader br;
	static int captured = 0;

	/*
	 * the main is where the game is initialized, then starts and finishes by
	 * printing out the winner
	 */
	public static void main(String[] args) {

		board = new String[10][10];
		br = new BufferedReader(new InputStreamReader(System.in));
		ArrayList<String> players = new ArrayList<String>(Arrays.asList(RED, BLUE, YELLOW, GREEN));
		int hp = -1;

		do {
			System.out.println("Select number of human players(0-4): ");
			try {
				hp = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Must introduce a number between 0 and 4. Try again! :-) ");
			}
		} while (hp < 0 || hp > 4);

		/* create the initial game board */
		createBoard();

		/* set each player's initial position */
		board[4][4] = RED;
		board[5][4] = BLUE;
		board[4][5] = YELLOW;
		board[5][5] = GREEN;

		/* show the current game board */
		printBoard(board);

		/* lets play a game with zero or more computer players */
		while (checkgameStatus(board).equals(ONGOING)) {
			/* the human player(s) move first */
			for (int i = 0; i < hp; i++)
				if (!humanPlayerMove(players.get(i), players.get((i + 1) % 4), players.get((i + 2) % 4),
						players.get((i + 3) % 4)).equals(ONGOING))
					break;

			/* we do this check again, in order to break from the while loop */
			if (!checkgameStatus(board).equals(ONGOING))
				break;

			/* the computer player(s) move second */
			for (int i = hp; i < 4; i++)
				if (!ComputerMove(players.get(i), players.get((i + 1) % 4), players.get((i + 2) % 4),
						players.get((i + 3) % 4)).equals(ONGOING))
					break;
		}

		System.out.println("Game finished! Congratulations to the winner: " + checkgameStatus(board) + " ! ");
	}

	/*
	 * this method represents the human player move in order to help the player
	 * win the game, the computer will display capturing moves - in game, the
	 * slots represented by the "*" character (asterisk) and also the best move
	 * - in game, the slot represented by the "#" character (root) The best move
	 * is defined as the move that can win most of the games out of 100 tries
	 * and in this method the best move is calculated from the same method as
	 * the computer player so we can say that the best move is the move which a
	 * computer would make if it were in the player's place
	 */
	public static String humanPlayerMove(String p, String other, String other2, String other3) {
		int row = 0, col = 0;
		boolean found = false;
		ArrayList<Pair<Integer, Integer>> moves = getPossibleMoves(board, p);
		Pair<Integer, Integer> pair;
		String[][] temp = new String[10][10];
		ArrayList<String> players = new ArrayList<String>(Arrays.asList(p, other, other2, other3));
		Pair<Integer, Integer> best_pair = getBestMove(players);

		while (true) {
			/* make a copy of the current table */
			for (int i = 0; i < board.length; i++)
				for (int j = 0; j < board.length; j++)
					temp[i][j] = board[i][j];

			/*
			 * the computer will use it to display all available moves for the
			 * human player including the best move
			 */
			for (int i = 0; i < moves.size(); i++) {
				pair = moves.get(i);
				temp[pair.getFirst()][pair.getSecond()] = "*";
			}
			temp[best_pair.getFirst()][best_pair.getSecond()] = "#";
			printBoard(temp);

			try {
				System.out.println("Player " + p + " select row (1-8): ");
				row = Integer.parseInt(br.readLine());
				System.out.println("Player " + p + " select column(1-8): ");
				col = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Must introduce a number between 1 and 8. Try again! :-) ");
				continue;
			}

			/* set the condition of a legal human player move */
			if (row > 0 || row < 9 || col > 0 || col < 9) {
				/* check if the move exists in the list of possible moves */
				found = false;
				for (int i = 0; i < moves.size(); i++)
					if (moves.get(i).getFirst().equals(row) && moves.get(i).getSecond().equals(col)) {
						found = true;
						break;
					}
				/* if exists, it is a legal move , else the loop resumes */
				if (found == false)
					System.out.println(
							"Invalid move. Available moves are marked with \"*\". Best move is marked with \"#\". Try again! :-) ");
				else
					break;
			}

		}

		return makeMove(p, new Pair<Integer, Integer>(row, col));
	}

	/* this method represents a move made by any of the computer players */
	public static String ComputerMove(String p, String other, String other2, String other3) {
		ArrayList<String> players = new ArrayList<String>(Arrays.asList(p, other, other2, other3));
		String curr = players.get(0);
		Pair<Integer, Integer> best_pair = getBestMove(players);
		return makeMove(curr, best_pair);
	}

	/*
	 * this method actually updates the board when a move is made, regardless if
	 * the player is human or computer and regardless of the players'color: it
	 * receives the player's color and the best move to make and then checks the
	 * board for captured pieces, prints it and updates the score
	 */
	public static String makeMove(String curr, Pair<Integer, Integer> best_pair) {
		board[best_pair.getFirst()][best_pair.getSecond()] = curr;
		board = checkPonta(board, best_pair, curr);
		printBoard(board);
		ArrayList<Integer> scores = calculateScore(board);
		System.out.println("Score: R: " + scores.get(0) + " , B: " + scores.get(1) + " , Y: " + scores.get(2) + " , G: "
				+ scores.get(3));
		return checkgameStatus(board);
	}

	/*
	 * this method return the best move possible based on how many games can be
	 * win out of 100 tries
	 */
	public static Pair<Integer, Integer> getBestMove(ArrayList<String> players) {

		Pair<Integer, Integer> pair, best_pair = new Pair<Integer, Integer>(0, 0);
		int score = 0, best_score = 0;
		String[][] temp, temp2;
		String curr = players.remove(0);
		ArrayList<Pair<Integer, Integer>> moves = getPossibleMoves(board, curr);
		players.add(curr);

		/*
		 * for every possible move, play 100 games and calculate the score for
		 * that move
		 */
		for (int i = 0; i < moves.size(); i++) {
			/*
			 * since the board is altered, we must copy the original board
			 * before alteration
			 */
			temp = new String[10][10];
			for (int k = 0; k < board.length; k++)
				temp[k] = board[k].clone();

			/* make the move, check for captured pieces and set the score */
			pair = moves.get(i);
			temp[pair.getFirst()][pair.getSecond()] = curr;
			temp = checkPonta(temp, pair, curr);
			score = 0;

			/*
			 * now play 100 random games and count how many times the current
			 * player wins this will be the score for that move
			 */
			for (int j = 0; j < 100; j++) {
				temp2 = new String[10][10];
				for (int k = 0; k < board.length; k++)
					temp2[k] = temp[k].clone();
				if (randomPlay(temp2, players).equals(curr))
					score++;
			}

			/*
			 * get the score for all possible moves and choose the maximum score
			 * to get the best move
			 */
			if (score >= best_score) {
				best_score = score;
				best_pair = pair;
			}
		}

		System.out.println("Best move of player " + curr + " is: " + best_pair.getFirst() + " " + best_pair.getSecond()
		+ " with a score of: " + best_score + " out of 100.");
		return best_pair;
	}

	/* simulate a random play and return the winner */
	public static String randomPlay(String tem[][], ArrayList<String> players) {

		ArrayList<Pair<Integer, Integer>> moves = new ArrayList<Pair<Integer, Integer>>();
		Pair<Integer, Integer> pair;
		Random r = new Random();

		while (checkgameStatus(tem).equals(ONGOING)) {

			for (int i = 0; i < players.size(); i++) {

				moves.clear();
				moves = getPossibleMoves(tem, players.get(i));
				pair = moves.get(r.nextInt(moves.size()));
				tem[pair.getFirst()][pair.getSecond()] = players.get(i);
				tem = checkPonta(tem, pair, players.get(i));

				if (!checkgameStatus(tem).equals(ONGOING))
					break;
			}
		}

		return checkgameStatus(tem);
	}

	/*
	 * check if captured any other color pieces and capture them this method
	 * represents the game mechanics part of the game
	 */
	public static String[][] checkPonta(String[][] tem, Pair<Integer, Integer> pair, String p) {

		int i, j, x = pair.getFirst(), y = pair.getSecond();
		boolean found = false;
		captured = 0;

		/* UP: iterating through the column from rows 1 to x - 1 */
		for (i = 1; i < x; i++) {
			found = false;
			/* if we found a piece of a given player */
			if (tem[i][y].equals(p)) {
				for (j = i + 1; j < x; j++)
					/* search for any empty slots from there to x - 1 */
					if (tem[j][y].equals(EMPTY))
						found = true;
				/*
				 * if no empty slot found, update all slots to the given
				 * player's color
				 */
				if (found == false) {
					for (j = i + 1; j < x; j++) {
						/* only if they are of other color */
						if (!tem[j][y].equals(p)) {
							tem[j][y] = p;
							/* remember the number of captured pieces */
							captured++;
						}
					}
					break;
				}
			}
		}

		/* DOWN: iterating through the column from rows 8 to x + 1 */
		for (i = 8; i > x; i--) {
			found = false;
			if (tem[i][y].equals(p)) {
				for (j = i - 1; j > x; j--)
					if (tem[j][y].equals(EMPTY))
						found = true;
				if (found == false) {
					for (j = i - 1; j > x; j--) {
						if (!tem[j][y].equals(p)) {
							tem[j][y] = p;
							captured++;
						}
					}
					break;
				}
			}
		}

		/* LEFT: iterating through the row from columns 1 to y - 1 */
		for (i = 1; i < y; i++) {
			found = false;
			if (tem[x][i].equals(p)) {
				for (j = i + 1; j < y; j++)
					if (tem[x][j].equals(EMPTY))
						found = true;
				if (found == false) {
					for (j = i + 1; j < y; j++) {
						if (!tem[x][j].equals(p)) {
							tem[x][j] = p;
							captured++;
						}
					}
					break;
				}
			}
		}

		/* RIGHT: iterating through the row from columns 8 to y + 1 */
		for (i = 8; i > y; i--) {
			found = false;
			if (tem[x][i].equals(p)) {
				for (j = i - 1; j > y; j--)
					if (tem[x][j].equals(EMPTY))
						found = true;
				if (found == false) {
					for (j = i - 1; j > y; j--) {
						if (!tem[x][j].equals(p)) {
							tem[x][j] = p;
							captured++;
						}
					}
					break;
				}
			}
		}

		/* RIGHT-UP: superior left triangle - first row */
		if (x + y <= 8) {
			for (i = 1; i < x; i++) {
				found = false;
				/* right-up maximum = tem[1][x+y-1] */
				if (tem[i][x + y - i].equals(p)) {
					for (j = i + 1; j < x; j++)
						if (tem[j][x + y - j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i + 1; j < x; j++) {
							if (!tem[j][x + y - j].equals(p)) {
								tem[j][x + y - j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* RIGHT-UP: inferior right triangle - last column */
		if (x + y > 8 && x + y < 16) {
			for (i = 8; i > y; i--) {
				found = false;
				/* right-up maximum = tem[x+y-8][8] */
				if (tem[x + y - i][i].equals(p)) {
					for (j = i - 1; j > y; j--)
						if (tem[x + y - j][j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i - 1; j > y; j--) {
							if (!tem[x + y - j][j].equals(p)) {
								tem[x + y - j][j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* RIGHT-DOWN: superior right triangle - last column */
		if (x < y) {
			for (i = 8; i > y; i--) {
				found = false;
				/* right-down maximum = tem[8-(y-x)][8] */
				if (tem[i - (y - x)][i].equals(p)) {
					for (j = i - 1; j > y; j--)
						if (tem[j - (y - x)][j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i - 1; j > y; j--) {
							if (!tem[j - (y - x)][j].equals(p)) {
								tem[j - (y - x)][j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* RIGHT-DOWN: inferior left triangle - last row */
		if (x >= y) {
			for (i = 8; i > x; i--) {
				found = false;
				/* right-down maximum = tem[8][8-(x-y)] */
				if (tem[i][i - (x - y)].equals(p)) {
					for (j = i - 1; j > x; j--)
						if (tem[j][j - (x - y)].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i - 1; j > x; j--) {
							if (!tem[j][j - (x - y)].equals(p)) {
								tem[j][j - (x - y)] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* LEFT-UP: superior right triangle - first row */
		if (x < y) {
			for (i = 1; i < x; i++) {
				found = false;
				/* left-up maximum = tem[1][1+(y-x)] */
				if (tem[i][i + (y - x)].equals(p)) {
					for (j = i + 1; j < x; j++)
						if (tem[j][j + (y - x)].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i + 1; j < x; j++) {
							if (!tem[j][j + (y - x)].equals(p)) {
								tem[j][j + (y - x)] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* LEFT-UP: inferior left triangle - first column */
		if (x >= y) {
			for (i = 1; i < y; i++) {
				found = false;
				/* left-up maximum = tem[1+(x-y)][1] */
				if (tem[i + (x - y)][i].equals(p)) {
					for (j = i + 1; j < y; j++)
						if (tem[j + (x - y)][j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i + 1; j < y; j++) {
							if (!tem[j + (x - y)][j].equals(p)) {
								tem[j + (x - y)][j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* LEFT-DOWN: superior left triangle - first row */
		if (x + y <= 8) {
			for (i = 1; i < y; i++) {
				found = false;
				/* left-down maximum = tem[x+y-1][1] */
				if (tem[x + y - i][i].equals(p)) {
					for (j = i + 1; j < y; j++)
						if (tem[x + y - j][j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i + 1; j < y; j++) {
							if (!tem[x + y - j][j].equals(p)) {
								tem[x + y - j][j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		/* LEFT-DOWN: inferior right triangle - last row */
		if (x + y > 8 && x + y < 16) {
			for (i = 8; i > x; i--) {
				found = false;
				/* left-down maximum = tem[8][x+y-8] */
				if (tem[i][x + y - i].equals(p)) {
					for (j = i - 1; j > x; j--)
						if (tem[j][x + y - j].equals(EMPTY))
							found = true;
					if (found == false) {
						for (j = i - 1; j > x; j--) {
							if (!tem[j][x + y - j].equals(p)) {
								tem[j][x + y - j] = p;
								captured++;
							}
						}
						break;
					}
				}
			}
		}

		return tem;
	}

	/*
	 * check if board[i][j] has neighbors, if it does not have it cannot be a
	 * legal move
	 */
	public static boolean hasNeighbors(String[][] table, int i, int j) {

		ArrayList<String> neighbors = new ArrayList<String>(
				Arrays.asList(table[i + 1][j], table[i + 1][j + 1], table[i + 1][j - 1], table[i - 1][j],
						table[i - 1][j - 1], table[i - 1][j + 1], table[i][j + 1], table[i][j - 1]));

		for (int i1 = 0; i1 < neighbors.size(); i1++)
			if (neighbors.get(i1).equals(RED) || neighbors.get(i1).equals(BLUE) || neighbors.get(i1).equals(YELLOW)
					|| neighbors.get(i1).equals(GREEN))
				return true;

		return false;
	}

	/*
	 * return the bestMoves if exists any, or all possible moves a bestMove is
	 * defined as a move that captures at least one of any opponent's pieces if
	 * the current player cannot capture any piece, it can move anywhere on the
	 * board adjacent to any piece
	 */
	public static ArrayList<Pair<Integer, Integer>> getPossibleMoves(String[][] table, String p) {
		ArrayList<Pair<Integer, Integer>> bestMoves = new ArrayList<Pair<Integer, Integer>>();
		ArrayList<Pair<Integer, Integer>> posMoves = new ArrayList<Pair<Integer, Integer>>();
		Pair<Integer, Integer> pair;
		String[][] temp;

		for (int i = 1; i < 9; i++)
			for (int j = 1; j < 9; j++)
				/* this is the computer legal possible move */
				if (table[i][j].equals(EMPTY) && hasNeighbors(table, i, j)) {
					pair = new Pair<Integer, Integer>(i, j);
					posMoves.add(pair);
					/* now we must check if the move captures anything */
					temp = new String[10][10];
					for (int k = 0; k < table.length; k++)
						temp[k] = table[k].clone();

					temp[i][j] = p;
					temp = checkPonta(temp, pair, p);
					if (captured > 0)
						bestMoves.add(pair);
				}

		/*
		 * in case no capture can be made, it can be placed anywhere adjacent to
		 * another piece
		 */
		if (bestMoves.isEmpty())
			return posMoves;

		return bestMoves;
	}

	/* check if game is over and if true return the winner */
	public static String checkgameStatus(String board[][]) {
		ArrayList<Integer> scores = calculateScore(board);

		/* if we found any empty slot, the game is not over */
		if (scores.get(4) != 0)
			return ONGOING;
		/* else return the player with the maximum score */
		else if (Collections.max(scores) == scores.get(0))
			return RED;
		else if (Collections.max(scores) == scores.get(1))
			return BLUE;
		else if (Collections.max(scores) == scores.get(2))
			return YELLOW;
		else
			return GREEN;
	}

	/* create initial board */
	public static void createBoard() {

		for (int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++) {
				board[i][j] = EMPTY;
				/* numbering the first and last row */
				if ((i == 0 || i == 9) && j != 0 && j != 9)
					board[i][j] = Integer.toString(j);
				/* numbering the first and last column */
				else if ((j == 0 || j == 9) && i != 0 && i != 9)
					board[i][j] = Integer.toString(i);
				/* eliminate the corners */
				else if ((i == 0 && j == 0) || (i == 9 && j == 9) || (i == 0 && j == 9) || (i == 9 && j == 0))
					board[i][j] = " ";
			}
	}

	/* print the current board */
	public static void printBoard(String[][] table) {
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++)
				System.out.print(table[i][j] + " ");
			System.out.println();
		}
		System.out.println();
	}

	/* calculate the current score of the players */
	public static ArrayList<Integer> calculateScore(String[][] board) {
		int reds = 0, blues = 0, yellows = 0, greens = 0, empties = 0;

		for (int i = 1; i < 9; i++)
			for (int j = 1; j < 9; j++)
				switch (board[i][j]) {
				case RED:
					reds++;
					break;
				case BLUE:
					blues++;
					break;
				case YELLOW:
					yellows++;
					break;
				case GREEN:
					greens++;
					break;
				case EMPTY:
					empties++;
					break;
				}

		return new ArrayList<Integer>(Arrays.asList(reds, blues, yellows, greens, empties));
	}
}