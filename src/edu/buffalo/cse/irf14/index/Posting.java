package edu.buffalo.cse.irf14.index;

public class Posting
{
	int docID;
	int termFrequency;
	
	public Posting(int docID)
	{
		this.docID = docID;
		termFrequency = 1;
	}
	
	public Posting(int docID, int termFrequency)
	{
		this.docID = docID;
		this.termFrequency = termFrequency;
	}
	
	public void incrementTermFrequency()
	{
		termFrequency++;
	}
	
	public boolean equals(Posting p)
	{
		return docID == p.docID;
	}
}