import applications.Simulation;
import rsem.model.ExpressionLevels;
import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcripts;
import test.Core;

public class TestReadSet 
{
	public static void main(String[] args)
	{
		//testSimulateReads();
		testSimulatedReads();
		//testFastaFormat();
	}
	
	public static void testSimulateReads()
	{
		Transcripts ts =  Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
		Reads rs = Simulation.simulateReads(ts, el, 5);
		
		for (Sequence r : rs.getSequences())
		{
			System.out.println(r);
		}
	}
	
	public static void testSimulatedReads()
	{
		Transcripts ts =  Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
		SimulatedReads rs = Simulation.simulateReads(ts, el, 5);
		
		System.out.println(rs.mapping());
	}
	
	public static void testFastaFormat()
	{
		Transcripts ts =  Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
		Reads rs = Simulation.simulateReads(ts, el, 5);
		
		System.out.println(rs.fastaFormat());
	}

}
