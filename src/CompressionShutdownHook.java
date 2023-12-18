import java.io.IOException;

/**
 * Performs closing actions (e.g. saving file) when program terminates
 * @author Victor Gong
 * @version 4/11/2023
 *
 */
public class CompressionShutdownHook extends Thread
{
	public void run()
	{
		System.out.println("Compression Program Shutting Down..");
		try
		{
			//Lock the state table
			Compression.lockTable();
			
			/*
			//Wait until writer thread finishes
			while (Compression.writerIsRunning()) {
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			*/
			
			//Save the state table to data file
			Compression.saveTable();
			
			//Record moves
			Compression.recordMoveLog();
			System.out.println("Saved successfully!");
		}
		catch (IOException e)
		{
			System.out.println("Save failed.");
			e.printStackTrace();
		}
		
	}
}
