package rsem.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import sequence.Sequence;
import sequence.Transcripts;

import common.Common;


public class ExpressionLevels 
{	 
	private Map<String, Double> parameters;
	
	
	public ExpressionLevels(Transcripts ts)
	{
		parameters = new HashMap<String, Double>();
		
		for (Sequence s : ts.getSequences())
		{
			parameters.put(s.getId(), Common.RNG.nextDouble());
		}
		
		normalize();
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
}
