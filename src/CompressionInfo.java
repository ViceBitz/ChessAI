/**
 * Stores the depth, type, and value of a node in the state table
 * @author Victor Gong
 * @version 4/19/2023
 */
public class CompressionInfo
{
	public int nodeType;
	public int depth;
	public int score;
	public CompressionInfo(int nodeType, int depth, int score)
	{
		/**
		 * Node Types:
		 * 1 - Exact, principal variation node; calculated to full depth
		 * 2 - Upper Bound, occurs when move falls short (<= alpha)
		 * such that current player already has a better option
		 * 3 - Lower Bound, occurs when move is too good (alpha >= beta)
		 * such that opponent already has a better option than this
		 */
		this.nodeType = nodeType;
		this.depth = depth;
		this.score = score;
	}
	
	//Converts CompressionInfo into a string
	public String toString()
	{
		return nodeType + " " + depth + " " + score;
	}
}
