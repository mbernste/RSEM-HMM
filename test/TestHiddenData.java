import java.io.File;

import applications.RSEM;
import rsem.model.ExpectedHiddenData;
import rsem.model.ExpressionLevels;
import rsem.model.ExpectedHiddenData;
import rsem.model.no_indels.SubstitutionMatrix;
import data.Read;
import data.Reads;
import data.Sequence;
import data.SimulatedReads;
import data.Transcript;
import data.Transcripts;
import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.SAMReader;
import data.simulation.ReadSimulator;

public class TestHiddenData 
{
	public static void main(String[] args)
	{
		//testSubstitutionMatrix();
		//testHiddenData();
		testSumOverTranscript();
	}
	
	public static void testSubstitutionMatrix()
	{
		SubstitutionMatrix sm = new SubstitutionMatrix();
		System.out.println(sm);
	}
	
	
	public static void testSumOverTranscript()
	{
SubstitutionMatrix pM = Core.buildDummySubstitutionMatrix();
		
		final String samFName = "./data/bowtie/NM_small/bowtie_small_25.txt";
		File samFile = new File(samFName);
		
		final String readsFName = Core.PATH_TO_OUTPUT +
				  "out_small_25" +
				  Core.FASTA_EXT;
		
		SimulatedReads rs = FASTAReader.readSimulatedReads(readsFName);
		Transcripts ts = Core.getSmallTranscriptSet();
		
		Alignments cAligns = SAMReader.readCandidateAlignments(samFile, rs, ts);		
		
		ExpressionLevels el = TestCommon.buildDummyExpressionLevels(ts);
		System.out.println(el);
		
		ExpectedHiddenData z = RSEM.eStep(rs, ts, cAligns, el, pM);
		System.out.println(z);
		
		Transcript t = ts.getTranscript(3);
		System.out.println(t.getId());
		double sum = z.sumOverTranscript(t.getId());
		System.out.println("SUM OVER TRANSCRIPT " + t.getId() + " : " + sum);
		
	}
	
	
}
