import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the rook in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Rook extends Piece
{
	
	public static final int ENUM = 4;
	
	public Rook(Color col, String fileName)
	{
		super(col, fileName, 500, ENUM);
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
		return locs;
	}
}
