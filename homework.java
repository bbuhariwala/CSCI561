import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;

enum TileType {
	BLACK, WHITE, NONE
}

enum GameMode {
	SINGLE, GAME
}

enum MoveType {
	JUMP, ADJ
}

class MiniMaxReturn {
	Move move;
	Double util_value;

	public MiniMaxReturn(Move move, Double util_value) {
		this.move = move;
		this.util_value = util_value;
	}

	public Move getMove() {
		return move;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	public Double getUtil_value() {
		return util_value;
	}

	public void setUtil_value(Double util_value) {
		this.util_value = util_value;
	}
}

class Move {
	private GridCell fromCell;
	private List<GridCell> intermediateCells;
	private GridCell toCell;
	private MoveType moveType;

	public Move() {
		this.intermediateCells = new ArrayList<GridCell>();
	}

	public Move(GridCell fromCell, GridCell toCell, MoveType moveType) {
		this.fromCell = fromCell;
		this.toCell = toCell;
		this.moveType = moveType;
		this.intermediateCells = new ArrayList<GridCell>();
	}

	public GridCell getFromCell() {
		return fromCell;
	}

	public GridCell getToCell() {
		return toCell;
	}

	public MoveType getMoveType() {
		return moveType;
	}

	public void setFromCell(GridCell fromCell) {
		this.fromCell = fromCell;
	}

	public void setToCell(GridCell toCell) {
		this.toCell = toCell;
	}

	public void setMoveType(MoveType moveType) {
		this.moveType = moveType;
	}

	public List<GridCell> getIntermediateCells() {
		return intermediateCells;
	}

	public void setIntermediateCells(List<GridCell> intermediateCells) {
		this.intermediateCells = intermediateCells;
	}

	@Override
	public String toString() {
		return "Type" + this.moveType + "fromCell" + fromCell + "," + ",Intermediate" + intermediateCells + ",toCells"
				+ toCell;
	}

}

class GridCell {
	int x_pos;
	int y_pos;
	TileType contents; // This refers to refers to the actual contents i.e what is contained here
	TileType goalCamp; // This refers to the position being a camp or none for a goal
	TileType homeCamp; // This refers to the home camp
	String parent;

	public GridCell(int x_pos, int y_pos, TileType contents) {
		this.x_pos = x_pos;
		this.y_pos = y_pos;
		this.contents = contents;
		this.goalCamp = TileType.NONE;
		this.homeCamp = TileType.NONE;
	}

	@Override
	public String toString() {
		return "{ x_pos:" + x_pos + " y_pos:" + y_pos + " Contents:" + this.contents + " type:" + this.goalCamp
				+ "parent: " + parent + "}";
	}
}

class HalmaGamePlay {

	GridCell[][] board = null;
	private GameMode gameMode;
	private float totalPlayTime;
	private TileType myPlayer;
	private TileType enemyPlayer;
	public Map<Integer, GridCell> myPawns = null;
	public Map<Integer, GridCell> enemyPawns = null;
	private Map<GridCell, List<GridCell>> listOfJumpMovesMapping = null;

	private List<GridCell> whiteGoals = new ArrayList<GridCell>();
	private List<GridCell> blackGoal = new ArrayList<GridCell>();

	public int globalCounter = 0;
	public int pruneCounter = 0;
	private long timeLimit;

	public HalmaGamePlay() {
		board = new GridCell[16][16];
		myPawns = new HashMap<Integer, GridCell>();
		enemyPawns = new HashMap<Integer, GridCell>();
	}
	
	public float getTotalPlayTime() {
		return this.totalPlayTime;
	}

