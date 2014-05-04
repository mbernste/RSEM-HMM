package hmm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import common.Common;
import data.readers.Alignments;

import sequence.Reads;
import sequence.Sequence;
import sequence.Transcript;
import sequence.Transcripts;

public class HMMConstructBuilder 
{
	/*
	 * Initial probability of emission when symbol does not match transcript
	 */
	private static final double NON_MATCH_P = 0.05;
	
	/*
	 * The ID for the tied insertion-state emission parameters
	 */
	private String INSERTION_PARAMS_ID = "Inert_Params";
	
	/*
	 * The HMM under construction
	 */
	private HMM mainHmm;
	
	/*
	 * Keep track of all mux states
	 */
	private ArrayList<State> muxStates;
	
	/*
	 * The HMMs corresponding to each individual read.  Maps the read ID to the
	 * HMM object.
	 */
	private Map<String, HMM> subHMMs;
	
	public HMMConstruct buildHMMConstruct(Transcripts ts, 
										  Reads rs, 
										  Alignments cAligns)
	{	
		mainHmm = new HMM();
		muxStates = new ArrayList<State>();
		subHMMs = new HashMap<String, HMM>();
		
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
		mainHmm.states.addState(startState);
		
		/*
		 * Build a profile HMM structure for each candidate alignment
		 */
		for (Object[] o : cAligns.getAlignments())
		{
			String rId = (String) o[0];
			String tId = (String) o[1];
			int startPos = (Integer) o[2];
			boolean orientation = (Boolean) o[3];
			
			/*
			 * Create sub-HMM for this read if it does not exist
			 */
			if (!subHMMs.containsKey(rId))
			{
				HMM rHMM = new HMM();
				rHMM.states.addState(startState);
				rHMM.setBeginStateId(startState.getId());
				subHMMs.put(rId, rHMM);
			}
			
			buildHMMForAlignment(rId,
								 ts.getTranscript(tId), 
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
		 * Create HMMConstruct object
		 */
		HMMConstruct hmmConstruct = new HMMConstruct();
		hmmConstruct.mainHMM = this.mainHmm;
		hmmConstruct.subHMMs = this.subHMMs;
		
		return hmmConstruct;
	}
	
	public State buildHMMForAlignment(String rId,
									  Transcript t, 
									  int startPos, 
									  boolean orient)
	{	
		/*
		 * Build profile-HMM structure
		 */
		buildStates(rId, t, startPos, orient);
		createInterSeqeunceTransitions(t, startPos, orient);
		
		/*
		 * Create mux state with connection to all match states
		 */
		String muxId = "MUX_" + t.getId();
		State muxState = mainHmm.getStateById(muxId);
		if (muxState == null)
		{
			muxState = new StateSilent();
			muxState.setId("MUX_" + t.getId() );
			muxStates.add(muxState);
			mainHmm.states.addState(muxState);
		}
		
		/*
		 * Always add this mux state to the HMM corresponding to the read
		 */
		subHMMs.get(rId).states.addState(muxState);
		
		for (int i = startPos; i < startPos + 2*Common.readLength; i++)
		{
			if (i >= t.length()) break;			
			
			muxState.addTransition(new Transition(muxState.getId(),
												  matchStateId(t, i, orient),
												  1.0));
		}

		return muxState;
	}
	
	public void buildStates(String rId,
							Transcript t, 
			 				int startPos, 
			 				boolean orient)
	{
		for (int i = startPos; i < startPos +  2*Common.readLength; i++)
		{				
			if (i >= t.length()) break;
			
			String mId = matchStateId(t, i, orient);
			String dId = deleteStateId(t, i, orient);
			String iId = insertStateId(t, i, orient);
			
			/*
			 * If the HMM already has states for this region, then we don't 
			 * creating states at this position.
			 */
			if (!mainHmm.states.containsState(mId))
			{
				char matchedSymbol = t.getSeq().charAt(i);
			
				State dState =  createDeleteState(dId, i, orient);
				State iState =  createInsertState(iId, i, orient);
				State mState =  createMatchState(mId, matchedSymbol, i, orient);
			
				mainHmm.states.addState(dState);
				mainHmm.states.addState(iState);
				mainHmm.states.addState(mState);
				
				HMM subHMM = subHMMs.get(rId);
				subHMM.states.addState(dState);
				subHMM.states.addState(iState);
				subHMM.states.addState(mState);
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
											   boolean orient)
	{
		
		for (int i = startPos; i < (startPos + 2*Common.readLength) - 1; i++)
		{
			if (i >= t.length() - 1) break;
			
			/*
			 * Current and next deletion states at given position
			 */
			State dState = mainHmm.getStateById(deleteStateId(t, i, orient));
			State dNextState = mainHmm.getStateById(deleteStateId(t, i+1, orient));
			
			/*
			 * Current insertion state at given position
			 */
			State iState = mainHmm.getStateById(insertStateId(t, i, orient));
			
			/*
			 * Current match state at given position
			 */
			State mState = mainHmm.getStateById(matchStateId(t, i, orient));
			State mNextState = mainHmm.getStateById(matchStateId(t, i+1, orient));
				
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
			
			State dState = mainHmm.getStateById(deleteStateId(t, i, orient));
			State iState = mainHmm.getStateById(insertStateId(t, i, orient));
			State mState = mainHmm.getStateById(matchStateId(t, i, orient));
			
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
