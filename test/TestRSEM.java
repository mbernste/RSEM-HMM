import java.io.File;

import pair.Pair;

import common.Common;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.SAMReader;
import applications.RSEM;
import rsem.model.ExpectedHiddenData;
import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Read;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;


public class TestRSEM 
{
	
	
	public static void main(String[] args)
	{
		//testProbabilityOfSequence();
		//testEStep();
		testMStep();
	}
	
	public static void testProbabilityOfSequence()
	{
		SubstitutionMatrix pM = Core.buildDummySubstitutionMatrix();
		
		System.out.println(pM);
		
		Read r = new Read();
		r.setId("TEST_READ");
		r.setSeq("TAACGT");
		
		Transcript t = new Transcript();
		t.setId("TEST_TRANSCRIPT");
		t.setSeq("AAAAATAAGTT");;
		
		int startPos = 5;
		
		/*
		 * Correct answer: 9.533225054727771e-05
		 */
		double pForward = RSEM.probabilityOfSequence(r, 
													 t, 
													 startPos, 
													 Common.FORWARD_ORIENTATION, 
													 pM);
		System.out.println(pForward);
		
		/*
		 * Correct answer: 1.0355029585798816E-4
		 */
		double pReverseCompliment = RSEM.probabilityOfSequence(r, 
				 									  t, 
				 									  startPos, 
				 									  Common.REVERSE_COMPLIMENT_ORIENTATION, 
				 									  pM);
		
		System.out.println(pReverseCompliment);
	}
	
	public static void testEStep()
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
	}
	
	public static void testMStep()
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
		System.out.println("\n ------- EXPRESSION LEVELS -------\n" + el);
		
		ExpectedHiddenData z = RSEM.eStep(rs, ts, cAligns, el, pM);
		System.out.println(z);
		Pair<ExpressionLevels, SubstitutionMatrix> params = RSEM.mStep(rs, ts, z);
		//System.out.println(params.getFirst());
	}
	
}
