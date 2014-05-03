package hmm.algorithms;

import hmm.HMM;
import hmm.State;

import java.util.ArrayList;

public class ForwardAlgorithm 
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
		
		/*
		 * Get the states
		 */
		ArrayList<State> states = model.getStateContainer().getStates();
		
		//********************************************************************
		// Fill in the Dynamic Programming matrix
		//********************************************************************
		
		for (int i = 0; i < sequence.length(); i++)
		{	
			for (State currState : states)
			{
				/*
				 *  The emission probability of the current symbol at the ith
				 *  time step
				 */
				double eProb = model.getEmissionProb(currState.getId(), 
						  			Character.toString(sequence.charAt(i)));
				
				/*
				 *  Initialize the summation
				 */
				double sum = 0;
				
				for (State lastState : states)
				{
					double fValue = dpMatrix.getValue(lastState, i);
					
					double tProb  = model.getTransitionProb(lastState.getId(), 
														   currState.getId());
			
					sum += (fValue * tProb);
					
				}
				
				double newFValue = sum * eProb;
				
				/*
				 *  Set the new value in the DP matrix
				 */
				dpMatrix.setValue(currState, i+1, newFValue);
			}
		}
		
		/*
		 * Compute the final probability of the sequence by summing over the
		 * joint probability of observing the sequence (i.e. of being in the 
		 * last time step) in each state.
		 */
		double sum = 0;
		for (State state : states)
		{
			double fValue = dpMatrix.getValue(state, sequence.length());
			sum += fValue;
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
		
		/*
		 *  Set all elements to 0.0
		 */
		for (State state : model.getStateContainer().getStates())
		{
			dpMatrix.setValue(state, 0, 0.0);
		}
		
		/*
		 *  Set coordnate (0,0) to 1.0 corresponding to 100% probability
		 *  that we are in the begin state at time step 0
		 */
 		State beginState = model.getBeginState();
		dpMatrix.setValue(beginState, 0, 1.0);
	}
	
	private static void printResults(DpMatrix dpMatrix,
									HMM model,
									String sequence,
									Double finalProb)
	{
		for (int timestep = 1; timestep <= sequence.length(); timestep++)
		{
			for (State state : model.getStateContainer().getStates())
			{
				if (!state.equals(model.getBeginState()) &&
					!state.equals(model.getEndState()))
				{
					System.out.println("alpha for state " + state.getId() + 
						" time " + (timestep) + ": " + 
						dpMatrix.getValue(state, timestep));
				}
			}
		}
		
		System.out.println("Forward probability: " + finalProb);
	}
}
