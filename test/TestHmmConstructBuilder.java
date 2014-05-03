import data.readers.Alignments;
import sequence.Reads;
import sequence.Transcript;
import sequence.Transcripts;
import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.algorithms.ForwardAlgorithm;


public class TestHmmConstructBuilder 
{
	public static void main(String[] args)
	{
		testProfileHMMBuilder();
	}
	
	public static void testProfileHMMBuilder()
	{
		Core.TestKit kit = Core.getDummyTestKit();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		
		Transcript t = ts.getTranscript(0);
		
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hmmC = builder.buildHMMConstruct(ts, rs, aligns);
		
		System.out.println(hmmC.getReadHMM("4"));
		
		String seq = rs.getRead("4").getSeq();
		ForwardAlgorithm.run(hmmC.getReadHMM("4"), seq);
	}
}
