import sequence.Transcripts;
import test.Core;
import data.readers.FASTAReader;


public class TestFastaReader 
{
	public static void main(String[] args)
	{
		readTranscriptsInMemory();
	}

	public static void readTranscriptsInMemory()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		for (int i = 0; i < 4; i++)
		{
			System.out.println(ts.getTranscript(i));
		}
		
	}
}
