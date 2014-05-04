package hmm.algorithms;

import hmm.HMM;
import hmm.State;

import java.util.Collection;
import java.util.ArrayList;

import pair.Pair;


public class BackwardAlgorithm 
{	
	private static int debug = 0;
	
	public static Pair<Double, DpMatrix> run(HMM model, String sequence)
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
				
		return new Pair<Double, DpMatrix>(finalProb, dpMatrix);
	}

	public static double runIteration(DpMatrix dpMatrix, 
									HMM model, 
									String sequence)
	{			
		
		for (int t = dpMatrix.getNumColumns() - 2; t >= 0; t--)
		{				
			/*
			 * Compute for non-silent states
			 */
			for (State currState : model.getStates())
			{
				if (!currState.isSilent())
				{
					double sum = 0;
					
					for (State forwardState : model.getStates())
					{
						double eProb = model.getEmissionProb(forwardState.getId(),
								  		Character.toString(sequence.charAt(t)));
						
						double bValue = dpMatrix.getValue(forwardState, t+1);
						
						double tProb  = model.getTransitionProb(currState.getId(),
																forwardState.getId());
									
						sum += (tProb * eProb * bValue);
					}
					
					// Set the new value in the DP matrix
					dpMatrix.setValue(currState, t, sum);
				}
			}
			
			/*
			 * Compute silent states
			 */
			ArrayList<State> sortedSilent = model.getSortedSilentStates();
			for (int j = sortedSilent.size() - 1; j >= 0; j--)
			{
				State currState = sortedSilent.get(j);
				
				double sum = 0;
				for (State forwardState : model.getStates())
				{
					double bValue = dpMatrix.getValue(forwardState, t);
					
					double tProb  = model.getTransitionProb( currState.getId(),
															 forwardState.getId() 
														   );				
					sum += (tProb * bValue);
				}
				
				// Set the new value in the DP matrix
				dpMatrix.setValue(currState, t, sum);
			}
			
			if (debug > 1)
				System.out.println(dpMatrix);
		}
				
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
		for (State state : model.getStates())
		{
			if (!state.isSilent())
			{
				dpMatrix.setValue(state, dpMatrix.getNumColumns() - 1, 1.0);
			}
		}
		
		/*
		 * Compute silent states
		 */
		ArrayList<State> sortedSilent = model.getSortedSilentStates();
		for (int j = sortedSilent.size() - 1; j >= 0; j--)
		{
			State currState = sortedSilent.get(j);
			
			double sum = 0;
			for (State forwardState : model.getStates())
			{
				double bValue = dpMatrix.getValue(forwardState, dpMatrix.getNumColumns() - 1);
				
				double tProb  = model.getTransitionProb( currState.getId(),
														 forwardState.getId() 
													  );				
				sum += (tProb * bValue);
			}
			
			// Set the new value in the DP matrix
			dpMatrix.setValue(currState, dpMatrix.getNumColumns() - 1, sum);
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
	
	
}
