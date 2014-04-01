import rsem.no_indels.model.ExpectedHiddenData;
import data.reads.Read;
import data.reads.ReadSet;
import data.simulation.ReadSimulator;
import data.transcript.TranscriptSet;


public class TestHiddenData 
{
	public static void main(String[] args)
	{
		testHiddenData();
	}
	
	public static void testHiddenData()
	{
		TranscriptSet ts =  TestCommon.getDummyTranscriptSet();
		ReadSet rs = ReadSimulator.simulateReadsAllUniform(ts, 2);
		
		for (Read r : rs.getReads())
		{
			System.out.println(r);
		}
		
		ExpectedHiddenData hd = new ExpectedHiddenData(rs, ts);
		
		System.out.println(hd);
		
	}
}
