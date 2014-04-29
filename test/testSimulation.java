import sequence.Transcripts;
import applications.Simulation;


public class testSimulation 
{
	public static void main(String[] args)
	{
		testGenerateExpressionLevels();
	}
	
	public static void testGenerateExpressionLevels()
	{
		Transcripts ts = Core.getSmallTranscriptSet();
		Simulation.generateExpressionLevels(ts);
	}
}
