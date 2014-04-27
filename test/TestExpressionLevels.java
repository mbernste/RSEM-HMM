import rsem.model.ExpressionLevels;
import data.Transcripts;


public class TestExpressionLevels 
{
	public static void main(String[] args)
	{
		testInitialize();
	}
	
	public static void testInitialize()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
	}
}
