package sequence;

import pair.Pair;

public class SimulatedReads extends Reads
{	
	@Override
	public void addRead(Read r)
	{
		if (!(r instanceof SimulatedRead))
		{
			throw new RuntimeException("Reads added to SimuluatedReads object" +
					" must be of type SimulatedRead.");
		}
		
		super.addRead(r);
	}
	
	public SimulatedRead getSimulatedRead(String id)
	{
		return (SimulatedRead) super.getRead(id);
	}
	
	public String mapping()
	{
		String str = "";
				
		for (Sequence r : sequences.values())
		{
			SimulatedRead simR = (SimulatedRead) r;
		
			Pair<String, Integer> fromData = simR.getFromTranscriptData();
			Pair<String, Integer> toData = simR.getToTranscriptData();
						
			str += simR.getId() + "," + fromData.getFirst() + "," + 
				   fromData.getSecond() + "," + toData.getFirst() + "," +
				   toData.getSecond() + "\n";
		}
		
		return str;
	}
	
	public SimulatedRead getRead(int id)
	{
		return (SimulatedRead) sequences.get(id);
	}
	
}
