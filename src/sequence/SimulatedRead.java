package sequence;

import pair.Pair;

public class SimulatedRead extends Read
{
	/**
	 * The data of the transcript that generated this read.  The first element
	 * of the pair is the ID of the transcript.  The second element is the
	 * starting position within the transcript for which this read aligns
	 */
	private Pair<String, Integer> fromTranscriptData;
	
	/**
	 * The data of the transcript for which we are mapping this read to.  The 
	 * first element of the pair is the ID of the transcript.  The second 
	 * element is the starting position within the transcript for which this 
	 * read aligns
	 */
	private Pair<String, Integer> toTranscriptData;

	
	/**
	 * Default constructor
	 */
	public SimulatedRead(String id) 
	{
		this.id = id; 
		fromTranscriptData = new Pair<String, Integer>();
		toTranscriptData = new Pair<String, Integer>();
	}
	
	public SimulatedRead(Pair<String, Integer> fromTranscriptData, 
						 Pair<String, Integer> toTranscriptData, 
						 String sequence)
	{
		super();
		this.sequence = sequence;
		this.fromTranscriptData = fromTranscriptData;
		this.toTranscriptData = toTranscriptData;
	}

	public SimulatedRead(Pair<String, Integer> fromTranscriptData, 
						 String sequence)
	{
		super();
		this.sequence = sequence;
		this.fromTranscriptData = fromTranscriptData;
		this.toTranscriptData = fromTranscriptData;
	}
	
	/**
	 * @return the name of the transcript that generated the read with the 
	 * start position of the alignment
	 */
	public Pair<String, Integer> getFromTranscriptData()
	{
		return this.fromTranscriptData;
	}
	
	/**
	 * @return the name of the transcript that the read with the 
	 * start position of the alignment
	 */
	public Pair<String, Integer> getToTranscriptData()
	{
		return this.toTranscriptData;
	}
	
	public void setFromTranscriptData(String fromTName, Integer fromTStartPos)
	{
		this.fromTranscriptData.setFirst(fromTName);
		this.fromTranscriptData.setSecond(fromTStartPos);
	}
	
	public void setToTranscriptData(String toTName, Integer toTStartPos)
	{
		this.toTranscriptData.setFirst(toTName);
		this.toTranscriptData.setSecond(toTStartPos);
	}
	
	@Override
	public String toString()
	{
		String str = "--- READ " + id + " ---\n";
		
		if (fromTranscriptData != null)
		{
			str += "Simulated from transcript " + fromTranscriptData.getFirst() 
				+ " aligned at "
				+  "position " + fromTranscriptData.getSecond() + "\n";
		
			str += "Maps to transcript " + toTranscriptData.getFirst() 
					+ " aligned at "
					+  "position " + toTranscriptData.getSecond() + "\n";
		
		}
		
		str += sequence + "\n--------------\n";
		
		return str;
	}

}
