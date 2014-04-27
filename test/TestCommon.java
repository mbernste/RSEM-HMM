import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import common.Common;
import data.Sequence;
import data.Transcripts;


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
	
	public static ExpressionLevels buildDummyExpressionLevels(Transcripts ts)
	{
		ExpressionLevels el = new ExpressionLevels(ts);
		double count = 1;
		for (Sequence s : ts.getSequences())
		{
			el.setExpressionLevel(s.getId(), count++);
		}
		el.normalize();
		System.out.println(el);
		return el;
	}
}
