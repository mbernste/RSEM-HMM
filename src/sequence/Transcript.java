package sequence;


public class Transcript extends Sequence
{
	/**
	 * Coordinates assignment of unique integer ID's
	 */
	private static int nextId = 0;
	
	/**
	 * Default constructor
	 */
	public Transcript() 
	{
		this.id = Integer.toString(nextId++);
	}
	
	public Transcript(String id)
	{
		this.id = id;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param name the name of the sequence
	 * @param sequence the sequence
	 */
	public Transcript(String id, String sequence)
	{
		this(id);
		this.sequence = sequence;
	}

}
