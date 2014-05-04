import java.io.File;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.SAMReader;

import rsem.model.ExpectedHiddenData;
import sequence.Sequence;
import sequence.SimulatedReads;
import sequence.Transcripts;
import test.Core;
import applications.RSEM;


public class TestSAMReader 
{
	public static void main(String[] args)
	{
		testInitializeHiddenData();
	}
	
	public static void testInitializeHiddenData()
	{
		final String samFName = "./data/bowtie/NM_small/bowtie_small_25.txt";
		File samFile = new File(samFName);
		
		final String readsFName = Core.PATH_TO_OUTPUT +
				  "out_small_25" +
				  Core.FASTA_EXT;
		
		SimulatedReads reads = FASTAReader.readSimulatedReads(readsFName);

		for (Sequence s : reads.getSequences())
		{
			System.out.println(s);
		}
		
		Transcripts ts = Core.getSmallTranscriptSet();
		Alignments aligns = SAMReader.readCandidateAlignments(samFile, reads, ts);		
	}

}
