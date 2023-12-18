import java.awt.Color;

/**
 * A class representing the Player and enabling interaction with the game
 * 
 * @author Victor Gong
 * @version 3/29/2023
 *
 */
public abstract class Player
{
	private Board board;
	private String name;
	private Color color;

	public Player(Board board, String name, Color color)
	{
		this.board = board;
		this.name = name;
		this.color = color;
	}

	/**
	 * Returns the next move of this Player
	 * 
	 * @return The next move
	 */
	public abstract Move nextMove();

	/**
	 * Getter method for current board
	 * 
	 * @return Current board
	 */
	public Board getBoard()
	{
		return board;
	}

	/**
	 * Getter method for player name
	 * 
	 * @return Player name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Getter method for player color
	 * 
	 * @return Player color
	 * @return
	 */
	public Color getColor()
	{
		return color;
	}
}
