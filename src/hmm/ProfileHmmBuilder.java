package hmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Common;
import data.readers.Alignments;

import sequence.Reads;
import sequence.Sequence;
import sequence.Transcript;
import sequence.Transcripts;

public class ProfileHmmBuilder 
{
	private static final double NON_MATCH_P = 0.05;
	
	private HiddenMarkovModel hmm;
	
	public HiddenMarkovModel buildHMM(Transcripts ts, 
									  Reads rs, 
									  Alignments cAligns)
	{
		hmm = new HiddenMarkovModel();

		
		
		for (Sequence s : ts.getSequences())
		{
			Transcript t = (Transcript) s;
			buildHMMForTranscript(t, Common.FORWARD_ORIENTATION);
		}
		
		/*
		 * Keeps track of the hmms constructed.
		 */
		Map<String, Boolean> hmmsConstructed = new HashMap<String, Boolean>();
		
		for (Object[] o : cAligns.getAlignments())
		{
			String readId = (String) o[0];
			String transId = (String) o[1];
			Integer startPos = (Integer) o[2];
			Boolean orientation = (Boolean) o[3];
			
			//if (hmmsConstructed.containsKey(transId) && 
			//	hmmsConstructed.get(key))
				
		}
			
		return hmm;
	}
	
	public void buildHMMForTranscript(Transcript t, boolean orientation)
	{
		
		createMatchStates(t, orientation);
		createInsertionStates(t, orientation);
		createDeletionStates(t, orientation);
		createTransitions(t, orientation);
	}
	
	
	
	public void createMatchStates(Transcript t, boolean orientation)
	{		
		/*
		 * Create all match states
		 */
		for (int i = 0; i < t.length(); i++)
		{			
			State mState = new State();
			mState.setId( matchStateId(t, i, orientation) );
			
			/*
			 * Set initial emission probabilities based on teh symbol in the
			 * transcript
			 */
			for (char symbol : Common.DNA_ALPHABET)
			{
				if (symbol == t.getSeq().charAt(i))
				{
					mState.addEmission(Character.toString(symbol), 
									   	   1 - (3 * NON_MATCH_P));
				}
				else
				{
					mState.addEmission(Character.toString(symbol), 
										   NON_MATCH_P);
				}
			}
			
			hmm.states.addState(mState);
		}	
	}
	
	public void createInsertionStates(Transcript t, boolean orientation)
	{
		/*
		 * Create all insertion states
		 */
		for (int i = 0; i < t.length(); i++)
		{
			State iState = new State();
			iState.setId( insertStateId(t, i, orientation) );
			
			/*
			 * Set initial emission probabilities to the uniform distribution
			 */
			for (char symbol : Common.DNA_ALPHABET)
			{
				iState.addEmission(Character.toString(symbol), 0.25);
			}
			
			hmm.states.addState(iState);
		}
	}
	
	public void createDeletionStates(Transcript t, boolean orientation)
	{
		/*
		 * Create all deletion states
		 */
		for (int i = 0; i < t.length(); i++)
		{
			State dState = new State();
			dState.setId( deleteStateId(t, i, orientation) );
			
			/*
			 * Set initial emission probabilities to the uniform distribution
			 */
			for (char symbol : Common.DNA_ALPHABET)
			{
				dState.addEmission(Character.toString(symbol), 0.0);
			}
			
			hmm.states.addState(dState);
		}
	}
	
	public void createTransitions(Transcript t, boolean orientation)
	{
		for (int i = 0; i < t.length(); i++)
		{
			State dState = hmm.getStateById(deleteStateId(t, i, orientation));
			State iState = hmm.getStateById(insertStateId(t, i, orientation));
			State mState = hmm.getStateById(matchStateId(t, i, orientation));
			
			dState.addTransition(new Transition(dState.getId(), 
												iState.getId(),
												1.0 / 3.0));
			
			iState.addTransition(new Transition(iState.getId(), 
												iState.getId(),
												1.0 / 3.0));
			
			mState.addTransition(new Transition(mState.getId(), 
												iState.getId(),
												1.0 / 3.0));
			
		}
		
		for (int i = 0; i < t.length() - 1; i++)
		{
			State dState = hmm.getStateById(deleteStateId(t, i, orientation));
			State dNextState = hmm.getStateById(deleteStateId(t, i+1, orientation));
			
			State iState = hmm.getStateById(insertStateId(t, i, orientation));
			
			State mState = hmm.getStateById(matchStateId(t, i, orientation));
			State mNextState = hmm.getStateById(matchStateId(t, i+1, orientation));
			
			dState.addTransition(new Transition(dState.getId(), 
												dNextState.getId(),
												1.0 / 3.0));
			
			dState.addTransition(new Transition(dState.getId(), 
												mNextState.getId(),
												1.0 / 3.0));
			
			iState.addTransition(new Transition(iState.getId(), 
												dNextState.getId(),
												1.0 / 3.0));
			
			iState.addTransition(new Transition(iState.getId(), 
												mNextState.getId(),
												1.0 / 3.0));
			
			mState.addTransition(new Transition(mState.getId(), 
												dNextState.getId(),
												1.0 / 3.0));
			
			mState.addTransition(new Transition(mState.getId(), 
												mNextState.getId(),
												1.0 / 3.0));
			
		}
	}
	
	public String matchStateId(Transcript t, int index, boolean orientation)
	{
		String id = "M_" + index + "_" + t.getId();
		if (orientation == Common.FORWARD_ORIENTATION)
		{
			id += "_F";
		}
		else
		{
			id += "_RC";
		}
		return id;
	}
	
	public String insertStateId(Transcript t, int index, boolean orientation)
	{
		String id = "I_" + index + "_" + t.getId();
		if (orientation == Common.FORWARD_ORIENTATION)
		{
			id += "_F";
		}
		else
		{
			id += "_RC";
		}
		return id;
	}
	
	public String deleteStateId(Transcript t, int index, boolean orientation)
	{
		String id = "D_" + index + "_" + t.getId();
		if (orientation == Common.FORWARD_ORIENTATION)
		{
			id += "_F";
		}
		else
		{
			id += "_RC";
		}
		return id;
	}
	
	
}
