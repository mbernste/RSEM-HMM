package rsem.no_indels.model;

import java.util.ArrayList;
import java.util.Map;

import data.reads.Read;
import data.transcript.Transcript;

import pair.Pair;

public class ExpectedAlignments 
{
	private int readId;
	
	private String transcriptId;
	
	ArrayList<Pair<Double, Double>> probabilities;
	
	public ExpectedAlignments(Read r, Transcript t)
	{
		this.readId = r.getId();
		this.transcriptId = t.getId();
		
		probabilities = new ArrayList<Pair<Double, Double>>(t.length());
	}
	
	/**
	 * Get the probability the read is aligned to the transcript starting at
	 * a target starting position
	 * 
	 * @param startPos the target starting position
	 * @return the probability that the read is aligned to the transcript 
	 * at the start position
	 */
	public Double getAlignmentProbability(int startPos)
	{
		return probabilities.get(startPos).getFirst();
	}
	
	/**
	 * Get the probability the read is aligned to the reverse compliment of the
	 * transcript starting at a target starting position
	 * 
	 * @param startPos the target starting position
	 * @return the probability that the read is aligned to the transcript's
	 * reverse compliment at the start position
	 */
	public Double getRCAlignmentProbability(int index)
	{
		return probabilities.get(index).getSecond();
	}
	
	public void setAlignmentProbability(int index, double p)
	{
		probabilities.get(index).setFirst(p);
	}
	
	public void setRCAlignmentProbability(int index, double p)
	{
		probabilities.get(index).setSecond(p);
	}
	
	@Override
	public String toString()
	{
		String str = "--- ALIGNMENT PROBABILITY ---\n";
		str += "Read ID: " + readId + "\n";
		str += "Transcript ID: " + transcriptId + "\n";
		
		
		str += " Alignment Probabilities:\n";
		for (int i = 0; i < probabilities.size(); i++)
		{
			str += i + ": " + probabilities.get(i).getFirst();
		}
		
		str += " Reverse Compliment Alignment Probabilities:\n";
		for (int i = 0; i < probabilities.size(); i++)
		{
			str += i + ": " + probabilities.get(i).getSecond();
		}
		
		str += "----------------------------\n";
		
		return str;
	}
	
	/**
	 * @return the sum of probabilities of the read being aligned to the 
	 * transcript and its reverse compliment at every position of the 
	 * transcript.
	 */
	protected double sumOfProbabilities()
	{
		double sum = 0;
		
		for (Pair<Double, Double> p : probabilities)
		{
			sum += p.getFirst();
			sum += p.getSecond();
		}
		
		return sum;
	}
	
	/**
	 * Divide all probabilities of the read being aligned to the 
	 * transcript and its reverse compliment at every position of the 
	 * transcript by some value.  This method is used for normalization.
	 * 
	 * @param value the value for which to divide all the probabilities by
	 */
	protected void divideAllProbabilitiesBy(double value)
	{
		for (Pair<Double, Double> p : probabilities)
		{
			p.setFirst(p.getFirst() / value);
			p.setSecond(p.getSecond() / value);
		}
	}
}
