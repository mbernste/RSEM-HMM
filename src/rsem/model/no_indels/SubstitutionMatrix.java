package rsem.model.no_indels;

import java.util.HashMap;
import java.util.Map;

import sequence.Read;
import sequence.Reads;
import sequence.Transcript;
import sequence.Transcripts;

import common.Common;
import data.readers.Alignments;


public class SubstitutionMatrix 
{	
	private Map<Character, Integer> symbolIndices;
	
	/**
	 * Index 0: Position in the read
	 * Index 1: Symbol in the read
	 * Index 2: Aligned symbol in the transcript
	 */
	private Double[][][] values;
		
	public SubstitutionMatrix(Reads rs, Transcripts ts, Alignments aligns)
	{
		symbolIndices = new HashMap<Character, Integer>();
		for (int i = 0; i < Common.DNA_ALPHABET.length; i++)
		{
			symbolIndices.put(Common.DNA_ALPHABET[i], i);
		}
		
		int rLength = Common.readLength;
		int aLength = Common.DNA_ALPHABET.length;
		values = new Double[rLength][aLength][aLength];
		for (int p = 0; p < rLength; p++)
		{
			for (int r = 0; r < aLength; r++)
			{
				for (int t = 0; t < aLength; t++)
				{
					values[p][r][t] = 1.0;
				}
			}
		}
		
		for (Object[] o : aligns.getAlignments())
		{	
			String readId = (String) o[0];
			String transId = (String) o[1];
			Integer startPos = (Integer) o[2];
			Boolean orientation = (Boolean) o[3];
			
			if (!aligns.getUniquelyMappingReads().contains(readId))
			{
				continue;
			}
			
			Read r = rs.getRead(readId);
			Transcript t = ts.getTranscript(transId);
			
			String rSeq = r.getSeq();
			String tSeq = t.getSeq(); 
			
			if (orientation == Common.REVERSE_COMPLIMENT_ORIENTATION)
			{	
				tSeq = Common.reverseCompliment(tSeq);
				int rcStartPos = tSeq.length() - (startPos + rSeq.length()); 		
				
				for (int i = 0; i < rSeq.length(); i++)
				{	
					char rSymbol = rSeq.charAt(i);
					char tSymbol = tSeq.charAt(rcStartPos + i);
					
					values[i][symbolIndices.get(rSymbol)][symbolIndices.get(tSymbol)]++;
				}
			}
			else if (orientation == Common.FORWARD_ORIENTATION)
			{
				for (int i = 0; i < rSeq.length(); i++)
				{
					char rSymbol = rSeq.charAt(i);
					char tSymbol = tSeq.charAt(startPos + i);
					
					values[i][symbolIndices.get(rSymbol)][symbolIndices.get(tSymbol)]++;
				}
			}
		}
	
		System.out.println(this);
		
		this.normalize();
	}
	
	public SubstitutionMatrix()
	{
		symbolIndices = new HashMap<Character, Integer>();
		for (int i = 0; i < Common.DNA_ALPHABET.length; i++)
		{
			symbolIndices.put(Common.DNA_ALPHABET[i], i);
		}
		
		int rLength = Common.readLength;
		int aLength = Common.DNA_ALPHABET.length;
		values = new Double[rLength][aLength][aLength];
		for (int p = 0; p < rLength; p++)
		{
			for (int r = 0; r < aLength; r++)
			{
				for (int t = 0; t < aLength; t++)
				{
					if (Common.DNA_ALPHABET[r] == Common.DNA_ALPHABET[t])
						values[p][r][t] = 0.85;
					else
						values[p][r][t] = 0.05;
				}
			}
		}
	}
	
	/**
	 * Get the value for substitution at target position of the 
	 * read
	 * 
	 * @param rSymbol the symbol on the read
	 * @param tSymbol the symbol on the transcript
	 * @param position the position of the read for the substitution we are 
	 * examining
	 * @return the value of the given substitution at the target position
	 * of the read
	 */
	public double getValue(int position,
						   Character rSymbol,
						   Character tSymbol)
	{
		int row = symbolIndices.get(rSymbol);
		int col = symbolIndices.get(tSymbol);
		return values[position][row][col];
	}
	
	/**
	 * Set the value for substitution at target position of the read
	 * 
	 * @param rSymbol the symbol on the read
	 * @param tSymbol the symbol on the transcript
	 * @param position the position of the read for the substitution we are 
	 * examining
	 */
	public void setValue(Character rSymbol,
			   			 Character tSymbol,
			   			 int position,
			   			 double value)
	{
		int row = symbolIndices.get(rSymbol);
		int col = symbolIndices.get(tSymbol);
		values[position][row][col] = value;
	}
	
	public void normalize()
	{
		for (int p = 0; p < Common.readLength; p++)
		{
			for (int t = 0; t < Common.DNA_ALPHABET.length; t++)
			{
				double sum = 0.0;
				
				for (int r = 0; r < Common.DNA_ALPHABET.length; r++)
				{
					sum += values[p][r][t];
				}
				
				for (int r = 0; r < Common.DNA_ALPHABET.length; r++)
				{
					values[p][r][t] /= sum;
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		String str = " ------------ ";
		
		int rLength = Common.readLength;
		for (int p = 0; p < rLength; p++)
		{
			str += "\n** Position " + p + " **\n";
			for (char rSymbol : Common.DNA_ALPHABET)
			{
				int r = symbolIndices.get(rSymbol);
				for (char tSymbol : Common.DNA_ALPHABET)
				{
					int c = symbolIndices.get(tSymbol);
					str += "(" + rSymbol + ", " + tSymbol + ") : " + 
							values[p][r][c] + "\n";			
				}
			}			
		}
		
		str += "------------\n";
		
		return str;
	}

}
