import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the king in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class King extends Piece
{

	public static final int ENUM = 6;
	
	public King(Color col, String fileName)
	{
		super(col, fileName, 10000, ENUM);
	}

	/**
	 * Returns a list of locations that the piece can move to
	 * 
	 * @return The list of possible move locations
	 */
	public ArrayList<Location> destinations()
	{
		ArrayList<Location> locs = new ArrayList<>();
		int[] dr = { -1, -1, -1, 1, 1, 1, 0, 0 };
		int[] dc = { 0, 1, -1, 0, 1, -1, 1, -1 };
		Location cur = getLocation();
		for (int i = 0; i < 8; i++)
		{
			Location newLoc = new Location(cur.getRow() + dr[i], cur.getCol() + dc[i]);
			if (isValidDestination(newLoc))
			{
				locs.add(newLoc);
			}
		}
		return locs;
	}

	/**
	 * Evaluates if the king is in check or not
	 * 
	 * @return True if in check, false otherwise
	 */
	public boolean inCheck()
	{
		Board board = getBoard();
		if (board == null) {
			System.out.println(this.getLocation());
		}
		return board.isAttacked(getLocation(), Board.oppositeColor(getColor()));
	}
	
	/**
	 * Evaluates if the king is in check by a specific piece
	 * 
	 * @param piece The specific piece
	 * @return True if in check, false otherwise
	 */
	public boolean inCheckBy(Piece piece)
	{
		Board board = getBoard();
		if (board == null) {
			System.out.println(this.getLocation());
		}
		return board.isAttackedBy(getLocation(), piece);
	}

	/**
	 * Evaluates if the king is in checkmate
	 * 
	 * @return True if in checkmate, false otherwise
	 */
	public boolean inCheckmate()
	{
		Board board = getBoard();
		ArrayList<Move> moves = board.allMoves(getColor());
		for (Move m : moves)
		{
			board.executeMove(m);
			if (!inCheck())
			{
				board.undoMove(m);
				return false;
			}
			board.undoMove(m);
		}
		return inCheck();
	}
	
	/**
	 * Checks if the king is in stalemate (basically same as checkmate except not in check)
	 * @return True if in stalemate, false otherwise
	 */
	public boolean inStalemate()
	{
		Board board = getBoard();
		ArrayList<Move> moves = board.allMoves(getColor());
		for (Move m : moves)
		{
			board.executeMove(m);
			if (!inCheck())
			{
				board.undoMove(m);
				return false;
			}
			board.undoMove(m);
		}
		return !inCheck();
	}
}
