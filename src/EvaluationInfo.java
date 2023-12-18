/**
 * Stores the evaluation value along with if it's a lazy value or a full value
 * @author victor
 *
 */
public class EvaluationInfo
{
	public int value;
	public boolean isFull;
	
	public EvaluationInfo(int value, boolean isFull)
	{
		this.value = value;
		this.isFull = isFull;
	}
}
