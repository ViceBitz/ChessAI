import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A subclass of Player that chooses a smart valid move each time, experimenting with NegaMax
 * 
 * @author Victor Gong
 * @version 3/29/2023
 *
 */
public class SmartPlayerNegamax extends Player
{
	
	private int PLAY_DEPTH;
	
	//Constants
	
	private static final int QUIESCENCE_PRUNING_BIG_DELTA = 900;
	private static final int QUIESCENCE_PRUNING_MARGIN_DELTA = 200;
	
	private static final int NEGAMAX_FUTILITY_FRONTIER_MARGIN = 300;
	private static final int NEGAMAX_FUTILITY_PREFRONTIER_MARGIN = 500;
	
	private static final int INF = Integer.MAX_VALUE;
	
	//Iterative deepening
	private boolean ITERATIVE_DEEPENING;
	private final int PLAY_TIME;
	private boolean time_break = false;
	private int baseline_depth;
	
	//General structures
	private Move bestMove = null;
	private Move[] prevMoves;

	// Debug & Performance
	private long inner_nodes = 0;
	private long leafs = 0;
	private long q_nodes = 0;
	private long total_nodes = 0;
	private long nodesProcessedByTT = 0;
	private long timeStart = 0;
	private int moveCount = 1;
	private String detailedLines = "";
	private final boolean SHOW_LINES = true;
	private final boolean SHOW_DETAILED_LINES = true;
	
	private final boolean USE_TT = true;
	
	//Heuristics Data Structures
	private Move[][][][] counterMove = new Move[8][8][8][8]; //Counter-move for ordering
	
	public SmartPlayerNegamax(Board board, String name, Color color, int baselineDepth) throws IOException
	{
		super(board, name, color);
		this.prevMoves = new Move[4];
		this.PLAY_DEPTH = baselineDepth;
		this.ITERATIVE_DEEPENING = true;
		this.PLAY_TIME = 3500;
		this.baseline_depth = baselineDepth;
		
	}
	
	public SmartPlayerNegamax(Board board, String name, Color color, int playDepth, boolean deepening, int playTime) throws IOException
	{
		super(board, name, color);
		this.prevMoves = new Move[4];
		this.PLAY_DEPTH = playDepth;
		this.ITERATIVE_DEEPENING = deepening;
		this.PLAY_TIME = playTime;
		this.baseline_depth = playDepth;
		
	}
	
	
	/**
	 * Sorts the moves in from volatile to quiet (capturing priority)
	 * @param moves The move list
	 */
	private void sortMoves(ArrayList<Move> moves, Move previousMove) {
		Board board = getBoard();
		moves.sort((m1, m2) -> {
			/**
			 * Sort order:
			 * Good captures
			 * Neutral captures
			 * Counter move heuristic
			 * Neutral movements
			 * Bad captures
			 */
			int m1Score = 0; int m2Score = 0;
			if (m1.getVictim() != null)
			{
				m1Score += 100 + Evaluation.SEE(board, m1)*1000;
			}
			if (m2.getVictim() != null) {
				m2Score += 100 + Evaluation.SEE(board, m2)*1000;
			}
			
			//Good, Neutral, Bad captures
			int captureComp = m2Score - m1Score;
			if (captureComp == 0)
			{
				//Counter-move heuristic
				if (previousMove != null) {
					int counterMoveBonus = 100;
					Location from = previousMove.getSource();
					Location to = previousMove.getDestination();
					Move counter = counterMove[from.getRow()][from.getCol()][to.getRow()][to.getCol()];
					m1Score += m1.equals(counter) ? counterMoveBonus : 0;
					m2Score += m2.equals(counter) ? counterMoveBonus : 0;
				}
				
				int CMComp = m2Score - m1Score;
				if (CMComp == 0)
				{
					//Start with more valuable pieces to move
					return m2.getPiece().getValue() - m1.getPiece().getValue();
				}
				else
				{
					return CMComp;
				}
			}
			else {
				return captureComp;
			}
		});
	}
	
