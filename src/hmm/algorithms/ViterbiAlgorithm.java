package hmm.algorithms;

import hmm.HMM;
import hmm.State;

import java.util.ArrayList;

import pair.Pair;

public class ViterbiAlgorithm 
{	
	public static void run(HMM model, String sequence)
	{
		DpMatrix dpMatrix = new DpMatrix(model, sequence);
	
		// Initialize the matrix
		intiailize(dpMatrix, model);
		
		// Run the algorithm
		Pair<Double, State> result = runIteration(dpMatrix, model, sequence);

		printResults(dpMatrix, model, sequence, result);
	}

	public static Pair<Double, State> runIteration(DpMatrix dpMatrix, 
									        	   HMM model, 
									        	   String sequence)
	{	
		
		// Get the states
		ArrayList<State> states = model.getStateContainer().getStates();
		
		//********************************************************************
		// Fill in the Dynamic Programming matrix
		//********************************************************************
		
		for (int i = 0; i < sequence.length(); i++)
		{	
			for (State currState : states)
			{
				// The emission probability of the current symbol at the ith
				// time step
				double eProb = model.getEmissionProb(currState.getId(), 
						  new Character(sequence.charAt(i)).toString());
				
				// Initialize the max
				double max = -1;
				State maxState = null;
				
				for (State lastState : states)
				{
					double vValue = dpMatrix.getValue(lastState, i);
					
					double tProb  = model.getTransitionProb(lastState.getId(), 
							   currState.getId());
					
					if ((vValue * tProb) > max)
					{	
						// TODO: THIS IS MESSY! NEEDS A BETTER SOLUTION!
						
						// If there is zero probability of being in a certain state
						// at a certain time step, we want to set the back pointer to 
						// a previous state for which a transition exists
						if (model.transitionExists(lastState.getId(), currState.getId()) || 
							lastState.equals(model.getEndState()))
						{
							max = vValue * tProb;
							maxState = lastState;
						}
					}
					
				}
				
				double newVValue = max * eProb;
				
				// Set the new value in the DP matrix
				dpMatrix.setValue(currState, i+1, newVValue);
				dpMatrix.setPreviousState(currState, i+1, maxState);				
			}
		}
		
		//********************************************************************
		// Compute the final probability for transitioning to the end state
		// from the end of the sequence
		//********************************************************************
		
		// Initialize the max
		double max = -1;
		State maxState = null;
		
		for (State state : states)
		{
			double vValue = dpMatrix.getValue(state, sequence.length());
			
			double tProb  = model.getTransitionProb(state.getId(), 
					   								model.getEndStateId());
			
			if ((vValue * tProb) > max)
			{
				max = vValue * tProb;
				maxState = state;
			}
		}
		
		// Return the result
		Pair<Double, State> result = new Pair<Double, State>();
		result.setFirst(max);
		result.setSecond(maxState);
		return result;
	}
	
	/**
	 * Initialize the dynamic programming matrix
	 * 
	 * @param dpMatrix the dynamic programming matrix object
	 * @param model the HMM object
	 */
	public static void intiailize(DpMatrix dpMatrix, HMM model)
	{
		
		// Set all elements to 0.0
		for (State state : model.getStateContainer().getStates())
		{
			dpMatrix.setValue(state, 0, 0.0);
		}
		
		// Set coordnate (0,0) to 1.0 corresponding to 100% probability
		// that we are in the begin state at time step 0
 		State beginState = model.getBeginState();
		dpMatrix.setValue(beginState, 0, 1.0);
	}
	
	private static void printResults(DpMatrix dpMatrix,
									HMM model,
									String sequence,
									Pair<Double, State> viterbiResult)
	{
		//******************************************************************
		// Print probabilities and max states
		//******************************************************************
		
		for (int timestep = 1; timestep <= sequence.length(); timestep++)
		{
			for (State state : model.getStateContainer().getStates())
			{
				if (!state.equals(model.getBeginState()) &&
					!state.equals(model.getEndState()))
				{
					System.out.println("viterbi for state " + state.getId() + 
						" time " + (timestep) + ": " + 
						dpMatrix.getValue(state, timestep) + " maxstate " +
						dpMatrix.getPreviousState(state, timestep).getId());
				}
			}
		}
		
		System.out.println("Viterbi probability: " + viterbiResult.getFirst());
		
		//******************************************************************
		// Construct the path
		//******************************************************************
		
		System.out.println("Viterbi Path:");
		ArrayList<Pair<State, Character>> path =
				new ArrayList<Pair<State, Character>>();
		
		State currState = viterbiResult.getSecond();
		for (int i = sequence.length() - 1; i >= 0; i--)
		{
			Pair<State, Character> pair = new Pair<State, Character>();
			pair.setFirst(currState);
			pair.setSecond( sequence.charAt(i));
			path.add(pair);
			
			currState = dpMatrix.getPreviousState(currState, i + 1);
		}
		
		Pair<State, Character> pair = new Pair<State, Character>();
		pair.setFirst(currState);
		pair.setSecond('\0');
		path.add(pair);
		
		
		//******************************************************************
		// Print the path
		//******************************************************************
		
		for (int i = path.size() - 1; i >= 0; i--)
		{
			System.out.print(path.get(i).getFirst().getId());
			if (path.get(i).getSecond() != '\0')
			{
				System.out.print( " -> " + 
						   path.get(i).getSecond());
			}
			System.out.print("\n");				  
		}
	}
}
