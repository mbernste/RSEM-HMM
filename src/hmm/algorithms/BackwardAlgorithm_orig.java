package hmm.algorithms;

import hmm.HMM;
import hmm.State;

import java.util.ArrayList;


public class BackwardAlgorithm_orig 
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
		ArrayList<State> states = new ArrayList<State>( model.getStateContainer().getStates() );
		
		//********************************************************************
		// Fill in the Dynamic Programming matrix
		//********************************************************************
		
		for (int i = sequence.length() - 2; i >= 0; i--)
		{	
			for (State currState : states)
			{
				double sum = 0;
				
				for (State forwardState : states)
				{
					double eProb = model.getEmissionProb(forwardState.getId(),
							  		Character.toString(sequence.charAt(i+1)));
					
					double bValue = dpMatrix.getValue(forwardState, i+2);
					
					double tProb  = model.getTransitionProb( currState.getId(),
															forwardState.getId() 
														  );
								
					sum += (tProb * eProb * bValue);
				}
				
				// Set the new value in the DP matrix
				dpMatrix.setValue(currState, i+1, sum);
				
			}
		}
		
		//********************************************************************
		// Compute the final probability for transitioning to the end state
		// from the end of the sequence
		//********************************************************************
		
		double sum = 0;
		
		for (State state : states)
		{
			double eProb = model.getEmissionProb(state.getId(), 
					  new Character(sequence.charAt(0)).toString());
			
			double bValue = dpMatrix.getValue(state, 1);
			
			double tProb  = model.getTransitionProb( model.getBeginStateId(),
													state.getId() 
												  );
						
			sum += (tProb * eProb * bValue);
						
		}
		
		return sum;
		
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
		 * If no end state, then set the probability at the last time step to 
		 * being uniform over all states.
		 * 
		 * We first count all non-silent states.
		 */
		int numNonSilentStates = 0; 
		for (State state : model.getStateContainer().getStates())
		{
			if (!state.isSilent()) numNonSilentStates++;
		}
		
		double pInit = 1.0 / numNonSilentStates;
		for (State state : model.getStateContainer().getStates())
		{
			if (!state.isSilent())
			{
				dpMatrix.setValue(state, dpMatrix.getNumColumns() - 1, pInit); 
			}
		}
		
		System.out.println(dpMatrix);
		
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
