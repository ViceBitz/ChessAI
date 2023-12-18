import java.awt.*;
import java.util.*;

// Represesents a rectangular game board, containing Piece objects.
public class Board extends BoundedGrid<Piece>
{
	private ArrayList<Piece>[][] pieces;
	
	// Constructs a new Board with the given dimensions
	public Board()
	{
		super(8, 8);
		pieces = new ArrayList[2][7]; //0 - White, 1 - Black; Pawn through King, index 1-6 (index 0 left empty)
		for (int c=0;c<2;c++) {
			for (int i=1;i<=6;i++) {
				pieces[c][i] = new ArrayList<>();
			}
		}
		
	}

	// Precondition: move has already been made on the board
	// Postcondition: piece has moved back to its source,
	// and any captured piece is returned to its location
	public void undoMove(Move move)
	{
		Piece piece = move.getPiece();
		Location source = move.getSource();
		Location dest = move.getDestination();
		Piece victim = move.getVictim();
		
		//Promotion
		if (move instanceof PromotionMove) {
			move.getPiece().putSelfInGrid(((PromotionMove) move).getUpgradePiece().getBoard(), source);
			((PromotionMove) move).getUpgradePiece().removeSelfFromGrid();
			
		}
				
		//General movement
		piece.moveTo(source);

		if (victim != null)
			victim.putSelfInGrid(piece.getBoard(), dest);
		
		piece.setMoved(move.getMovedBefore());
		
		//Castle movement
		if (move instanceof CastleMove) {
			Location rookSource = ((CastleMove) move).getRookSource();
			((CastleMove) move).getRook().moveTo(rookSource);
			((CastleMove) move).getRook().setMoved(move.getMovedBefore());
		}
		
		
	}
	
	/**
	 * Adds a piece to the piece array of the board
	 * @param p The added piece
	 */
	public void addPiece(Piece p)
	{
		pieces[p.getColor().equals(Color.WHITE) ? 0 : 1][p.getEnum()].add(p);
	}
	
	/**
	 * Removes the piece from the piece array of the board
	 * @param p The removed piece
	 */
	public void removePiece(Piece p)
	{
		pieces[p.getColor().equals(Color.WHITE) ? 0 : 1][p.getEnum()].remove(p);
	}
	
	/**
	 * Retrieves the list of pieces of a certain pieceEnum and color
	 * 
	 * @param pieceEnum The enum of the piece
	 * @param color The color of the piece
	 * @return An ArrayList of pieces with enum pieceEnum and color 'color'
	 */
	public ArrayList<Piece> getPiecesOfType(int pieceEnum, Color color)
	{
		return pieces[color.equals(Color.WHITE) ? 0 : 1][pieceEnum];
	}
	
	/**
	 * Retrieves the king of a certain color
	 * @param color The color of the king
	 * @return The king
	 */
	public King getKing(Color color)
	{
		return (King)getPiecesOfType(King.ENUM, color).get(0);
	}
	