	/*
	 * Updates the counter table to the current move that produced a beta-cutoff
	 * @param m The current move
	 * @param prev The previous move
	 */
	private void updateCounterTable(Move m, Move prev)
	{
		//If non-capture move and previous move exists, set counter-move
		if (prev != null && m.getVictim() == null)
		{
			Location from = prev.getSource();
			Location to = prev.getDestination();
			counterMove[from.getRow()][from.getCol()][to.getRow()][to.getCol()] = m;
		}
	}
	
	/**
	 * Helps retrieve an evaluation score, either from calculation or data file
	 * @param compressedState The compressed version of the board
	 * @return The ABSOLUTE evaluation (not relative)
	 * @throws IOException 
	 */
	private CompressionInfo retrieveEvaluation(String compressedState) throws IOException
	{
		CompressionInfo table_info = null;
		//Check if leaf calculation already done
		if (Compression.tableHasState(compressedState)) {
			table_info = Compression.retrieveFromTable(compressedState);
		}
		return table_info;
	}
	/**
	 * Quiescence search to ensure that there's no traps or capturebacks
	 * @param alpha The max value
	 * @param beta The cutoff value
	 * @param color The current color
	 * @param depth The current depth
	 * @return The evaluational value
	 * @throws IOException
	 */
	private int quiescence(int alpha, int beta, int color, int depth, int maxDepth, Move previousMove) throws IOException
	{
		Board board = getBoard();
		int plysLeft = maxDepth - depth;
		Color pieceColor = color == 1 ? Color.WHITE : Color.BLACK;
		boolean inCheck = board.getKing(pieceColor).inCheck();
		
		q_nodes++;
		
		//Stand pat
		int absoluteEval = Evaluation.evaluate(board).value;
		
		int evalScore = absoluteEval * color;
		
		boolean ableDeltaPrune = !inCheck && !Evaluation.isEndgame(board);
		
		//Don't use stand pat as lower bound if in check (special case)
		if (!inCheck) {
					
			/* If rating >= beta, then opponent (parent) already has a move
			 * that favors them more than this path, so break
			 */
			
			if (evalScore >= beta) {
				return evalScore;
			}
		
			if (alpha < evalScore) {
				alpha = evalScore;
			}
		}
		
		/*
		 * Probe the state table (transposition table)
		 * 
		 * TT still works in quiescence search because maxDepth - depth always <= 0, so 
		 * quiescence saved states will always be distinct from main states (main TT won't
		 * use these values because table_info.depth will always be >= maxDepth-depth
		 *
		 */
		
		String compressedState = Compression.compressState(board, color);
		
		if (USE_TT)
		{
			CompressionInfo table_info = retrieveEvaluation(compressedState);
			//Only use quiescence TT values (negative depth)
			if (table_info != null && table_info.depth >= plysLeft)
			{
				//PV Node (Exact)
				if (table_info.nodeType == 1) {
					nodesProcessedByTT++;
					return table_info.score;
				}
				//Upper Bound (<= alpha) (improves beta)
				else if (table_info.nodeType == 2) {
					if (table_info.score <= alpha) {
						nodesProcessedByTT++;
						return table_info.score;
					}
					
				}
				//Lower Bound (>= beta) (improves alpha)
				else if (table_info.nodeType == 3) {
					if (table_info.score >= beta)
					{
						nodesProcessedByTT++;
						return table_info.score;
					}
					
				}
			}
		}
		
		//Delta Pruning
		if (ableDeltaPrune && evalScore + QUIESCENCE_PRUNING_BIG_DELTA < alpha)
		{
			return alpha;
		}
		
		ArrayList<Move> moves = board.allMoves(pieceColor);
		
		// Sort the moves by best depth 1 evaluation score
		sortMoves(moves, previousMove);
		
		int value = evalScore;
		int originalAlpha = alpha;
		
		// Searches through all captures
		for (Move m: moves) {
			/*
			 * Ignore non-capture, quiet moves (unless in check or if move checks)
			 */
			
			if (!inCheck) {
				if (m.getVictim() == null) {
					break;
				}
			}
			
			//Delta Pruning for a capture move
			if (m.getVictim() != null)
			{
				//Check additional delta pruning (move-specific)
				if (ableDeltaPrune && evalScore + Evaluation.SEE(board, m) + QUIESCENCE_PRUNING_MARGIN_DELTA < alpha)
				{
					continue;
				}
			}
			
			
			board.executeMove(m);
			value = Math.max(value,-quiescence(-beta, -alpha, -color, depth+1, maxDepth, m));
			board.undoMove(m);
			
			if (value > alpha)
			{
				alpha = value;
				
				//Beta cutoff
				if (alpha >= beta) {
					break;
				}
			}
		}
				
		//TT Store
		if (USE_TT) {
			if (value <= originalAlpha) {
				//Fail-low (<= alpha)
				Compression.addToTable(compressedState, 2, plysLeft, value);
			}
			else if (value >= beta) {
				//Fail-high (alpha-beta cutoff, >= beta)
				Compression.addToTable(compressedState, 3, plysLeft, value);
			}
			else {
				//Exact score: alpha < score < beta
				Compression.addToTable(compressedState, 1, plysLeft, value);
			}
		}
		
		return value;
		
	}
	/**
	 * The negamax algorithm to find the optimal move
	 * 
	 * @return The best score in the subtree
	 * @throws IOException
	 */
	public int negamax(int depth, int maxDepth, int alpha, int beta, int color, Move previousMove, EvaluationLine currentLine, boolean nullMoveSearch) throws IOException
	{
		
		//Iterative Deepening: If search runs over the play time limit, flag time_break and exit
		//Don't cut time if haven't reached baseline depth
		if (ITERATIVE_DEEPENING && maxDepth > baseline_depth)
		{
			if (time_break || System.currentTimeMillis()-timeStart >= PLAY_TIME) {
				time_break = true;
				return 10000;
			}
		}
		
		//If leaf node, run evaluation/quiescence search
		if (depth == maxDepth)
		{
			leafs++;
			return quiescence(alpha, beta, color, depth, maxDepth, previousMove);
			
		}
		
		// Debugging & Statistics
		inner_nodes++;
		
		
		int plysLeft = maxDepth - depth;
		Color pieceColor = color == 1 ? Color.WHITE : Color.BLACK;
		Board board = getBoard();
		
		
		String compressedState = Compression.compressState(board, color);
		
		//Probe the state table (transposition table)
		if (USE_TT) {
			CompressionInfo table_info = retrieveEvaluation(compressedState);
			if (table_info != null && table_info.depth >= plysLeft && depth > 0) //Do not read TT if root
			{
				//PV Node (Exact)
				if (table_info.nodeType == 1) {
					nodesProcessedByTT++;
					currentLine.special = 1;
					return table_info.score;
				}
				//Upper Bound (<= alpha) (improves beta)
				else if (table_info.nodeType == 2) {
					if (table_info.score <= alpha) {
						nodesProcessedByTT++;
						currentLine.special = 2;
						return table_info.score;
					}
				}
				//Lower Bound (>= beta) (improves alpha)
				else if (table_info.nodeType == 3) {
					if (table_info.score >= beta)
					{
						nodesProcessedByTT++;
						currentLine.special = 3;
						return table_info.score;
					}
				}
			}
		}
		
		boolean inCheck = board.getKing(pieceColor).inCheck();
		
		//Evaluation of current node for pruning purposes
		int absoluteEval = Evaluation.evaluate(board).value;
		int evalScore = absoluteEval * color;
		
		
		
		/*
		 * Null Move Pruning
		 * 
		 * Conditions:
		 * - NOT Frontier Node (plysLeft == 1)
		 * - NOT previous move is null
		 * - NOT in check
		 * - Must have pieces other than pawns
		 * - Must produce a beta cutoff based on evaluation
		 */
		
		
		ArrayList<Piece> nonPawnPieces = new ArrayList<>();
		nonPawnPieces.addAll(board.getPiecesOfType(Knight.ENUM, pieceColor));
		nonPawnPieces.addAll(board.getPiecesOfType(Bishop.ENUM, pieceColor));
		nonPawnPieces.addAll(board.getPiecesOfType(Rook.ENUM, pieceColor));
		nonPawnPieces.addAll(board.getPiecesOfType(Queen.ENUM, pieceColor));
		
		if (plysLeft > 1 && previousMove != null && !inCheck && !nonPawnPieces.isEmpty() && evalScore >= beta)
		{
			int R = plysLeft <= 3 ? 1 : (plysLeft <= 6 ? 3 : 4); //[1-3] -> R=1; [4-6] -> R=3; [7+] -> R=4
			int nullScore = -negamax(depth+R,maxDepth,-beta,-beta+1,-color,null,new EvaluationLine(null),true);
			
			//Cutoff if still better than beta
			if (nullScore >= beta)
			{
				currentLine.special = 4;
				return quiescence(alpha, beta, color, maxDepth, maxDepth, previousMove);
			}
		}
		
		
		/*
		 * Reverse Futility Pruning
		 * 
		 * Conditions:
		 * - Remaining Plys <= 2
		 * - NOT in check
		 * - Beta is not close to mate value
		 */

		
		if (plysLeft <= 2 && !inCheck && Math.abs(beta) < INF-1000)
		{
			if (evalScore - (plysLeft == 2 ? NEGAMAX_FUTILITY_PREFRONTIER_MARGIN : NEGAMAX_FUTILITY_FRONTIER_MARGIN) >= beta)
			{
				currentLine.special = 5;
				return evalScore;
			}
		}
		
		
		//Get all moves
		ArrayList<Move> moves = board.allMoves(pieceColor);
		
		//Sort the moves
		sortMoves(moves, previousMove);
		
		int value = -INF;
		int originalAlpha = alpha;
		for (Move m : moves)
		{

			EvaluationLine childLine = new EvaluationLine(null);
			
			board.executeMove(m);
			
			int childValue = -negamax(depth+1, maxDepth, -beta, -alpha, -color, m, childLine,nullMoveSearch);
			if (childValue > value)
			{
				value = childValue;
				
				//Update the current line if found better move
				currentLine.bestMove = m;
				currentLine.next = childLine;
			}
			
			board.undoMove(m);
			
			//Check for time break (if time break, return)
			if (time_break)
			{
				return 10000;
			}
			
			/*
			//Debug Output
			if (depth == 0) {
				System.out.println(m+" | "+value/100.0);
			}
			*/
			
			//Debug All Lines Output
			if (SHOW_DETAILED_LINES && depth == 0)
			{
				double adjustedEval = (childValue * color) / 100.0;
				String evalPrint = (adjustedEval == 0 ? "" : (adjustedEval > 0 ? "+" : "-")) + Math.abs(adjustedEval);
				detailedLines += ("Line (" + evalPrint + "): " + "SEE=" + (m.getVictim() != null ? Evaluation.SEE(board, m) : 0) + " ");
				detailedLines += (m.toStandardNotation()) + " ";
				detailedLines += (childLine) + "\n";
			}
			
			
			
			if (value > alpha)
			{
				alpha = value;
				if (depth == 0)
				{
					bestMove = m;
				}
				//Beta-cutoff
				if (alpha >= beta) {
					updateCounterTable(m, previousMove);
					break;
				}
			}
		}
		//Check for checkmate/draw
		if (moves.size() == 0)
		{
			if (inCheck)
			{
				return -INF + depth;
			}
			else
			{
				return 0;
			}
		}
		
		//TT Store
		if (USE_TT && !nullMoveSearch && !time_break) {
			if (value <= originalAlpha) {
				//Fail-low (<= alpha)
				Compression.addToTable(compressedState, 2, plysLeft, value);
			}
			else if (value >= beta) {
				//Fail-high (alpha-beta cutoff, >= beta)
				Compression.addToTable(compressedState, 3, plysLeft, value);
			}
			else {
				//Exact score: alpha < score < beta
				Compression.addToTable(compressedState, 1, plysLeft, value);
			}
		}
		
		return value;
	}
	/**
	 * Procedure to run negamax on a certain depth
	 * @return The debug output of the run
	 */
	private String runNegamax(int depth, int color)
	{
		
		//Reset Debug/Performance variables
		bestMove = null;
		inner_nodes = 0;
		leafs = 0;
		q_nodes = 0;
		total_nodes = 0;
		nodesProcessedByTT = 0;
		timeStart = System.currentTimeMillis();
		detailedLines = "";
		time_break = false;

		int score;
		EvaluationLine PVLine = new EvaluationLine(null);
		try
		{
			score = negamax(0, depth, -INF, INF, color, null, PVLine, false);
		}
		catch (IOException e)
		{
			score = 0;
		}
		total_nodes = inner_nodes + q_nodes; //No leaf nodes because q_nodes includes leafs
		
		double adjustedEval = (score * color) / 100.0;
		String evalPrint = (adjustedEval == 0 ? "" : (adjustedEval > 0 ? "+" : "-")) + Math.abs(adjustedEval);
		
		String output = "";
		
		if (SHOW_LINES) {
			if (SHOW_DETAILED_LINES)
			{
				output += ("\nDetailed Lines -> \n");
				output += (detailedLines+"\n");
				output += ("Best -> \n");
			}
			
			output += ("Main Line (" + evalPrint + "): ");
			output += (PVLine + "\n");
		}
		
		output += (moveCount
				+ " | Eval: " + evalPrint
				+ " || "
				+ "Node Data:"
				+ " | Inner: " + inner_nodes
				+ " | Leaf: " + leafs
				+ " | Quies: " + q_nodes
				+ " | Total: " + total_nodes
				+ " | From TT: " + nodesProcessedByTT
				+ " || "
				+ "\nGeneral:"
				+ " | Time Elapsed: " + (System.currentTimeMillis() - timeStart) / 1000.0 + "s"
				+ " | Depth: " + depth)
				+ "\n";
		
		return output;
	}
	
