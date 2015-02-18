package edu.buffalo.cse.irf14.index;

public class DocumentStats 
{
	public int doclength;
	public String documentName;
	int docID;
	public int maxTermFreq;
	public double docEuc=0.0f;
	
	
	public DocumentStats(int docID,String docName,int docLength,double docEdu ,int maxTermFreq)
	{
		this.doclength=docLength;
		this.documentName=docName;
		this.docID=docID;
		this.maxTermFreq=maxTermFreq;
		this.docEuc=docEdu;
	}
	
	public DocumentStats(int docID,String docName,int docLength)
	{
		this.doclength=docLength;
		this.documentName=docName;
		this.docID=docID;
	}
	
	public DocumentStats(int docID,String docName)
	{
		this.documentName=docName;
		this.docID=docID;
	}
}
