import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Player that chooses a random valid move each time
 * 
 * @author Victor Gong
 * @version 3/29/2023
 *
 */
public class RandomPlayer extends Player
{
	public RandomPlayer(Board board, String name, Color color)
	{
		super(board, name, color);
	}

	/**
	 * Gets the next move by selecting a random one
	 * 
	 * @return The next move
	 */
	public Move nextMove()
	{
		Board board = getBoard();
		ArrayList<Move> moves = board.allMoves(getColor());
		return moves.get((int) (Math.random() * moves.size()));
	}

}
