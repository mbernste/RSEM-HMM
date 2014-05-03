package hmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.Common;
import data.readers.Alignments;

import sequence.Transcript;
import sequence.Transcripts;

public class ProfileHmmBuilder 
{
	/*
	 * Initial probability of emission when symbol does not match transcript
	 */
	private static final double NON_MATCH_P = 0.05;
	
	/*
	 * The HMM under construction
	 */
	private HiddenMarkovModel hmm;
	
	/*
	 * Keep track of all mux states
	 */
	private ArrayList<State> muxStates;
	
	private String INSERTION_PARAMS_ID = "Inert_Params";
	
	public HiddenMarkovModel buildHMM(Transcripts ts, Alignments cAligns)
	{
		hmm = new HiddenMarkovModel();
		muxStates = new ArrayList<State>();
		
		StateParamsTied.tiedEmissionParams.put(INSERTION_PARAMS_ID, 
											   new HashMap<String, Double>());
		
		/*
		 * Set initial emission probabilities for all insertion states 
		 * to the uniform distribution.  These parameters are all tied.
		 */
		for (char symbol : Common.DNA_ALPHABET)
		{
			StateParamsTied.tiedEmissionParams
						   .get(INSERTION_PARAMS_ID)
						   .put(Character.toString(symbol), 0.25);
		}		
		
		/*
		 * The start state that attaches to all profile-HMM "mux" states
		 */
		State startState = new StateSilent();
		startState.setId("START");
		hmm.states.addState(startState);
		
		/*
		 * Build a profile HMM structure for each candidate alignment
		 */
		for (Object[] o : cAligns.getAlignments())
		{
			String tId = (String) o[1];
			int startPos = (Integer) o[2];
			boolean orientation = (Boolean) o[3];
			
			buildHMMForAlignment(ts.getTranscript(tId), 
												  startPos, 
												  orientation);	
		}
		
		/*
		 * Normalize the transition probabilities for all mux states.
		 * Add transitions from the start state to all of the mux states,
		 * then normalize these probabilities.
		 */
		for (State m : muxStates)
		{
			m.normalizeTransitionProbabilities();
			startState.addTransition(new Transition(startState.getId(),
													m.getId(),
													1.0));
		}
		startState.normalizeTransitionProbabilities();
		
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
		System.out.println("\nSTART BUILDING TRANS " + t.getId() + " STARTING AT " + startPos);
		buildStates(t, startPos, orientation);
		createInterSeqeunceTransitions(t, startPos, orientation);
		
		/*
		 * Create mux state with connection to all match states
		 */
		String muxId = "MUX_" + t.getId();
		State muxState = hmm.getStateById(muxId);
		if (muxState == null)
		{
			muxState = new StateSilent();
			muxState.setId("MUX_" + t.getId() );
			muxStates.add(muxState);
			hmm.states.addState(muxState);
		}
		
		for (int i = startPos; i < startPos + 2*Common.readLength; i++)
		{
			if (i >= t.length()) break;			
			
			// TODO MAKE SURE MUX TRANSITION PROBABILITIES ARE UNIFORM
			muxState.addTransition(new Transition(muxState.getId(),
												  matchStateId(t, i, orientation),
												  1.0));
		}

		return muxState;
	}
	
	public void buildStates(Transcript t, 
			 				int startPos, 
			 				boolean orientation)
	{
		for (int i = startPos; i < startPos +  2*Common.readLength; i++)
		{				
			if (i >= t.length()) 
			{
				break;
			}
			
			String mId = matchStateId(t, i, orientation);
			String dId = deleteStateId(t, i, orientation);
			String iId = insertStateId(t, i, orientation);
			
			/*
			 * If the HMM already has states for this region, then we don't 
			 * creating states at this position.
			 */
			if (!hmm.states.containsState(mId))
			{
				char matchedSymbol = t.getSeq().charAt(i);
			
				State dState =  createDeleteState(dId, i, orientation);
				State iState =  createInsertState(iId, i, orientation);
				State mState =  createMatchState(mId, matchedSymbol, i, orientation);
			
				hmm.states.addState(dState);
				hmm.states.addState(iState);
				hmm.states.addState(mState);
			}
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
		State iState = new StateParamsTied(INSERTION_PARAMS_ID);
		iState.setId( sId );
		return iState;
	}
	
	public State createDeleteState(String dId, int startPos, boolean orient)
	{
		State dState = new StateSilent();
		dState.setId( dId );
			
		return dState;
	}
	
	public void createInterSeqeunceTransitions(Transcript t, 
											   int startPos, 
											   boolean orientation)
	{
		
		for (int i = startPos; i < (startPos + 2*Common.readLength) - 1; i++)
		{
			if (i >= t.length() - 1) break;
			
			/*
			 * Current and next deletion states at given position
			 */
			State dState = hmm.getStateById(deleteStateId(t, i, orientation));
			State dNextState = hmm.getStateById(deleteStateId(t, i+1, orientation));
			
			/*
			 * Current insertion state at given position
			 */
			State iState = hmm.getStateById(insertStateId(t, i, orientation));
			
			/*
			 * Current match state at given position
			 */
			State mState = hmm.getStateById(matchStateId(t, i, orientation));
			State mNextState = hmm.getStateById(matchStateId(t, i+1, orientation));
				
			double p = 1.0 /3.0;
			dState.addTransition(new Transition(dState.getId(), 
												dNextState.getId(),
												p));
			
			dState.addTransition(new Transition(dState.getId(), 
												mNextState.getId(),
												p));

			iState.addTransition(new Transition(iState.getId(), 
												dNextState.getId(),
												p));
			
			iState.addTransition(new Transition(iState.getId(), 
												mNextState.getId(),
												p));
			
			mState.addTransition(new Transition(mState.getId(), 
												dNextState.getId(),
												p));
			
			mState.addTransition(new Transition(mState.getId(), 
												mNextState.getId(),
												p));
		}
		
		for (int i = startPos; i < startPos + 2*Common.readLength; i++)
		{
			if (i >= t.length()) break;
			
			State dState = hmm.getStateById(deleteStateId(t, i, orientation));
			State iState = hmm.getStateById(insertStateId(t, i, orientation));
			State mState = hmm.getStateById(matchStateId(t, i, orientation));
			
			double p = 1.0 /3.0;
			dState.addTransition(new Transition(dState.getId(), 
												iState.getId(),
												p));
			
			iState.addTransition(new Transition(iState.getId(), 
												iState.getId(),
												p));
			
			mState.addTransition(new Transition(mState.getId(), 
												iState.getId(),
												p));	
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
