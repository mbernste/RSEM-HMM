import pair.Pair;
import data.readers.Alignments;
import sequence.Reads;
import sequence.Transcript;
import sequence.Transcripts;
import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.DpMatrix;
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
		
		String seq = rs.getRead("4").getSeq();
		//Pair<Double, DpMatrix> result = ForwardAlgorithm.run(hmmC.getReadHMM("4"), seq);
		
		//System.out.println(result.getSecond());
		
		//ForwardAlgorithm.run(hmmC.getReadHMM("4"), seq);
		//BackwardAlgorithm.run(hmmC.getReadHMM("4"), seq);
		
		
		//System.out.println(result.getSecond());
	}
}