	/**
	 * Gets the next move by selecting a random one
	 * 
	 * @return The next move
	 */
	public Move nextMove()
	{
		Board board = getBoard();
		int numColor = getColor().equals(Color.WHITE) ? 1 : -1;
		
		/*
		
		E.g. White, depth should be even; Black, depth should be odd
		This is to ensure the game tree always ends on White playing next move,
		so both players have played a move
		(basically completing a cycle of played moves (W,B,W,B))
		
		
		int depthAdjustment = numColor == 1 ? (PLAY_DEPTH%2==0 ? 0 : 1) : (PLAY_DEPTH%2==1 ? 0 : 1);
		
		*/
		
		
		//Run search
		String runInfo = null;
		
		//Iterative Deepening approach, cap out at certain time
		if (ITERATIVE_DEEPENING) {
			int addition = 0;
			Move prevBest = null;
			String prevInfo = null;
			
			time_break = false;
			while (!time_break) {
				prevBest = bestMove;
				prevInfo = runInfo;
				//System.out.println("Encountered Simplicity, running depth " + (PLAY_DEPTH+addition));
				runInfo = runNegamax(PLAY_DEPTH+addition, numColor);
				addition++;
			}
			bestMove = prevBest;
			runInfo = prevInfo;
		}
		//Standard hard depth approach
		else {
			runInfo = runNegamax(PLAY_DEPTH, numColor);
		}
		
		//If 2 repetitions, run deeper
		if (prevMoves[0] != null && prevMoves[0].equals(prevMoves[2])
				&& prevMoves[1] != null && prevMoves[1].equals(prevMoves[3])
				&& bestMove != null && bestMove.equals(prevMoves[1])) {
			
			boolean oldITERATIVE_DEEPENING = ITERATIVE_DEEPENING;
			ITERATIVE_DEEPENING = false; //Shut off Iterative Deepening for now
			
			int addition = 1;
			while (bestMove != null && bestMove.equals(prevMoves[1]) && addition <= 6) {
				System.out.println("Encountered Repetition, running depth " + (PLAY_DEPTH+addition));
				runInfo = runNegamax(PLAY_DEPTH+addition, numColor);
				addition++;
			}
			
			ITERATIVE_DEEPENING = oldITERATIVE_DEEPENING; //Return Iterative Deepening to original state
		}
		//Print search debug output
		System.out.println(runInfo);
		
		//Update previous moves
		for (int i=prevMoves.length-1;i>=1;i--) {
			prevMoves[i] = prevMoves[i-1];
		}
		prevMoves[0] = bestMove;
		
		//Update move count
		moveCount++;
		
		//Add to move queue
		return bestMove;
		
	}
	
	/**
	 * Prints information about the settings of this player
	 */
	public void printAIDetails() {
		System.out.println("----- AI Settings -----");
		System.out.println("INITIAL DEPTH: " + (PLAY_DEPTH));
		System.out.println("PLAY TIME CUTOFF: " + PLAY_TIME);
		Compression.printDataDetails();
		
	}

}