	public void setGoalsPositions() {
		// White Goals is in the Black Position
		whiteGoals.add(board[0][0]);
		whiteGoals.add(board[0][1]);
		whiteGoals.add(board[0][2]);
		whiteGoals.add(board[0][3]);
		whiteGoals.add(board[0][4]);

		whiteGoals.add(board[1][0]);
		whiteGoals.add(board[1][1]);
		whiteGoals.add(board[1][2]);
		whiteGoals.add(board[1][3]);
		whiteGoals.add(board[1][4]);

		whiteGoals.add(board[2][0]);
		whiteGoals.add(board[2][1]);
		whiteGoals.add(board[2][2]);
		whiteGoals.add(board[2][3]);

		whiteGoals.add(board[3][0]);
		whiteGoals.add(board[3][1]);
		whiteGoals.add(board[3][2]);

		whiteGoals.add(board[4][0]);
		whiteGoals.add(board[4][1]);

		board[0][0].goalCamp = TileType.WHITE;
		board[0][1].goalCamp = TileType.WHITE;
		board[0][2].goalCamp = TileType.WHITE;
		board[0][3].goalCamp = TileType.WHITE;
		board[0][4].goalCamp = TileType.WHITE;

		board[1][0].goalCamp = TileType.WHITE;
		board[1][1].goalCamp = TileType.WHITE;
		board[1][2].goalCamp = TileType.WHITE;
		board[1][3].goalCamp = TileType.WHITE;
		board[1][4].goalCamp = TileType.WHITE;

		board[2][0].goalCamp = TileType.WHITE;
		board[2][1].goalCamp = TileType.WHITE;
		board[2][2].goalCamp = TileType.WHITE;
		board[2][3].goalCamp = TileType.WHITE;

		board[3][0].goalCamp = TileType.WHITE;
		board[3][1].goalCamp = TileType.WHITE;
		board[3][2].goalCamp = TileType.WHITE;

		board[4][0].goalCamp = TileType.WHITE;
		board[4][1].goalCamp = TileType.WHITE;

		// Black Position to White Goals
		blackGoal.add(board[15][11]);
		blackGoal.add(board[15][12]);
		blackGoal.add(board[15][13]);
		blackGoal.add(board[15][14]);
		blackGoal.add(board[15][15]);

		blackGoal.add(board[14][11]);
		blackGoal.add(board[14][12]);
		blackGoal.add(board[14][13]);
		blackGoal.add(board[14][14]);
		blackGoal.add(board[14][15]);

		blackGoal.add(board[13][12]);
		blackGoal.add(board[13][13]);
		blackGoal.add(board[13][14]);
		blackGoal.add(board[13][15]);

		blackGoal.add(board[12][13]);
		blackGoal.add(board[12][14]);
		blackGoal.add(board[12][15]);

		blackGoal.add(board[11][14]);
		blackGoal.add(board[11][15]);

		board[15][11].goalCamp = TileType.BLACK;
		board[15][12].goalCamp = TileType.BLACK;
		board[15][13].goalCamp = TileType.BLACK;
		board[15][14].goalCamp = TileType.BLACK;
		board[15][15].goalCamp = TileType.BLACK;

		board[14][11].goalCamp = TileType.BLACK;
		board[14][12].goalCamp = TileType.BLACK;
		board[14][13].goalCamp = TileType.BLACK;
		board[14][14].goalCamp = TileType.BLACK;
		board[14][15].goalCamp = TileType.BLACK;

		board[13][12].goalCamp = TileType.BLACK;
		board[13][13].goalCamp = TileType.BLACK;
		board[13][14].goalCamp = TileType.BLACK;
		board[13][15].goalCamp = TileType.BLACK;

		board[12][13].goalCamp = TileType.BLACK;
		board[12][14].goalCamp = TileType.BLACK;
		board[12][15].goalCamp = TileType.BLACK;

		board[11][14].goalCamp = TileType.BLACK;
		board[11][15].goalCamp = TileType.BLACK;

		// White Goals is in the Black Position
		board[0][0].homeCamp = TileType.BLACK;
		board[0][1].homeCamp = TileType.BLACK;
		board[0][2].homeCamp = TileType.BLACK;
		board[0][3].homeCamp = TileType.BLACK;
		board[0][4].homeCamp = TileType.BLACK;

		board[1][0].homeCamp = TileType.BLACK;
		board[1][1].homeCamp = TileType.BLACK;
		board[1][2].homeCamp = TileType.BLACK;
		board[1][3].homeCamp = TileType.BLACK;
		board[1][4].homeCamp = TileType.BLACK;

		board[2][0].homeCamp = TileType.BLACK;
		board[2][1].homeCamp = TileType.BLACK;
		board[2][2].homeCamp = TileType.BLACK;
		board[2][3].homeCamp = TileType.BLACK;

		board[3][0].homeCamp = TileType.BLACK;
		board[3][1].homeCamp = TileType.BLACK;
		board[3][2].homeCamp = TileType.BLACK;

		board[4][0].homeCamp = TileType.BLACK;
		board[4][1].homeCamp = TileType.BLACK;

		// Black Position to White Goals
		board[15][11].homeCamp = TileType.WHITE;
		board[15][12].homeCamp = TileType.WHITE;
		board[15][13].homeCamp = TileType.WHITE;
		board[15][14].homeCamp = TileType.WHITE;
		board[15][15].homeCamp = TileType.WHITE;

		board[14][11].homeCamp = TileType.WHITE;
		board[14][12].homeCamp = TileType.WHITE;
		board[14][13].homeCamp = TileType.WHITE;
		board[14][14].homeCamp = TileType.WHITE;
		board[14][15].homeCamp = TileType.WHITE;

		board[13][12].homeCamp = TileType.WHITE;
		board[13][13].homeCamp = TileType.WHITE;
		board[13][14].homeCamp = TileType.WHITE;
		board[13][15].homeCamp = TileType.WHITE;

		board[12][13].homeCamp = TileType.WHITE;
		board[12][14].homeCamp = TileType.WHITE;
		board[12][15].homeCamp = TileType.WHITE;

		board[11][14].homeCamp = TileType.WHITE;
		board[11][15].homeCamp = TileType.WHITE;

	}

	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	public GameMode getGameMode() {
		return this.gameMode;
	}

