package edu.buffalo.cse.irf14.index;
public class termStats
{
	public double df;
	public double tf;
	public int termID;
	public String term;
	public double qtf;
	
	public termStats(int df,int tf,double qTF,String term)
	{
		this.df=df;
		this.tf=tf;
		this.term=term;
		this.qtf=qTF;
	}
	
	
	public void setqtf(int qTF)
	{
		this.qtf=qTF;
	}
	
	public termStats(int df,int tf,String term)
	{
		this.df=df;
		this.tf=tf;
		this.term=term;
	}
	
	/*public void setqueryTermWeight(double wTQ)
	{
		this.wTQ=wTQ;
	}
	*/
	public void setTerm(String term)
	{
		this.term=term;
	}
}