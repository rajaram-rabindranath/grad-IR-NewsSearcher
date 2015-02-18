package edu.buffalo.cse.irf14.query;



import java.util.ArrayList;

import edu.buffalo.cse.irf14.index.DocVec_Element;
public class QueryResultSet 
{
	
	public ArrayList<resultElement> resultSet=new ArrayList<resultElement>();
	String qID;
	
	public QueryResultSet(String qID)
	{
		this.qID=qID;
	}
	
	public QueryResultSet(String qID,ArrayList<DocVec_Element> topResults)
	{
		this.qID=qID;
		for(DocVec_Element a : topResults)
		{
			resultElement n = new resultElement(a.docName,a.relevanceScore);
			resultSet.add(n);
		}
	}
	
	public void add(resultElement element)
	{
		resultSet.add(element);
	}
	/**
	 * 
	 * @return
	 */
	public String unroll()
	{
		StringBuffer re=new StringBuffer();
		re.append(qID);
		re.append(":");
		re.append("{");
		resultElement ele;
		for(int i=0;i<resultSet.size()-1;i++)
		{
			ele=resultSet.get(i);
			re.append(ele.docName);
			re.append("#");
			if(ele.relevanceScore > 1.0) ele.relevanceScore=0.99;
			re.append(ele.relevanceScore);
			re.append(",");
			re.append(" ");
		}
		
		ele=resultSet.get(resultSet.size()-1);
		re.append(ele.docName);re.append("#"); re.append(ele.relevanceScore);
		re.append("}");
		re.append("\n");
		return re.toString();
	}
	
	public int size()
	{
		return resultSet.size();
	}
	
	public void pack(ArrayList<DocVec_Element> bag)
	{
		for(DocVec_Element a : bag)
		{
			
			resultSet.add(new resultElement(a.docName,a.relevanceScore));
		}
	}
}


