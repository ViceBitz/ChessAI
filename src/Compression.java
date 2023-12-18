
/**
 * A class to handle compressing chess states into Strings and managing the data files, allowing for
 * access and retrieval
 * 
 * @author Victor Gong
 * @version 4/11/2023
 */
import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.StringTokenizer;

public final class Compression
{
	private static HashMap<String, CompressionInfo> allStates = new HashMap<>();
	private static final int TABLE_SIZE_CUTOFF = 6000000; // Maximum states that table/file can hold
	private static CompressionWriter compressionWriter = new CompressionWriter();
	
	private static boolean tableLocked = false; //boolean that locks the state table on true
	
	private static final boolean SAVETOFILE = false;
	
	/**
	 * Encodes a base 10 number, represented as a String, to a base 90 number
	 * @param number The base 10 number
	 * @return The base 90 number
	 */
	private static String encodeToBase90(BigInteger number) {
		String DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()[];',./{}|:<>?_+`~";
		BigInteger BASE = BigInteger.valueOf(90);
	    if (number.compareTo(BigInteger.ZERO) == -1) { // number < 0
	      throw new IllegalArgumentException("number must not be negative");
	    }
	    StringBuilder result = new StringBuilder();
	    while (number.compareTo(BigInteger.ZERO) == 1) { // number > 0
	      BigInteger[] divmod = number.divideAndRemainder(BASE);
	      number = divmod[0];
	      int digit = divmod[1].intValue();
	      result.insert(0, DIGITS.charAt(digit));
	    }
	    return (result.length() == 0) ? DIGITS.substring(0, 1) : result.toString();
	}
	/**
	 * Compresses the current board state into a base90 number stored in a String
	 * 
	 * @param board The current board
	 * @param color The color to play
	 * @return The compressed string
	 */
	public static String compressState(Board board, int color)
	{
		// 1 - Pawn, 2 - Knight, 3 - Bishop, 4 - Rook, 5 - Queen, 6 - King
		String[][] enumTable = {
				{"1","2","3","4","5","6"},
				{"7","8","9","A","B","C"}
		};
		String ret = "";
		// 0 - Empty
		
		// Shift 0 for white, Shift 6 for black
		for (int i = 0; i < board.getNumRows(); i++)
		{
			for (int j = 0; j < board.getNumCols(); j++)
			{
				Piece p = board.get(new Location(i, j));
				String pieceEnum = "0";
				if (p == null) {
					pieceEnum = "0";
				}
				else {
					int colorEnum = p.getColor().equals(Color.WHITE) ? 0 : 1;
					pieceEnum = enumTable[colorEnum][p.getEnum()-1];
				}
				
				ret += pieceEnum;
			}
		}
		
		//Always compress with white near-side
		if (Game.NEAR_COLOR.equals(Color.BLACK)) {
			String retRev = "";
			for (int i=ret.length()-1;i>=0;i--) {
				retRev += ret.substring(i,i+1);
			}
			ret = retRev;
		}
		
		/**
		 * Add tag for castling rights
		 * 0 = none, 1 = short, 2 = long, 3 = both
		 */
		ret = (board.getCastlingRights(color == 1 ? Color.WHITE : Color.BLACK)) + ret;
		
		/*
		 * Add tag for color to play
		 * 1 = White, 2 = Black
		 */
		ret = (color == 1 ? 1 : 2) + ret;
		
		BigInteger base13 = new BigInteger(ret, 13);
		ret = encodeToBase90(base13); //Encoded board

		return ret;
	}
	
	/**
	 * Clears the data file
	 * 
	 * @throws IOException
	 */
	public static void clearFile() throws IOException
	{
		compressionWriter.clearFile();
	}
	
	/**
	 * Loads the state table from the file
	 */
	public static void setup() throws IOException
	{
		//Read in the state table from data file
		BufferedReader br = new BufferedReader(new FileReader(CompressionWriter.DATA_FILE));
		StringTokenizer s;
		String nextLine;
		while ((nextLine = br.readLine()) != null)
		{
			s = new StringTokenizer(nextLine);
			if (allStates.size() >= TABLE_SIZE_CUTOFF)
			{
				break;
			}
			String key = s.nextToken();
			int nodeType = Integer.parseInt(s.nextToken());
			int depth = Integer.parseInt(s.nextToken());
			int value = Integer.parseInt(s.nextToken());

			allStates.put(key, new CompressionInfo(nodeType, depth, value));
		}
		br.close();
		
		//Start the CompressionWriter thread
		compressionWriter.start();
	}
	
	/**
	 * Saves the current state table to the data file (Warning: Replaces all existing data)
	 * @throws IOException 
	 */
	public static void saveTable() throws IOException
	{
		if (!SAVETOFILE)
		{
			return;
		}
		CompressionWriter.saveToFile(allStates);
	}
	
	/**
	 * Records the move log into the log file (Appends)
	 * @throws IOException 
	 */
	public static void recordMoveLog() throws IOException
	{
		//If no moves, don't record empty log
		if (Game.moveLog.isEmpty()) {
			return;
		}
		compressionWriter.recordMoveLog(Game.moveLog);
	}
	
	/**
	 * Adds a state with a processed depth to the state table
	 * 
	 * @param key            The current state, compressed as a string
	 * @param nodeType		 The type of node
	 * @param depth			 The depth of the search
	 * @param value          The evaluation value of the state
	 */
	public static void addToTable(String key, int nodeType, int depth, int value) throws IOException
	{
		if (allStates.size() >= TABLE_SIZE_CUTOFF || tableLocked)
		{
			return;
		}
		CompressionInfo prevInfo = allStates.get(key);
		
		//Don't replace entry in TT if a shallower search (less accurate) than current TT entry (if exists)
		if (prevInfo != null && prevInfo.depth > depth) {
			return;
		}
		allStates.put(key, new CompressionInfo(nodeType, depth, value));
		
		//compressionWriter.addToQueue(key, value);
		
	}

	/**
	 * Checks if the state table contains a specific state
	 * 
	 * @param key The current state, compressed as a string
	 * @return True if contains, false otherwise
	 */
	public static boolean tableHasState(String key)
	{
		String fullKey = key;
		return allStates.containsKey(fullKey);
	}

	/**
	 * Retrieves a CompressionInfo object from the state table given the appropriate parameters
	 * 
	 * @precondition Key exists in the state table
	 * 
	 * @param key The current state, compressed as a string
	 * @return The evaluation value
	 */
	public static CompressionInfo retrieveFromTable(String key)
	{
		String fullKey = key;
		return allStates.get(fullKey);
	}
	
	public static void lockTable()
	{
		tableLocked = true;
	}
	
	public static boolean writerIsRunning()
	{
		return compressionWriter.isWriting() && compressionWriter.isAlive();
	}
	
	public static void printDataDetails()
	{
		DecimalFormat df = new DecimalFormat("###,###,###,###.#");
		System.out.println("----- Compression Settings -----");
		System.out.println("COMPRESSION BASE: 36");
		System.out.println("CURRENT MEMORY USAGE: " + df.format(Runtime.getRuntime().totalMemory()/1024.0/1024.0) + " MB");
		System.out.println("MAX HEAP MEMORY: " + df.format(Runtime.getRuntime().maxMemory()/1024.0/1024.0) + " MB");
		System.out.println("TOTAL STATES IN TABLE: " + df.format(allStates.size()));
		System.out.println("% TABLE SPACE USED: " + df.format(allStates.size()/(double)TABLE_SIZE_CUTOFF*100) + "%");
	}
}
