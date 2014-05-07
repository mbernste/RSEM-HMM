import applications.RSEM_HMM;
import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.DpMatrix;
import pair.Pair;
import sequence.Reads;
import sequence.Transcript;
import sequence.Transcripts;
import test.Core;
import data.readers.Alignments;


public class TestRSEM_HMM 
{
	public static void main(String[] args)
	{
		//testProbOfData();
		testEM();
	}

	public static void testProbOfData()
	{
		Core.TestKit kit = Core.getDummyTestKit();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		
		Transcript t = ts.getTranscript(0);
		
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hmmC = builder.buildHMMConstruct(ts, aligns);		
				
		System.out.println("FULL PROBABILITY: " + RSEM_HMM.probabilityOfData(rs, aligns, hmmC));
	}
	
	public static void testEM()
	{
		Core.TestKit kit = Core.getDummyTestKit();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		
		Transcript t = ts.getTranscript(0);
		
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hmmC = builder.buildHMMConstruct(ts, aligns);	
		
		RSEM_HMM.EM(rs, ts, aligns);
	}
}
