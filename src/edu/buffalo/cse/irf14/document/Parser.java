/**
 * 
 */
package edu.buffalo.cse.irf14.document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import edu.buffalo.cse.irf14.analysis.REGEXCONSTANTS;

/**
 * @author nikhillo
 * Class that parses a given file into a Document
 */
public class Parser 
{

	static String delim=",";
	private static final Pattern find_month_pattern = Pattern.compile(REGEXCONSTANTS.REGEX_MONTH_FIND);
	private static Matcher find_month_matcher=null;
	public static int withAuthor = 0;
	public static int total = 0;
	
	static boolean dbg = true;
	
	public Parser()
	{
	}
	/**
	 * Static method to parse the given file into the Document object
	 * @param filename : The fully qualified filename to be parsed
	 * @return The parsed and fully loaded Document object
	 * @throws ParserException In case any error occurs during parsing
	 */
	public static Document parse(String filename) throws ParserException
	{
		total++;
		BufferedReader br = null;
		String currentLine = null;
		Document d = new Document();
		String[] tokens = null;
		
		if(filename == null) throw new ParserException();
		
		/* set category and filename */
		tokens = filename.split(File.separator);
		if(tokens.length == 1) // bad file path
		{
			throw new ParserException("Something seriously wrong with file seperator");
		}
		
		//setting fieldID and category
		d.setField(FieldNames.FILEID, tokens[tokens.length -1]);
		d.setField(FieldNames.CATEGORY,tokens[tokens.length -2]);
		
		try 
		{
			br = new BufferedReader(new FileReader(filename));
			
			/*
			 * Search for title and loop till you get all of it
			 * Empty line is all that separates title from content
			 * Some rogue documents don't even follow that!!!!!
			 */
			while((currentLine = br.readLine())!= null)
			{
				/**
				 * Not a valid title element --- ie, if there
				 * is not A-Z or a-z alphabets after having replaced all
				 * non alphabetic characters then this cannot be a title
				 */
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
					d.setField(FieldNames.TITLE,title_contender.toString()); // setting title
					break;
				}
				else
				{
					//System.out.println("BADNES"+currentLine+"::"+d.getField(FieldNames.CATEGORY)[0]+"/"+d.getField(FieldNames.FILEID)[0]);
				}
			
			}
			
			
			
			/**
			 * Current line could be 
			 * 1. empty
			 * 2. A line that could be content -- NEWS DATE
			 * 3. A line that could be content
			 * 4. The 3rd line of title
			 * 5. NULL
			 * In any case we have moved ahead of the title
			 */
			if(currentLine == null)
			{
				// no content post the title
				System.out.println("+++++++++++++BAD DOCUMENT HAVE MOVED FORWARD++++++++++");
				return d;
			}
			else if(currentLine.isEmpty())
			{
				
				if((currentLine=br.readLine())== null)
				{
					return d;
				}
			}
			
			// check if authorTAG present or not
			if(populateAuthorTAG(currentLine, d))
			{
				currentLine = br.readLine();
				withAuthor++;
			}
						
			// process to get Place and date fields
			String str=placeDate(currentLine, d); // FIXME
			StringBuilder content =  new StringBuilder();
			if(!str.isEmpty())
			{
				content.append(str+" ");
			}

			while ((currentLine = br.readLine()) != null && !currentLine.trim().isEmpty())
			{
				content.append(currentLine);
				content.append(" ");
			}
			d.setField(FieldNames.CONTENT,content.toString().trim());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			throw new ParserException();
		} 
		finally 
		{
			try
			{
				if (br != null)br.close();
			} 
			catch (IOException ex)
			{
				ex.printStackTrace();
				return d;
			}
		}
		return d;
	}
	
	/**
	 * Static method to parse the given file into the Document object
	 * @param filename : The fully qualified filename to be parsed
	 * @return The parsed and fully loaded Document object
	 * @throws ParserException In case any error occurs during parsing
	 */
	static boolean populateAuthorTAG(String line, Document d)
	{
		String patternAuthorTAG = "<AUTHOR>\\s+(?i)by\\s+(.*)</AUTHOR>";
		String [] tokens = null;

		Pattern pattern = Pattern.compile(patternAuthorTAG);
		Matcher matcher = pattern.matcher(line);
		if(matcher.find())
		{
			tokens = null;
			tokens=matcher.group(1).trim().split(",");
		    if(tokens.length > 1)
		    {
		    	d.setField(FieldNames.AUTHORORG,tokens[1].trim());
		    	
		    }
		   	d.setField(FieldNames.AUTHOR,tokens[0].replaceAll("(?i)and", "and"));
		   	return true;
		}
		return false;
	}
	
	/**
	 * Static method to parse the given file into the Document object
	 * @param filename : The fully qualified filename to be parsed
	 * @return The parsed and fully loaded Document object
	 * @throws ParserException In case any error occurs during parsing
	 */
	static String placeDate(String line, Document d) 
	{
		/**
		 *  if the line does not contain "-" then we do not have
		 *  any means of differentiating and getting the date elements
		 */
		if(!line.contains(" -"))
		{
			/**
			 *  but if the line contains some date related info
			 *  like --- 
			 */
			return line;
		}
		
		/**
		 * Split the string line into constituent elements
		 * and proceed with processing
		 * to handle "Munich, 26th January -"
		 * therefore " -" to split and not " - "
		 */
		String[] tokens = line.split(" -");
		String rouge_place=null;
		// ideally there should be 2 tokens only but in case there are more
		
		// trimming all tokens
		for(int i=0;i<tokens.length;i++)
		{
			tokens[i]=tokens[i].trim();
		
		}
		
		/** cleaning and finding date related component **/
		tokens[0] = tokens[0].replaceAll(",$","");
		
		String place_N_date[] = tokens[0].split(",");
		int size_place_date = place_N_date.length;
		int offset=0;
		/**
		 * we cannot be under the assumption that the last element of the
		 * tokens[0] component will be date 
		 */
		find_month_matcher= find_month_pattern.matcher(place_N_date[size_place_date-1]);
		if(find_month_matcher.find())
		{
			// to handle cases like MINNETONKA,Minn. March 12
			if(place_N_date[size_place_date-1].matches(REGEXCONSTANTS.REGEX_FIND_ROUGE_NEWSDATE))
			{
				String rouge[] =place_N_date[size_place_date-1].trim().split(" ");
				
				rouge_place=rouge[0];
				if(rouge.length==2)
				{
					d.setField(FieldNames.NEWSDATE,rouge[1].trim());
				}
				else if(rouge.length==3)
				{
					d.setField(FieldNames.NEWSDATE,rouge[1]+" "+rouge[2]);
				}
			}
			else
			{
				d.setField(FieldNames.NEWSDATE,place_N_date[size_place_date-1].trim());
			}
			offset=1;
		}
		else
		{
			// This must be true in this scenario // FIXME --
			if(place_N_date[size_place_date-1].trim().equalsIgnoreCase("Reuter"))
			{
				d.setField(FieldNames.AUTHORORG,place_N_date[size_place_date-1].trim());
				d.setField(FieldNames.NEWSDATE,place_N_date[size_place_date-2].trim());
				offset=2;
			}
			else // non std. format
			{	
				return(formatOutOFSync(line,d));
			}
		}
		
		/** Build place info and have it added to the document **/
		StringBuilder tmpPlace = new StringBuilder(place_N_date[0]);
		if(rouge_place!=null)
		{
			tmpPlace.append(rouge_place);
		}
		for(int i = 1;i<place_N_date.length-offset;i++) 
		{
			tmpPlace.append(", ");
			tmpPlace.append(place_N_date[i].trim());
			
		}
		d.setField(FieldNames.PLACE,tmpPlace.toString());
		
		
		/** 
		 * Since the line has some content as part of it we need 
		 * to send back the content 
		 * e.g. Munich, 26 January - Rajaram was found dead!
		 */
		
		/**
		 *  if line dealing with is something like
		 *  Munich, 27th January -
		 */
		if(tokens.length==1)
		{
			return "";
		}
		else // remember token[0] is the one we have been dealing with
		{
			StringBuilder content= new StringBuilder();
			for(int i=1;i<tokens.length;i++) 
			{
				content.append(tokens[i]);
			}
			//System.out.println("sending  "+content);
			return content.toString();	
		}
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

	/**
	 * 
	 * @param line
	 * @return
	 */
	private static String formatOutOFSync(String line,Document d)
	{
		StringBuilder content= new StringBuilder();
		/**
		 *  identify the problem -- Find out which one of these tokens
		 *  has the dateformat 
		 */
		
		if(!line.contains("-")) return line;
		
		String[] tokens = line.split("-");
		int index=0;
		for(;index<tokens.length;index++)
		{
			find_month_matcher=find_month_pattern.matcher(tokens[index]);
			if(find_month_matcher.find())
				break;
		}
		
		/**
		 * This document in out of whack --- there is a hypen but no
		 * date term -- so extract place and content and send it out
		 */
		if((index >= tokens.length))
		{
			return line;
		}
		
		
		/***********************
		 * settle content now!
		 * Having found "---"
		 ***********************/
		for(int i=index+1;i < tokens.length;i++)
		{
			content.append(tokens[i]+" ");
		}
		/*****************************
		 * settle date and place
		 * handle the date index ---
		 ****************************/
		String date=tokens[index].replaceAll(",$","");
		String date_tokens[]=date.split(",");
		int size_dateTokens=date_tokens.length;
		
		/********************************************
		 * Now find a date element and parse the rest
		 * where ever date appears we shall have the 
		 * place before that
		 ********************************************/
		int date_index=0;
		for(;date_index<size_dateTokens;date_index++)
		{
			find_month_matcher=find_month_pattern.matcher(date_tokens[date_index]);
			if(find_month_matcher.find())
			{
				d.setField(FieldNames.NEWSDATE,date_tokens[date_index]);
				break;
			}
		}
		
		/******************************
		 *  construct place level info
		 ******************************/
		StringBuilder tmpPlace=new StringBuilder();
		if(date_index!=0)
		{
			for(int i=0;i<date_index;i++)
			{
				tmpPlace.append(date_tokens[i]+" ");
			}	
			
		}
		else if(index !=0)
		{
			for(int i=0;i<index;i++)
			{
				tmpPlace.append(tokens[i]+" ");
			}
		}
		
		if(!tmpPlace.toString().isEmpty())
		{
			d.setField(FieldNames.PLACE,tmpPlace.toString());
		}
		return content.toString();
	}
}