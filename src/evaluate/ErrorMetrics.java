package evaluate;

public class ErrorMetrics 
{
	public static double meanPercentageError(Double[] truthVals,
											 Double[] estimateVals)
	{
		double sum = 0.0;
		for (int i = 0; i < truthVals.length; i++)
		{
			sum += Math.abs((truthVals[i] - estimateVals[i]) / truthVals[i]);
		}
		
		return (100.0 / truthVals.length) * sum;
	}
}
