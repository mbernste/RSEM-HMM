package sequence;

import java.util.HashMap;
import java.util.Map;



public class Transcripts extends SequenceCollection
{
	/**
	 * The name of the species for which these transcripts belong
	 */
	private String speciesName;
	
	/**
	 * Maps a transcript name to its ID
	 */
	private Map<String, Integer> namesToId;
		
	/**
	 * Default constructor
	 */
	public Transcripts()
	{
		super();
		namesToId = new HashMap<String, Integer>();
	}
	
	public String getSpecies()
	{
		return this.speciesName;
	}
	
	public Transcript getTranscript(String id)
	{
		return (Transcript) this.sequences.get(id);
	}
	
	public Transcript getTranscript(int index)
	{
		return (Transcript) this.sequences.values().toArray()[index];
	}
}