	public void setTotalPlayTime(float totalPlayTime) {
		this.totalPlayTime = totalPlayTime;
	}

	public void setMyPlayer(TileType myPlayer) {
		this.myPlayer = myPlayer;
	}

	public TileType getMyPlayer() {
		return this.myPlayer;
	}

	public TileType getEnemyPlayer() {
		return enemyPlayer;
	}

	public void setEnemyPlayer(TileType enemyPlayer) {
		this.enemyPlayer = enemyPlayer;
	}

	public void printBoard() {
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				System.out.print(board[i][j].contents + ",");
			}
			System.out.println();
		}
		System.out.println();
	}

	public Map<GridCell, ArrayList<Move>> getConsolidatedMoves(TileType player) {
		// Container List for all the moves
		// I need to give preference to players who are in my camp first
		// Thus, I will first check if its my player and I have anyone in the camp
		// In that case, I will
		boolean myCampCeck = false;
		if (player == this.getMyPlayer()) {
			for (int i = 0; i < this.board.length; i++) {
				for (int j = 0; j < this.board.length; j++) {
					if (board[i][j].contents == player && board[i][j].homeCamp == player) {
						myCampCeck = true; // This indicates that there is a player in my camp!
						// I will only consider those pawns now that are inside my camp.
					}
				}
			}
		}

		Map<GridCell, ArrayList<Move>> consolidatedMoves = new HashMap<GridCell, ArrayList<Move>>();
		consolidatedMoves = getConsolidatedMovesUtil(player, myCampCeck);

		if (myCampCeck == true && !isPreferenceMapEmpty(consolidatedMoves)) {
			return consolidatedMoves;
		} else {
			return getConsolidatedMovesUtil(player, false);
		}
	}

	private Map<GridCell, ArrayList<Move>> getConsolidatedMovesUtil(TileType player, boolean myCampCeck) {
		Map<GridCell, ArrayList<Move>> consolidatedMoves = new HashMap<GridCell, ArrayList<Move>>();
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				if (myCampCeck == true && board[i][j].contents == player && board[i][j].homeCamp == player) {
					consolidatedMoves = getMoves(board[i][j], player, consolidatedMoves);
				} else if (myCampCeck == true && board[i][j].contents == player && board[i][j].homeCamp != player) {
					consolidatedMoves.put(board[i][j], new ArrayList<Move>());
				} else if (myCampCeck == false) {
					if (board[i][j].contents == player) {
						consolidatedMoves = getMoves(board[i][j], player, consolidatedMoves);
					}
				}
			}
		}
		return consolidatedMoves;
	}

	private boolean isPreferenceMapEmpty(Map<GridCell, ArrayList<Move>> consolidatedMoves) {
		for (Map.Entry<GridCell, ArrayList<Move>> consolidatedMove : consolidatedMoves.entrySet()) {
			ArrayList<Move> currentmoves = consolidatedMove.getValue();
			if (currentmoves.size() != 0) {
				return false;
			}
		}
		return true;
	}

	private Map<GridCell, ArrayList<Move>> getMoves(GridCell gridCell, TileType player,
			Map<GridCell, ArrayList<Move>> container) {
		listOfJumpMovesMapping = new HashMap<GridCell, List<GridCell>>();

		ArrayList<GridCell> moves = new ArrayList<GridCell>();

		moves = populateAdjacentMovesAndJumpMoves(gridCell, moves, player, true, container);

		// This method will create all the possible paths for the jump moves since all
		// of them are possible
		// equally likely but that will be decided by the evaluation function.
		getAllPathsForJumps(gridCell, container);
		
		// All moves have come and been populated in all moves list

		// Prune moves such that they are away from the origin
		pruneMovesBasedOnLocation(gridCell, player, container);

		// System.out.println("All Valid Moves" + container + "for the player have been
		// calulated");
		// System.out.println("Continaer" + container);
		return container;
	}

	public Move playGame() {
		int depth = 3;
		this.timeLimit = System.currentTimeMillis() + 20000;
		
		//Safety mode to avoid premature killing of agent
		if(this.getTotalPlayTime() < 25 || this.getGameMode() == GameMode.SINGLE) {
			depth = 2;
			if(this.getTotalPlayTime() < 5) {
				depth = 1;
			}
		}
		
		float alpha = Float.MIN_VALUE;
		float beta = Float.MAX_VALUE;
		MiniMaxReturn miniMaxReturn = minimax(depth, alpha, beta, this.getMyPlayer());
		return miniMaxReturn.getMove();
	}

	private MiniMaxReturn minimax(int depth, double alpha, double beta, TileType player) {
		// Depth Case or timeout
		if (depth == 0 || this.isGoalReached() || System.currentTimeMillis() > this.timeLimit) {
			return new MiniMaxReturn(null, getUtilityValue(this.getMyPlayer()));
		}

		Move optimal_move = null;
		double optimal_value = 0.0;
		Map<GridCell, ArrayList<Move>> all_moves = null;

		if (player == this.getMyPlayer()) {
			optimal_value = -9999999.0;
			all_moves = this.getConsolidatedMoves(player);

		} else {
			optimal_value = 9999999.0;
			all_moves = this.getConsolidatedMoves(player);
		}

		for (Map.Entry<GridCell, ArrayList<Move>> mp : all_moves.entrySet()) {
			GridCell from = mp.getKey();
			ArrayList<Move> to = mp.getValue();

			for (int i = 0; i < to.size(); i++) {

				Move move = to.get(i);
				double value = 0.0;
				MiniMaxReturn miniMaxReturn = null;
				GridCell toCell = move.getToCell();
				TileType oldContent = from.contents;

				this.board[toCell.y_pos][toCell.x_pos].contents = from.contents;
				this.board[from.y_pos][from.x_pos].contents = TileType.NONE;

				miniMaxReturn = this.minimax(depth - 1, alpha, beta, getTheOtherPlayer(player));
				value = miniMaxReturn.getUtil_value();
				// System.out.println("Value received" + value);
				this.board[toCell.y_pos][toCell.x_pos].contents = TileType.NONE;
				this.board[from.y_pos][from.x_pos].contents = oldContent;

				if (player == this.getMyPlayer() && value > optimal_value) { // Alpha is always maximized
					optimal_value = value;
					optimal_move = move;
					alpha = Math.max(alpha, value);
				}

				if (player == this.getEnemyPlayer() && value < optimal_value) { // Beta always wants to minimize
					optimal_value = value;
					optimal_move = move;
					beta = Math.min(beta, value);
				}

				if (beta <= alpha) {
					this.pruneCounter += 1;
					return new MiniMaxReturn(optimal_move, optimal_value);
				}

			}
		}
		return new MiniMaxReturn(optimal_move, optimal_value);

	}

	private TileType getTheOtherPlayer(TileType player) {
		if (player == this.getMyPlayer()) {
			return this.getEnemyPlayer();
		} else {
			return this.getMyPlayer();
		}
	}
	
	private double getUtilityValue(TileType player) {
		if(this.getMyPlayer() == TileType.WHITE)
			return getUtilityValueW(this.getMyPlayer());
		else
			return getUtilityValueB(this.getMyPlayer());
	}
	
	// black favoured winning condition
	private double getUtilityValueB(TileType player) {
		double value = 0.0;
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				GridCell gridCell = board[i][j];
				if (gridCell.contents == TileType.WHITE) {
					List<Double> vals = new ArrayList<Double>();
					for (int k = 0; k < this.whiteGoals.size(); k++) {
						if (this.board[this.whiteGoals.get(k).y_pos][this.whiteGoals
								.get(k).x_pos].contents != TileType.WHITE) {
							vals.add(getDistanceUtil(gridCell.x_pos, gridCell.y_pos, this.whiteGoals.get(k).x_pos,
									this.whiteGoals.get(k).y_pos));
						}
					}
					if (vals.size() != 0) {
						value -= Collections.max(vals);
					} else {
						value -= (-50);
					}

				} else if (gridCell.contents == TileType.BLACK) {
					List<Double> vals = new ArrayList<Double>();
					for (int k = 0; k < this.blackGoal.size(); k++) {
						if (this.board[this.blackGoal.get(k).y_pos][this.blackGoal
								.get(k).x_pos].contents != TileType.BLACK) {
							vals.add(getDistanceUtil(gridCell.x_pos, gridCell.y_pos, this.blackGoal.get(k).x_pos,
									this.blackGoal.get(k).y_pos));
						}
					}
					if (vals.size() != 0) {
						value += Collections.max(vals);
					} else {
						value += (-50);
					}
				}
			}
		}
		if (this.myPlayer == TileType.BLACK) {
			value *= -1;
		}
		return value;
	}

	// White favoured winning condition
	private double getUtilityValueW(TileType player) {
		double value = 0.0;
		for (int i = 0; i < this.board.length; i++) {
			for (int j = 0; j < this.board.length; j++) {
				GridCell gridCell = board[i][j];
				if (gridCell.contents == TileType.BLACK) {
					List<Double> vals = new ArrayList<Double>();
					for (int k = 0; k < this.blackGoal.size(); k++) {
						if (this.board[this.blackGoal.get(k).y_pos][this.blackGoal
								.get(k).x_pos].contents != TileType.BLACK) {
							vals.add(getDistanceUtil(gridCell.x_pos, gridCell.y_pos, this.blackGoal.get(k).x_pos,
									this.blackGoal.get(k).y_pos));
						}
					}
					if (vals.size() != 0) {
						value -= Collections.max(vals);
					} else {
						value -= (-50);
					}

				} else if (gridCell.contents == TileType.WHITE) {
					List<Double> vals = new ArrayList<Double>();
					for (int k = 0; k < this.whiteGoals.size(); k++) {
						if (this.board[this.whiteGoals.get(k).y_pos][this.whiteGoals
								.get(k).x_pos].contents != TileType.WHITE) {
							vals.add(getDistanceUtil(gridCell.x_pos, gridCell.y_pos, this.whiteGoals.get(k).x_pos,
									this.whiteGoals.get(k).y_pos));
						}
					}
					if (vals.size() != 0) {
						value += Collections.max(vals);
					} else {
						value += (-50);
					}
				}
			}
		}
		
		if (player == TileType.WHITE) {
			value *= -1;
		}
		return value;
	}

	public boolean isGoalReached() {
		boolean status = false;
		for (int i = 0; i < this.whiteGoals.size(); i++) {
			if (this.board[this.whiteGoals.get(i).y_pos][this.whiteGoals.get(i).x_pos].contents == TileType.NONE) {
				status = false;
				break;
			}
			if (this.board[this.whiteGoals.get(i).y_pos][this.whiteGoals.get(i).x_pos].contents == TileType.WHITE) {
				status = true;
			}
		}
		if (status == true) {
			return true;
		}

		status = false;

		for (int i = 0; i < this.blackGoal.size(); i++) {
			if (this.board[this.blackGoal.get(i).y_pos][this.blackGoal.get(i).x_pos].contents == TileType.NONE) {
				status = false;
				break;
			}
			if (this.board[this.blackGoal.get(i).y_pos][this.blackGoal.get(i).x_pos].contents == TileType.BLACK) {
				status = true;
			}
		}
		return status;
	}

	private Double getDistanceUtil(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	private void getAllPathsForJumps(GridCell parent, Map<GridCell, ArrayList<Move>> container) {
		Queue<GridCell> queue = new LinkedList<GridCell>();
		queue.add(parent);
		ArrayList<String> jumpPaths = new ArrayList<String>();
		Map<String, String> temp = new HashMap<String, String>();

		temp.put(parent.y_pos + "_" + parent.x_pos, parent.y_pos + "_" + parent.x_pos);

		Map<String, Integer> visited = new HashMap<String, Integer>();

		while (!queue.isEmpty()) {
			GridCell elem = queue.remove();
			visited.put(elem.y_pos + "_" + elem.x_pos, 1);

			String parent_key = elem.parent;
			String generate_key = elem.y_pos + "_" + elem.x_pos;

			if (elem.parent != null && temp.containsKey(parent_key)) {
				jumpPaths.add(temp.get(parent_key) + "," + generate_key);
				temp.put(generate_key, temp.get(parent_key) + "," + generate_key);
			}
			// Get children
			if (listOfJumpMovesMapping.get(elem) != null) {
				for (int i = 0; i < listOfJumpMovesMapping.get(elem).size(); i++) {
					String temp_key = listOfJumpMovesMapping.get(elem).get(i).y_pos + "_"
							+ listOfJumpMovesMapping.get(elem).get(i).x_pos;
					if (!visited.containsKey(temp_key)) {
						board[listOfJumpMovesMapping.get(elem).get(i).y_pos][listOfJumpMovesMapping.get(elem)
								.get(i).x_pos].parent = elem.y_pos + "_" + elem.x_pos;
						queue.add(listOfJumpMovesMapping.get(elem).get(i));
					}
				}
			}

		}

		for (int i = 0; i < jumpPaths.size(); i++) {
			String[] currentPath = jumpPaths.get(i).split(",");
			if (currentPath.length != 1) {
				Move move = new Move();
				String[] source = currentPath[0].split("_");
				move.setFromCell(board[Integer.parseInt(source[0])][Integer.parseInt(source[1])]);
				move.setMoveType(MoveType.JUMP);
				for (int j = 1; j < currentPath.length; j++) {
					String[] Intermediate = currentPath[j].split("_");
					if (j != currentPath.length - 1) {
						move.getIntermediateCells()
								.add(board[Integer.parseInt(Intermediate[0])][Integer.parseInt(Intermediate[1])]);
					} else {
						move.setToCell(board[Integer.parseInt(Intermediate[0])][Integer.parseInt(Intermediate[1])]);
					}
				}
				if (container.containsKey(move.getFromCell())) {
					ArrayList<Move> temp_moves = container.get(move.getFromCell());
					temp_moves.add(0, move);
					container.put(move.getFromCell(), temp_moves);
				} else {
					ArrayList<Move> temp_moves = new ArrayList<Move>();
					temp_moves.add(0, move);
					container.put(move.getFromCell(), temp_moves);
				}

			}

		}
	}

	private void pruneMovesBasedOnLocation(GridCell gridCell, TileType player,
			Map<GridCell, ArrayList<Move>> container) {
		if (gridCell.homeCamp != player && gridCell.goalCamp != player) { // The player is out of his camp and is not
																			// allowed to go in camp
			// Remove all destination that end in home camp
			pruneInCenterMovesIfRequired(gridCell, player, container);
		} else if (gridCell.homeCamp == player) {
			// Remove all destinations that are NOT further away from origin
			pruneInHouseMovesIfRequried(gridCell, player, container);
		} else {
			// Remove all the destinations that are outside the goal camp because he isn't
			// supposed to leave.
			pruneInGoalMovesIfRequired(gridCell, player, container);
		}
	}

	private void pruneInHouseMovesIfRequried(GridCell gridCell, TileType player,
			Map<GridCell, ArrayList<Move>> container) {
		// Find if there are any moves which are taking me outside came. If yes,
		// remove all moves that are ending up inside camp;
		if (container.containsKey(gridCell)) {
			ArrayList<Move> container_move = container.get(gridCell);

			Iterator<Move> listIterator = container_move.iterator();
			boolean onlyInCamp = true;
			while (listIterator.hasNext()) {
				Move move = listIterator.next();
				if (move.getToCell().homeCamp == TileType.NONE) {
					onlyInCamp = false;
					break;
				}
			}
			if (onlyInCamp == false) {
				listIterator = container_move.iterator();
				while (listIterator.hasNext()) {
					Move move = listIterator.next();
					if (move.getToCell().homeCamp == player) {
						listIterator.remove();
					}
				}
			} else {
				int originX = 0;
				int originY = 0;
				if (player == TileType.WHITE) {
					originX = 15;
					originY = 15;
				}

				int min = getManhattanDistance(originX, originY, gridCell);
				listIterator = container_move.iterator();
				while (listIterator.hasNext()) {
					Move move = listIterator.next();
					if (move.getToCell().homeCamp == player
							&& getManhattanDistance(originX, originY, move.getToCell()) <= min) {
						listIterator.remove();
					}
				}
			}
			container.put(gridCell, container_move);
		}
	}

	private void pruneInCenterMovesIfRequired(GridCell gridCell, TileType player,
			Map<GridCell, ArrayList<Move>> container) {
		if (container.containsKey(gridCell)) {
			ArrayList<Move> container_move = container.get(gridCell);
			Iterator<Move> listIterator = container_move.iterator();
			while (listIterator.hasNext()) {
				Move move = listIterator.next();
				if (move.getToCell().homeCamp == player) {
					listIterator.remove();
				}
			}
			container.put(gridCell, container_move);
		}
	}

	private void pruneInGoalMovesIfRequired(GridCell gridCell, TileType player,
			Map<GridCell, ArrayList<Move>> container) {
		if (container.containsKey(gridCell)) {
			ArrayList<Move> container_move = container.get(gridCell);
			Iterator<Move> listIterator = container_move.iterator();
			while (listIterator.hasNext()) {
				Move move = listIterator.next();
				if (move.getToCell().goalCamp != player) {
					listIterator.remove();
				}
			}
			container.put(gridCell, container_move);
		}
	}

	private int getManhattanDistance(int originX, int originY, GridCell cellToCheck) {
		return Math.abs((cellToCheck.x_pos - originX)) + Math.abs(cellToCheck.y_pos - originY);
	}

	private ArrayList<TileType> getAndFilterValidMoves(GridCell cell, TileType player) {
		ArrayList<TileType> valid_tiles = new ArrayList<TileType>();
		valid_tiles.add(TileType.NONE);
		valid_tiles.add(TileType.BLACK);
		valid_tiles.add(TileType.WHITE);
		return valid_tiles;
	}

	public ArrayList<GridCell> populateAdjacentMovesAndJumpMoves(GridCell cell, ArrayList<GridCell> moves,
			TileType player, boolean adjacent, Map<GridCell, ArrayList<Move>> container) {

		int row = cell.y_pos;
		int col = cell.x_pos;

		ArrayList<TileType> valid_tiles = getAndFilterValidMoves(cell, player);

		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				int next_row = row + i;
				int next_col = col + j;
				if ((next_row == row && next_col == col) || next_row < 0 || next_col < 0 || next_row >= 16
						|| next_col >= 16)
					continue;

				if (!valid_tiles.contains(board[next_row][next_col].homeCamp)) // Revisit
					continue;

				if (board[next_row][next_col].contents == TileType.NONE) {
					if (adjacent == true) {
						Move move = new Move();
						move.setFromCell(board[row][col]);
						move.setToCell(board[next_row][next_col]);
						move.setMoveType(MoveType.ADJ);
						if (container.containsKey(board[row][col])) {
							ArrayList<Move> temp = container.get(board[row][col]);
							temp.add(move);
							container.put(board[row][col], temp);
						} else {
							ArrayList<Move> temp = new ArrayList<Move>();
							temp.add(move);
							container.put(board[row][col], temp);
						}

						moves.add(board[next_row][next_col]);
					}
					continue;
				}
				// End of adding Adjacent moves

				next_row = next_row + i;
				next_col = next_col + j;

				if (next_row < 0 || next_col < 0 || next_row >= 16 || next_col >= 16)
					continue;

				if (moves.contains(board[next_row][next_col])) {
					if (listOfJumpMovesMapping.get(board[row][col]) != null) {
						List<GridCell> temp = listOfJumpMovesMapping.get(board[row][col]);
						temp.add(board[next_row][next_col]);
						listOfJumpMovesMapping.put(board[row][col], temp);
					} else {
						List<GridCell> temp = new LinkedList<GridCell>();
						temp.add(board[next_row][next_col]);
						listOfJumpMovesMapping.put(board[row][col], temp);
					}
				}

				if (moves.contains(board[next_row][next_col])
						|| !valid_tiles.contains(board[next_row][next_col].homeCamp)) {

					continue;
				}

				if (board[next_row][next_col].contents == TileType.NONE) {
					if (listOfJumpMovesMapping.get(board[row][col]) != null) {
						List<GridCell> temp = listOfJumpMovesMapping.get(board[row][col]);
						temp.add(board[next_row][next_col]);
						listOfJumpMovesMapping.put(board[row][col], temp);
					} else {
						List<GridCell> temp = new LinkedList<GridCell>();
						temp.add(board[next_row][next_col]);
						listOfJumpMovesMapping.put(board[row][col], temp);
					}

					moves.add(0, board[next_row][next_col]);
					populateAdjacentMovesAndJumpMoves(board[next_row][next_col], moves, player, false, container);
				}
			}
		}
		// Print adjacent valid moves
		// System.out.println(moves);
		return moves;
	}
}

