package hmm;

import java.util.Map;

public class HMMParameterCounts extends HMM 
{
	public HMMParameterCounts(HMM hmm)
	{
		super();
		
		for (State s : hmm.getStates())
		{
			this.states.addState(new State(s));
		}
	}
	
	public void resetCounts()
	{
		for (State s : this.states.getStates())
		{
			for (Transition t : s.getTransitions())
			{
				t.setTransitionProbability(0.00001);
			}
			
			for (String str : s.getEmissionProbabilites().keySet())
			{
				s.addEmission(str, 0.00001);
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
				   .put(symbol, value + currVal);
	}

}
