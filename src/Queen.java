import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the queen in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Queen extends Piece
{

	public static final int ENUM = 5;
	
	public Queen(Color col, String fileName)
	{
		super(col, fileName, 900, ENUM);
	}

	/**
	 * Returns a list of locations that the piece can move to
	 * 
	 * @return The list of possible move locations
	 */
	public ArrayList<Location> destinations()
	{
		ArrayList<Location> locs = new ArrayList<>();
		sweep(locs, Location.EAST);
		sweep(locs, Location.WEST);
		sweep(locs, Location.NORTH);
		sweep(locs, Location.SOUTH);
		sweep(locs, Location.SOUTHEAST);
		sweep(locs, Location.SOUTHWEST);
		sweep(locs, Location.NORTHEAST);
		sweep(locs, Location.NORTHWEST);
		return locs;
	}

}