public class homework {

	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("input.txt");
		@SuppressWarnings("resource")
		Scanner in = new Scanner(file);
		HalmaGamePlay halmaGamePlay = new HalmaGamePlay();
		int myCount = 0;
		int enemyCount = 0;
		int count = 1;
		while (in.hasNextLine()) {
			String nextLine = in.nextLine();
			if (count == 1) { // SINGLE or GAME
				if (nextLine.equals("SINGLE"))
					halmaGamePlay.setGameMode(GameMode.SINGLE);
				else
					halmaGamePlay.setGameMode(GameMode.GAME);
			} else if (count == 2) { // WHITE or BLACK
				if (nextLine.equals("BLACK")) {
					halmaGamePlay.setMyPlayer(TileType.BLACK);
					halmaGamePlay.setEnemyPlayer(TileType.WHITE);
				} else {
					halmaGamePlay.setMyPlayer(TileType.WHITE);
					halmaGamePlay.setEnemyPlayer(TileType.BLACK);
				}
			} else if (count == 3) { // Time Limit
				halmaGamePlay.setTotalPlayTime(Float.parseFloat(nextLine));
			} else {
				String[] tiles = nextLine.split("");
				for (int i = 0; i < tiles.length; i++) {
					if (tiles[i].equals(".")) {
						halmaGamePlay.board[count - 4][i] = new GridCell(i, count - 4, TileType.NONE);
					} else if (tiles[i].equals("B")) {
						halmaGamePlay.board[count - 4][i] = new GridCell(i, count - 4, TileType.BLACK);
						if (halmaGamePlay.getMyPlayer() == TileType.BLACK) {
							myCount += 1;
							halmaGamePlay.myPawns.put(myCount, halmaGamePlay.board[count - 4][i]);
						} else {
							enemyCount += 1;
							halmaGamePlay.enemyPawns.put(enemyCount, halmaGamePlay.board[count - 4][i]);
						}

					} else {
						halmaGamePlay.board[count - 4][i] = new GridCell(i, count - 4, TileType.WHITE);
						if (halmaGamePlay.getMyPlayer() == TileType.WHITE) {
							myCount += 1;
							halmaGamePlay.myPawns.put(myCount, halmaGamePlay.board[count - 4][i]);
						} else {
							enemyCount += 1;
							halmaGamePlay.enemyPawns.put(enemyCount, halmaGamePlay.board[count - 4][i]);
						}
					}
				}
			}
			count += 1;
		}
		halmaGamePlay.setGoalsPositions();
		
