package sequence;

import java.util.HashMap;

public class Reads extends SequenceCollection
{
	/**
	 * The name of the species for which these reads belong
	 */
	private String speciesName;

	
	public Reads()
	{
		sequences = new HashMap<String, Sequence>();
	}
	
	public Read getRead(String id)
	{
		return (Read) sequences.get(id);
	}
	
	public void addRead(Read r) 
	{
		sequences.put(r.getId(), r);
	} 

}
