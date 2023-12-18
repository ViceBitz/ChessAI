
/**
 * Tracks the principal variation of the search (best move path)
 * @author Victor Gong
 * @version 4/15/2023
 */
public class EvaluationLine
{
	public Move bestMove;
	public EvaluationLine next;
	public int special; //0 - None, 1 - PV, 2 - UB, 3 - LB
	public EvaluationLine(Move bestMove)
	{
		this.bestMove = bestMove;
		this.next = null;
		this.special = 0;
	}
	
	public String toString()
	{
		String out = "";
		EvaluationLine currentLine = this;
		while (currentLine.next != null)
		{
			if (currentLine.bestMove != null) {
				out += (currentLine.bestMove.toStandardNotation()) + " ";
			}
			currentLine = currentLine.next;
			
		}
		if (currentLine.special == 1) {out += "PV ";}
		if (currentLine.special == 2) {out += "UB ";}
		if (currentLine.special == 3) {out += "LB ";}
		if (currentLine.special == 4) {out += "NMP ";}
		if (currentLine.special == 5) {out += "RFP ";}
		return out;
	}
}
