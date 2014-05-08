package applications;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import common.Common;

import pair.Pair;

import rsem.model.ExpectedHiddenData;
import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.NoiseTranscript;
import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;
import test.Core;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.MappingReader;
import data.readers.SAMReader;

public class RSEM 
{
	private static int debug = 1;
	
	private static final double EPSILON = 0.001;
	
	public static final String NOISE_ID = "NOISE";
	
	public static double averageTranscriptLength;
	
	public static void main(String[] args)
	{	

		SimulatedReads rs = FASTAReader.readSimulatedReads(args[0]);
		MappingReader.recoverMapping(args[1], rs);
		
		Transcripts ts = FASTAReader.readTranscripts(args[2]);

		File samFile = new File(args[3]);
		Alignments aligns = SAMReader.readCandidateAlignments(samFile, 
															  rs, 
															  ts, 
															  false);
		
		/*
		Core.TestKit kit = Core.getDummyTestKit();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
		*/
		
		
		
		double sum = 0.0;
		for (Sequence t : ts.getSequences())
		{
			sum += t.getSeq().length();
		}
		averageTranscriptLength = sum / ts.size();
		
		System.out.println("Average transcript length: " + averageTranscriptLength);
		
		
		/*
		 * Add the "noise-isophorm" and noise alignment
		 */
		ts.addSequence(new NoiseTranscript());
		//for (String rId : aligns.getAllMappedReads())
		//{
		//	aligns.addAlignment(rId, "NOISE", 0, Common.FORWARD_ORIENTATION);
		//}
		
		SubstitutionMatrix pM = new SubstitutionMatrix(rs, ts, aligns);
		ExpressionLevels el = new ExpressionLevels(ts);
		
		System.out.println(el);
		
		
		/*
		 * Run RSEM
		 */
		ExpressionLevels result = RSEM.EM(rs, ts, aligns, el, pM);
		
		sum = 0.0;
		for (Sequence t : ts.getSequences())
		{
			sum += result.getExpressionLevel(t.getId());
		}
		System.out.println("SUM: " + sum);
		
		/*
		 * Print the resultant expression levels
		 */
		System.out.println(result);
	}
	
	public static ExpressionLevels EM( Reads rs,
						   			   Transcripts ts,
						   			   Alignments cAligns,
						   			   ExpressionLevels pEl,
						   			   SubstitutionMatrix pM)
	{	
		int numReadsAligned = 0;
		Set<String> counted = new HashSet<String>();
		
		for (Object[] o : cAligns.getAlignments())
		{
			String readId = (String) o[0];
			
			if (!counted.contains(readId))
			{
				counted.add(readId);
				numReadsAligned++;
			}
		}
		
		
		ExpectedHiddenData z = null;
		
		/*
		 *  Repeat E-Step & M-Step until convergence
		 */
		double probData = 1.0;
		double prevProbData = 0.0;
		while (Math.abs(probData - prevProbData) > EPSILON)
		{
			/*
			 * E-Step
			 */
			z = eStep(rs, ts, cAligns, pEl, pM);
						
			/*
			 * M-Step
			 */
			Pair<ExpressionLevels, SubstitutionMatrix> params = mStep(rs,
																	  ts, 
																	  z, 
																	  numReadsAligned);
			pEl = params.getFirst();
			//pM = params.getSecond();
		
			prevProbData = probData;
			probData = probabilityOfData(rs, ts, cAligns, z, pEl, pM);	
			
			if (debug > 0)
				System.out.println("Current probability of data: " + probData);
		}

		return pEl;
	}
	
	public static ExpectedHiddenData eStep(Reads rs, 
							 Transcripts ts,
							 Alignments cAligns,
							 ExpressionLevels pEl,
							 SubstitutionMatrix pM)
	{ 
		ExpectedHiddenData z = new ExpectedHiddenData(rs, ts);
			
		for (Object[] o : cAligns.getAlignments())
		{
			String readId = (String) o[0];
			String transId = (String) o[1];
			Integer startPos = (Integer) o[2];
			Boolean orientation = (Boolean) o[3];
			
			double pSequence = probabilityOfSequence(rs.getRead(readId), 
													 ts.getTranscript(transId),
													 startPos,
													 orientation,
													 pM);						
			
			/*
			 *  Multiply probability of sequence by the expression level of the
			 *  reference transcript it is aligned to
			 */
			double finalP = pSequence * (pEl.getExpressionLevel(transId) / 
										 ts.getSequence(transId).length());
			
			z.setValue(readId, transId, startPos, orientation, finalP);
		}
		
		for (Sequence r : rs.getSequences())
		{
			/*
			 * The noise transcript
			 */
			double pSequence = probabilityOfSequenceFromNoise();
			double finalP = pSequence * (pEl.getExpressionLevel("NOISE") / 
										 averageTranscriptLength);
						
			z.setValue(r.getId(), NoiseTranscript.NOISE_ID, 0, Common.FORWARD_ORIENTATION, finalP);
		}
		
		/*
		 *	Normalize 
		 */
		z.normalizeOverAllReads();
		
		return z;
	}
	
