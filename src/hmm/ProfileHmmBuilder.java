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
	
	private String INSERTION_PARAMS;
	
	public HiddenMarkovModel buildHMM(Transcripts ts, Alignments cAligns)
	{
		hmm = new HiddenMarkovModel();

		/*
		 * Set initial emission probabilities for all insertion states 
		 * to the uniform distribution.  These parameters are all tied.
		 */
		for (char symbol : Common.DNA_ALPHABET)
		{
			StateParamsTied.tiedEmissionParams
						   .get(INSERTION_PARAMS)
						   .put(Character.toString(symbol), 0.25);
		}
		
		/*
		 * The start state that attaches to all profile-HMM "mux" states
		 */
		State startState = new StateSilent();
		
		/*
		 * Build a profile HMM structure for each candidate alignment
		 */
		for (Object[] o : cAligns.getAlignments())
		{
			String tId = (String) o[1];
			int startPos = (Integer) o[2];
			boolean orientation = (Boolean) o[3];
			
			buildHMMForAlignment(ts.getTranscript(tId), startPos, orientation);
		}
		
		/*
		 * Keeps track of the hmms constructed.
		 */
		Map<String, Boolean> hmmsConstructed = new HashMap<String, Boolean>();
		
		return hmm;
	}
	
	public State buildHMMForAlignment(Transcript t, 
									 int startPos, 
									 boolean orientation)
	{	
		/*
		 * Build profile-HMM structure
		 */
		buildStates(t, startPos, orientation);
		createInterSeqeunceTransitions(t, startPos, orientation);
		
		/*
		 * Create mux state with connection to all match states
		 */
		State muxState = new StateSilent();
		for (int i = startPos; i < startPos + Common.readLength + Common.bonusLength; i++)
		{
			State mState = hmm.getStateById(matchStateId(t, i, orientation));
			
			muxState.addTransition(new Transition(muxState.getId(),
												  matchStateId(t, i, orientation),
												  1.0 / (Common.readLength + Common.bonusLength)));
		}
		
		return muxState;
	}
	
	public void buildStates(Transcript t, 
			 				int startPos, 
			 				boolean orientation)
	{
		for (int i = startPos; i < startPos + Common.readLength + Common.bonusLength; i++)
		{		
			String mId = matchStateId(t, i, orientation);
			String dId = deleteStateId(t, i, orientation);
			String iId = insertStateId(t, i, orientation);
			
			/*
			 * If the HMM already has states for this region, then we don't 
			 * continue creating states.  Instead connect the current
			 * construction to the next construction.
			 */
			if (hmm.states.containsState(mId))
			{
				// TODO ATTACH THE TWO CONSTRUCTIONS
				
				return;
			}
			
			char matchedSymbol = t.getSeq().charAt(i);
			
			State mState =  createMatchState(mId, matchedSymbol, i, orientation);
			State iState =  createInsertState(iId, i, orientation);
			State dState =  createDeleteState(dId, i, orientation);
	
			hmm.states.addState(mState);
			hmm.states.addState(iState);
			hmm.states.addState(dState);
		}	
	}
	
	public State createMatchState(String mId, 
					 			  char matchedSymbol, 
					 			  int startPos, 
					 			  boolean orient)
	{
		State mState = new State();
		mState.setId(mId);
		
		/*
		 * Set initial emission probabilities based on teh symbol in the
		 * transcript
		 */
		for (char symbol : Common.DNA_ALPHABET)
		{
			if (symbol == matchedSymbol)
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
		
		return mState;
	}
	
	
	public State createInsertState(String sId, int startPos, boolean orient)
	{
		State iState = new StateParamsTied(INSERTION_PARAMS);
		iState.setId( sId );
		return iState;
	}
	
	public State createDeleteState(String dId, int startPos, boolean orient)
	{
		State dState = new State();
		dState.setId( dId );
		
		/*
		 * Set initial emission probabilities to the uniform distribution
		 */
		for (char symbol : Common.DNA_ALPHABET)
		{
			dState.addEmission(Character.toString(symbol), 0.0);
		}
		
		return dState;
	}
	
	
	public void createInterSeqeunceTransitions(Transcript t, 
											   int startPos, 
											   boolean orientation)
	{
		for (int i = startPos; i < startPos + Common.readLength + Common.bonusLength; i++)
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
