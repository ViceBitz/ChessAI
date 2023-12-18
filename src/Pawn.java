import java.awt.Color;
import java.util.ArrayList;

/**
 * A subclass of Piece that represents the pawn in chess
 * 
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public class Pawn extends Piece
{

	public static final int ENUM = 1;
	
	public Pawn(Color col, String fileName)
	{
		super(col, fileName, 100, ENUM);
	}

	/**
	 * Returns a list of locations that the piece can move to
	 * 
	 * @return The list of possible move locations
	 */
	public ArrayList<Location> destinations()
	{
		ArrayList<Location> locs = new ArrayList<>();
		int dir = getColor().equals(Game.NEAR_COLOR) ? -1 : 1;
		
		Location cur = getLocation();
		Location up1 = new Location(cur.getRow() + dir, cur.getCol());
		Location up2 = new Location(cur.getRow() + dir * 2, cur.getCol());
		Location diagL = new Location(up1.getRow(), up1.getCol() - 1);
		Location diagR = new Location(up1.getRow(), up1.getCol() + 1);
		if (isValidDestination(up1) && !isEnemyOccupied(up1))
		{
			locs.add(up1);
			if (dir == -1 && cur.getRow() == 6 || dir == 1 && cur.getRow() == 1)
			{
				if (isValidDestination(up2) && !isEnemyOccupied(up2))
				{
					locs.add(up2);
				}
			}
		}
		
		if (isValidDestination(diagL) && isEnemyOccupied(diagL))
		{
			locs.add(diagL);
		}
		if (isValidDestination(diagR) && isEnemyOccupied(diagR))
		{
			locs.add(diagR);
		}
		return locs;
	}

}
