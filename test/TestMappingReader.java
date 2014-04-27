import java.io.File;

import applications.RSEM;

import common.FileWriter;

import data.Sequence;
import data.SimulatedReads;
import data.Transcripts;
import data.readers.FASTAReader;
import data.readers.MappingReader;
import data.simulation.ReadSimulator;


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
		SimulatedReads rs = ReadSimulator.simulateReadsAllUniform(ts, 20);
		
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
