package applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import common.Common;

import pair.Pair;

import rsem.model.ExpectedHiddenData;
import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedRead;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.MappingReader;
import data.readers.SAMReader;

public class RSEM 
{
	private static final double EPSILON = 0.001;

	public static void main(String[] args)
	{
		SimulatedReads reads = FASTAReader.readSimulatedReads(args[0]);
		MappingReader.recoverMapping(args[1], reads);
		
		Transcripts transcripts = FASTAReader.readTranscripts(args[2]);

		File samFile = new File(args[1]);
		Alignments aligns = SAMReader.readCandidateAlignments(samFile, 
															  reads,
															  transcripts);
	}
	
	public static void EM( Reads rs,
						   Transcripts ts,
						   Alignments cAligns)
	{
		//Pair<Motif, MotifLocations> result = new Pair<Motif, MotifLocations>();
		
		ExpectedHiddenData z = null;
		ExpressionLevels pEl = new ExpressionLevels(ts);
		SubstitutionMatrix pM = new SubstitutionMatrix();
		
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
			Pair<ExpressionLevels, SubstitutionMatrix> params = mStep(rs, ts, z);
			pEl = params.getFirst();
			pM = params.getSecond();
		
			prevProbData = probData;
			//probData = probabilityOfData(reads, params, hiddenData);			
		}

		//result.setFirst(p);
		//result.setSecond(z);
		
		//return result;
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
			double finalP = pSequence * pEl.getExpressionLevel(transId);
			
			z.setValue(readId, transId, startPos, orientation, finalP);
		}
		
		/*
		 *	Normalize 
		 */
		z.normalizeOverAllReads();
		
		return z;
	}
	
	public static Pair<ExpressionLevels, SubstitutionMatrix> mStep(Reads rs, 
																   Transcripts ts, 
																   ExpectedHiddenData z)
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
		
		/*
		 *  Count occurrences of each pair of bases at each position along the
		 *  read.
		 */
		double sum = 0.0;
		for (int p = 0; p < Common.READ_LENGTH; p++)
		{
			for (char t : Common.DNA_ALPHABET)
			{
				for (char r : Common.DNA_ALPHABET)
				{
					z.countAlignedBasePairs(r, t, p);
				}
				
				z.countTranscriptBaseOccurrences(t, p);
			}
		}
		
		/*
		 * Count the occurrences of each base in the transcript
		 */
		// TODO THIS
		
		// TODO RETURN SUB MATRIX
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
	
	private double probabilityOfData(Reads reads, 
									 Transcripts ts,
									 ExpectedHiddenData z,
									 ExpressionLevels pEl,
									 SubstitutionMatrix pM)
	{
		double p = 1.0;
		
		for (Sequence r : reads.getSequences())
		{
			// TODO CALCULATE THIS SHIT
		}
		
		return p;
	}
}
