import data.readers.FastaReader;
import data.transcript.TranscriptSet;


public class TestCommon 
{
	
	public static TranscriptSet getSmallTranscriptSet()
	{
		return FastaReader.readTranscripts("./data/NM_refseq_ref.transcripts_short.fa");
	}
	
	public static TranscriptSet getDummyTranscriptSet()
	{
		return FastaReader.readTranscripts("./data/dummy.fa");
	}

}
