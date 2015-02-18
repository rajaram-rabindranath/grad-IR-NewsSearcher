package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class IndexBundle
{
	private HashMap<String,Integer> Dictionary; 
	private HashMap<Integer,ArrayList<Posting>> postingCollection;
	private HashMap<Integer,Integer> df_Map;
	
	public IndexBundle(HashMap<String,Integer> Dictionary,HashMap<Integer,ArrayList<Posting>> postingCollection,HashMap<Integer,Integer> df_Map)
	{
		this.Dictionary=Dictionary;
		this.postingCollection=postingCollection;
		this.df_Map=df_Map;
	}
	
	public DocVector search(String term, double qTF)
	{
		ArrayList<Posting> list = postingCollection.get(Dictionary.get(term));
		
		if(list==null)
		{
			System.out.println("Search item "+term+" does not exist");
			return null;
		}
		
		int df=list.size();
		DocVector vector = new DocVector();
		for(Posting p : list)
		{
			String docName=IndexReader.getDocName(p.docID);
			DocVec_Element a = new DocVec_Element(p.docID,docName,df,p.termFrequency,term,qTF);
			vector.add(a);
		}
		return vector;
	}
}
