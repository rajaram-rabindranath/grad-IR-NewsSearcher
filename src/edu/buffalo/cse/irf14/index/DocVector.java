package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;


public class DocVector
{
	public IndexBundleType type;
	public ArrayList<DocVec_Element> vector=  new ArrayList<DocVec_Element>();
	
	// --- 
	/*public double wTQ=0.0f;
	public int qTF=0;
	*/
	/*public int df=0;
	public String term;
	*///public vectorType vec_type; // helps distinguish of it is a result of a query ops or result of AND/OR/NOT
	
	/*public DocVector(int df,String term)
	{
		
		
	}
	*/
	//public void setWTQ(int index,int qTF)
	/*public void setQueryTermStats(int qTF,double wTQ)
	{
		for(int i=0;i<vector.size();i++)
		{
			vector.get(i).setWTQ(qTF,wTQ);
		}
	}*/
	
	// store the wTQ
	/*public void computeWTQ(int qTF,double numDocs)
	{
		*//**
		 * Each element of the document vector must have 
		 * the wTQ weight -- 
		 *//*
		System.out.println("df"+df);
		System.out.println("qTF"+qTF);
		System.out.println("num docs"+numDocs);
		double a = ((double)numDocs)/((double)df);
		double b = (double) qTF;
		double wTQ=b*Math.log(a);
		setQueryTermStats(qTF,wTQ);
	}*/
	
	public void add(DocVec_Element ele)
	{
		vector.add(ele);
	}
	
	public boolean isEmpty()
	{
		if(vector.size() == 0) return true;
		else return false;
	}
	
	public int size()
	{
		return vector.size();
	}
	
	public DocVec_Element get(int index)
	{
		return vector.get(index);
	}
	
	/**
	 * 
	 */
	public void printVector()
	{
		StringBuffer re = new StringBuffer();
		
		for(DocVec_Element a : vector)
		{
			re.append(a.docName);
			re.append("::");
			re.append(a.docID);
			re.append("#");
			re.append(a.relevanceScore);
			re.append("--->");
			re.append(a.getTermStats());
			re.append("\n");
		}
		
		//System.out.println(re.toString());
	}
}


