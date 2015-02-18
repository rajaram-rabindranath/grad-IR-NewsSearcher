package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;

public class DocVec_Element
{
	public int docID;
	public String docName;
	public double relevanceScore;
	public ArrayList<termStats> statsList = new ArrayList<termStats>();
	
	/**
	 * 
	 * @param term
	 */
	public void setTerm(String term)
	{
		for(termStats ele : statsList)
		{
			ele.term=term;
		}
	}
	
	/**
	 * 
	 * @param docID
	 * @param docName
	 * @param df
	 * @param tf
	 * @param term
	 * @param qTF
	 */
	public DocVec_Element(int docID,String docName,int df,int tf,String term,double qTF)
	{
		statsList.add(new termStats(df,tf,qTF,term));
		this.docID=docID;
		this.docName=docName;
	}
	
	/**
	 * 
	 * @param tstatsList
	 */
	public void add(ArrayList<termStats> tstatsList)
	{
		statsList.addAll(tstatsList);
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	public boolean equals(DocVec_Element a)
	{
		if(a.docID == this.docID) return true;
		else return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getTermStats()
	{
		
		String theList="";
		for(termStats n : statsList)
		{
			theList+="<"+n.term+":"+n.df+":"+n.tf+"|"+n.qtf+">";
		}
		return theList;
	}
}