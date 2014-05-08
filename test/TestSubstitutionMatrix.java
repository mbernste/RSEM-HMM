import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Reads;
import sequence.Transcripts;
import test.Core;
import data.readers.Alignments;


public class TestSubstitutionMatrix 
{
	public static void main(String[] args)
	{
		testSubstitutionMatrix();
	}
	
	public static void testSubstitutionMatrix()
	{
		Core.TestKit kit = Core.getDummyTestKitTwo();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		
		SubstitutionMatrix m = new SubstitutionMatrix(rs, ts, aligns);
		
		System.out.println(m);
	}
}
