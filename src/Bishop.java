import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the bishop in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Bishop extends Piece
{

	public static final int ENUM = 3;
	
	public Bishop(Color col, String fileName)
	{
		super(col, fileName, 330, ENUM);
	}

	/**
	 * Returns a list of locations that the piece can move to
	 * 
	 * @return The list of possible move locations
	 */
	public ArrayList<Location> destinations()
	{
		ArrayList<Location> locs = new ArrayList<>();
		sweep(locs, Location.SOUTHEAST);
		sweep(locs, Location.SOUTHWEST);
		sweep(locs, Location.NORTHEAST);
		sweep(locs, Location.NORTHWEST);
		return locs;
	}

}
