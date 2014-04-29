package hmm;

import common.Common;

public class ProfileHmmBuilder 
{
	public HiddenMarkovModel build()
	{
		HiddenMarkovModel hmm = new HiddenMarkovModel();
		
		State beginState = new State();
		hmm.states.addState(beginState);
		hmm.setBeginStateId(beginState.getId());
		
		State firstInsertState = new State();
		beginState.addTransition(new Transition(beginState.getId(),
												firstInsertState.getId(),
												1.0));
		firstInsertState.addTransition(new Transition(firstInsertState.getId(),
													  firstInsertState.getId(),
													  1.0));
		hmm.states.addState(firstInsertState);

		// TODO THIS
		State currMatchState = beginState;
		
		/*
		 * Build the match states
		 */
		for (int i = 0; i < Common.READ_LENGTH; i++)
		{/*
			prevMatchState = currMatchState;
			currMatchState = new State();
			
			if (prevMatchState != null)
			{
				prevMatchState.addTransition(new Transition(prevMatchState.getId(),
													   	 	currMatchState.getId(),
													   	 	1.0));
			}*/
		}
	
		return hmm;
	}
}
