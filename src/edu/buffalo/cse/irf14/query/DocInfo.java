package edu.buffalo.cse.irf14.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.index.DocVec_Element;

public class DocInfo 
{
	
	public static resultElement fetchDocDetails(String corpusDir,DocVec_Element ele)
	{
		resultElement result = new resultElement(ele.docName, ele.relevanceScore);
		// read the file
		String filepath=corpusDir + File.separator + ele.docName;
		BufferedReader br = null;
		
		try
		{
			br = new BufferedReader(new FileReader(filepath));
			/*
			 * Search for title and loop till you get all of it
			 * Empty line is all that separates title from content
			 * Some rogue documents don't even follow that!!!!!
			 */
			String title = getTitle(br);
			String snippet=getSnippet(br);
			
			result.snippet=snippet;
			result.title=title;
			
			br.close();
		}
		catch(FileNotFoundException FEX)
		{
			FEX.printStackTrace();
		}
		catch(IOException iex)
		{
			iex.printStackTrace();
		}
		return result;
	}
	
	private static String getSnippet(BufferedReader br) throws IOException
	{
		int snippetSize=4;
		String currLine=null;
		String snippet="";
		while((currLine=br.readLine())!=null)
		{
			if(currLine.trim().isEmpty())
				continue;
			
			if(snippetSize==0) break;
			if(currLine.matches("<AUTHOR>.+</AUTHOR>")) continue;
			
				snippetSize--;
			
			snippet+=currLine+"\n";
			
		}
		
		return snippet;
	}
	
	private static String getTitle(BufferedReader br) throws IOException
	{
		String currentLine=null;
		String title=null;
		/**
		 * Not a valid title element --- ie, if there
		 * is not A-Z or a-z alphabets after having replaced all
		 * non alphabetic characters then this cannot be a title
		 */
		while((currentLine = br.readLine()) != null)
		{
			if(currentLine.trim().isEmpty())
				continue;
			// simple check to avoid garbage like "1;"
			if(!currentLine.replaceAll("[^A-Za-z]","").isEmpty())
			{
				
				StringBuilder title_contender=new StringBuilder(currentLine);
				// is the next line also a title contd.?
				if((currentLine = br.readLine())!= null && !currentLine.isEmpty())
				{
					if(isLineAllCaps(currentLine))
					{
						title_contender.append(" "+currentLine);
						currentLine=br.readLine();
					}
				}
				title=title_contender.toString(); // setting title
				break;
			}
			else
			{
				//System.out.println("BADNES"+currentLine+"::"+d.getField(FieldNames.CATEGORY)[0]+"/"+d.getField(FieldNames.FILEID)[0]);
			}
		}
		
		
		return title;
	}
	
	
	
	static boolean isLineAllCaps(String str)
	{
		boolean retVal=true;
		String con = str.replaceAll("[^A-Za-z\\s]","");
		String[] t=con.split(" ");
		for(String a:t)
		{
			if(a.isEmpty()) continue;
			if(!a.matches("^[A-Z]+$")) // not all CAPS
			{
				return false;
			}
		}
		return retVal;
	}
}
