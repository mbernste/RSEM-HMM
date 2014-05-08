package rsem.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import sequence.Sequence;
import sequence.Transcripts;

import common.Common;


public class ExpressionLevels 
{	 
	private Map<String, Double> parameters;
	
	/**
	 * Constructor.  
	 * <br>
	 * <br>
	 * Sets the expression levels randomly.
	 * 
	 * @param ts the set of referrence transcripts
	 */
	public ExpressionLevels(Transcripts ts)
	{
		this();
		for (Sequence s : ts.getSequences())
		{
			parameters.put(s.getId(), Common.RNG.nextDouble());
		}
		normalize();
	}
	
	public ExpressionLevels()
	{
		parameters = new HashMap<String, Double>();
	}
	
	public  Map<String, Double> getValues()
	{
		return this.parameters;
	}
	
	public ExpressionLevels(Transcripts ts, boolean zero)
	{
		parameters = new HashMap<String, Double>();
		for (Sequence s : ts.getSequences())
		{
			parameters.put(s.getId(), 0.0);
		}
	}
	
	public void normalize()
	{
		double sum = 0.0;
		for (Double value : parameters.values())
		{
			sum += value;
		}
		
		for (String key : parameters.keySet())
		{
			parameters.put(key, parameters.get(key) / sum );
		}
	}
	
	public void setExpressionLevel(String tId, Double expression)
	{
		parameters.put(tId, expression);
	}
	
	public double getExpressionLevel(String tId)
	{
		return parameters.get(tId);
	}
	
	/**
	 * Sample a transcript randomly based on the expression levels of the 
	 * transcripts
	 * 
	 * @return the ID of a randomly sampled transcript
	 */
	public String sampleTranscript()
	{
		double r = Common.RNG.nextDouble();
				
		/*
		 * Sort the transcripts based on their expression levels
		 */
        ValueComparator vc =  new ValueComparator(parameters);
        TreeMap<String,Double> sortedMap = new TreeMap<String,Double>(vc);
        sortedMap.putAll(parameters);
        
        /*
         * Sample a transcript based on their expression level
         */
        double lowBound = 0.0;
        double uppBound = 0.0;
        for (Entry<String, Double> e : sortedMap.entrySet())
        {
        	lowBound = uppBound;
        	uppBound = lowBound + e.getValue();
        	
        	System.out.println("[" + lowBound + ", " + uppBound + "]");
        	
        	if (r >= lowBound && r < uppBound)
        	{
        		return e.getKey();
        	}
        }
        
		return null;
	}
	
	@Override
	public String toString()
	{
		String str = "";
		
		for (Entry<String, Double> e : parameters.entrySet())
		{
			str += e.getKey() + "," + e.getValue() + "\n";
		}
		
		return str;
	}
	
	class ValueComparator implements Comparator<String> 
	{

	    Map<String, Double> base;
	   
	    public ValueComparator(Map<String, Double> base) 
	    {
	        this.base = base;
	    }

	    /*
	     *  Note: this comparator imposes orderings that are inconsistent with 
	     *  equals.
	     */
	    public int compare(String a, String b) 
	    {
	        if (base.get(a) >= base.get(b)) 
	        {
	            return 1;
	        } 
	        else 
	        {
	            return -1;
	        } // returning 0 would merge keys
	    }
	}
}
