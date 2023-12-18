import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Player that chooses a move based on user input
 * 
 * @author Victor Gong
 * @version 3/29/2023
 *
 */
public class HumanPlayer extends Player
{
	BoardDisplay display;

	public HumanPlayer(BoardDisplay display, Board board, String name, Color color)
	{
		super(board, name, color);
		this.display = display;
	}

	/**
	 * Gets the next move by selecting a random one
	 * 
	 * @return The next move
	 */
	public Move nextMove()
	{
		Move cur = display.selectMove();
		Board board = getBoard();
		ArrayList<Move> valid = board.allMoves(getColor());
		King king = getBoard().getKing(getColor());

		while (!valid.contains(cur) || (!getBoard().escapesCheck(cur)))
		{
			cur = display.selectMove();
		}
		return cur;
	}

}