	/**
	 * Retrieves all attackers of a certain color of a location
	 * @param loc The location to check
	 * @param color The piece color of attackers to find
	 * @return A list of all attackers of the square
	 */
	public ArrayList<Piece> getAllAttackers(Location loc, Color color)
	{
		ArrayList<Piece> attackers = new ArrayList<>();
		
		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++)
		{
			for (Piece p: pieces[color.equals(Color.WHITE) ? 0 : 1][pieceEnum])
			{
				ArrayList<Location> dests = p.destinations();
				for (Location d : dests)
				{
					if (p instanceof Pawn && d.getCol() == p.getLocation().getCol()) {
						continue;
					}
					if (d.equals(loc)) {
						attackers.add(p);
						break;
					}
				}
			}
		}
		return attackers;
	}
	
	/**
	 * Checks if a certain location is attacked by any pieces of a color
	 * 
	 * @param loc   The location to check
	 * @param color The piece color
	 * @return True if attacked, false otherwise
	 */
	public boolean isAttacked(Location loc, Color color)
	{
		return getAllAttackers(loc, color).size() > 0;
	}
	
	/**
	 * Checks if a certain location is attacked by a specific piece
	 * 
	 * @param loc   The location to check
	 * @param piece The specific piece
	 * @return True if attacked, false otherwise
	 */
	public boolean isAttackedBy(Location loc, Piece piece)
	{
		ArrayList<Location> dests = piece.destinations();
		for (Location d : dests)
		{
			if (piece instanceof Pawn && d.getCol() == piece.getLocation().getCol()) {
				continue;
			}
			if (d.equals(loc)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieves all locations that pieces of a certain color attack
	 * 
	 */
	public HashSet<Location> allAttackedSquares(Color color)
	{
		HashSet<Location> attacked = new HashSet<>();
		
		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++)
		{
			for (Piece p: pieces[color.equals(Color.WHITE) ? 0 : 1][pieceEnum])
			{
				ArrayList<Location> dests = p.destinations();
				for (Location d : dests)
				{
					if (p instanceof Pawn && d.getCol() == p.getLocation().getCol()) {
						continue;
					}
					attacked.add(d);
				}
			}
		}
		return attacked;
	}
	
	/**
	 * Checks if a move escapes check by opposite
	 * @param move The escaping move
	 * @return True if evades check, false otherwise
	 */
	public boolean escapesCheck(Move move) {
		Color col = move.getPiece().getColor();
		King king = getKing(col);
		executeMove(move);
		boolean ret = !king.inCheck();
		undoMove(move);
		return ret;
	}
	
	/**
	 * Checks if a specific move delivers check to the enemy king
	 * @param move The move
	 * @return True if delivers check, false otherwise
	 */
	public boolean deliversCheck(Move move)
	{
		Color col = move.getPiece().getColor();
		King king = getKing(oppositeColor(col));
		
		/*
		 * Checking piece is usually the move piece
		 * (unless castle -> rook, or promotion -> upgrade piece)
		 */
		Piece checkingPiece = move.getPiece();
		if (move instanceof CastleMove) {
			checkingPiece = ((CastleMove) move).getRook();
		}
		else if (move instanceof PromotionMove) {
			checkingPiece = ((PromotionMove) move).getUpgradePiece();
		}
		
		executeMove(move);
		boolean ret = king.inCheckBy(checkingPiece);
		undoMove(move);
		return ret;
	}
	
	/**
	 * Returns the opposite color (White to Black, Black to White)
	 * 
	 * @param col The original color
	 * @return The opposite color
	 */
	public static Color oppositeColor(Color col)
	{
		Color opposite = null;
		if (col.equals(Color.WHITE))
		{
			opposite = Color.BLACK;
		}
		else
		{
			opposite = Color.WHITE;
		}
		return opposite;
	}
	
	/**
	 * Helper to check validity and add castle moves to list of possible moves
	 * @param possibleMoves The list of possible moves
	 * @param king The king involved
	 */
	private void addCastleMoves(ArrayList<Move> possibleMoves, King king) {
		if (!king.inCheck() && !king.getMoved()) {
			ArrayList<Location> shortSide = new ArrayList<>();
			ArrayList<Location> longSide = new ArrayList<>();
			Location kingLocation = king.getLocation();
			king.sweep(shortSide, Location.EAST);
			king.sweep(longSide, Location.WEST);
			Location shortEmpty = new Location(kingLocation.getRow(),7-1);
			Location longEmpty = new Location(kingLocation.getRow(),0+1);
			Piece shortRook = get(new Location(kingLocation.getRow(),7));
			Piece longRook = get(new Location(kingLocation.getRow(),0));
			boolean shortSafe = !isAttacked(new Location(kingLocation.getRow(),kingLocation.getCol()+1),oppositeColor(king.getColor()));
			boolean longSafe = !isAttacked(new Location(kingLocation.getRow(),kingLocation.getCol()-1),oppositeColor(king.getColor()));
			if (shortRook != null && shortRook.getColor().equals(king.getColor()) && shortRook instanceof Rook && !shortRook.getMoved() && shortSide.contains(shortEmpty) && get(shortEmpty) == null && shortSafe) {
				CastleMove castle = new CastleMove(king, new Location(kingLocation.getRow(),kingLocation.getCol()+2),(Rook)shortRook,1);
				if (escapesCheck(castle)) {
					possibleMoves.add(castle);
				}
			}
			if (longRook != null && longRook.getColor().equals(king.getColor()) && longRook instanceof Rook && !longRook.getMoved() && longSide.contains(longEmpty) && get(longEmpty) == null && longSafe) {
				CastleMove castle = new CastleMove(king, new Location(kingLocation.getRow(),kingLocation.getCol()-2),(Rook)longRook,2);
				if (escapesCheck(castle)) {
					possibleMoves.add(castle);
				}
			}
		}
	}
	
	/**
	 * Checks the castling rights of a color (doesn't check if CAN castle, just if it's still possible)
	 * (neither king nor rook moved + rooks not captured + correct spots)
	 * 
	 * @param color The color to check
	 * @return 0 if no castling rights, 1 if short castle, 2 if long castle, 3 if both
	 */
	public int getCastlingRights(Color color)
	{
		King king = getKing(color);
		if (king.getMoved()) {
			return 0;
		}
		Piece shortRook = get(new Location(king.getLocation().getRow(),7));
		Piece longRook = get(new Location(king.getLocation().getRow(),0)); 
		boolean canCastleShort = shortRook != null && shortRook.getColor().equals(color) && shortRook instanceof Rook && !shortRook.getMoved();
		boolean canCastleLong = longRook != null && longRook.getColor().equals(color) && longRook instanceof Rook && !longRook.getMoved();
		if (canCastleShort && canCastleLong) {
			return 3;
		}
		else if (canCastleShort) {
			return 1;
		}
		else if (canCastleLong) {
			return 2;
		}
		return 0;
	}
	
	/**
	 * Returns an ArrayList of all valid moves for pieces of a certain color
	 * 
	 * @param color The piece color to detect
	 * @return All possible moves
	 */
	public ArrayList<Move> allMoves(Color color)
	{
		ArrayList<Piece> pieceCopy = new ArrayList<>();
		ArrayList<Move> possibleMoves = new ArrayList<>();
		
		for (int pieceEnum=1;pieceEnum<=6;pieceEnum++)
		{
			for (Piece p: pieces[color.equals(Color.WHITE) ? 0 : 1][pieceEnum])
			{
				pieceCopy.add(p);
			}
		}
		
		//Moving
		for (Piece p: pieceCopy)
		{
			ArrayList<Location> dests = p.destinations();
			for (Location d : dests)
			{
				//Check for promotion
				if (p instanceof Pawn) {
					int frontRank = p.getColor().equals(Game.NEAR_COLOR) ? 0 : 7;
					if (d.getRow() == frontRank) {
						PromotionMove promotion = new PromotionMove(p, d);
						if (escapesCheck(promotion)) {
							possibleMoves.add(promotion);
						}
						continue;
					}
				}
				Move move = new Move(p, d);
				if (escapesCheck(move)) {
					possibleMoves.add(move);
				}
			}
		}
		
		//Castling
		addCastleMoves(possibleMoves, getKing(color));
		
		return possibleMoves;
	}
	
	/**
	 * Returns an ArrayList of all possible captures by a specific color
	 * 
	 * @param color The piece color to detect
	 * @return An ArrayList of all captures
	 */
	public ArrayList<Move> allCaptures(Color color)
	{
		ArrayList<Move> validMoves = allMoves(color);
		ArrayList<Move> captureMoves = new ArrayList<>();
		for (Move m: validMoves)
		{
			if (m.getVictim() != null) {
				captureMoves.add(m);
			}
		}
		return captureMoves;
	}

	/**
	 * Executes a move, reflecting it to the board
	 * 
	 * @param move The move to execute
	 */
	public void executeMove(Move move)
	{
		//General Movement
		move.getPiece().moveTo(move.getDestination());
		move.getPiece().setMoved(true);
		//Castle Movement
		if (move instanceof CastleMove) {
			((CastleMove) move).getRook().moveTo(((CastleMove) move).getRookDestination());
			((CastleMove) move).getRook().setMoved(true);
		}
		//Promotion
		if (move instanceof PromotionMove) {
			
			((PromotionMove) move).getUpgradePiece().putSelfInGrid(move.getPiece().getBoard(), move.getDestination());
			((PromotionMove) move).getUpgradePiece().setMoved(true);
		}
	}
	

}