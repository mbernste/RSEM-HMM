package applications;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;

import data.readers.ExpressionLevelsReader;
import evaluate.ErrorMetrics;
import rsem.model.ExpressionLevels;

public class EvaluateResults 
{
	public static void main(String[] args)
	{
		String truthFile = args[0];
		String estimateFile = args[1];
		
		ExpressionLevels truth = ExpressionLevelsReader.read(truthFile);
		ExpressionLevels estimate = ExpressionLevelsReader.read(estimateFile);	
		
		double mpe = computeTotalMeanPercentageError(truth, estimate);
		System.out.println("Mean Percentage Error: " + mpe);
	}
	
	public static double computeTotalMeanPercentageError(ExpressionLevels truth, 
										   		  ExpressionLevels estimate)
	{		
		Double[] truthVals = new Double[truth.getValues().size()];
		Double[] estimateVals = new Double[estimate.getValues().size()];
		
		
		int index = 0;
		for (Entry<String, Double> tE : truth.getValues().entrySet())
		{
			double tVal = tE.getValue();
			double eVal = estimate.getValues().get(tE.getKey());
			
			truthVals[index] = tVal;
			estimateVals[index] = eVal;
			index++;
		}
		
		System.out.println("T VALS: " + Arrays.toString(truthVals));
		System.out.println("E VALS: " + Arrays.toString(estimateVals));
						
		return ErrorMetrics.meanPercentageError(truthVals, estimateVals);
	}
}
