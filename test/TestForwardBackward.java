import hmm.HMM;
import hmm.State;
import hmm.StateSilent;
import hmm.Transition;
import hmm.algorithms.ForwardAlgorithm;


public class TestForwardBackward 
{	
	public static void main(String[] args)
	{
		testForward();
		testBackward();
	}

	public static HMM buildToyHMM()
	{
		HMM hmm = new HMM();
		
		State C = new StateSilent("C");		
		State A = new StateSilent("A");		
		State B = new StateSilent("B");
		State D = new StateSilent("D");
		State E = new State("E");
		
		A.addTransition(new Transition("A", "E", 0.5));
		A.addTransition(new Transition("A", "B", 0.5));
		B.addTransition(new Transition("B", "C", 0.5));
		C.addTransition(new Transition("C", "D", 0.5));
		D.addTransition(new Transition("D", "E", 0.5));
		E.addTransition(new Transition("E", "E", 1.0));
		
		E.addEmission("x", 0.5);
		E.addEmission("y", 0.5);
		
		hmm.addState(A);
		hmm.addState(B);
		hmm.addState(C);
		hmm.addState(D);
		hmm.addState(E);

		hmm.setBeginStateId("A");
		
		return hmm;
	}
	
	public static void testForward()
	{
		HMM toyHmm = buildToyHMM();
		ForwardAlgorithm.run(toyHmm, "xyx");
	}
	
	public static void testBackward()
	{
		// TODO THIS
	}
	
}
