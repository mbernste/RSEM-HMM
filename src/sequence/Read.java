package sequence;

public class Read extends Sequence
{
	private static int nextId = 0;
	
	public Read() 
	{
		this.id = Integer.toString(nextId++);
	}
	
	public Read(String id)
	{
		this.id = id;
	}
	
	public Read(String id, String sequence)
	{
		this(id);
		this.sequence = sequence;
	}
}
