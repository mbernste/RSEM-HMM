package sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SequenceCollection 
{
	/**
	 * Maps an id of a transcript to the transcript object
	 */
	protected Map<String, Sequence> sequences;
	

	
	public SequenceCollection()
	{
		sequences = new HashMap<String, Sequence>();
	}
	
	/**
	 * Add a sequence to the collection
	 * @param t
	 */
	public void addSequence(Sequence s) 
	{		
		if (sequences.containsKey(s.getId()))
		{
			System.err.println("Warning! Adding a sequence with ID " + 
					s.getId() + " to a sequence collection, but the " +
					"already contains a sequence with this ID.");
		}
		
		sequences.put(s.getId(), s);
	}
	
	public int size() 
	{
		return sequences.size();
	}
	
	public List<Sequence> getSequences()
	{
		List<Sequence> list = new ArrayList<Sequence>();
		
		for (Sequence s : sequences.values())
		{
			list.add(s);
		}
			
		return list;
	}
	
	public Sequence getSequence(String id) 
	{
		return sequences.get(id);
	}
	
	public String fastaFormat()
	{
		String str = "";
		
		for (Sequence seq : sequences.values())
		{
			str += ">" + seq.getId() + "\n" + seq.getSeq() + "\n";
		}
		
		return str;
	}
	
}
