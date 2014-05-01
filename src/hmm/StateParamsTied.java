package hmm;

import java.util.HashMap;
import java.util.Map;

public class StateParamsTied extends State
{
	public static Map<String,Map<String, Double>> tiedEmissionParams;
	
	/**
	 * The ID of the parameters that this State uses
	 */
	private String paramsId;
	
	/**
	 * Constructor.
	 * 
	 * @param paramsId the ID of the parameters that this State uses
	 */
	public StateParamsTied(String paramsId)
	{
		this.paramsId = paramsId;
		tiedEmissionParams.put(this.paramsId, new HashMap<String, Double>());
	}
	
	/**
	 * @return the emission probabilities from this state
	 */
	@Override
	public Map<String, Double> getEmissionProbabilites()
	{
		return tiedEmissionParams.get(this.paramsId);
	}
	
	/**
	 * Add an emission probability to this State
	 * 
	 * @param symbol the symbol this state will emmit
	 * @param probability the probability the state will emmit this symbol
	 */
	@Override
	public void addEmission(String symbol, Double probability)
	{
		tiedEmissionParams.get(this.paramsId).put(symbol, probability);
	}
	
	/**
	 * Get the emission probability of a specific symbol from this state
	 * 
	 * @param symbol the symbol of interest
	 * @return the emission probability
	 */
	public double getEmissionProb(String symbol)
	{
		if (tiedEmissionParams.get(this.paramsId).containsKey(symbol))
		{
			return tiedEmissionParams.get(this.paramsId).get(symbol);
		}
		else
		{
			return 0.0;
		}
	}
}