	public static Pair<ExpressionLevels, SubstitutionMatrix> mStep(Reads rs, 
																   Transcripts ts, 
																   ExpectedHiddenData z,
																   int numReadsAligned)
	{
		/*
		 *	Calculate the new estimate of the expression levels parameters
		 */
		ExpressionLevels pEl = new ExpressionLevels(ts);
		for (Sequence s : ts.getSequences())
		{
			String tId = s.getId();
			double sumOverT = z.sumOverTranscript(tId);
			pEl.setExpressionLevel(tId, sumOverT / rs.size());
		}
						
		return new Pair<ExpressionLevels, SubstitutionMatrix>(pEl, null);
	}
	
	public static double probabilityOfSequence(Read r, 
										 Transcript t,
										 int startPos,
										 boolean orientation,
										 SubstitutionMatrix pM)
	{				
		// Total probability of sequence
		double p = 1.0;
		
		String rSeq = r.getSeq();
		String tSeq = t.getSeq();
		
		
		if (orientation == Common.REVERSE_COMPLIMENT_ORIENTATION)
		{	
			tSeq = Common.reverseCompliment(tSeq);
			int rcStartPos = tSeq.length() - (startPos + rSeq.length()); 		
			
			for (int i = 0; i < rSeq.length(); i++)
			{	
				char rSymbol = rSeq.charAt(i);
				char tSymbol = tSeq.charAt(rcStartPos + i);
				
				p *= pM.getValue(i, rSymbol, tSymbol);
			}
		}
		else if (orientation == Common.FORWARD_ORIENTATION)
		{
			for (int i = 0; i < rSeq.length(); i++)
			{
				char rSymbol = rSeq.charAt(i);
				char tSymbol = tSeq.charAt(startPos + i);
				
				p *= pM.getValue(i, rSymbol, tSymbol);
			}
		}
		
		return p;
	}
	
	public static double probabilityOfSequenceFromNoise()
	{
		double prod = 1.0;
		for (int i = 0; i < Common.readLength; i++)
		{
			prod *= 0.25;
		}
		return prod;
	}
	
	private static double probabilityOfData(
									 Reads rs, 
									 Transcripts ts,
									 Alignments cAligns,
									 ExpectedHiddenData z,
									 ExpressionLevels pEl,
									 SubstitutionMatrix pM)
	{
		/*
		 * Map that will be used to store all
		 * partial computations of each sequence when calculating the total 
		 * probability of the data.
		 */
		Map<String, Double> pSequences = new HashMap<String, Double>();
		for (Sequence r : rs.getSequences())
		{
			pSequences.put(r.getId(), 0.0);
		}
		
		double p = 1.0;
		
		for (Object[] o : cAligns.getAlignments())
		{
			String rId = (String) o[0];
			String tId = (String) o[1];
			Integer startPos = (Integer) o[2];
			Boolean orient = (Boolean) o[3];
			
			double pSequence = probabilityOfSequence(rs.getRead(rId), 
					 ts.getTranscript(tId),
					 startPos,
					 orient,
					 pM);	
			
			
			double zVal = z.getValue(rId, tId, startPos, orient);
			
			double partialCalculation = pSequence * zVal;
						
			pSequences.put(rId, pSequences.get(rId) + partialCalculation);
		}
		
		for (Sequence r : rs.getSequences())
		{
			String rId = r.getId();
			
			double pSequence = probabilityOfSequenceFromNoise();
			double zVal = z.getValue(rId, "NOISE", 0, Common.FORWARD_ORIENTATION);
			double partialCalculation = pSequence * zVal;
			pSequences.put(rId, pSequences.get(rId) + partialCalculation);
		}
		
		for (Double val : pSequences.values())
		{
			p += Math.log(val);
		}
		
		return p;
	}
}
