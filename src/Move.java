import java.awt.Color;
import java.util.Arrays;

// 

/**
 * Represents a single move, in which a piece moves to a destination location. Since a move can be
 * undone, also keeps track of the source location and any captured victim.
 * 
 * @author Victor Gong
 * @version 3/29/2023
 *
 */
public class Move
{
	private Piece piece; // the piece being moved
	private Location source; // the location being moved from
	private Location destination; // the location being moved to
	private boolean movedBefore; // the previous 'moved' status of the piece
	private Piece victim; // any captured piece at the destination

	// Constructs a new move for moving the given piece to the given destination.
	public Move(Piece piece, Location destination)
	{
		this.piece = piece;
		this.source = piece.getLocation();
		this.destination = destination;
		this.victim = piece.getBoard().get(destination);
		this.movedBefore = piece.getMoved();
		
		if (source.equals(destination))
			throw new IllegalArgumentException("Both source and dest are " + source);
	}

	// Returns the piece being moved
	public Piece getPiece()
	{
		return piece;
	}

	// Returns the location being moved from
	public Location getSource()
	{
		return source;
	}

	// Returns the location being moved to
	public Location getDestination()
	{
		return destination;
	}
	
	//Returns if the piece had previously moved before
	public boolean getMovedBefore()
	{
		return movedBefore;
	}
	
	// Returns the piece being captured at the destination, if any
	public Piece getVictim()
	{
		return victim;
	}

	// Returns a string description of the move
	public String toString()
	{
		return piece + " from " + source + " to " + destination + " containing " + victim;
	}

	// Returns the standard notation of the chess move
	public String toStandardNotation()
	{
		String fromCoord = "("+source.toStandardNotation()+")";
		String toCoord = destination.toStandardNotation();
		
		//Capture
		if (victim != null)
		{
			return fromCoord + piece.toStandardNotation() + "x" + toCoord;
		}
		//Movement
		else
		{
			return fromCoord + piece.toStandardNotation() + toCoord;
		}
	}
	
	// Returns true if this move is equivalent to the given one.
	public boolean equals(Object x)
	{
		if (x == null) {
			return false;
		}
		Move other = (Move) x;
		return piece == other.getPiece() && source.equals(other.getSource()) && destination.equals(
				other.getDestination()) && victim == other.getVictim();
	}

	// Returns a hash code for this move, such that equivalent moves have the same hash code.
	public int hashCode()
	{
		return piece.hashCode() + source.hashCode() + destination.hashCode();
	}
}