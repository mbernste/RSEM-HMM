package rsem.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import common.Common;

import data.Reads;
import data.Transcripts;

public class ExpectedHiddenData 
{
	/**
	 * The data structure for storing all of the probability values
	 */
	private NIJK data;

	/**
	 * The reference transcripts
	 */
	private Transcripts ts;

	/**
	 * The reference reads
	 */
	private Reads rs;

	public ExpectedHiddenData(Reads rs, Transcripts ts)
	{
		data = new NIJK();
		this.rs = rs;
		this.ts = ts;
	}

	public Double getValue(String readId, 
			String transId, 
			int startPos, 
			boolean orientation)
	{
		return data.getValue( readId,  transId, startPos,  orientation);
	}

	public void setValue(String readId, 
			String transId, 
			int startPos, 
			boolean orientation,
			Double value)
	{
		data.setValue( readId,  transId, startPos,  orientation, value);
	}

	public void incrementValue(String readId,
			String transId,
			int startPos,
			boolean orientation,
			Double incrValue)
	{
		data.incrementValue( readId,  transId, startPos,  orientation, incrValue);
	}

	public void initializeDataEntry(String readId, 
			String transId, 
			int startPos, 
			boolean orientation)
	{
		data.setValue( readId,  transId, startPos,  orientation, 1.0);
	}
	
	public double sumOverTranscript(String tId)
	{
		return data.sumOverTranscript(tId);
	}
	
	public void normalizeOverAllReads()
	{
		data.normalize();
	}
	
	public double countAlignedBasePairs(char rSymbol, char tSymbol, int pos)
	{
		return data.countAlignedBasePairs(rSymbol, tSymbol, pos);
	}
	
	public double countTranscriptBaseOccurrences(char tSymbol, int pos)
	{
		return data.countTranscriptBaseOccurrences(tSymbol, pos);
	}

	
	@Override
	public String toString()
	{
		return "-------  EXPECTED HIDDEN DATA  ---------- \n" + data.toString();
	}

	/**
	 * The data structure for storing all of the probability values.  This
	 * data structure stores all of the values.
	 * 
	 * N - Reads
	 * I - Transcripts
	 * J - Positions 
	 * K - Orientation
	 * 
	 * @author matthewb
	 *
	 */
	public class NIJK
	{
		/**
		 * Maps all read IDs to the data structure that stores the 
		 * probabilities for aligning this read to every transcript and 
		 * position along that transcript in the forward and reverse-compliment
		 * orientations.
		 */
		private Map<String, IJK> readToIJK;

		public NIJK()
		{
			readToIJK = new HashMap<String, IJK>();
		}

		public Double getValue(String readId, 
				String transId, 
				int startPos, 
				boolean orientation)
		{
			return readToIJK.get(readId).getValue(transId, 
					startPos, 
					orientation);
		}

		public void setValue(String readId, 
				String transId, 
				int startPos, 
				boolean orientation,
				Double value)
		{
			if (readToIJK.get(readId) == null)
			{
				readToIJK.put(readId, new IJK());
			}

			readToIJK.get(readId).setValue( transId, 
					startPos, 
					orientation,
					value);			
		}

		public void incrementValue(String readId, 
							String transId, 
							int startPos, 
							boolean orientation,
							Double value)
		{
			if (readToIJK.get(readId) == null)
			{
				throw new RuntimeException("No entry in NIJK data structrue" +
						" found for read " + readId + ".");
			}

			readToIJK.get(readId).incrementValue( transId, 
					startPos, 
					orientation,
					value);			
		}
		
		public void normalize()
		{
			for (IJK readData : readToIJK.values())
			{
				readData.normalize();
			}
		}
		
		public double sumOverTranscript(String tId)
		{
			double sum = 0.0;
			for (IJK readData : readToIJK.values())
			{
				sum += readData.sumOverTranscript(tId);
			}
			return sum;
		}
		
		public double countAlignedBasePairs(char rSymbol, char tSymbol, int pos)
		{
			double sum = 0.0;
			// TODO THIS!
			//readToIJK.values();
			return sum;
		}
		
		public double countTranscriptBaseOccurrences(char tSymbol, int pos)
		{
			double sum = 0.0;
			
			// TODO: THIS!!!
			
			return sum;
		}
		
		@Override
		public String toString()
		{
			String str = "";
			for (Entry<String, IJK> e: readToIJK.entrySet())
			{
				str += "READ ID: " + e.getKey() + "\n";
				str += e.getValue().toString();
			}
			return str;
		}
	}

	public class IJK
	{
		/**
		 * Maps a transcript to data structure that stores position 
		 */
		private Map<String, JK> transcriptToJK;

		public IJK()
		{
			transcriptToJK = new HashMap<String, JK>();
		}

		public Double getValue( String transId, 
				int startPos, 
				boolean orientation)
		{
			return transcriptToJK.get(transId).getValue(startPos, orientation);
		}

		public void setValue( String transId, 
				int startPos, 
				boolean orientation,
				Double value)
		{
			if (transcriptToJK.get(transId) == null)
			{
				transcriptToJK.put(transId, new JK());
			}

			transcriptToJK.get(transId).setValue(startPos, 
					orientation,
					value);
		}
		
