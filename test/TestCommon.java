import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Sequence;
import sequence.Transcripts;
import common.Common;


public class TestCommon 
{
	public static void main(String[] args)
	{
		testReverseCompliment();
	}
	
	public static void testReverseCompliment()
	{
		String seq = "ATCGGGT";
		
		System.out.println(Common.reverseCompliment(seq));
	}
}
