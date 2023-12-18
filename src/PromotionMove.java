import java.awt.Color;

public class PromotionMove extends Move
{
	private Piece upgradePiece;

	// Constructs a new move for moving the given piece to the given destination.
	public PromotionMove(Piece piece, Location destination)
	{
		super(piece, destination);
		String fileName = piece.getColor().equals(Color.WHITE) ? "white_queen.gif" : "black_queen.gif";
		this.upgradePiece = new Queen(piece.getColor(),fileName);
	}

	// Returns the piece being moved
	public Piece getUpgradePiece()
	{
		return upgradePiece;
	}

	// Returns a string description of the move
	public String toString()
	{
		return super.toString() + " || " + getPiece() + " promotes to " + upgradePiece;
	}
	
	// Returns the standard notation of the chess move
	public String toStandardNotation()
	{
		return super.toStandardNotation() + "=" + upgradePiece.toStandardNotation();
	}
}
