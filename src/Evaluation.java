import java.awt.Color;
import java.util.*;

/**
 * A class containing all the methods necessary to evaluate a chess board state
 * 
 * @author Victor Gong
 * @version 3/31/2023
 *
 */
public class Evaluation
{
	
	private static final int LAZY_CUTOFF = 250;

	/**
	 * Sums up the material for a certain color (doesn't use object piece value)
	 * 
	 * @param board The board
	 * @param color The color
	 * @return The total material
	 */
	private static int calculateMaterial(Board board, Color color, boolean isEndgame)
	{
		int[] mg_value = { 82, 337, 365, 477, 1025,  0};
		int[] eg_value = { 94, 281, 297, 512,  936,  0};
		
		int material = 0;
		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++) {
			if (isEndgame)
			{
				material += eg_value[pieceEnum-1]*board.getPiecesOfType(pieceEnum, color).size();
			}
			else
			{
				material += mg_value[pieceEnum-1]*board.getPiecesOfType(pieceEnum, color).size();
			}
		}
		
		return material;
	}
	private static int calculateMaterialWithoutPawns(Board board, Color color)
	{
		int[] value = {100,300,300,500,900};
		int material = 0;
		for (int pieceEnum=2;pieceEnum<=5;pieceEnum++) {
			material += value[pieceEnum-1]*board.getPiecesOfType(pieceEnum, color).size();
		}
		
		return material;
	}
	
	/**
	 * Calculates the bishop pair score of a certain color
	 * 
	 * @param board The board
	 * @param col   The color
	 * @return The bishop pair score
	 */
	private static int calculateBishopPair(Board board, Color col, boolean isEndgame)
	{
		ArrayList<Piece> bishops = board.getPiecesOfType(Bishop.ENUM, col);
		
		//Bishop pairs most effective on open board (endgame)
		return isEndgame ? (bishops.size() == 2 ? 15 : 0) : 0;
	}
	
	/**
	 * Calculates the pawn structure for a certain color by evaluating how many columns filled with
	 * pawns
	 * 
	 * @param board The board
	 * @param col   The color
	 * @return The pawn structure score
	 */
	private static int calculatePawnStructure(Board board, Color col)
	{
		int score = 0;
		ArrayList<Location> locs = board.getOccupiedLocations();
		ArrayList<Piece> pawns = board.getPiecesOfType(Pawn.ENUM, col);
		
		boolean[] hasPawn = new boolean[board.getNumCols()];
		for (Piece p: pawns) {
			hasPawn[p.getLocation().getCol()] = true;
		}
		for (boolean has: hasPawn)
		{
			if (has)
			{
				score += 20;
			}
		}
		return score;
	}

	/**
	 * Calculates the king safety of a certain color
	 * 
	 * @param board The board
	 * @param col   The color
	 * @return The king safety score
	 */
	public static int calculateKingSafety(Board board, Color col)
	{
		int score = 0;
		King king = board.getKing(col);
		HashSet<Location> attacked = board.allAttackedSquares(Board.oppositeColor(col));

		if (attacked.contains(king.getLocation()))
		{
			score -= 40; // Check
		}
		for (Location loc : board.getOccupiedAdjacentLocations(king.getLocation()))
		{
			if (attacked.contains(loc))
			{
				score -= 10; // Adjacent Squares attacked
			}
		}
		return score;

	}

	/**
	 * Calculates the phase value for tapered evaluation
	 * 
	 * @param The current board
	 * @return The phase value
	 */
	private static int calculatePhase(Board board)
	{
		//Calculate number of every piece
		int[][] numPieces = new int[2][6];
		
		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++)
		{
			for (int color=0;color<=1;color++)
			{
				numPieces[color][pieceEnum-1] = board.getPiecesOfType(pieceEnum, color==0?Color.WHITE:Color.BLACK).size();
			}
		}
		
		int[] pieceP = {0,1,1,2,4};
		int pawnP = pieceP[0];
		int knightP = pieceP[1];
		int bishopP = pieceP[2];
		int rookP = pieceP[3];
		int queenP = pieceP[4];
		int totP = pawnP*16 + knightP*4 + bishopP*4 + rookP*4 + queenP*2;

		//Calculate current phase
		int curP = totP;
		for (int color=0;color<2;color++)
		{
			for (int piece=0;piece<5;piece++)
			{
				curP -= numPieces[color][piece] * pieceP[piece];
			}
		}
		return (curP * 256 + (totP/2))/totP;
	}
	
	/**
	 * Calculates the tapered evaluation for a current evaluation that
	 * includes opening and endgame scenarios
	 * @param board The current board
	 * @param openingEval The evaluation for midgame
	 * @param endgameEval the evaluation for endgame
	 * @return The calculated tapered evaluation
	 */
	private static int calculateTapered(Board board, int midgameEval, int endgameEval)
	{
		int phase = calculatePhase(board);
		return ((midgameEval * (256 - phase)) + (endgameEval * phase))/256;
	}
	
	/**
	 * Calculates the piece position of a certain color
	 * 
	 * @param board The board
	 * @param col   The color
	 * @return The position rating
	 */
	public static int calculatePiecePosition(Board board, Color col, boolean isEndgame)
	{
		int[] mg_pawn_table = {
			      0,   0,   0,   0,   0,   0,  0,   0,
			     98, 134,  61,  95,  68, 126, 34, -11,
			     -6,   7,  26,  31,  65,  56, 25, -20,
			    -14,  13,   6,  21,  23,  12, 17, -23,
			    -27,  -2,  -5,  12,  17,   6, 10, -25,
			    -26,  -4,  -4, -10,   3,   3, 33, -12,
			    -35,  -1, -20, -23, -15,  24, 38, -22,
			      0,   0,   0,   0,   0,   0,  0,   0,
		};

		int[] eg_pawn_table = {
		      0,   0,   0,   0,   0,   0,   0,   0,
		    178, 173, 158, 134, 147, 132, 165, 187,
		     94, 100,  85,  67,  56,  53,  82,  84,
		     32,  24,  13,   5,  -2,   4,  17,  17,
		     13,   9,  -3,  -7,  -7,  -8,   3,  -1,
		      4,   7,  -6,   1,   0,  -5,  -1,  -8,
		     13,   8,   8,  10,  13,   0,   2,  -7,
		      0,   0,   0,   0,   0,   0,   0,   0,
		};

		int[] mg_knight_table = {
		    -167, -89, -34, -49,  61, -97, -15, -107,
		     -73, -41,  72,  36,  23,  62,   7,  -17,
		     -47,  60,  37,  65,  84, 129,  73,   44,
		      -9,  17,  19,  53,  37,  69,  18,   22,
		     -13,   4,  16,  13,  28,  19,  21,   -8,
		     -23,  -9,  12,  10,  19,  17,  25,  -16,
		     -29, -53, -12,  -3,  -1,  18, -14,  -19,
		    -105, -21, -58, -33, -17, -28, -19,  -23,
		};

		int[] eg_knight_table = {
		    -58, -38, -13, -28, -31, -27, -63, -99,
		    -25,  -8, -25,  -2,  -9, -25, -24, -52,
		    -24, -20,  10,   9,  -1,  -9, -19, -41,
		    -17,   3,  22,  22,  22,  11,   8, -18,
		    -18,  -6,  16,  25,  16,  17,   4, -18,
		    -23,  -3,  -1,  15,  10,  -3, -20, -22,
		    -42, -20, -10,  -5,  -2, -20, -23, -44,
		    -29, -51, -23, -15, -22, -18, -50, -64,
		};

		int[] mg_bishop_table = {
		    -29,   4, -82, -37, -25, -42,   7,  -8,
		    -26,  16, -18, -13,  30,  59,  18, -47,
		    -16,  37,  43,  40,  35,  50,  37,  -2,
		     -4,   5,  19,  50,  37,  37,   7,  -2,
		     -6,  13,  13,  26,  34,  12,  10,   4,
		      0,  15,  15,  15,  14,  27,  18,  10,
		      4,  15,  16,   0,   7,  21,  33,   1,
		    -33,  -3, -14, -21, -13, -12, -39, -21,
		};

		int[] eg_bishop_table = {
		    -14, -21, -11,  -8, -7,  -9, -17, -24,
		     -8,  -4,   7, -12, -3, -13,  -4, -14,
		      2,  -8,   0,  -1, -2,   6,   0,   4,
		     -3,   9,  12,   9, 14,  10,   3,   2,
		     -6,   3,  13,  19,  7,  10,  -3,  -9,
		    -12,  -3,   8,  10, 13,   3,  -7, -15,
		    -14, -18,  -7,  -1,  4,  -9, -15, -27,
		    -23,  -9, -23,  -5, -9, -16,  -5, -17,
		};

		int[] mg_rook_table = {
		     32,  42,  32,  51, 63,  9,  31,  43,
		     27,  32,  58,  62, 80, 67,  26,  44,
		     -5,  19,  26,  36, 17, 45,  61,  16,
		    -24, -11,   7,  26, 24, 35,  -8, -20,
		    -36, -26, -12,  -1,  9, -7,   6, -23,
		    -45, -25, -16, -17,  3,  0,  -5, -33,
		    -44, -16, -20,  -9, -1, 11,  -6, -71,
		    -19, -13,   1,  17, 16,  7, -37, -26,
		};

		int[] eg_rook_table = {
		    13, 10, 18, 15, 12,  12,   8,   5,
		    11, 13, 13, 11, -3,   3,   8,   3,
		     7,  7,  7,  5,  4,  -3,  -5,  -3,
		     4,  3, 13,  1,  2,   1,  -1,   2,
		     3,  5,  8,  4, -5,  -6,  -8, -11,
		    -4,  0, -5, -1, -7, -12,  -8, -16,
		    -6, -6,  0,  2, -9,  -9, -11,  -3,
		    -9,  2,  3, -1, -5, -13,   4, -20,
		};

		int[] mg_queen_table = {
		    -28,   0,  29,  12,  59,  44,  43,  45,
		    -24, -39,  -5,   1, -16,  57,  28,  54,
		    -13, -17,   7,   8,  29,  56,  47,  57,
		    -27, -27, -16, -16,  -1,  17,  -2,   1,
		     -9, -26,  -9, -10,  -2,  -4,   3,  -3,
		    -14,   2, -11,  -2,  -5,   2,  14,   5,
		    -35,  -8,  11,   2,   8,  15,  -3,   1,
		     -1, -18,  -9,  10, -15, -25, -31, -50,
		};

		int[] eg_queen_table = {
		     -9,  22,  22,  27,  27,  19,  10,  20,
		    -17,  20,  32,  41,  58,  25,  30,   0,
		    -20,   6,   9,  49,  47,  35,  19,   9,
		      3,  22,  24,  45,  57,  40,  57,  36,
		    -18,  28,  19,  47,  31,  34,  39,  23,
		    -16, -27,  15,   6,   9,  17,  10,   5,
		    -22, -23, -30, -16, -16, -23, -36, -32,
		    -33, -28, -22, -43,  -5, -32, -20, -41,
		};

		int[] mg_king_table = {
		    -65,  23,  16, -15, -56, -34,   2,  13,
		     29,  -1, -20,  -7,  -8,  -4, -38, -29,
		     -9,  24,   2, -16, -20,   6,  22, -22,
		    -17, -20, -12, -27, -30, -25, -14, -36,
		    -49,  -1, -27, -39, -46, -44, -33, -51,
		    -14, -14, -22, -46, -44, -30, -15, -27,
		      1,   7,  -8, -64, -43, -16,   9,   8,
		    -15,  36,  12, -54,   8, -28,  24,  14,
		};

		int[] eg_king_table = {
		    -74, -35, -18, -18, -11,  15,   4, -17,
		    -12,  17,  14,  17,  17,  38,  23,  11,
		     10,  17,  23,  15,  20,  45,  44,  13,
		     -8,  22,  24,  27,  26,  33,  26,   3,
		    -18,  -4,  21,  24,  27,  23,   9, -11,
		    -19,  -3,  11,  21,  23,  16,   7,  -9,
		    -27, -11,   4,  13,  14,   4,  -5, -17,
		    -53, -34, -21, -11, -28, -14, -24, -43
		};
		
		int[][] mg_piece_table =
		{
		    mg_pawn_table,
		    mg_knight_table,
		    mg_bishop_table,
		    mg_rook_table,
		    mg_queen_table,
		    mg_king_table
		};

		int[][] eg_piece_table =
		{
		    eg_pawn_table,
		    eg_knight_table,
		    eg_bishop_table,
		    eg_rook_table,
		    eg_queen_table,
		    eg_king_table
		};
		
		int rating = 0;

		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++)
		{
			for (Piece p: board.getPiecesOfType(pieceEnum, col))
			{
				Location l = p.getLocation();
				int r = p.getColor().equals(Game.NEAR_COLOR) ? l.getRow() : 7 - l.getRow();
				int c = l.getCol();

				int pos = 8*r + c;
				
				if (isEndgame)
				{
					rating += eg_piece_table[p.getEnum()-1][pos];
				}
				else
				{
					rating += mg_piece_table[p.getEnum()-1][pos];
				}
			}
		}
		return rating;
	}
	
	/**
	 * Evaluation function for the current state of the board
	 * 
	 * @param board The board
	 * @return An integer describing the board, more negative favoring black and vice versa
	 *         (utilizes units of centipawns)
	 */
	public static EvaluationInfo evaluate(Board board)
	{
		int score = 0;
		
		// Material Balance
		int whiteMaterialMG = calculateMaterial(board, Color.WHITE, false);
		int blackMaterialMG = calculateMaterial(board, Color.BLACK, false);
		int whiteMaterialEG = calculateMaterial(board, Color.WHITE, true);
		int blackMaterialEG = calculateMaterial(board, Color.BLACK, true);
		
		int deltaMaterialMG = whiteMaterialMG - blackMaterialMG;
		int deltaMaterialEG = whiteMaterialEG - blackMaterialEG;
		
		// Position
		int whitePositionMG = calculatePiecePosition(board, Color.WHITE, false);
		int blackPositionMG = calculatePiecePosition(board, Color.BLACK, false);
		int whitePositionEG = calculatePiecePosition(board, Color.WHITE, true);
		int blackPositionEG = calculatePiecePosition(board, Color.BLACK, true);
		
		int deltaPositionMG = whitePositionMG - blackPositionMG;
		int deltaPositionEG = whitePositionEG - blackPositionEG;
		
		//Bishop Pair
		int whiteBishopPairMG = calculateBishopPair(board, Color.WHITE, false);
		int blackBishopPairMG = calculateBishopPair(board, Color.BLACK, false);
		int whiteBishopPairEG = calculateBishopPair(board, Color.WHITE, true);
		int blackBishopPairEG = calculateBishopPair(board, Color.BLACK, true);
		
		int deltaBishopPairMG = whiteBishopPairMG - blackBishopPairMG;
		int deltaBishopPairEG = whiteBishopPairEG - blackBishopPairEG;
		
		//Tapered Evaluation with material-position hybrid
		int deltaMatPosMG = deltaMaterialMG + deltaPositionMG + deltaBishopPairMG;
		int deltaMatPosEG = deltaMaterialEG + deltaPositionEG + deltaBishopPairEG;
		score += calculateTapered(board, deltaMatPosMG, deltaMatPosEG);	
		
	
		// [[More advanced evaluations (will slow down computing drastically)]]
		
		// Pawn Structure
		int whiteStructure = calculatePawnStructure(board, Color.WHITE);
		int blackStructure = calculatePawnStructure(board, Color.BLACK);
		int deltaStructure = whiteStructure - blackStructure;
		score += deltaStructure;
		
		// King Safety
		int whiteKingSafety = calculateKingSafety(board, Color.WHITE);
		int blackKingSafety = calculateKingSafety(board, Color.BLACK);
		int deltaSafety = whiteKingSafety - blackKingSafety;
		score += deltaSafety;
				
		// Mobility (deep, slows down computing by 3x) (+25 legal moves = 1 pawn (100 centipawns))
		int whiteMoves = board.allMoves(Color.WHITE).size();
		int blackMoves = board.allMoves(Color.BLACK).size();
		int deltaMoves = (whiteMoves - blackMoves)*4;
		score += deltaMoves;
		
		
		return new EvaluationInfo(score, true);
		
	}
	
	/**
	 * Checks if the current state is in endgame
	 * @param board The current board
	 * @return True if in endgame, false otherwise
	 */
	public static boolean isEndgame(Board board) {
		int whiteMaterial = calculateMaterialWithoutPawns(board, Color.WHITE);
		int blackMaterial = calculateMaterialWithoutPawns(board, Color.BLACK);
		return whiteMaterial / 100 <= 14 && blackMaterial / 100 <= 14;
	}
	
	/**
	 * Returns the smallest attacker of a location that's a certain color
	 * @param board The current board
	 * @param loc The location to check
	 * @param color The piece color
	 * @return The smallest attacker
	 */
	private static Piece getSmallestAttacker(Board board, Location loc, int color)
	{
		ArrayList<Piece> attackers = board.getAllAttackers(loc, color == 1 ? Color.WHITE : Color.BLACK);
		Piece smallest = null;
		for (Piece p: attackers)
		{
			if (smallest == null || p.getValue() < smallest.getValue())
			{
				smallest = p;
			}
		}
		return smallest;
	}
	
	/**
	 * Static Exchange Evaluation for better analyzing captures
	 * @param board The current board
	 * @param loc The location of battle
	 * @param color The starting color (to make first capture)
	 */
	public static int SEE(Board board, Move m)
	{
		int color = m.getPiece().getColor().equals(Color.WHITE) ? 1 : -1;
		Location square = m.getDestination();
		
		Move[] moves = new Move[32];
		int[] gain = new int[32];
		int d = 0;
		int onSquare = m.getPiece().getValue();
		
		gain[0] = m.getVictim().getValue();
		moves[0] = m;
		board.executeMove(m);
		
		color = -color;
		Piece attackDef = getSmallestAttacker(board, square, color);
		
		while (attackDef != null) {
			d++;
			gain[d] = onSquare - gain[d-1];
			
			//If the capture isn't favorable, prune
			if (Math.max(-gain[d-1], gain[d]) < 0) {
				d--;
				break;
			}
			
			//Make the move (record it for undoing later)
			moves[d] = new Move(attackDef, square);
			board.executeMove(moves[d]);
			
			color = -color;
			onSquare = attackDef.getValue();
			attackDef = getSmallestAttacker(board, square, color);
		}
		
		//Propagate the scores down to depth = 0
		for (int i=d;i>=1;i--) {
			//For every gain, the color can either take the piece (gain[i]) or leave it (-gain[i-1])
			//Thus the opposite color gains (- max of these two values, b/c opponent always pick best)
			gain[i-1] = -Math.max(-gain[i-1], gain[i]);
		}

		//Undo the moves
		for (int i=d;i>=0;i--) {
			board.undoMove(moves[i]);
		}
		
		return gain[0];
	}
}
