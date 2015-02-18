package edu.buffalo.cse.irf14.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class TermMetaData 
{

	private String term;
	int termID;
	int totalFreq;
	public ArrayList<Posting> postingsList;
	private HashMap<Integer,Integer> locationOfPosting;
	
	//field for type of Entry
	public TermMetaData()
	{
		
	}
	
	public TermMetaData(String term)
	{
		this.term = term;
		termID = -1;
		postingsList=new ArrayList<Posting>();
		locationOfPosting = new HashMap<Integer,Integer>();
		totalFreq = 0;
	}
	
	/**
	 * Will be set once the entire corpus is 
	 * parsed for terms -- as TreeMap shall take care
	 * of sort
	 * @param TermID
	 */
	public void setTermID(int TermID)
	{
		termID =  TermID;
	}
	
	public String toString()
	{
		return term;
	}
	
	public void addPosting(int docID)
	{
		/**
		 * have encountered this term before in the 
		 * document referenced by docID ?
		 */
		if(!locationOfPosting.containsKey(docID))
		{
			postingsList.add(new Posting(docID));
			locationOfPosting.put(docID,postingsList.size() - 1);
		}
		else
		{
			Posting posting = postingsList.get(locationOfPosting.get(docID));
			posting.incrementTermFrequency();
		}
		totalFreq++;
	}

	/*public ArrayList<Posting> getPostings()
	{
		return postingsList;
	}
	*/
	public int getTotalFreq()
	{
		return totalFreq;
	}
	
	public String postingListToString() 
	{
		StringBuilder postingsData = new StringBuilder();
		postingsData.append(Delimiters.delimTermPosting);
		Posting v = null;
		int lastDocID = 0, diffDocID = 0;
		for(int i = 0; i < postingsList.size(); i++)
		{	
			v = postingsList.get(i);
			if(i == 0)
			{
				lastDocID = v.docID;
				diffDocID = lastDocID;
			}
			else
			{
				diffDocID = v.docID - lastDocID;
			}
			
			postingsData.append(diffDocID + Delimiters.delimPosting + v.termFrequency);
			
			if(i != postingsList.size() - 1)
			{
				postingsData.append(",");
			}
			lastDocID = v.docID;
		}
		// appending the last posting
		postingsData.append(">");
		return postingsData.toString();
	}
}