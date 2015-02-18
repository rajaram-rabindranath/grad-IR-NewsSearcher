package edu.buffalo.cse.irf14.query;

public class resultElement
{
	public String docName;
	public String snippet;
	public String title;
	public double relevanceScore;
	
	public resultElement(String docName,double rel)
	{
		this.docName=docName;
		this.relevanceScore=rel;
	}
}