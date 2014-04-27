import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcripts;
import data.simulation.ReadSimulator;


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
		Reads rs = ReadSimulator.simulateReadsAllUniform(ts, 5);
		
		for (Sequence r : rs.getSequences())
		{
			System.out.println(r);
		}
	}
	
	public static void testSimulatedReads()
	{
		Transcripts ts =  Core.getSmallTranscriptSet();
		SimulatedReads rs = ReadSimulator.simulateReadsAllUniform(ts, 5);
		
		System.out.println(rs.mapping());
	}
	
	public static void testFastaFormat()
	{
		Transcripts ts =  Core.getSmallTranscriptSet();
		Reads rs = ReadSimulator.simulateReadsAllUniform(ts, 5);
		
		System.out.println(rs.fastaFormat());
	}

}
