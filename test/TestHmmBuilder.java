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
		Transcripts ts = Core.getDummyTranscriptSet();
		Transcript t = ts.getTranscript(0);
		
		ProfileHmmBuilder builder = new ProfileHmmBuilder();
		//HiddenMarkovModel hmm = builder.buildHMM(t);
		
		//System.out.println(hmm);
	}
}
