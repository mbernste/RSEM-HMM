package applications;

import java.io.File;
import java.util.ArrayList;

import common.Common;
import common.FileWriter;

import sequence.Transcripts;

import data.readers.FASTAReader;

/**
 * This program randomly sample transcripts from a large set of transcripts 
 * and writes them to a new FASTA file format.
 * <br>
 * This is useful for running RSEM read simulations on a subset of the 
 * transcripts
 * 
 * @author matthewbernstein
 *
 */
public class SampleTranscripts 
{
	public static void main(String[] args)
	{
		/*
		 * Number of transcripts to sample
		 */
		int numTranscripts = Integer.parseInt(args[0]);
		
		/*
		 * Read full transcripts set
		 */
		Transcripts ts = FASTAReader.readTranscripts(args[1]);
		
		/*
		 * Randomly sample transcript IDs
		 */
		ArrayList<String> tIds = new ArrayList<String>();
		while (tIds.size() < numTranscripts)
		{
			String tId = ts.getTranscript( Common.RNG.nextInt(ts.size()) ).getId();
			if (!tIds.contains(tId))
			{
				tIds.add(tId);
			}
		}
		
		/*
		 * Construct transcript set from sample IDs
		 */
		Transcripts sample = new Transcripts();
		for (String id : tIds)
		{
			sample.addSequence(ts.getTranscript(id));
		}
		
		/*
		 * Write transcripts to FASTA file 
		 */
		FileWriter.writeToFile(new File(args[2]), sample.fastaFormat());
		
	}

}
