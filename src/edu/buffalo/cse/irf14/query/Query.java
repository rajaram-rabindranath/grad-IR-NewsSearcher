package edu.buffalo.cse.irf14.query;

/**
 * Class that represents a parsed query
 * @author nikhillo
 *
 */
public class Query 
{

	/** query grammar semantics **/
	static final String queryEnd=" }";
	static final String queryBegin="{ ";
	static final String querySubsetStart="[ ";
	static final String querySubsetEnd=" ]";
	static final String notEnd=">";
	static final String notBegin="<";
	
	public String userQuery;
	public String representation;
	public String queryID;
	
	public qBag bag;
	
	public boolean hasClause=false;
	public boolean hasIndex=false;
	
	public Query(String userQ,String rep)
	{
		this.userQuery=userQ;
		this.representation=rep;
	}
	
	public Query(String userQ,String rep,String queryID)
	{
		this.userQuery=userQ;
		this.representation=rep;
		this.queryID=queryID;
	}
	
	/**
	 * Break query into individual components
	 */
	/*public qBag queryBreakUp()
	{
		String qClear = representation.substring(1,representation.length()-1).trim();
		 Interpret the query
		 *  Break it into consumable component
		 *  { [ Term:Love AND <Term:War> ] AND [ Category:movies AND <Category:crime> ] }
		 *  A. [ Term:Love AND <Term:War> ] 
		 *  B. AND
		 *  C. [ Category:movies AND <Category:crime> ]
		 
		
		// each clause has to be handled
		String toks[]=qClear.split(" ");
		
		for(int i=0;i<toks.length;i++)
		{
			System.out.println(toks[i]);
			if(toks[i].equals("["))
			{
				
			}
		}
	}*/
	
	/**
	 * EXTRA INFO
	 */
	
	/**
	 * Method to convert given parsed query into string
	 */
	public String toString() 
	{
		return representation;
	}
	
	
}

class qBag
{
	
}
