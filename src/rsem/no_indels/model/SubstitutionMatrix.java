package rsem.no_indels.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

import pair.Pair;

public class SubstitutionMatrix 
{
	private Map<Pair<Character, Character>, Double> subProbs;
	
	public SubstitutionMatrix(String file)
	{
		try 
		{
			Scanner scan = new Scanner(new File(file));
			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

}
