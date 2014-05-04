package applications;

import hmm.HMM;
import hmm.HMMConstruct;
import hmm.HMMConstructBuilder;
import hmm.HMMParameterCounts;
import hmm.State;
import hmm.Transition;
import hmm.algorithms.BackwardAlgorithm;
import hmm.algorithms.DpMatrix;
import hmm.algorithms.ForwardAlgorithm;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import pair.Pair;
import rsem.model.ExpressionLevels;
import sequence.Read;
import sequence.Reads;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcript;
import sequence.Transcripts;

import common.Common;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.MappingReader;
import data.readers.SAMReader;

public class RSEM_HMM 
{
	private static int debug = 1;
	
	private static final double EPSILON = 0.001;
	
	
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
		{		
			prevProbData = probData;
			probData = probabilityOfData(rs, ts, hConstruct);	
						
			/*
			 * E-Step
			 */
			z = eStep(rs, z, hConstruct);
			
			/*
			 * M-Step
			 */
			hConstruct = mStep(z, hConstruct);
			
			// TODO REMOVE
			//System.out.println(hConstruct.getReadHMM("1"));
						
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
				String x = r.getSeq();
				Pair<Double, DpMatrix> fResult = ForwardAlgorithm.run(rHMM, 
																	  r.getSeq());
				Pair<Double, DpMatrix> bResult = BackwardAlgorithm.run(rHMM, 
																	   r.getSeq());
				
				double pSeq = fResult.getFirst();
				System.out.println("PSEQ: " + pSeq); // TODO REMOVE
				DpMatrix f = fResult.getSecond();
				DpMatrix b = bResult.getSecond();
				
				for (State s : rHMM.getStates())
				{
					/*
					 * Calculate expected counts of the transitions
					 */
					for (Transition t : s.getTransitions())
					{
						double tCount = 0.0;
						for (int i = 0; i < f.getNumColumns() - 1; i++)
						{
							tCount += f.getValue(s, i) * 
									  t.getTransitionProbability() * 
									  s.getEmissionProb(Character.toString(x.charAt(i))) *
									  b.getValue(s, i+1);
						}
						
						tCount /= pSeq;
						
						z.incrementTransitionCount(t.getOriginId(), 
												   t.getDestinationId(),
												   tCount);
					}
					/*
					if (!s.isSilent())
					{*/
						/*
						 * Count emissions along the read
						 *//*
						Map<Character, Double> symCounts = new HashMap<Character, Double>();
						for (int i = 0; i < x.length(); i++)
						{
							for (Character c : Common.DNA_ALPHABET)
							{
								symCounts.put(c, 0.0);
							}
							
							Character symbol = x.charAt(i);
							double val = f.getValue(s, i+1) * b.getValue(s, i+1);
							symCounts.put(symbol, symCounts.get(symbol) + val);
						}
						
						*//*
						 * Update the counts in the counts data structure
						 *//*
						for (Entry<Character, Double> e : symCounts.entrySet())
						{
							z.incrementEmissionCount(s.getId(), 
													 e.getKey().toString(), 
													 e.getValue() / pSeq);
						}
					}*/
					
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
			
			/*
			 * Update transition probabilities
			 */
			for (Transition t : s.getTransitions())
			{
				
				String origId = t.getOriginId();
				String destId = t.getDestinationId();
				
				double tCount = z.getTransitionProb(origId, destId);
				
				hConstruct.getMainHMM().getStateById(s.getId())
									   .getTransition(destId)
									   .setTransitionProbability(tCount / sum);
			}
			
			/*
			 * Update emission probabilities
			 */
			sum = 0.0;
			if (!s.isSilent())
			{
				for (Character c : Common.DNA_ALPHABET)
				{
					sum += z.getEmissionProb(s.getId(), c.toString());
				}
				
				for (Character c : Common.DNA_ALPHABET)
				{
					double eCount = z.getEmissionProb(s.getId(), c.toString()); 
					hConstruct.getMainHMM().getStateById(s.getId())
										   .addEmission(c.toString(), eCount / sum);
				}
			}
		}
		
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
				Pair<Double, DpMatrix> result = ForwardAlgorithm.run(rHMM, r.getSeq());
				double pSeq = result.getFirst();
				
				System.out.println(r.getId() + " with P = " + pSeq );
				
				if (pSeq != 0)
					p += -Math.log(pSeq);				
			}
		}
		return p;
	}
}
