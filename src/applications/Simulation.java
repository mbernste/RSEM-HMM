package applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.GammaDistribution;

import pair.Pair;
import rsem.model.ExpressionLevels;
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
	public static void main(String[] args)
	{	
		Transcripts ts = FASTAReader.readTranscripts(args[0]);
		ExpressionLevels trueEl = generateExpressionLevels(ts);

		SimulatedReads rs = simulateReads(ts, trueEl, 20);

		File fastaFile = new File(args[1]);
		FileWriter.writeToFile(fastaFile, rs.fastaFormat());

		File mapFile = new File(args[2]);
		FileWriter.writeToFile(mapFile, rs.mapping());
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
											   int numReads) 
	{
		SimulatedReads reads = new SimulatedReads();

		for (int i = 0; i < numReads; i++)
		{
			/* 
			 * Get random transcript 
			 */
			String tId = trueEl.sampleTranscript();
			Transcript t = ts.getTranscript( tId );

			/*
			 *  Generate random starting position from uniform distribution 
			 */
			int startPos = Common.RNG.nextInt(t.length());
			String readSeq;

			/*
			 *  Generate the read sequence 
			 */
			if (t.length() - startPos < Common.READ_LENGTH)  
			{
				readSeq = t.getSeq().substring(startPos, t.length());
			}
			else
			{
				readSeq = t.getSeq().substring(startPos, startPos + Common.READ_LENGTH);
			}

			reads.addRead(new SimulatedRead(new Pair<String, Integer>(t.getId(), startPos),
					readSeq));
		}

		return reads;
	}

}
