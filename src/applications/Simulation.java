package applications;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.GammaDistribution;

import pair.Pair;
import rsem.model.ExpressionLevels;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedRead;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;

import common.Common;
import common.FileWriter;

import data.readers.FASTAReader;


public class Simulation 
{
	public static int MAX_INSERTS_PER_HUNDRED;
	public static int MAX_DELETES_PER_HUNDRED;
	public static double GEOM_PARAM = 0.1828;
	
	public static void main(String[] args)
	{	
		try
		{
			boolean indels = Boolean.parseBoolean(args[0]);
			MAX_INSERTS_PER_HUNDRED = Integer.parseInt(args[1]);
			MAX_DELETES_PER_HUNDRED = Integer.parseInt(args[2]);
			
			Common.readLength = Integer.parseInt(args[3]);
			int numReads = Integer.parseInt(args[4]);
			
			Transcripts ts = FASTAReader.readTranscripts(args[5]);
			ExpressionLevels generatorEl = generateExpressionLevels(ts);
	
			SimulatedReads rs = simulateReads(ts, generatorEl, numReads, indels);
			
			ExpressionLevels trueEl = computeTrueExpressionLevels(rs, ts);
	
			File fastaFile = new File(args[6]);
			FileWriter.writeToFile(fastaFile, rs.fastaFormat());
	
			File mapFile = new File(args[7]);
			FileWriter.writeToFile(mapFile, rs.mapping());
			
			File elFile = new File(args[8]);
			FileWriter.writeToFile(elFile, trueEl.toString());
		}
		catch(NumberFormatException e)
		{
			System.out.println("Usage:");
			System.out.println("<indels = true/false>\n " +
							   "<max inserts per 100 bases>\n " +
							   "<max deletes per 100 bases>\n " +
							   "<read length>\n " +
							   "<num reads>\n " +
							   "<Transcript FASTA file>\n " +
							   "<Dest reads FASTA file name>\n " +
							   "<map file name>\n " +
							   "<expression level file name>\n");
		}
	}

	public static ExpressionLevels generateExpressionLevels(Transcripts ts)
	{
		GammaDistribution gDist = new GammaDistribution(1, 1);

		Map<String, Double> params = new HashMap<String, Double>();

		/*
		 * For each transcript, sample from a gamma distribution
		 */
		for (Sequence t : ts.getSequences())
		{
			params.put(t.getId(), new Double( gDist.sample() ) );
		}

		/*
		 * Normalize each sample from the gamma distribution in order to make
		 * a valid sample from the dirichlet distribution
		 */
		double sum = 0.0;
		for (Double val : params.values())
		{
			sum += val;
		}

		for (Entry<String, Double> e : params.entrySet())
		{
			params.put(e.getKey(), e.getValue() / sum);
		}

		/*
		 * Build expression levels
		 */
		ExpressionLevels el = new ExpressionLevels(ts);
		for (Entry<String, Double> e : params.entrySet())
		{
			String tId = e.getKey();
			el.setExpressionLevel(tId, params.get(tId));
		}

		return el;
	}

	/**
	 * Simulate reads from the transcript set based on a "true" abundance level
	 * of the transcripts
	 * 
	 * @param ts the set of reference transcripts from which to simulate the
	 * reads
	 * @param trueEl the true expression levels
	 * @param numReads the number of reads to simulate
	 * @return the set of simulated reads
	 */
	public static SimulatedReads simulateReads(Transcripts ts,
											   ExpressionLevels trueEl,
											   int numReads,
											   boolean indels) 
	{
		SimulatedReads reads = new SimulatedReads();


		Transcripts sourceTs = null;
		
		/*
		 * If indel-mode, then we need to augment the transcript set to
		 * make insertions, deletions, and point mutations.
		 */
		if (indels)
		{
			sourceTs = augmentTranscripts(ts);
		}
		else
		{
			sourceTs = ts;
		}
		
		for (int i = 0; i < numReads; i++)
		{	
			/* 
			 * Get random transcript 
			 */
			String tId = trueEl.sampleTranscript();
			Transcript t = sourceTs.getTranscript( tId );
			String tSeq = t.getSeq();
			
			/*
			 * Randomly use the reverse compliment
			 */
			if (Common.RNG.nextBoolean())
			{
				tSeq = Common.reverseCompliment(tSeq);
			}

			/*
			 *  Generate random starting position from uniform distribution 
			 */
			int startPos = Common.RNG.nextInt(t.length() - Common.readLength);
			
			/*
			 *  Generate the read sequence 
			 */
			String readSeq = t.getSeq().substring(startPos, 
										   		  startPos + Common.readLength);
			
				
			
			reads.addRead(new SimulatedRead(new Pair<String, Integer>(t.getId(), 
																	  startPos), 
																	  readSeq));
		}

		return reads;
	}
	
	public static Transcripts augmentTranscripts(Transcripts ts)
	{
		Transcripts augTs = new Transcripts();
		
		for (Sequence t : ts.getSequences())
		{
			int numInserts = Common.RNG.nextInt(((t.length() / 100) + 1) * MAX_INSERTS_PER_HUNDRED);
			int numDeletes = Common.RNG.nextInt(((t.length() / 100) + 1) * MAX_DELETES_PER_HUNDRED);
			
			String seq = t.getSeq();
			for (int i = 0; i < numDeletes; i++)
			{
				seq = makeDeletion(seq);
			}
			
			for (int i = 0; i < numInserts; i++)
			{
				seq = makeInsertion(seq);
			}

			Transcript newT = new Transcript(t.getId());
			newT.setSeq(seq);
			augTs.addSequence(newT);
		}
		
		return augTs;
	}
	
	public static String makeDeletion(String seq)
	{
		int rLength = sampleGeometricDistribution(GEOM_PARAM);
		
		if (rLength < seq.length())
		{
			int index = Common.RNG.nextInt(seq.length() - rLength);		
			return seq.substring(0, index) + seq.substring(index+rLength, 
														   seq.length());
		}
		else
		{
			return seq;
		}
	}
	
	
	public static String makeInsertion(String seq)
	{
		int rLength = sampleGeometricDistribution(GEOM_PARAM);
		int index = Common.RNG.nextInt(seq.length());
		
		String insert = "";
		for (int i = 0; i < rLength; i++)
		{
			insert += Common.DNA_ALPHABET[Common.RNG.nextInt(4)];
		}
		
		return seq.substring(0, index) + insert + seq.substring(index, 
																seq.length());
	}
	
	public static int sampleGeometricDistribution(double param)
	{
		int result = 1;
		while (true)
		{
			if (Common.RNG.nextDouble() < param)
				return result;
			else
				result++;
		}
	}
	
	public static ExpressionLevels computeTrueExpressionLevels(SimulatedReads rs,
															   Transcripts ts)
	{
		ExpressionLevels el = new ExpressionLevels(ts);

		Map<String, Double> totals = new HashMap<String, Double>();
		for (Sequence t : ts.getSequences())
		{
			totals.put(t.getId(), 0.0);
		}
		
		for (Sequence r : rs.getSequences())
		{
			SimulatedRead read = (SimulatedRead) r;
			totals.put(read.getFromTranscriptData().getFirst(),
					   totals.get( read.getFromTranscriptData().getFirst()) + 1.0);
		}
		
		for (Entry<String, Double> e : totals.entrySet())
		{
			el.setExpressionLevel(e.getKey(), e.getValue() / rs.size());
		}
			
		return el;
	}
}
