package applications;

import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.HMMParameterCounts;
import hmm.State;
import hmm.StateParamsTied;
import hmm.Transition;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.DpMatrix;
import hmm.algorithms.ForwardAlgorithm;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import LogTransforms.LogTransforms;

import pair.Pair;
import rsem.model.ExpressionLevels;
import sequence.Reads;
import sequence.Sequence;
import sequence.Transcripts;
import test.Core;

import common.Common;

import data.readers.Alignments;

public class RSEM_HMM 
{
	private static int debug = 1;
	
	private static final double EPSILON = 0.001;
	
	
	public static void main(String[] args)
	{
		Core.TestKit kit = Core.getDummyTestKit();
		
		Transcripts ts = kit.transcripts();
		Reads rs = kit.reads();
		Alignments aligns = kit.alignments();
				
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hmmC = builder.buildHMMConstruct(ts, rs, aligns);	
			
		//System.out.println(hmmC.getReadHMM("3"));
		RSEM_HMM.EM(rs, ts, aligns);
	}
	
	public static ExpressionLevels EM( Reads rs,
						   			   Transcripts ts,
						   			   Alignments cAligns) 
	{	
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hConstruct = builder.buildHMMConstruct(ts, rs, cAligns);
		HMMParameterCounts z = new HMMParameterCounts(hConstruct.getMainHMM());
		
		/*
		 *  Repeat E-Step & M-Step until convergence
		 */
		double probData = 1.0;
		double prevProbData = 0.0;
		while (Math.abs(probData - prevProbData) > EPSILON)
		//for (int i = 0; i < 2; i++)
		{		
			System.out.println("*************************** NEW ITERATION **********************************");		
			
			prevProbData = probData;
			probData = probabilityOfData(rs, ts, hConstruct);	
						
			/*
			 * E-Step
			 */
			z = eStep(rs, z, hConstruct);
			
			System.out.println("---------------------------- M-STEP ------------------------------------");
			
			/*
			 * M-Step
			 */
			hConstruct = mStep(z, hConstruct);
			
						
			if (debug > 0)
				System.out.println("Current probability of data: " + probData);
		}

		ExpressionLevels el = new ExpressionLevels(ts);
		return el;
	}
	
	public static HMMParameterCounts eStep(Reads rs, 
							 			   HMMParameterCounts z,
							 			   HMMConstruct hConstruct)
	{ 
		/*
		 * Reset the counts
		 */
		z.resetCounts();
				
		/*
		 * Calculate expected value of all emissions and transitions
		 */
		for (Sequence r : rs.getSequences())
		{
			HMM rHMM = hConstruct.getReadHMM(r.getId());
			
			if (rHMM != null)
			{
				// TODO
				//if (r.getId().equals("2"))
				//{
				//	System.out.println(rHMM);
				//}
					
				String x = r.getSeq();
				if (r.getId().equals("2"))
					System.out.println("SEQ 2 = " + x);
				
				
				if (r.getId().equals("2"))
					ForwardAlgorithm.debug = 0; //TODO
				
				Pair<Double, DpMatrix> fResult = ForwardAlgorithm.run(rHMM, x);
				Pair<Double, DpMatrix> bResult = BackwardAlgorithm.run(rHMM, x);
				ForwardAlgorithm.debug = 0; // TODO
				
				double pSeq = LogTransforms.eExp(fResult.getFirst());
				
				System.out.println("PSEQ: " + pSeq); // TODO REMOVE
				DpMatrix f = fResult.getSecond();
				DpMatrix b = bResult.getSecond();
				
				for (State s : rHMM.getStates())
				{
					/*
					 * Expected counts for transitions
					 */
					for (Transition t : s.getTransitions())
					{
						State destState = rHMM.getStateById(t.getDestinationId());
						
						if (destState != null)
						{
							double tCount = 0.0;
							
							for (int i = 0; i < f.getNumColumns() - 1; i++)
							{		
								if (!destState.isSilent())
								{
									tCount += LogTransforms.eExp(f.getValue(s, i)) * 
											  t.getTransitionProbability() * 
											  destState.getEmissionProb(Character.toString(x.charAt(i))) *
											  LogTransforms.eExp(b.getValue(s, i+1));
								}
								else
								{
									tCount +=  LogTransforms.eExp(f.getValue(s, i)) * 
											  t.getTransitionProbability() * 
											  LogTransforms.eExp(b.getValue(s, i));
								}
							}
							
							if (s.getId().equals("MUX_DUMMY.4"))
								System.out.println("T Count " + tCount);
							
							tCount /= pSeq;
							
							z.incrementTransitionCount(t.getOriginId(), 
													   t.getDestinationId(),
													   tCount);
						}
					}
					
					/*
					 * Expected emission counts for non-silent states
					 */
					if (!s.isSilent())
					{
						/*
						 * Count emissions along the read
						 */
						Map<Character, Double> symCounts = new HashMap<Character, Double>();
						for (Character c : Common.DNA_ALPHABET)
						{
							symCounts.put(c, 0.0);
						}
						for (int i = 0; i < x.length(); i++)
						{	
							Character symbol = x.charAt(i);
							double val = LogTransforms.eExp(f.getValue(s, i+1)) * 
										 LogTransforms.eExp(b.getValue(s, i+1));
							symCounts.put(symbol, symCounts.get(symbol) + val);
						}						
						
						/*
						 * Update the counts in the counts data structure
						 */
						for (Entry<Character, Double> e : symCounts.entrySet())
						{
							z.incrementEmissionCount(s.getId(), 
													 e.getKey().toString(), 
													 e.getValue() / pSeq);
						}	
					}
				}
				
				
			}
			
		}
		
		return z;
	}
	
