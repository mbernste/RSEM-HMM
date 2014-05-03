import java.io.File;

import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Reads;
import sequence.Sequence;
import sequence.SequenceCollection;
import sequence.SimulatedReads;
import sequence.Transcripts;

import common.Common;

import data.readers.Alignments;
import data.readers.FASTAReader;
import data.readers.SAMReader;


public class Core 
{
	public static final String PATH_TO_DATA = "./data/";
	public static final String PATH_TO_OUTPUT = "./output/";
	
	public static final String MAP_EXT = ".map";
	public static final String FASTA_EXT = ".fa";
	public static final String TXT_EXT = ".txt";
	
	public static final String TRANSCRIPT_SHORT_FNAME = "NM_refseq_ref.transcripts_small";
	public static final String TRANSCRIPT_DUMMY_FNAME = "dummy";
	public static final String READS_DUMMY_FNAME = "dummy_reads";
	public static final String ALIGN_DUMMY_FNAME = "dummy_aligns";
	
	public static Transcripts getSmallTranscriptSet()
	{
		 Transcripts ts = FASTAReader.readTranscripts(PATH_TO_DATA + 
				 									  TRANSCRIPT_SHORT_FNAME + 
				 									  FASTA_EXT);
		 return ts;
	}
	
	public static Reads getDummyReads()
	{
		Reads rs = FASTAReader.readReads(PATH_TO_DATA + 
					 					 READS_DUMMY_FNAME + 
					 					 FASTA_EXT);
		return rs;
	}
	
	public static TestKit getDummyTestKit()
	{
		Transcripts ts = FASTAReader.readTranscripts(PATH_TO_DATA + 
				  									 TRANSCRIPT_DUMMY_FNAME + 
				  									 FASTA_EXT);
		
		SimulatedReads rs = FASTAReader.readSimulatedReads(PATH_TO_DATA + 
				 						 		  		   READS_DUMMY_FNAME + 
				 						 		  		   FASTA_EXT);
		
		System.out.println(ts);
		System.out.println(rs);
		
		File samFile = new File(PATH_TO_DATA + 
								ALIGN_DUMMY_FNAME +
								TXT_EXT);
		
		
		Alignments aligns = SAMReader.readCandidateAlignments(samFile, rs, ts);
	
		return new Core.TestKit(rs, ts, aligns);
	}
	
	public static Transcripts getDummyTranscriptSet()
	{
		Transcripts ts = FASTAReader.readTranscripts(PATH_TO_DATA + 
				  									 TRANSCRIPT_DUMMY_FNAME + 
				  									 FASTA_EXT);
		return ts;
	}
	
	public static SubstitutionMatrix buildDummySubstitutionMatrix()
	{
		SubstitutionMatrix dummyMatrix = new SubstitutionMatrix();
		
		for (int i = 0; i < Common.readLength; i++)
		{
			for (int j = 0; j < Common.DNA_ALPHABET.length; j++)
			{
				for (int k = 0; k < Common.DNA_ALPHABET.length; k++)
				{
					// Deterministically set a value in the dummy matrix
					double value = j + k + i + 1;
										
					dummyMatrix.setValue(Common.DNA_ALPHABET[j], 
										 Common.DNA_ALPHABET[k], 
										 i, 
										 value);
				}
			}
		}
		
		dummyMatrix.normalize();
		
		return dummyMatrix;
	}
	
	public static ExpressionLevels buildDummyExpressionLevels(Transcripts ts)
	{
		ExpressionLevels el = new ExpressionLevels(ts);
		double count = 1;
		for (Sequence s : ts.getSequences())
		{
			el.setExpressionLevel(s.getId(), count++);
		}
		el.normalize();
		return el;
	}
	
	public static class TestKit
	{
		private Reads rs;
		private Transcripts ts;
		private Alignments aligns;
		
		TestKit(Reads rs, Transcripts ts, Alignments aligns)
		{
			this.rs = rs;
			this.ts = ts;
			this.aligns = aligns;
		}
		
		public Reads reads()
		{
			return this.rs;
		}
		
		public Transcripts transcripts()
		{
			return this.ts;
		}
		
		public Alignments alignments()
		{
			return this.aligns;
		}
	}

}
