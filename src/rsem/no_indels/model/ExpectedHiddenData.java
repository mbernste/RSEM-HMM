package rsem.no_indels.model;

import java.util.HashMap;
import java.util.Map;

import pair.Pair;

import data.reads.Read;
import data.reads.ReadSet;
import data.transcript.Transcript;
import data.transcript.TranscriptSet;

public class ExpectedHiddenData 
{
	/**
	 * Maps a Read to every transcript and the data strcture that stores the 
	 * expected alignment of this read to the transcript.
	 * 
	 * The inner map maps a Transcript to an expected alignment for the 
	 * read (outer key) to the transcript (inner key).
	 */
	Map<Read, Map<Transcript, ExpectedAlignments>> hiddenData;

	public ExpectedHiddenData(ReadSet rs, TranscriptSet ts)
	{
		hiddenData = new HashMap<Read, Map<Transcript, ExpectedAlignments>>();
		
		for (Read r : rs.getReads())
		{
			for (Transcript t : ts.getTranscripts(0, ts.size()))
			{
				Map<Transcript, ExpectedAlignments> map 
								= new HashMap<Transcript, ExpectedAlignments>();
				map.put(t, new ExpectedAlignments(r, t));
				hiddenData.put(r, map);
			}
		}
	}
	
	public ExpectedAlignments getAlignment(Read r, Transcript t)
	{
		return hiddenData.get(r).get(t);
	}
	
	public void normalize()
	{
		for (Map<Transcript, ExpectedAlignments> map : hiddenData.values())
		{
			double sum = 0;
			for (ExpectedAlignments e : map.values())
			{
				sum += e.sumOfProbabilities();
			}
			
			for (ExpectedAlignments e : map.values())
			{
				e.divideAllProbabilitiesBy(sum);
			}
		}
	}
	
	@Override
	public String toString()
	{
		String str = "";
		
		for (Map<Transcript, ExpectedAlignments> m : hiddenData.values())
		{
			for (ExpectedAlignments e : m.values())
			{
				str += e;
			}
		}
		
		return str;
	}
	
}
