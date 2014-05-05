package common;

import java.util.Random;

public class Common 
{
	public static final Random RNG = new Random();
	
	public static final boolean FORWARD_ORIENTATION = true;
	public static final boolean REVERSE_COMPLIMENT_ORIENTATION = false;
	
	public static int readLength;
	public static int bonusLength;
	
	public static final char[] DNA_ALPHABET = {'A', 'T', 'C', 'G'};
	
	
	public static String reverseCompliment(String seq)
	{
		System.out.println(seq == null);
		char[] rcChars = new char[seq.length()];
		
		int length = seq.length();
		for (int i = 0; i < seq.length(); i++)
		{			
			rcChars[i] = complimentSymbol(seq.charAt(length - i - 1));			
		}
		
		return new String(rcChars);		
	}
	
	public static char complimentSymbol(char symbol)
	{
		switch(symbol)
		{
		case 'A':
			return 'T';
		case 'T':
			return 'A';
		case 'G':
			return 'C';
		case 'C':
			return 'G';
		default:
			return '\0';
		}
	}

}
