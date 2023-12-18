import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the knight in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Knight extends Piece
{

	public static final int ENUM = 2;
	
	
	public Knight(Color col, String fileName)
	{
		super(col, fileName, 320, ENUM);
	}

	/**
	 * Returns a list of locations that the piece can move to
	 * 
	 * @return The list of possible move locations
	 */
	public ArrayList<Location> destinations()
	{
		ArrayList<Location> locs = new ArrayList<>();
		int[] dr = { -2, -1, 1, 2, -2, -1, 1, 2 };
		int[] dc = { -1, -2, -2, -1, 1, 2, 2, 1 };
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

}
