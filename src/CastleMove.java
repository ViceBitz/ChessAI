import java.awt.Color;

public class CastleMove extends Move
{	
	private Rook rookPiece;
	private Location rookSource;
	private Location rookDestination;
	private boolean rookMovedBefore;
	private int type;
	public CastleMove(King king, Location destination, Rook rook, int type) {
		super(king, destination);
		this.type = type;
		this.rookPiece = rook;
		this.rookSource = rookPiece.getLocation();
		this.rookDestination = new Location(king.getLocation().getRow(),king.getLocation().getCol()+(type == 1 ? 1 : -1));
		this.rookMovedBefore = rookPiece.getMoved();
		
	}
	
	// Returns the king involved in castle
	public King getKing()
	{
		return (King) getPiece();
	}

	// Returns the rook involved in castle
	public Rook getRook()
	{
		return rookPiece;
	}
	
	// Returns the original location of the rook
	public Location getRookSource()
	{
		return rookSource;
	}
	
	// Returns the destination of the rook
	public Location getRookDestination()
	{
		return rookDestination;
	}
	
	// Returns if the rook had previously moved before
	public boolean getRookMovedBefore()
	{
		return rookMovedBefore;
	}
	
	//Returns the type of castle
	public int getType()
	{
		return type;
	}
	
	// Returns a string description of the move
	public String toString()
	{
		return (getKing().getColor().equals(Color.WHITE) ? "White" : "Black") + " king castles " + (type == 1 ? "short" : "long") + ".";
	}
	
	// Returns the standard notation of the chess move
	public String toStandardNotation()
	{
		if (type == 1)
		{
			return "O-O";
		}
		else
		{
			return "O-O-O";
		}
	}
}