	public static HMMConstruct mStep(HMMParameterCounts z, 
									 HMMConstruct hConstruct)
	{			
		for (State s : hConstruct.getMainHMM().getStates())
		{
			/*
			 * Sum counts over transitions outgoing from this state
			 */
			double sum = 0.0;
			for (Transition t : s.getTransitions())
			{
				sum += z.getTransitionProb(t.getOriginId(), t.getDestinationId());
			}
		
			//TODO
			if (s.getId().equals("MUX_DUMMY.4"))
			{
				System.out.println("COUNTER STATE FOR 2:");
				System.out.println(z.getStateById("MUX_DUMMY.4"));
			}
			
			// TODO
			/*
			System.out.println("SUM " + sum);
			if (sum == 0.0)
				System.out.println("WARNING SUM IS ZERO: " + s.getId());
			if (sum >= 1)
				System.out.println("ERROR SUM IS GREATER THAN 1: " + s.getId());*/
				
			/*
			 * Update transition probabilities
			 */
			for (Transition t : s.getTransitions())
			{
				String origId = t.getOriginId();
				String destId = t.getDestinationId();
				
				double tCount = z.getTransitionProb(origId, destId);
				
				/*
				System.out.println("T PROB 1:" + hConstruct.getMainHMM().getStateById(s.getId())
						   .getTransition(destId)
						   .getTransitionProbability()); */
						   
				
				if (sum > 0)
				{
					hConstruct.getMainHMM().getStateById(s.getId())
								   .getTransition(destId)
								   .setTransitionProbability(tCount / sum);
				}
				else
				{
					hConstruct.getMainHMM().getStateById(s.getId())
					   .getTransition(destId)
					   .setTransitionProbability(0.0);
				}
			
				
				/*
				System.out.println("T PROB 2:" + hConstruct.getMainHMM().getStateById(s.getId())
					   .getTransition(destId)
					   .getTransitionProbability()); */
			}
			
			/*
			 * Update emission probabilities
			 */
			sum = 0.0;
			if (!s.isSilent() && !(s instanceof StateParamsTied))
			{
				for (Character c : Common.DNA_ALPHABET)
				{
					sum += z.getEmissionProb(s.getId(), c.toString());
				}
				
				for (Character c : Common.DNA_ALPHABET)
				{
					double eCount = z.getEmissionProb(s.getId(), c.toString()); 
					if (sum > 0)
					{
						hConstruct.getMainHMM().getStateById(s.getId())
										   .addEmission(c.toString(), eCount / sum);
					}
					else
					{
						hConstruct.getMainHMM().getStateById(s.getId())
						   					   .addEmission(c.toString(), 0.0);
					}
				}
			}
		}
		
		/*
		 * Update emission probabilities for tied insertion state emission
		 * parameters
		 */
		double sum = 0.0;
		for (Character c : Common.DNA_ALPHABET)
		{
			sum += z.getTiedEmissionPrams(HMMConstructBuilder.INSERTION_PARAMS_ID) 
				    .get(c.toString());
		}
		for (Character c : Common.DNA_ALPHABET)
		{
			double eCount = z.getTiedEmissionPrams(HMMConstructBuilder.INSERTION_PARAMS_ID) 
				    		 .get(c.toString());
			if (sum > 0)
			{
				StateParamsTied.tiedEmissionParams.get(HMMConstructBuilder.INSERTION_PARAMS_ID)
							   .put(c.toString(), eCount / sum);
			}
			else
			{
				StateParamsTied.tiedEmissionParams.get(HMMConstructBuilder.INSERTION_PARAMS_ID)
				   								  .put(c.toString(), 0.0);
			}
		}
		
		// TODO REMOVE
		//System.out.println(hConstruct.getMainHMM());
		
		return hConstruct;
	}
	
	public static double probabilityOfData( Reads rs, 
									 	 	Transcripts ts,
									 	 	HMMConstruct hConstruct)
	{
		double p = 0.0;
		for (Sequence r : rs.getSequences())
		{
			HMM rHMM = hConstruct.getReadHMM(r.getId());
			
			if (rHMM != null)
			{
				//if (r.getId().equals("2"))
				//	System.out.println(rHMM);
				ForwardAlgorithm.debug = 0; // TODO FIX
				Pair<Double, DpMatrix> result = ForwardAlgorithm.run(rHMM, r.getSeq());
				ForwardAlgorithm.debug = 0;
				double pSeq = result.getFirst();
				
				// TODO REMOVE
				System.out.println(r.getId() + " with P = " + pSeq );
				
				p += pSeq;				
			}
		}
		return p;
	}
}
