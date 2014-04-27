package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class FileWriter 
{
	public static void writeToFile(File file, String text)
	{
		PrintWriter out = null;
		try 
		{
			out = new PrintWriter(file);
			out.write(text);			
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			out.close();
		}
	}
}
