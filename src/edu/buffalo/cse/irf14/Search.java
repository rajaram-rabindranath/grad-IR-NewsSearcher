package edu.buffalo.cse.irf14;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import edu.buffalo.cse.irf14.SearchRunner.ScoringModel;

public class Search
{
	
	public static void main(String args[])
	{
		//String qfile="/home/raja/Education/sem 3/IR/prj2/queryFile";
		String qfile="/home/raja/Education/sem 3/IR/prj2/queries_3";
		File queryFile = new File(qfile);
		try
		{
			File resultFile= new File("/home/raja/Education/sem 3/IR/prj2/resultFile");
			if(resultFile.createNewFile());
			else
			{
				resultFile.delete(); resultFile.createNewFile();
			}
			PrintStream ps= new PrintStream(resultFile);
			
			SearchRunner sr= new SearchRunner("/home/raja/Education/sem 3/IR/prj1/index_dir", "/home/raja/Education/sem 3/IR/prj2/corpus",'E',System.out);
			//SearchRunner sr= new SearchRunner("/home/raja/Education/sem 3/IR/prj1/index_dir", "/home/raja/Education/sem 3/IR/prj2/corpus",'E',new PrintStream(resultFile));
			
			sr.query("NATO",ScoringModel.TFIDF);
			//sr.query(queryFile);
		}
		catch(FileNotFoundException FEX)
		{
			FEX.printStackTrace();
		}
		catch(IOException IOEX)
		{
			IOEX.printStackTrace();
		}
	}
}
