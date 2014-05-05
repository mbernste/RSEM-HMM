import rsem.model.ExpressionLevels;
import sequence.Reads;
import sequence.Sequence;
import sequence.Transcripts;
import test.Core;
import applications.Simulation;


public class TestSimulation 
{
	public static void main(String[] args)
	{
		//testGenerateExpressionLevels();
		//testDeletion();
		//testInsertion();
		//testGeometricDistribution();
		testSimulation();
	}
	
	public static void testGenerateExpressionLevels()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		ExpressionLevels el = Simulation.generateExpressionLevels(ts);
		System.out.println(el);
	}
		
	public static void testDeletion()
	{
		String tSeq = "AAAAAAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAAAAAA" +
						  "AAAAAAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAAAAAA" +
						  "AAAAAAAAAAAAAAAAAAAA";
		
		System.out.println(tSeq);
		
		
		tSeq = Simulation.makeDeletion(tSeq);
		
		
		System.out.println(tSeq);
	}
	
	public static void testInsertion()
	{
		String tSeq = "AAAAAAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAAAAAA" +
						  "AAAAAAAAAAAAAAAAAAAA" + "AAAAAAAAAAAAAAAAAAAA" +
						  "AAAAAAAAAAAAAAAAAAAA";
		
		System.out.println(tSeq);
		
		
		tSeq = Simulation.makeInsertion(tSeq);
		
		System.out.println(tSeq);
	}
	
	public static void testGeometricDistribution()
	{
		for (int i = 0; i < 100; i++)
		{
			System.out.println(Simulation.sampleGeometricDistribution(0.1828));
		}
	}
	
	public static void testSimulation()
	{
		Core.TestKit kit = Core.getDummyTestKit();
		Transcripts ts = kit.transcripts();
		
		ExpressionLevels el = Simulation.generateExpressionLevels(ts);
		Reads rs = Simulation.simulateReads(ts, el, 20, true);
		
		for (Sequence r : rs.getSequences())
		{
			System.out.println(r);
		}
	}
}
