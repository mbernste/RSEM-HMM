package hmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * This class implements a single state in the HMM.  Each State object
 * stores all Transition objects that transition from this state to 
 * other states in the model. 
 */
public class State
{
	private Map<String, Double> emissionProbs;
	private String id;
	private ArrayList<Transition> transitions;
	
	/**
	 * Constructor
	 */
	public State()
	{
		transitions = new ArrayList<Transition>();
		emissionProbs = new HashMap<String, Double>();
	}
	
	/**
	 * @return the unique integer ID of this state
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * @param id the unique integer ID of this state
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	/**
	 * @return the emission probabilities from this state
	 */
	public Map<String, Double> getEmissionProbabilites()
	{
		return emissionProbs;
	}
	
	/**
	 * Add an emission probability to this State
	 * 
	 * @param symbol the symbol this state will emmit
	 * @param probability the probability the state will emmit this symbol
	 */
	public void addEmission(String symbol, Double probability)
	{
		emissionProbs.put(symbol, probability);
	}
	
	/**
	 * Get the emission probability of a specific symbol from this state
	 * 
	 * @param symbol the symbol of interest
	 * @return the emission probability
	 */
	public double getEmissionProb(String symbol)
	{
		if (emissionProbs.containsKey(symbol))
		{
			return emissionProbs.get(symbol);
		}
		else
		{
			return 0.0;
		}
	}
	
	public double getTransitionProb(String destId)
	{
		for (Transition trans : transitions)
		{
			if (trans.getDestinationId().equals(destId))
			{
				return trans.getTransitionProbability();
			}
		}
		
		return 0.0;
	}
	
	public boolean transitionExists(String destId)
	{
		for (Transition trans : transitions)
		{
			if (trans.getDestinationId().equals(destId))
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Add an outgoing transition from this state to some other state.
	 *
	 * @param transition the new transition
	 */
	public void addTransition(Transition transition)
	{
		transitions.add(transition);
	}

	/**
	 * @return the array list containing all transition objects moving from 
	 * this state
	 */
	public ArrayList<Transition> getTransitions()
	{
		return transitions;
	}
		
	/**
	 * Find and return a transition from this state to the specified state.  
	 * If this transition does not exist this method returns null.
	 * 
	 * @return the transition that moves from this state to the specified 
	 * state, if such a transition does not exist, this method returns null
	 */
	public Transition findTransition(State nextState)
	{	
		String nextStateId = nextState.getId();
		
		Transition foundTransition = null;
				
		for (int i = 0; i < transitions.size(); i++)
		{
			if (transitions.get(i).getDestinationId().equals(nextStateId))
			{
				foundTransition = (Transition) transitions.get(i);
			}
		}
		
		return foundTransition;
	}
}
