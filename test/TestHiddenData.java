import java.io.File;

import applications.RSEM;
import rsem.model.ExpectedHiddenData;
import rsem.model.ExpressionLevels;
import rsem.model.ExpectedHiddenData;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;
import test.Core;
import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.SAMReader;

public class TestHiddenData 
{
	public static void main(String[] args)
	{
		//testSubstitutionMatrix();
		//testSumOverTranscript();
		testSumBasePairs();
	}
	
	public static void testSubstitutionMatrix()
	{
		SubstitutionMatrix sm = new SubstitutionMatrix();
		System.out.println(sm);
	}
	
	
	public static void testSumOverTranscript()
	{
		SubstitutionMatrix pM = Core.buildDummySubstitutionMatrix();
		
		final String samFName = "./data/bowtie/NM_small/bowtie_small_5_augmented.txt";
		File samFile = new File(samFName);
		
		final String readsFName = Core.PATH_TO_OUTPUT +
				  "out_small_5" +
				  Core.FASTA_EXT;
		
		SimulatedReads rs = FASTAReader.readSimulatedReads(readsFName);
		Transcripts ts = Core.getSmallTranscriptSet();
		
		Alignments cAligns = SAMReader.readCandidateAlignments(samFile, rs, ts);		
		
		ExpressionLevels el = Core.buildDummyExpressionLevels(ts);
		System.out.println(el);
		
		ExpectedHiddenData z = RSEM.eStep(rs, ts, cAligns, el, pM);
		System.out.println(z);
		
		Transcript t = ts.getTranscript(3);
		System.out.println(t.getId());
		double sum = z.sumOverTranscript(t.getId());
		System.out.println("SUM OVER TRANSCRIPT " + t.getId() + " : " + sum);
		
	}
	
	public static void testSumBasePairs()
	{
		SubstitutionMatrix pM = Core.buildDummySubstitutionMatrix();
		
		final String samFName = "./data/bowtie/NM_small/bowtie_small_5_augmented.txt";
		File samFile = new File(samFName);
		
		final String readsFName = Core.PATH_TO_OUTPUT +
				  "out_small_5" +
				  Core.FASTA_EXT;
		
		SimulatedReads rs = FASTAReader.readSimulatedReads(readsFName);
		Transcripts ts = Core.getSmallTranscriptSet();
		
		Alignments cAligns = SAMReader.readCandidateAlignments(samFile, rs, ts);		
		
		ExpressionLevels el = Core.buildDummyExpressionLevels(ts);
		System.out.println(el);
		
		ExpectedHiddenData z = RSEM.eStep(rs, ts, cAligns, el, pM);
		System.out.println(z);
		
		Transcript t = ts.getTranscript(3);
		System.out.println(t.getId());
		
		char rSymbol = 'A';
		char tSymbol = 'T';
		int position = 3;
		double sum = z.countAlignedBasePairs(rSymbol, tSymbol, position);
		System.out.println("SUM OVER PAIR R='" + rSymbol + "', T='" + tSymbol + "': " + sum);
		
		sum = z.countTranscriptBaseOccurrences(tSymbol, position);
		System.out.println("SUM TRANSCRIPT OCCURRENCES T='" + tSymbol + "': " + sum);
	}
	
}
