package hmm.algorithms;

import hmm.HMM;
import hmm.State;

import java.util.Collection;
import java.util.ArrayList;


public class BackwardAlgorithm 
{	
	public static void run(HMM model, String sequence)
	{
		DpMatrix dpMatrix = new DpMatrix(model, sequence);
	
		/*
		 *  Initialize the matrix
		 */
		intiailize(dpMatrix, model);
			
		/*
		 *  Run the algorithm
		 */
		Double finalProb = runIteration(dpMatrix, model, sequence);
		
		printResults(dpMatrix, model, sequence, finalProb);
	}

	public static Double runIteration(DpMatrix dpMatrix, 
									HMM model, 
									String sequence)
	{	
		
		// Get the states
		Collection<State> states = model.getStateContainer().getStates();
		
		//********************************************************************
		// Fill in the Dynamic Programming matrix
		//********************************************************************
		
		for (int i = dpMatrix.getNumColumns() - 2; i >= 0; i--)
		{	
			System.out.println("I " + i);
			
			/*
			 * Compute for non-silent states
			 */
			for (State currState : states)
			{
				if (!currState.isSilent())
				{
					double sum = 0;
					
					for (State forwardState : states)
					{
						double eProb = model.getEmissionProb(forwardState.getId(),
								  		Character.toString(sequence.charAt(i)));
						
						double bValue = dpMatrix.getValue(forwardState, i+1);
						
						double tProb  = model.getTransitionProb(currState.getId(),
																forwardState.getId());
									
						sum += (tProb * eProb * bValue);
					}
					
					// Set the new value in the DP matrix
					dpMatrix.setValue(currState, i, sum);
				}
			}
			
			/*
			 * Compute silent states
			 */
			for (State currState : states)
			{
				if (currState.isSilent())
				{
					double sum = 0;
					for (State forwardState : states)
					{
						double bValue = dpMatrix.getValue(forwardState, i);
						
						double tProb  = model.getTransitionProb( currState.getId(),
																 forwardState.getId() 
															  );				
						sum += (tProb * bValue);
					}
					
					// Set the new value in the DP matrix
					dpMatrix.setValue(currState, i, sum);
				}
			}
		}
		
		System.out.println(dpMatrix + "\n");
		
		return dpMatrix.getValue(model.getBeginState(), 0);		
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
		
		
		/*
		 * If no end state, then set the probability at the last time step 
		 * for all states should be 1.0
		 */
		for (State state : model.getStateContainer().getStates())
		{
			if (!state.isSilent())
			{
				dpMatrix.setValue(state, dpMatrix.getNumColumns() - 1, 1.0);
			}
		}
		
		// TODO THIS IS WHEN THE END STATE EXISTS
		/*
		for (State state : model.getStateContainer().getStates())
		{
			if (state.transitionExists(model.getEndStateId()))
			{				
				dpMatrix.setValue(state, 
								  dpMatrix.getNumColumns() - 1, 
								  model.getTransitionProb(state.getId(), 
														  model.getEndStateId())); 
			}
		}*/
	}
	
	private static void printResults(DpMatrix dpMatrix,
									HMM model,
									String sequence,
									Double finalProb)
	{
		for (int timestep = sequence.length() - 1; timestep >= 1; timestep--)
		{
			for (State state : model.getStateContainer().getStates())
			{
				if (!state.equals(model.getBeginState()) &&
					!state.equals(model.getEndState()))
				{
					System.out.println("beta for state " + state.getId() + 
						" time " + (timestep) + ": " + 
						dpMatrix.getValue(state, timestep));
				}
			}
		}
		
		System.out.println("Backward probability: " + finalProb);
	}
}
