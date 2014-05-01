import rsem.model.ExpressionLevels;
import rsem.model.no_indels.SubstitutionMatrix;
import sequence.Sequence;
import sequence.SequenceCollection;
import sequence.Transcripts;

import common.Common;

import data.readers.FASTAReader;


public class Core 
{
	public static final String PATH_TO_DATA = "./data/";
	public static final String PATH_TO_OUTPUT = "./output/";
	
	public static final String MAP_EXT = ".map";
	public static final String FASTA_EXT = ".fa";
	
	public static final String TRANSCRIPT_SHORT_FNAME = "NM_refseq_ref.transcripts_small";
	public static final String TRANSCRIPT_DUMMY_FNAME = "dummy";
	
	public static Transcripts getSmallTranscriptSet()
	{
		 Transcripts ts = FASTAReader.readTranscripts(PATH_TO_DATA + 
				 									  TRANSCRIPT_SHORT_FNAME + 
				 									  FASTA_EXT);
		 return ts;
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
		
		for (int i = 0; i < Common.READ_LENGTH; i++)
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

}
