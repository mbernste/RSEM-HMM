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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;



import pair.Pair;
import rsem.model.ExpressionLevels;
import sequence.Read;
import sequence.Reads;
import sequence.SimulatedReads;
import sequence.Transcripts;

import common.Common;
import common.LogP;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.MappingReader;
import data.readers.SAMReader;

public class RSEM_HMM 
{
	/**
	 * Debug setting
	 */
	private static int debug = 1;
	
	/**
	 * Stopping criteria for the Baum-Welch algorithm
	 */
	private static final double EPSILON = 0.001;
	
	public static void main(String[] args)
	{	
		SimulatedReads rs = FASTAReader.readSimulatedReads(args[0]);
		MappingReader.recoverMapping(args[1], rs);
		
		Transcripts ts = FASTAReader.readTranscripts(args[2]);

		File samFile = new File(args[3]);
		Alignments aligns = SAMReader.readCandidateAlignments(samFile, 
															  rs, 
															  ts, 
															  false);
				
		ExpressionLevels el = RSEM_HMM.EM(rs, ts, aligns);
		
		System.out.println(el);
	}
	
	/**
	 * Runs a form of the Baum-Welch algorithm for finding the parameters in
	 * the HMM that maximizes the likelihood of the data.
	 * 
	 * @param rs the set of reads
	 * @param ts the set of reference transcripts
	 * @param cAligns the candidate alignments considered in the analysis
	 * @return the estimated expression levels of the reference transcripts
	 */
	public static ExpressionLevels EM( Reads rs,
									   Transcripts ts,
						   			   Alignments cAligns) 
	{	
		HMMConstructBuilder builder = new HMMConstructBuilder();
		HMMConstruct hConstruct = builder.buildHMMConstruct(ts, cAligns);
		HMMParameterCounts z = new HMMParameterCounts(hConstruct.getMainHMM());
		
		/*
		 *  Repeat E-Step & M-Step until convergence
		 */
		double probData = 1.0;
		double prevProbData = 0.0;
		while (Math.abs(probData - prevProbData) > EPSILON)
		{		
			System.out.println("*************************** NEW ITERATION **" +
							   "********************************");		
			
			prevProbData = probData;
			probData = probabilityOfData(rs, cAligns, hConstruct);	
			
			System.out.println("---------------------------- E-STEP --------" +
							   "----------------------------");
			
			/*
			 * E-Step
			 */
			z = eStep(rs, cAligns, z, hConstruct);
			
			System.out.println("---------------------------- M-STEP --------" +
							   "----------------------------");
			
			/*
			 * M-Step
			 */
			hConstruct = mStep(z, hConstruct);
			
						
			if (debug > 0)
				System.out.println("Current probability of data: " + probData);
		}
		
		/*
		 * Build expression level data
		 */
		ExpressionLevels el = new ExpressionLevels(ts, true);		
		for (Transition t : hConstruct.getMainHMM().getBeginState()
												   .getTransitions())
		{
			String tId = t.getDestinationId().split("-")[1];
			double tProb = LogP.exp(t.getTransitionProbability());
			el.setExpressionLevel(tId, tProb);
		}
		
		return el;
	}
	
