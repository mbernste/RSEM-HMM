import applications.Simulation;
import rsem.model.ExpressionLevels;
import sequence.Transcripts;


public class TestExpressionLevels 
{
	public static void main(String[] args)
	{
		//testInitialize();
		testSampleTranscript();
	}
	
	public static void testInitialize()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
	}
	
	public static void testSampleTranscript()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		ExpressionLevels el = Simulation.generateExpressionLevels(ts);
		System.out.println(el);
		
		System.out.println( el.sampleTranscript() );
	}
}
