import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A class that handles the chess game by utilizing other classes and methods in the project
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Game
{
	/**
	 * Simulates one turn in the game of chess for a specific player
	 * 
	 * @param board   The current active board
	 * @param display The current active display
	 * @param player  The player to execute next turn
	 * 
	 * @return True if next turn successful, false if resign/failed
	 */
	
	public static ArrayList<Move> moveLog = new ArrayList<>();
	public static String gameTitle = "Unnamed";
	
	public static final Color NEAR_COLOR = Color.BLACK;
	public static final boolean LOOP_GAME = true;
	
	private static boolean nextTurn(Board board, BoardDisplay display, Player player)
	{
		display.setTitle(player.getName());
		Move next = player.nextMove();
		if (next == null) {
			return false;
		}
		board.executeMove(next);
		display.clearColors();
		display.setColor(next.getSource(), Color.YELLOW);
		display.setColor(next.getDestination(), Color.YELLOW);
		try
		{
			Thread.sleep(0); //Set to 0 for instant moves
		}
		catch (InterruptedException e)
		{
		}
		moveLog.add(next);
		return true;
	}

	/**
	 * Simulates the full game of chess
	 * 
	 * @param board   The current active board
	 * @param display The current active display
	 * @param white   The white player
	 * @param black   The black player
	 */
	public static void play(Board board, BoardDisplay display, Player white, Player black)
	{
		King whiteKing = board.getKing(Color.WHITE);
		King blackKing = board.getKing(Color.BLACK);
		while (true)
		{
			boolean success;
			success = nextTurn(board, display, white);
			if (!success) {
				System.out.println("Game Over. Black Wins through White's Resignation.");
				return;
			}
			if (blackKing.inCheckmate())
			{
				System.out.println("Game Over. White Wins.");
				return;
			}
			success = nextTurn(board, display, black);
			if (!success) {
				System.out.println("Game Over. White Wins through Black's Resignation.");
				return;
			}
			if (whiteKing.inCheckmate())
			{
				System.out.println("Game Over. Black Wins.");
				return;
			}
		}
	}

	/**
	 * Sets up the chess board with the correct pieces in their positions (Black faces away)
	 * 
	 * @param board The current active board
	 */
	public static void setupBoard(Board board)
	{
		
		int whiteBackRank = NEAR_COLOR.equals(Color.WHITE) ? 7 : 0;
		int whitePawnRow = NEAR_COLOR.equals(Color.WHITE) ? 6 : 1;
		int kingColumn = NEAR_COLOR.equals(Color.WHITE) ? 4 : 3;
		// Kings
		Piece blackKing = new King(Color.BLACK, "black_king.gif");
		blackKing.putSelfInGrid(board, new Location(7-whiteBackRank, kingColumn));

		Piece whiteKing = new King(Color.WHITE, "white_king.gif");
		whiteKing.putSelfInGrid(board, new Location(whiteBackRank, kingColumn));

		// Queens
		Piece blackQueen = new Queen(Color.BLACK, "black_queen.gif");
		blackQueen.putSelfInGrid(board, new Location(7-whiteBackRank, 7-kingColumn));

		Piece whiteQueen = new Queen(Color.WHITE, "white_queen.gif");
		whiteQueen.putSelfInGrid(board, new Location(whiteBackRank, 7-kingColumn));

		// Rooks
		Piece blackRook1 = new Rook(Color.BLACK, "black_rook.gif");
		blackRook1.putSelfInGrid(board, new Location(7-whiteBackRank, 0));

		Piece blackRook2 = new Rook(Color.BLACK, "black_rook.gif");
		blackRook2.putSelfInGrid(board, new Location(7-whiteBackRank, 7));

		Piece whiteRook1 = new Rook(Color.WHITE, "white_rook.gif");
		whiteRook1.putSelfInGrid(board, new Location(whiteBackRank, 0));

		Piece whiteRook2 = new Rook(Color.WHITE, "white_rook.gif");
		whiteRook2.putSelfInGrid(board, new Location(whiteBackRank, 7));

		// Bishops
		Piece blackBishop1 = new Bishop(Color.BLACK, "black_bishop.gif");
		blackBishop1.putSelfInGrid(board, new Location(7-whiteBackRank, 2));

		Piece blackBishop2 = new Bishop(Color.BLACK, "black_bishop.gif");
		blackBishop2.putSelfInGrid(board, new Location(7-whiteBackRank, 5));

		Piece whiteBishop1 = new Bishop(Color.WHITE, "white_bishop.gif");
		whiteBishop1.putSelfInGrid(board, new Location(whiteBackRank, 2));

		Piece whiteBishop2 = new Bishop(Color.WHITE, "white_bishop.gif");
		whiteBishop2.putSelfInGrid(board, new Location(whiteBackRank, 5));

		// Knights
		Piece blackKnight1 = new Knight(Color.BLACK, "black_knight.gif");
		blackKnight1.putSelfInGrid(board, new Location(7-whiteBackRank, 1));

		Piece blackKnight2 = new Knight(Color.BLACK, "black_knight.gif");
		blackKnight2.putSelfInGrid(board, new Location(7-whiteBackRank, 6));

		Piece whiteKnight1 = new Knight(Color.WHITE, "white_knight.gif");
		whiteKnight1.putSelfInGrid(board, new Location(whiteBackRank, 1));

		Piece whiteKnight2 = new Knight(Color.WHITE, "white_knight.gif");
		whiteKnight2.putSelfInGrid(board, new Location(whiteBackRank, 6));
		
		// Pawns
		for (int i = 0; i < board.getNumCols(); i++)
		{
			Piece pawn = new Pawn(Color.BLACK, "black_pawn.gif");
			pawn.putSelfInGrid(board, new Location(7-whitePawnRow, i));
		}
		for (int i = 0; i < board.getNumCols(); i++)
		{
			Piece pawn = new Pawn(Color.WHITE, "white_pawn.gif");
			pawn.putSelfInGrid(board, new Location(whitePawnRow, i));
		}

	}

	public static void main(String args[]) throws IOException, InterruptedException
	{
		//Load state table for Smart Player algorithms
		//Compression.clearFile(); //Uncomment if clear file
		Compression.setup();

		//Load shut down hook
		Runtime.getRuntime().addShutdownHook(new CompressionShutdownHook());
		
		do {
			//Load game 
			Board board = new Board();
			setupBoard(board);
			
			BoardDisplay display = new BoardDisplay(board);
			/**
			 * Smart Player constructor
			 * 1. No extra args (defaults to iterativeDeepening, playTime=6000)
			 * 2. playDepth, iterativeDeepening?, playTime
			 */
			SmartPlayerNegamax other = new SmartPlayerNegamax(board, "Walter Nernst", Color.BLACK, 2);
			SmartPlayerNegamax other2 = new SmartPlayerNegamax(board, "Walter White", Color.WHITE, 2);

			//Print bot details
			other.printAIDetails();
			
			//Game Configurations
			boolean botWhite = true;
			gameTitle = "Ultimate";
			
			//Run main play method
			HumanPlayer me = new HumanPlayer(display, board, "Me", botWhite ? Color.BLACK : Color.WHITE);
			if (botWhite) {
				play(board, display, other2, me);
			}
			else {
				play(board, display, me, other);
			}
			
			//Once game over, print move log
			System.out.println("===== MOVE LOG =====");
			for (int i=0;i<moveLog.size();i+=2) {
				System.out.print((i/2+1)+". " + moveLog.get(i).toStandardNotation() + " "
						+ (i+1 < moveLog.size() ? moveLog.get(i+1).toStandardNotation() : "")
						+ " | ");
			}
		}
		while (LOOP_GAME);
	}
}
