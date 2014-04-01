import data.reads.Read;
import data.reads.ReadSet;
import data.simulation.ReadSimulator;
import data.transcript.TranscriptSet;


public class TestReadSet 
{
	public static void main(String[] args)
	{
		testSimulateReads();
	}
	
	public static void testSimulateReads()
	{
		TranscriptSet ts =  TestCommon.getSmallTranscriptSet();
		ReadSet rs = ReadSimulator.simulateReadsAllUniform(ts, 5);
		
		for (Read r : rs.getReads())
		{
			System.out.println(r);
		}
	}

}
