import data.readers.FastaReader;
import data.transcript.TranscriptSet;


public class TestFastaReader 
{
	public static void main(String[] args)
	{
		readTranscriptsInMemory();
	}

	public static void readTranscriptsInMemory()
	{
		TranscriptSet ts = FastaReader.readTranscripts("./data/NM_refseq_ref.transcripts_short.fa");
		for (int i = 0; i < 4; i++)
		{
			System.out.println(ts.getTranscript(i));
		}
		
	}
}
