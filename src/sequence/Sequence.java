package sequence;

public class Sequence 
{
	/**
	 * The name of the transcript
	 */
	protected String id;
	
	/**
	 * The sequence of the read itself
	 */
	protected String sequence;
	
	/**
	 * @return the read's sequence
	 */
	public String getSeq()
	{
		return this.sequence;
	}
	
	/**
	 * @param seq the sequence's actual sequence of symbols
	 */
	public void setSeq(String seq)
	{
		this.sequence = seq;
	}
	
	/**
	 * @return the unique integer ID of the sequence
	 */
	public String getId()
	{
		return this.id;
	}
	
	/**
	 * @param id the unique integer ID of the sequence
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	@Override
	public String toString()
	{
		String result = "";
		result += "ID: " + this.id + "\n";
		result += this.sequence + "\n";
		return result;
	}
	
	/**
	 * @return the length of the transcript
	 */
	public int length()
	{
		return this.sequence.length();
	}
}
