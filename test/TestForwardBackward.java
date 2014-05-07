import java.util.ArrayList;

import common.LogP;



import pair.Pair;

import hmm.HMM;
import hmm.State;
import hmm.StateSilent;
import hmm.Transition;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.BackwardAlgorithmNoLogSpace;
import hmm.algorithms.ForwardAlgorithmNoLogSpace;
import hmm.algorithms.DpMatrix;
import hmm.algorithms.ForwardAlgorithm;
import hmm.algorithms.SortSilentStates;


public class TestForwardBackward 
{	
	public static void main(String[] args)
	{
		testForward();
		//testBackward();
	}

	public static HMM buildToyHMM()
	{
		HMM hmm = new HMM();
		
		State A = new StateSilent("A");		
		State B = new StateSilent("B");
		State C = new StateSilent("C");		
		State D = new StateSilent("D");
		State E = new State("E");
		State F = new State("F");
		State G = new StateSilent("G");
		
		A.addTransition(new Transition("A", "E", LogP.ln(0.5)));
		A.addTransition(new Transition("A", "X", LogP.ln(0.5)));
		A.addTransition(new Transition("A", "B", LogP.ln(0.5)));
		B.addTransition(new Transition("B", "C", LogP.ln(0.5)));
		B.addTransition(new Transition("B", "F", LogP.ln(0.5)));
		C.addTransition(new Transition("C", "D", LogP.ln(0.5)));
		D.addTransition(new Transition("D", "E", LogP.ln(0.5)));
		E.addTransition(new Transition("E", "E", LogP.ln(1.0)));
		F.addTransition(new Transition("F", "E", LogP.ln(1.0)));
		E.addTransition(new Transition("E", "G", LogP.ln(0.5)));
		
		E.addEmission("x", LogP.ln(0.5));
		E.addEmission("y", LogP.ln(0.5));
		
		F.addEmission("x", LogP.ln(0.9));
		F.addEmission("y", LogP.ln(0.1));
		
		hmm.addState(B);
		hmm.addState(D);
		hmm.addState(C);
		hmm.addState(E);
		hmm.addState(F);
		hmm.addState(G);
		hmm.addState(A);

		hmm.setBeginStateId("A");
		
		return hmm;
	}
	
	public static void testForward()
	{
		HMM toyHmm = buildToyHMM();
		Pair<Double, DpMatrix> result = ForwardAlgorithm.run(toyHmm, "yyxx");
		//Pair<Double, DpMatrix> result = CopyOfForwardAlgorithm.run(toyHmm, "yyxx");
		System.out.println(result.getFirst());
		System.out.println(Math.pow(Math.E, result.getFirst()));
	}
	
	public static void testBackward()
	{
		HMM toyHmm = buildToyHMM();
		Pair<Double, DpMatrix> result = BackwardAlgorithm.run(toyHmm, "yyxx");
		//Pair<Double, DpMatrix> result = CopyOfBackwardAlgorithm.run(toyHmm, "yyxx");
		
		System.out.println(result.getFirst());
		System.out.println(Math.pow(Math.E, result.getFirst()));
		
	}
	
}