		Move move = halmaGamePlay.playGame();
		
        /*
        halmaGamePlay.board[move.getFromCell().y_pos][move.getFromCell().x_pos].contents = TileType.NONE;
		halmaGamePlay.board[move.getToCell().y_pos][move.getToCell().x_pos].contents = halmaGamePlay.getMyPlayer();
		try {
			writeToFile(halmaGamePlay);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		while (!halmaGamePlay.isGoalReached()) {
			Move move = halmaGamePlay.playGame();
			halmaGamePlay.board[move.getFromCell().y_pos][move.getFromCell().x_pos].contents = TileType.NONE;
			halmaGamePlay.board[move.getToCell().y_pos][move.getToCell().x_pos].contents = halmaGamePlay.getMyPlayer();
			halmaGamePlay.printBoard();			
			if (halmaGamePlay.getMyPlayer() == TileType.WHITE) {
				halmaGamePlay.setMyPlayer(TileType.BLACK);
				halmaGamePlay.setEnemyPlayer(TileType.WHITE);
			} else {
				halmaGamePlay.setMyPlayer(TileType.WHITE);
				halmaGamePlay.setEnemyPlayer(TileType.BLACK);
			}

		}
		System.out.println("Move" + move);
		System.out.println(System.currentTimeMillis() - start);
		*/
		writeMovestoFiles(move);
	}
	
	private static void writeToFile(HalmaGamePlay halmaGamePlay) throws IOException {
		  String str = "";
		  BufferedWriter writer = new BufferedWriter(new FileWriter("input.txt"));
		  writer.write("GAME" + "\n");
		  writer.write("WHITE" + "\n");
		  writer.write("100.00" + "\n");
		  
		  for(int i = 0 ; i < 16; i++){
		    str = "";
		    for(int j = 0; j < 16; j++) {
		        if(halmaGamePlay.board[i][j].contents == TileType.BLACK){
		          str += "B";
		        }else if(halmaGamePlay.board[i][j].contents == TileType.WHITE){
		          str += "W";
		        }else{
		          str += ".";
		        }
		    }
		    writer.write(str + "\n");
		  }
		  writer.close();
		}

	private static void writeMovestoFiles(Move move) throws FileNotFoundException {
		PrintWriter printWriter = new PrintWriter("output.txt");
		StringBuilder stringBuilder = new StringBuilder();
		if (move.getMoveType() == MoveType.ADJ) {
			stringBuilder.append("E");
			stringBuilder.append(" ");
			stringBuilder.append(move.getFromCell().x_pos);
			stringBuilder.append(",");
			stringBuilder.append(move.getFromCell().y_pos);
			stringBuilder.append(" ");
			stringBuilder.append(move.getToCell().x_pos);
			stringBuilder.append(",");
			stringBuilder.append(move.getToCell().y_pos);
			printWriter.println(stringBuilder.toString());
		} else {
			stringBuilder.append("J");
			stringBuilder.append(" ");
			stringBuilder.append(move.getFromCell().x_pos);
			stringBuilder.append(",");
			stringBuilder.append(move.getFromCell().y_pos);
			stringBuilder.append(" ");
			if (move.getIntermediateCells().size() == 0) {
				stringBuilder.append(move.getToCell().x_pos);
				stringBuilder.append(",");
				stringBuilder.append(move.getToCell().y_pos);
			} else {
				stringBuilder.append(move.getIntermediateCells().get(0).x_pos);
				stringBuilder.append(",");
				stringBuilder.append(move.getIntermediateCells().get(0).y_pos);
				for (int i = 1; i < move.getIntermediateCells().size(); i++) {
					stringBuilder.append("\n");
					stringBuilder.append("J");
					stringBuilder.append(" ");
					stringBuilder.append(move.getIntermediateCells().get(i - 1).x_pos);
					stringBuilder.append(",");
					stringBuilder.append(move.getIntermediateCells().get(i - 1).y_pos);
					stringBuilder.append(" ");
					stringBuilder.append(move.getIntermediateCells().get(i).x_pos);
					stringBuilder.append(",");
					stringBuilder.append(move.getIntermediateCells().get(i).y_pos);
				}
				stringBuilder.append("\n");
				stringBuilder.append("J");
				stringBuilder.append(" ");
				stringBuilder.append(move.getIntermediateCells().get(move.getIntermediateCells().size() - 1).x_pos);
				stringBuilder.append(",");
				stringBuilder.append(move.getIntermediateCells().get(move.getIntermediateCells().size() - 1).y_pos);
				stringBuilder.append(" ");
				stringBuilder.append(move.getToCell().x_pos);
				stringBuilder.append(",");
				stringBuilder.append(move.getToCell().y_pos);
			}
			printWriter.println(stringBuilder.toString());
		}
		printWriter.close();
	}

}
