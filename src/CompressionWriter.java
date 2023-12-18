import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A class that utilizes multithreading to concurrently write information to the data file
 * @author Victor Gong
 * @version 4/11/2023
 */
public class CompressionWriter extends Thread
{
	
	public static final String DATA_FILE = "chess.data";
	public static final String MOVE_LOG_FILE = "move.log";
	private Queue<String> writeQueue;
	private boolean isWriting;
	
	public CompressionWriter() {
		writeQueue = new LinkedList<>();
		isWriting = false;
	}
	
	/**
	 * Saves a state table to the data file (Warning: Replaces all previous information)
	 * 
	 * @throws IOException
	 */
	public static void saveToFile(HashMap<String, CompressionInfo> allStates) throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_FILE)));
		for (String s : allStates.keySet())
		{
			out.write(s + " " + allStates.get(s) + "\n");
		}
		out.flush();
		out.close();
	}
	
	/**
	 * Records the move log of the current game in the move.log file (appends)
	 * @throws IOException 
	 */
	public void recordMoveLog(ArrayList<Move> moveLog) throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(MOVE_LOG_FILE, true)));
		out.write("\n\n\n");
		out.write(Game.gameTitle+"\n");
		out.write("Timestamp: "+LocalDate.now()+" | "+LocalTime.now()+"\n");
		out.write("-----------------------\n\n");
		for (int i=0;i<moveLog.size();i+=2)
		{
			out.write(Integer.toString(i/2+1)+".");
			out.write(moveLog.get(i).toStandardNotation());
			if (i+1 < moveLog.size())
			{
				out.write(" " + moveLog.get(i+1).toStandardNotation() + "\n");
			}
		}
		out.write("\n~End of Log~");
		out.flush();
		out.close();
	}
	
	/**
	 * Clears the data file
	 * 
	 * @throws IOException
	 */
	public void clearFile() throws IOException
	{
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_FILE)));
		out.write("");
		out.flush();
		out.close();
	}
	
	/**
	 * Appends a state table to the data file
	 * 
	 * @throws IOException
	 */
	public void addToQueue(String key, CompressionInfo info) throws IOException
	{
		String data = key + " " + info
				+ "\n";
		writeQueue.add(data);
	}
	
	/**
	 * Returns if the writer is appending to the data file
	 * @return True if the writer is writing, false otherwise
	 */
	public boolean isWriting()
	{
		return isWriting;
	}
	
	public void run()
	{
		
		
		while (true) {
			PrintWriter out = null;
			try
			{
				out = new PrintWriter(new BufferedWriter(new FileWriter(DATA_FILE, true)));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			while (!writeQueue.isEmpty()) {
				isWriting = true;
				String data = writeQueue.poll();
				if (data != null) {
					out.write(data);
				}	
			}
			out.flush();
			out.close();
			
			isWriting = false;
		}
	}
}
