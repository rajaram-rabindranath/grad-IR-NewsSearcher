/**
 * 
 */
package edu.buffalo.cse.irf14.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.irf14.analysis.REGEXCONSTANTS;



/**
 * @author nikhillo
 * Static parser that converts raw text to Query objects
 */
public class QueryParser 
{
	private static final String clauseBegin="(";
	private static final String clauseEnd=")";
	private static final String delim=" ";
	private static final String TERM="Term:";
	private static final String defOp="OR";
	
	
	public static Query parse(String userQuery)
	{
		return parse(userQuery,defOp);
	}
	
	/**
	 * MEthod to parse the given user query into a Query object
	 * @param userQuery : The query to parse
	 * @param defaultOperator : The default operator to use, one amongst (AND|OR)
	 * @return Query object if successfully parsed, null otherwise
	 */
	public static Query parse(String userQuery, String defaultOperator) 
	{
		boolean isPhrase_present=false;
		boolean isClause_present=false;
		boolean isNOT = false;
		/** query re-constructor **/
		StringBuffer re =new StringBuffer(Query.queryBegin);
				
		/**
		 * 1. Identify the indexes that are being requested
		 * 2. Identify Phrases
		 * 2. Deal with clauses
		 * 3. Reconstruct the query given by user
		 */
		//System.out.println("Read from file"+userQuery+"----");
		
		if(userQuery.matches(REGEXCONSTANTS.REGEX_HAS_PHRASE))
		{
			System.out.println("Has phrase"+userQuery);
			/*if(userQuery.matches(REGEXCONSTANTS.REGEX_ONLY_PHRASE))
			{
				
				// pack the query and send it across
				re.append(TERM);
				re.append(userQuery);
				re.append(Query.queryEnd);
				return new Query(userQuery,re.toString());
			}*/
		}
		// if has clause
		if(userQuery.matches(REGEXCONSTANTS.REGEX_HAS_CLAUSE))
		{
			
			// BAD QUERY---
			if(userQuery.length() - userQuery.replace(clauseBegin, "").length() != userQuery.length() - userQuery.replace(clauseEnd,"").length())
			{
				//System.out.println(userQuery.length() - userQuery.replace(clauseBegin, "").length());
				//System.out.println(userQuery.length() - userQuery.replace(clauseEnd,"").length());
				System.out.println("BAD "+userQuery);
				return null;
			}
			return new Query(userQuery,handleClause(userQuery,defaultOperator));
		}
		
		
		// need state information ---
		// if the query has Index reference 
		boolean enclose=false;
		boolean hasFacet=false;
		
		// A simple query
		if(!isClause_present && !isPhrase_present)
		{
			String tokens[]=userQuery.split(" ");
			for(int i =0;i<tokens.length;i++)//String a : tokens)
			{
				/**
				 * Keep accumulating if it is a phrase query
				 */
				if(tokens[i].matches("^\".+"))
				{
					if(isNOT)
					{
						re.append(Query.notBegin);
					}
					re.append(TERM);
					int j=0;
					for(j=i;j<tokens.length;j++)
					{
						//System.out.println("JAAM");
						re.append(tokens[j]);
						re.append(" ");
						if(tokens[j].matches(".+\""))
						{
							break;
						}
					}
					i=j;
					if(isNOT)
					{
						re.deleteCharAt(re.length()-1);
						re.append(Query.notEnd);
						re.append(" ");
						isNOT=false;
					}
					continue;
				}
				else if(tokens[i].matches(REGEXCONSTANTS.REGEX_IS_BOOL))
				{
					if(tokens[i].equals("NOT"))
					{
						re.append("AND ");
						isNOT=true;
					}
					else
					{
						re.append(tokens[i]+" ");
					}
					continue;
				}
				else if(tokens[i].matches(REGEXCONSTANTS.REGEX_IS_INDECES))
				{
					if(enclose)
					{
						re.append(Query.querySubsetEnd);
						re.append(" ");
					}
					hasFacet=true;
					// check if this is a phrase right -- Author:"james newton"
					if(tokens[i].contains("\""))
					{
						int j=0;
						for(j=i;j<tokens.length;j++)
						{
							re.append(tokens[j]);
							re.append(" ");
							if(tokens[j].matches(".+\""))
							{
								break;
							}
						}
						i=j;
					}
					else
					{
						re.append(tokens[i]+" ");
					}
					continue;
				}
				
				/**
				 * NOT INDEX 
				 * NOT PHRASE QUERY
				 * NOT BOOLEAN
				 * -- so go ahead with this twisted logic
				 */
				if(i>0 && !tokens[i-1].matches(REGEXCONSTANTS.REGEX_IS_BOOL))
				{
					re.append(defaultOperator);
					re.append(" ");
				}
				/*if(i>0 && tokens[i-1].matches(REGEXCONSTANTS.REGEX_IS_INDECES))
				{
					re.append(Query.querySubsetStart);
				}*/
				
				if(isNOT)
				{
					re.append(Query.notBegin);
					re.append(TERM);
					re.append(tokens[i]);
					re.append(Query.notEnd);
					isNOT=false;
				}
				else
				{ 
					if(hasFacet)
					{
						if(i<tokens.length-1 && i>0)
						{
							/*
							 * Next term should not have quotes
							 */
							System.out.println(tokens[i]);
							System.out.println(tokens[i+1]);
							if(!tokens[i+1].matches(REGEXCONSTANTS.REGEX_IS_BOOL))
							{
								re.append(Query.querySubsetStart);
								enclose=true;
								hasFacet=false;
							}
							else
							{
								hasFacet=false;
							}
						}
					}
					re.append(TERM);
					re.append(tokens[i]);
				}
				re.append(" ");
			}
		}
		re.deleteCharAt(re.length()-1);
		if(enclose) re.append(Query.querySubsetEnd);
		re.append(Query.queryEnd);
		return new Query(userQuery, re.toString());
	}
	
	
	/**
	 * @param toks
	 * @param i
	 * @return
	 */
	static packInfo indexClause(String[] toks,int i)
	{
		String []toks2 = toks[i].split(":");
		boolean isNot=false;
		//System.out.println("Index clause is being called");
		//System.out.println("toks 1"+toks2[0]);
		//System.out.println("toks 2"+toks2[1]);
		StringBuffer re =new StringBuffer();
		
		int numOpen=0;
		int numClose=0;
		
		numOpen=toks[i].length() - toks[i].replaceAll("\\(","").length();
		//System.out.println("num open"+numOpen);
		
		Matcher matcher= Pattern.compile(REGEXCONSTANTS.REGEX_OPEN_BRACKS).matcher(toks2[1]);
		matcher.find();
		re.append(toks2[1].substring(0,matcher.end()).replace(clauseBegin,Query.querySubsetStart));
		re.append(toks2[0]+":");
		String text=toks2[1].substring(matcher.end());
		re.append(text);
		re.append(" ");
		boolean getout=false;
		i++;
		if(text.matches("^\".+"))
		{
			if(text.matches(".+\"$"))
			{
				
				
			}
			else
			{
				for(;i<toks.length;i++)
				{
					// wary of end of bracks
					if(toks[i].matches(".+\"\\)?"))
					{
						if(toks[i].matches(".+\\)"))
						{
							matcher = Pattern.compile(REGEXCONSTANTS.REGEX_CLOSE_BRACKS).matcher(toks[i]);
							matcher.find();
							re.append(toks[i].substring(0,matcher.start()));
							re.append(toks[i].substring(matcher.start()).replace(clauseEnd,Query.querySubsetEnd));
							re.append(" ");
						}
						else
						{
							re.append(toks[i]);
							re.append(" ");
						}
						i++;
						break;
					}
					re.append(toks[i]);
					re.append(" ");
				}
			}
		}
		
		
		for(;i<toks.length;i++)
		{
			//System.out.println("printing toks"+toks[i]);
			if(toks[i].matches(REGEXCONSTANTS.REGEX_IS_BOOL))
			{
				if(toks[i].equals("NOT"))
				{
					re.append("AND ");
					re.append(Query.notBegin);
					isNot=true;
					continue;
				}
				else
				{
					re.append(toks[i]);
				}	
			}
			else if(toks[i].matches(REGEXCONSTANTS.REGEX_CLOSE_BRACKS_SEQ))
			{
				if(!toks[i-1].matches("OR|AND") && !isNot)
				{
					re.append(defOp);
					re.append(" ");
				}
				int howMany=toks[i].length() - toks[i].replaceAll("\\(","").length()-1;
				matcher = Pattern.compile(REGEXCONSTANTS.REGEX_CLOSE_BRACKS).matcher(toks[i]);
				matcher.find();
				re.append(toks2[0]+":");
				re.append(toks[i].substring(0,matcher.start()));
				if(isNot)
				{
					re.append(Query.notEnd);
					isNot=false;
				}
				re.append(toks[i].substring(matcher.start()).replace(clauseEnd,Query.querySubsetEnd));
				numOpen-=howMany;
				//if(numOpen==0)
				break;
			}
			else
			{
				if(!toks[i-1].matches("OR|AND") && !isNot)
				{
					re.append(defOp);
					re.append(" ");
				}
				/*
				if(toks[i].contains("("))
				{
					System.out.println("I am here");
					int howMany=toks[i].length() - toks[i].replaceAll("\\(","").length();
					System.out.println("How many"+howMany);
					numOpen+=howMany;
					matcher = Pattern.compile(REGEXCONSTANTS.REGEX_OPEN_BRACKS).matcher(toks[i]);
					
					matcher.find();
					re.append(toks2[1].substring(0,matcher.end()).replace(clauseBegin,Query.querySubsetStart));
					re.append(toks2[0]+":");
					
					System.out.println("start "+matcher.start());
					re.append(toks[i].substring(matcher.end()));
				}*/
				///else
				{
					re.append(toks2[0]+":");
					re.append(toks[i]);
				}
				
				if(toks[i].matches("^\".+") && !toks[i].matches(".+\""))
				{
					re.append(" ");
					i++;
					for(;i<toks.length;i++)
					{
						if(toks[i].matches(".+\"\\)?"))
						{
							if(toks[i].matches(".+\\)"))
							{
								matcher = Pattern.compile(REGEXCONSTANTS.REGEX_CLOSE_BRACKS).matcher(toks[i]);
								matcher.find();
								re.append(toks[i].substring(0,matcher.start()));
								if(isNot)
								{
									re.append(Query.notEnd);
									isNot=false;
								}
								re.append(toks[i].substring(matcher.start()).replace(clauseEnd,Query.querySubsetEnd));
								re.append(" ");
							}
							else
							{
								re.append(toks[i]);
								re.append(" ");
							}
							getout=true;
							break;
						}
						re.append(toks[i]);
						re.append(" ");
					}
				}
				if(isNot)
				{
					re.append(Query.notEnd);
					isNot=false;
				}
				
				//if(getout && numOpen==0)break;
				if(getout)break;
			}
			re.append(" ");
			
		}
		
		//System.out.println(re.toString());
		String ret =re.toString().replaceAll("\\)", " ]");
		ret=ret.replaceAll("  ", " ");
		//String[] tokens=ret.toString().split(" ");
		/*if(!(ret.contains("OR") || ret.contains("AND")))
		{
			String[] tokens=ret.split(" ");
			String retVal="";
			for(int m=0;i<tokens.length;i++)
			{
				if(tokens[i].matches("\\["))
				{
					retVal=
				}
			}
		}
		else
		{*/
			return new packInfo(ret,i);
		/*}*/
	}
	
	
	/**
	 * 
	 * @param userQuery
	 * @return
	 */
	static String handleClause(String userQuery,String defOperator)
	{
		//System.out.println("Handle clauses");
		StringBuffer re=new StringBuffer(Query.queryBegin);
		boolean isNot=false;
		String toks[]=userQuery.split(delim);
		Matcher matcher;
		boolean inBracketNot=false;
		for(int i=0;i<toks.length;i++)
		{
			//System.out.println(toks[i]);
			if(i>0 && (!toks[i].matches(REGEXCONSTANTS.REGEX_IS_BOOL) && !toks[i-1].matches(REGEXCONSTANTS.REGEX_IS_BOOL)))
			{
				re.append(defOperator);
				re.append(" ");
			}
			
			if(toks[i].matches("NOT"))
			{
				isNot=true;
				re.append("AND ");
				re.append(Query.notBegin);
				continue;
			}
			else if(toks[i].matches("^\".+"))
			{
				if(inBracketNot) re.append(Query.notBegin);
				re.append(TERM);
				int j=0;
				for(j=i;j<toks.length;j++)
				{
					
					//System.out.println(toks[j]);
					/**
					 * get everything that belongs to quotes
					 * sometimes because there is not space btw quotes
					 * and brackets we can have some problems
					 */
					if(toks[j].matches(".+\"\\)*"))
					{
						//System.out.println("THE CULPRIT "+toks[j]);
						if(toks[j].matches(".+\\)+"))
						{
							matcher = Pattern.compile(".+\\)+").matcher(toks[j]);
							matcher.find();
							
							re.append(toks[j].substring(0,matcher.start()));
							if(inBracketNot)
							{
								re.append(Query.notEnd);
								inBracketNot=false;
							}
							re.append(toks[j].substring(matcher.start()).replaceAll("\\)",Query.querySubsetEnd));
							re.append(" ");
						}
						else
						{
							re.append(toks[j]);
							re.append(" ");
						}
						break;
					}
					re.append(toks[j]);
					re.append(" ");
				}
				i=j;
				
				if(isNot || inBracketNot)
				{
					re.deleteCharAt(re.length()-1);
					re.append(Query.notEnd);
					re.append(" ");
					isNot=false;
				}
				continue;
			}
			else if(toks[i].matches("AND|OR"))
			{
				re.append(toks[i]);
			}
			// do we have index info ? yes then send it across to index clause handler
			else if(toks[i].matches(REGEXCONSTANTS.REGEX_IS_INDECES))
			{
				if(toks[i].matches(REGEXCONSTANTS.REGEX_HAS_CLAUSE_START))
				{
					packInfo M=indexClause(toks,i);
					re.append(M.result);
					i=M.index;
				}
				else if(toks[i].contains("\""))
				{
					int j=0;
					for(j=i;j<toks.length;j++)
					{
						re.append(toks[j]);
						re.append(" ");
						if(toks[j].matches(".+\""))
						{
							
							break;
						}
					}
					i=j;
					continue;
				}
				else
				{
					re.append(toks[i]);
				}
			}
			else if(toks[i].matches(REGEXCONSTANTS.REGEX_OPEN_BRACKS_SEQ))
			{
				if(isNot) // the whole clause have to go into the not sections
				{
					re.deleteCharAt(re.length()-1); // I would have added the "<" to indicate NOT operation therefore
					inBracketNot=true;
					isNot=false;
				}
				
				matcher = Pattern.compile(REGEXCONSTANTS.REGEX_OPEN_BRACKS).matcher(toks[i]);
				matcher.find();
				re.append(toks[i].substring(0,matcher.end()).replace(clauseBegin,Query.querySubsetStart));
				if(toks[i].substring(matcher.end()).equals("NOT"))
				{
					isNot=true;
					re.append(Query.notBegin);
					continue;
				}
				else
				{
					if(inBracketNot)re.append(Query.notBegin);
					re.append(TERM);
					re.append(toks[i].substring(matcher.end()));
					if(toks[i].contains("\""))
					{
						re.append(" ");
						int j=0;
						for(j=i+1;j<toks.length;j++)
						{
							
							if(toks[j].matches(".+\"\\)*"))
							{
								if(toks[j].matches(".+\\)"))
								{
									matcher = Pattern.compile(REGEXCONSTANTS.REGEX_CLOSE_BRACKS).matcher(toks[j]);
									matcher.find();
									re.append(toks[j].substring(0,matcher.start()));
									if(inBracketNot)
									{
										re.append(Query.notEnd);
										inBracketNot=false;
									}
									re.append(toks[j].substring(matcher.start()).replace(clauseEnd,Query.querySubsetEnd));
									
									re.append(" ");
								}
								else
								{
									re.append(toks[j]);
									re.append(" ");
								}
								break;
							}
							re.append(toks[j]);
							re.append(" ");
						}
						i=j;
						re.deleteCharAt(re.length()-1);
					}
					if(inBracketNot)re.append(Query.notEnd);
				}
			}
			else if(toks[i].matches(REGEXCONSTANTS.REGEX_CLOSE_BRACKS_SEQ))
			{
				matcher = Pattern.compile(REGEXCONSTANTS.REGEX_CLOSE_BRACKS).matcher(toks[i]);
				matcher.find();
				if(inBracketNot)re.append(Query.notBegin);
				re.append(TERM);
				re.append(toks[i].substring(0,matcher.start()));
				if(isNot || inBracketNot)
				{
					re.append(Query.notEnd);
					isNot=false;
					inBracketNot=false;
				}
				re.append(toks[i].substring(matcher.start()).replace(clauseEnd,Query.querySubsetEnd));
			}
			else
			{
				if(inBracketNot)
				{
					re.append(Query.notBegin);
				}
				re.append(TERM);
				re.append(toks[i]);
				if(isNot || inBracketNot)
				{
					re.append(Query.notEnd);
					isNot=false;
				}
			}
			re.append(" ");
		}
		re.deleteCharAt(re.length()-1);
		re.append(Query.queryEnd);
		return re.toString();		
	}
	
}

class packInfo
{
	int index;
	String result;
	
	public packInfo(String s,int i)
	{
		result=s;
		index=i;
	}
}