	/**
	 * 	E-Step
	 *  <br>
	 *  <br>
	 *  Computes the expected counts for each emission and transition through
	 *  the model
	 *  
	 *  @param rs the set of reads
	 *  @param aligns the candidate alignments considered in the analysis
	 *  @param z the data structure used for storing the counts from the HMM
	 *  @param hConstruct the current HMM
	 *  @return a data structure that stores the expected counts of all 
	 *  emissions and transitions
	 */
	public static HMMParameterCounts eStep(Reads rs, 
										   Alignments aligns,
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
		for(Object[] o : aligns.getAlignments())
		{
			String rId = (String) o[0];
			Boolean orient = (Boolean) o[3];
			
			Read r = (Read) rs.getSequence(rId);
			HMM rHMM = hConstruct.getReadHMM(rId);
			
		
			/*
			 * Reverse compliment the string according to alignment
			 */
			String x = r.getSeq();
			if (orient == Common.REVERSE_COMPLIMENT_ORIENTATION)
			{
				x = Common.reverseCompliment(x);
			}
			
			/*
			 * Run forward and backward algorithms
			 */
			Pair<Double, DpMatrix> fResult = ForwardAlgorithm.run(rHMM, x);
			Pair<Double, DpMatrix> bResult = BackwardAlgorithm.run(rHMM, x);
			
			double pSeq = fResult.getFirst();
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
						double tCount = Double.NaN;
						for (int i = 0; i < f.getNumColumns() - 1; i++)
						{		
							if (!destState.isSilent())
							{
								/*
								 * tCount = f(s,i) * transitionP * emissionP *
								 *  b(s,i+1)
								 */
								double product;
								product = LogP.prod(f.getValue(s, i), 
													t.getTransitionProbability());
								product = LogP.prod(product, 
													destState.getEmissionProb(Character.toString(x.charAt(i))));
								product = LogP.prod(product, 
													b.getValue(destState, i+1));
								tCount = LogP.sum(tCount, product);
							}
							else
							{
								
								/*
								 * tCount = f(s,i) * transitionP * b(s,i)
								 */
								double product;
								product = LogP.prod(f.getValue(s, i), 
													t.getTransitionProbability());
								product = LogP.prod(product, 
													b.getValue(destState, i));
								tCount = LogP.sum(tCount, product);
							}
						}
						
						/*
						 * Divide by probability of the sequence
						 */
						tCount = LogP.div(tCount, pSeq);
						
						/*
						 * Increment count
						 */
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
						symCounts.put(c, Double.NaN);
					}
					for (int i = 0; i < x.length(); i++)
					{	
						Character symbol = x.charAt(i);
						
						double val = LogP.prod(f.getValue(s, i+1), 
											   b.getValue(s, i+1));
						
						symCounts.put(symbol, LogP.sum(symCounts.get(symbol), val));
					}	
											
					/*
					 * Update the counts in the counts data structure
					 */
					for (Entry<Character, Double> e : symCounts.entrySet())
					{
						z.incrementEmissionCount(s.getId(), 
												 e.getKey().toString(), 
												 LogP.div(e.getValue(), pSeq));
					}
				}
			}
		}
		
		return z;
	}
	
	/**
	 * The M-Step
	 * <br>
	 * <br>
	 * Computes the values for the parameters that maximize the probability of
	 * the expected counts
	 * 
	 * @param z the expected counts of all emissions and transitions
	 * @param hConstruct the HMM
	 * @return the updated HMM
	 */
	public static HMMConstruct mStep(HMMParameterCounts z, 
									 HMMConstruct hConstruct)
	{			
		for (State s : hConstruct.getMainHMM().getStates())
		{
			
			/*
			 * Sum counts over transitions outgoing from this state
			 */
			double sum = Double.NaN;
			for (Transition t : s.getTransitions())
			{
				sum = LogP.sum(sum,
							   z.getTransitionProb(t.getOriginId(), 
									   			   t.getDestinationId()));
			}
		
			/*
			 * Update transition probabilities
			 */
			for (Transition t : s.getTransitions())
			{
				String origId = t.getOriginId();
				String destId = t.getDestinationId();
				
				double tCount = z.getTransitionProb(origId, destId);
				
				if (LogP.exp(sum) > 0)
				{					
					hConstruct.getMainHMM().getStateById(s.getId())
								   		   .getTransition(destId)
								   		   .setTransitionProbability(LogP.div(tCount, sum));
				}
				else
				{
					hConstruct.getMainHMM().getStateById(s.getId())
					   .getTransition(destId)
					   .setTransitionProbability(LogP.ln(0.0));
				}
			}
			
			/*
			 * Update emission probabilities
			 */
			sum = Double.NaN;
			if (!s.isSilent() && !(s instanceof StateParamsTied))
			{
				for (Character c : Common.DNA_ALPHABET)
				{
					sum = LogP.sum(sum, z.getEmissionProb(s.getId(), c.toString()));
				}
				
				for (Character c : Common.DNA_ALPHABET)
				{
					double eCount = z.getEmissionProb(s.getId(), c.toString()); 
					
					if (!Double.isNaN(sum))
					{
						hConstruct.getMainHMM().getStateById(s.getId())
										   	   .addEmission(c.toString(), 
										   			   		LogP.div(eCount, sum));
					}
					else
					{
						hConstruct.getMainHMM().getStateById(s.getId())
						   					   .addEmission(c.toString(), 
						   							   		LogP.ln(0.0));
					}
				}
			}
		}
		
		/*
		 * Update emission probabilities for tied insertion state emission
		 * parameters
		 */
		double sum = Double.NaN;
		for (Character c : Common.DNA_ALPHABET)
		{
			sum = LogP.sum(sum, z.getTiedEmissionPrams(HMMConstructBuilder.INSERTION_PARAMS_ID) 
				    			 .get(c.toString()));		
		}
		for (Character c : Common.DNA_ALPHABET)
		{
			double eCount = z.getTiedEmissionPrams(HMMConstructBuilder.INSERTION_PARAMS_ID) 
				    		 .get(c.toString());
			
			if (!Double.isNaN(sum))
			{
				StateParamsTied.tiedEmissionParams.get(HMMConstructBuilder.INSERTION_PARAMS_ID)
							   .put(c.toString(), LogP.div(eCount, sum));
			}
			else
			{
				StateParamsTied.tiedEmissionParams.get(HMMConstructBuilder.INSERTION_PARAMS_ID)
				   								  .put(c.toString(), LogP.ln(0.0));
			}
		}
		
		return hConstruct;
	}
	
	/**
	 * Compute the likelihood of the alignments
	 * 
	 * @param rs the set of reads
	 * @param aligns the set of candidate alignments
	 * @param hConstruct the constructed HMM
	 * @return the log-probability of the data
	 */
	public static double probabilityOfData( Reads rs, 
											Alignments aligns,
									 	 	HMMConstruct hConstruct)
	{		
		System.out.println("Calculating probability...");
		
		double p = Double.NaN;
		for (Object[] o : aligns.getAlignments())
		{			
			String rId = (String) o[0];
			Boolean orient = (Boolean) o[3];
					
			System.out.println("Read " + rId);
			
			Read r = rs.getRead(rId);
			String x = r.getSeq();
			if (orient == Common.REVERSE_COMPLIMENT_ORIENTATION)
			{
				x = Common.reverseCompliment(x);
			}
			
			HMM rHMM = hConstruct.getReadHMM(rId);
			
			Pair<Double, DpMatrix> result = ForwardAlgorithm.run(rHMM, x);
			double pSeq = result.getFirst();
						
			p = LogP.sum(p, pSeq);				
			
		}
		return p;
	}
}
