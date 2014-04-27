package applications;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import pair.Pair;

import common.FileWriter;

import data.SimulatedReads;
import data.Transcripts;
import data.readers.FASTAReader;
import data.simulation.ReadSimulator;

public class Simulation 
{
	public static void main(String[] args)
	{
		Transcripts ts = FASTAReader.readTranscripts(args[0]);
		SimulatedReads rs = ReadSimulator.simulateReadsAllUniform(ts, 20);
		
		File fastaFile = new File(args[1]);
		FileWriter.writeToFile(fastaFile, rs.fastaFormat());
		
		File mapFile = new File(args[2]);
		FileWriter.writeToFile(mapFile, rs.mapping());
	}
}
