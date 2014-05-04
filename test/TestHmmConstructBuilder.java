import pair.Pair;
import data.readers.Alignments;
import sequence.Reads;
import sequence.Sequence;
import sequence.Transcript;
import sequence.Transcripts;
import test.Core;
import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.State;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.DpMatrix;
import hmm.algorithms.ForwardAlgorithm;
import hmm.algorithms.SortSilentStates;


public class TestHmmConstructBuilder 
{
	public static void main(String[] args)
	{
		testProfileHMMBuilder();
	}
	
	public static void testProfileHMMBuilder()
	{
		Core.TestKit kit = Core.getDummyTestKitTwo();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		
		Transcript t = ts.getTranscript(0);
		
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hmmC = builder.buildHMMConstruct(ts, rs, aligns);		
				
		String seq = rs.getRead("0").getSeq();
				
		HMM hmm = hmmC.getReadHMM("0");
		//System.out.println(hmm);
		
		//Pair<Double, DpMatrix> result = BackwardAlgorithm.run(hmm, seq);		
		Pair<Double, DpMatrix> result = ForwardAlgorithm.run(hmm, seq);
		
		System.out.println("FULL PROBABILITY: " + result.getFirst());
	}
}
