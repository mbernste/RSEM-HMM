package hmm;

import java.util.HashMap;
import java.util.Map;

import common.LogP;


public class HMMParameterCounts extends HMM 
{
	public Map<String, String> paramsKeys;

	public final static String PARAMS_KEY_SUFFIX = "_COPY";
	
	public HMMParameterCounts(HMM hmm)
	{
		super();
		
		paramsKeys = new HashMap<String, String>();
		String paramsKeyCopy = null;
		
		for (State s : hmm.getStates())
		{
			if (s instanceof StateParamsTied)
			{
				String paramsKey = ((StateParamsTied) s).getParamsKey();
				paramsKeyCopy = paramsKey + PARAMS_KEY_SUFFIX;
				paramsKeys.put(paramsKey, paramsKeyCopy);
				
				this.states.addState(new StateParamsTied(s, paramsKeyCopy));
			}
			else
			{
				this.states.addState(new State(s));
			}
		}
	}
	
	public void resetCounts()
	{
		for (State s : this.states.getStates())
		{
			for (Transition t : s.getTransitions())
			{
				t.setTransitionProbability(LogP.ln(0.0001));
			}
			for (String str : s.getEmissionProbabilites().keySet())
			{
				s.addEmission(str, LogP.ln(0.0001));
			}
		}
	}
	
	public void incrementTransitionCount(String originId, 
										 String destId, 
										 double value)
	{	
		this.states.getStateById(originId)
				   .getTransition(destId)
				   .incrementTransitionValue(value);
	}
	
	public void incrementEmissionCount(String stateId, String symbol, double value)
	{
		double currVal = this.states.getStateById(stateId)
									.getEmissionProb(symbol);
		
		this.states.getStateById(stateId)
				   .getEmissionProbabilites()
				   .put(symbol, LogP.sum(currVal, value));
	}
	
	public Map<String, Double> getTiedEmissionPrams(String paramsKey)
	{
		return StateParamsTied.tiedEmissionParams.get(paramsKeys.get(paramsKey));
	}

}
