package hmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StateSilent extends State
{
	/**
	 * Constructor
	 */
	public StateSilent()
	{
		super();
		this.isSilent = true;
	}
	
	@Override
	public Map<String, Double> getEmissionProbabilites()
	{
		System.err.println("Attempting to retrieve emission probabilities on " +
						   "emission probabilities for silent state " + 
						   	this.id);
		return null;
	}
	
	@Override
	public void addEmission(String symbol, Double probability)
	{
		System.err.println("Attempting to add emission probabilities to " +
				   " silent state " + this.id);
	}
	
	@Override
	public double getEmissionProb(String symbol)
	{
		// TODO CHECK IF THIS IS CORRECT
		return 1.0;
	}

}
