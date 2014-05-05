import java.io.File;

import rsem.model.ExpressionLevels;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcripts;
import test.Core;

import applications.RSEM;
import applications.Simulation;

import common.FileWriter;

import data.readers.FASTAReader;
import data.readers.MappingReader;

public class TestMappingReader 
{
	public static void main(String[] args)
	{
		testRecoverMapping();
	}
	
	public static void testRecoverMapping()
	{
		final String readsFName = Core.PATH_TO_OUTPUT +
								  "Sim_Reads_From_" + 
								  Core.TRANSCRIPT_SHORT_FNAME + 
								  Core.FASTA_EXT;
		
		final String mapFName = Core.PATH_TO_OUTPUT + 
								"Sim_Reads_From_" + 
  								Core.TRANSCRIPT_SHORT_FNAME + 
  								Core.MAP_EXT;
		
		Transcripts ts = Core.getSmallTranscriptSet();
		ExpressionLevels el = new ExpressionLevels(ts);
		SimulatedReads rs = Simulation.simulateReads(ts, el, 20, false);
		
		File fastaFile = new File(readsFName);
		FileWriter.writeToFile(fastaFile, rs.fastaFormat());
		
		File mapFile = new File(mapFName);
		FileWriter.writeToFile(mapFile, rs.mapping());
		

		SimulatedReads reads = FASTAReader.readSimulatedReads(readsFName);
		
		MappingReader.recoverMapping(mapFName, reads);

		for (Sequence r : reads.getSequences())
		{
			System.out.println(r);
		}
	}
	
	

}
