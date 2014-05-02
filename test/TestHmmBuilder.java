import data.readers.Alignments;
import sequence.Reads;
import sequence.Transcript;
import sequence.Transcripts;
import hmm.HiddenMarkovModel;
import hmm.ProfileHmmBuilder;


public class TestHmmBuilder 
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
		
		ProfileHmmBuilder builder = new ProfileHmmBuilder();
		HiddenMarkovModel hmm = builder.buildHMM(ts, aligns);
		
		System.out.println(hmm);
	}
}
