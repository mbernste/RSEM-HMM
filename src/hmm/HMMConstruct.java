package hmm;

import java.util.ArrayList;
import java.util.Map;

public class HMMConstruct 
{
	/*
	 * The entire HMM
	 */
	protected HMM mainHMM;
	
	/*
	 * Used for storing all of the counts for the E-Step
	 */
	protected HMMParameterCounts counter;
	
	/*
	 * Sub HMM structures which will be used for the Forward and Backward 
	 * algorithms in order to generate the counts.
	 */
	protected Map<String, HMM> subHMMs;	
	
	public HMM getHMM()
	{
		return mainHMM;
	}
	
}