		public void incrementValue( String transId, 
				int startPos, 
				boolean orientation,
				Double value)
		{
			if (transcriptToJK.get(transId) == null)
			{
				throw new RuntimeException("No entry in IJK data structrue" +
					" found for transcript " + transId + ".");
			}
			
			transcriptToJK.get(transId).incrementValue(startPos, 
					orientation,
					value);
		}
		
		public void normalize()
		{
			double sum = 0.0;
			for (JK dataEntry : transcriptToJK.values())
			{
				sum += dataEntry.sumAllValues();
			}
			
			for (JK dataEntry : transcriptToJK.values())
			{
				dataEntry.normalizeBy(sum);
			}
		}
		
		public double sumOverTranscript(String tId)
		{
			if ( transcriptToJK.get(tId) != null)
				return transcriptToJK.get(tId).sumAllValues();
			else return 0.0;
		}
		
		@Override
		public String toString()
		{
			String str = "";
			for (Entry<String, JK> e: transcriptToJK.entrySet())
			{
				str += "\tTRANSCRIPT ID: " + e.getKey() + "\n";
				str += e.getValue().toString();
			}
			return str;
		}
	}

	public class JK
	{
		private Map<Integer, K> positionToOrientation;

		public JK()
		{
			positionToOrientation = new HashMap<Integer, K>();
		}

		public Double getValue( int startPos, 
				boolean orientation)
		{
			return positionToOrientation.get(startPos).getValue(orientation);
		}

		public void setValue(int startPos, 
				boolean orientation,
				Double value)
		{
			if (positionToOrientation.get(startPos) == null)
			{
				positionToOrientation.put(startPos, new K());
			}

			positionToOrientation.get(startPos).setValue(orientation, value);
		}
		
		public void incrementValue(int startPos, 
				boolean orientation,
				Double value)
		{
			if (positionToOrientation.get(startPos) == null)
			{
				throw new RuntimeException("No entry in JK data structrue" +
					" found for startPosition " + startPos + ".");
			}

			positionToOrientation.get(startPos).incrementValue(orientation, 
															   value);
		}
		
		public double sumAllValues()
		{
			double sum = 0.0;
			for (K dataEntry : positionToOrientation.values())
			{
				sum += dataEntry.sumAllValues();
			}
			return sum;
		}
		
		public void normalizeBy(double value)
		{
			double sum = 0.0;
			for (K dataEntry : positionToOrientation.values())
			{
				dataEntry.normalizeBy(value);
			}
		}

		@Override
		public String toString()
		{
			String str = "";
			for (Entry<Integer, K> e: positionToOrientation.entrySet())
			{
				str += "\t\tSTART POSITION: " + e.getKey() + "\n";
				str += e.getValue().toString();
			}
			return str;
		}
	}

	public class K
	{
		private static final int FORWARD = 0;
		private static final int REVERSE_COMPLIMENT = 1;

		private Double[] orientationToProbability;

		public K()
		{
			orientationToProbability = new Double[2];
		}

		public Double getValue(boolean orientation)
		{

			if (orientation == Common.FORWARD_ORIENTATION)
			{
				return orientationToProbability[FORWARD];
			}
			else
			{
				return orientationToProbability[REVERSE_COMPLIMENT];
			}
		}

		public void setValue(boolean orientation, Double value)
		{			
			if (orientation == Common.FORWARD_ORIENTATION)
			{
				orientationToProbability[FORWARD] = value;
			}
			else
			{
				orientationToProbability[REVERSE_COMPLIMENT] = value;
			}
		}
		
		public void incrementValue(boolean orientation, Double value)
		{			
			if (orientation == Common.FORWARD_ORIENTATION)
			{
				orientationToProbability[FORWARD] += value;
			}
			else
			{
				orientationToProbability[REVERSE_COMPLIMENT] += value;
			}
		}
		
		public double sumAllValues()
		{
			double sum = 0.0;
			
			if (orientationToProbability[FORWARD] != null)
				sum += orientationToProbability[FORWARD];
			
			if (orientationToProbability[REVERSE_COMPLIMENT] != null)
				sum += orientationToProbability[REVERSE_COMPLIMENT];
			
			return sum;
		}
		
		public void normalizeBy(double value)
		{
			if (orientationToProbability[FORWARD] != null)
				System.out.println("Dividing " + orientationToProbability[FORWARD] + " by " + value);
				orientationToProbability[FORWARD] /= value;
			
			if (orientationToProbability[REVERSE_COMPLIMENT] != null)

				orientationToProbability[REVERSE_COMPLIMENT] /= value;
		}
		
		@Override
		public String toString()
		{
			String str = "";
			if (orientationToProbability[FORWARD] != null)
				str += "\t\t\tF:  " + orientationToProbability[FORWARD] + "\n";
			if (orientationToProbability[REVERSE_COMPLIMENT] != null)
				str += "\t\t\tRC: " + orientationToProbability[REVERSE_COMPLIMENT] + "\n";
			
			return str;
		}
	}
}
