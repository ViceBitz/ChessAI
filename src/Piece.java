import java.awt.Color;
import java.util.ArrayList;
import java.math.*;

/**
 * A abstract parent class that contains all methods of a chess piece, including moving, capturing..etc
 * @author Victor Gong
 * @version 3/28/2023
 *
 */
public abstract class Piece
{
	//the board this piece is on
	private Board board;

	//the location of this piece on the board
	private Location location;

	//the color of the piece
	private Color color;

	//the file used to display this piece
	private String imageFileName;

	//the approximate value of this piece in a game of chess
	private int value;
	
	//boolean representing if the piece has been moved
	private boolean moved;
	
	//integer representing the type of piece (1-6)
	private int pieceEnum;
	
	//constructs a new Piece with the given attributes.
	public Piece(Color col, String fileName, int val, int pieceEnum)
	{
		color = col;
		imageFileName = fileName;
		value = val;
		moved = false;
		this.pieceEnum = pieceEnum;
	}

	//returns the board this piece is on
	public Board getBoard()
	{
		return board;
	}

	//returns the location of this piece on the board
	public Location getLocation()
	{
		return location;
	}

	//returns the color of this piece
	public Color getColor()
	{
		return color;
	}

	//returns the name of the file used to display this piece
	public String getImageFileName()
	{
		return imageFileName;
	}

	//returns a number representing the relative value of this piece
	public int getValue()
	{
		return value;
	}
	
	//returns if the piece has moved
	public boolean getMoved() {
		return moved;
	}
	
	//returns the piece enum of the piece
	public int getEnum()
	{
		return pieceEnum;
	}
	
	/**
	 * Sets 'moved' of the piece
	 * @param status The status to change 'moved' to
	 */
	public void setMoved(boolean status) {
		moved = status;
	}
	
    /**
     * Puts this piece into a board. If there is another piece at the given
     * location, it is removed. <br />
     * Precondition: (1) This piece is not contained in a grid (2)
     * <code>loc</code> is valid in <code>gr</code>
     * @param brd the board into which this piece should be placed
     * @param loc the location into which the piece should be placed
     */
    public void putSelfInGrid(Board brd, Location loc)
    {
        if (board != null)
            throw new IllegalStateException(
                    "This piece is already contained in a board.");

        Piece piece = brd.get(loc);
        if (piece != null)
            piece.removeSelfFromGrid();
        brd.put(loc, this);
        board = brd;
        location = loc;
        
        //Add to pieces list
        board.addPiece(this);
    }

    /**
     * Removes this piece from its board. <br />
     * Precondition: This piece is contained in a board
     */
    public void removeSelfFromGrid()
    {
        if (board == null)
            throw new IllegalStateException(
                    "This piece is not contained in a board.");
        if (board.get(location) != this)
            throw new IllegalStateException(
                    "The board contains a different piece at location "
                            + location + ".");


        //Remove from pieces list
        board.removePiece(this);
        
        board.remove(location);
        board = null;
        location = null;
        
        
    }

    /**
     * Moves this piece to a new location. If there is another piece at the
     * given location, it is removed. <br />
     * Precondition: (1) This piece is contained in a grid (2)
     * <code>newLocation</code> is valid in the grid of this piece
     * @param newLocation the new location
     */
    public void moveTo(Location newLocation)
    {
        if (board == null)
            throw new IllegalStateException("This piece is not on a board.");
        if (board.get(location) != this)
            throw new IllegalStateException(
                    "The board contains a different piece at location "
                            + location + ".");
        if (!board.isValid(newLocation))
            throw new IllegalArgumentException("Location " + newLocation
                    + " is not valid.");

        if (newLocation.equals(location))
            return;
        board.remove(location);
        Piece other = board.get(newLocation);
        if (other != null)
            other.removeSelfFromGrid();
        location = newLocation;
        board.put(location, this);
    }
    
    /**
     * Determines if a location on the board is valid if it's either empty or occupied by a piece of a different color
     * @param dest The location to check
     * @return True if valid, false otherwise
     */
    public boolean isValidDestination(Location dest) {
    	return (board.isValid(dest) && (board.get(dest) == null || !board.get(dest).getColor().equals(color)));
    }
    
    /**
     * Determines if a valid location on the board is occupied by an opposing piece
     * @precondition Location is valid
     * @param dest The location to check
     * @return True if occupied, false otherwise
     */
    public boolean isEnemyOccupied(Location dest) {
    	return (board.get(dest) != null && !board.get(dest).getColor().equals(color));
    }
    /**
     * An abstract method to get all possible move locations of this piece
     * @return The list of all locations
     */
    public abstract ArrayList<Location> destinations();
    
    /**
     * A method that sweeps a certain direction and adds all locations, including ones occupied by opposing pieces, to 'dest'
     * 
     * @param dests The list of locations to add to
     * @param direction The compass direction to sweep
     */
    public void sweep(ArrayList<Location> dests, int direction) { 
    	Location cur = location.getAdjacentLocation(direction);
    	while (isValidDestination(cur)) {
    		dests.add(cur);
    		//Check if took a piece, then stop sweep
    		if (isEnemyOccupied(cur)) {
    			break;
    		}
    		cur = cur.getAdjacentLocation(direction);
    		
    	}
    }
    
    /**
     * Checks if this piece is equal to another
     */
    public boolean equals(Object other)
    {
    	if (other == null)
    	{
    		return false;
    	}
    	if (!(other instanceof Piece))
    	{
    		return false;
    	}
    	Piece p = (Piece) other;
    	return p.getEnum() == pieceEnum && p.getColor().equals(color) && p.getLocation().equals(location);
    }
    
    /**
     * Returns the hashcode of this piece
     */
    public int hashCode()
    {
    	return pieceEnum*3737 + color.hashCode()*13 + location.hashCode();
    }
    
    /**
     * Returns the standard notation of this piece 
     * @return The standard notation
     */
    public String toStandardNotation()
    {
    	if (this instanceof Knight)
    	{
    		return "N";
    	}
    	else if (this instanceof Bishop)
    	{
    		return "B";
    	}
    	else if (this instanceof Rook)
    	{
    		return "R";
    	}
    	else if (this instanceof Queen)
    	{
    		return "Q";
    	}
    	else if (this instanceof King)
    	{
    		return "K";
    	}
    	return "";
    }
}