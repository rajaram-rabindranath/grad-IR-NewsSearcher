package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import edu.buffalo.cse.irf14.analysis.Token;
import edu.buffalo.cse.irf14.analysis.TokenStream;

public class IndexConstructor extends Thread
{
	TreeMap<String,TermMetaData> index_;
	IndexType type;
	File indexFile;
	File dictionaryFile;
	/**
	 * 
	 * @param type
	 */
	public IndexConstructor(IndexType type,File indexFile,File dictionaryFile)
	{
		this.type = type;
		index_=new TreeMap<String,TermMetaData>();
		this.indexFile=indexFile;
		this.dictionaryFile=dictionaryFile;
	}
	
	/**
	 * 
	 * @param term
	 * @param docID
	 */
	public void addTermsInBulk(TokenStream dataStream, int docID)
	{
		Token tok=null;
		String term=null;
		dataStream.reset();
		
		while(dataStream.hasNext())
		{
			tok=dataStream.next();
			term=tok.toString().toLowerCase();
			if(term==null || term.isEmpty()) continue;
			
			TermMetaData termData;
			if(index_.containsKey(term))
			{
				index_.get(term).addPosting(docID);
			}
			else
			{
				termData = new TermMetaData(term);
				index_.put(term,termData);
				termData.addPosting(docID);
			}
		}
	}
	
	public int getTermId(String term)
	{
		int ID = -1;
		if(index_.containsKey(term))
			ID=index_.get(term).termID;
		
		return ID;
	}
	
	public int getDocumentFrequency(String term)
	{
		int documentFrequency=0;
		if(index_.containsKey(term))
		{
			documentFrequency = index_.get(term).postingsList.size();
		}
		return documentFrequency;
	}
	
	public boolean containsTerm(String entry)
	{
		boolean retVal=false;
		if(index_.containsKey(entry))
			retVal=true;
		
		return retVal;
	}
	
	private void assignTermID()
	{
		int termID = 1;
		TermMetaData termObj;
		for(String term : index_.keySet())
		{
			termObj = index_.get(term);
			termObj.setTermID(termID);
			termID++;
		}
	}
	
	
	public void run()
	{
		try
		{
			writeIndex_n_Dictionary();
		}
		catch(FileNotFoundException fex)
		{
			fex.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @throws FileNotFoundException
	 */
	public void writeIndex_n_Dictionary() throws FileNotFoundException
	{
		BufferedWriter dictionaryWriter, indexWriter;
		TermMetaData termObj;
		int termID;
		StringBuilder dictionaryData=new StringBuilder();
		StringBuilder indexData=new StringBuilder();
	
		// actually don't have to do this -- FIXME
		this.assignTermID();
		
		try
		{
			dictionaryWriter=new BufferedWriter(new FileWriter(dictionaryFile));
			indexWriter=new BufferedWriter(new FileWriter(indexFile));
			
			for(String term : index_.keySet())
			{
				termObj=index_.get(term);
				termID=termObj.termID;
				
				
				/* construct index data */
				dictionaryData.append(term); 
				dictionaryData.append(Delimiters.delimDictionary);
				dictionaryData.append(termID);
				dictionaryData.append(Delimiters.delimDictionary);
				dictionaryData.append(termObj.postingsList.size());
				dictionaryData.append(Delimiters.delimDictionary);
				dictionaryData.append(termObj.getTotalFreq());
				dictionaryData.append("\n");
						 
				/* construct index data */
				indexData.append(termID);
				indexData.append(termObj.postingListToString());
				indexData.append("\n");
			}
			
			// write N flush
			dictionaryWriter.write(dictionaryData.toString());
			dictionaryWriter.flush();
			indexWriter.write(indexData.toString());
			indexWriter.flush();
			
			// close
			indexWriter.close();
			dictionaryWriter.close();
		}
		catch(FileNotFoundException fex)
		{
			throw fex;
		}
		catch (IOException e) 
		{
			System.out.println("Problems in writing index to file");
			e.printStackTrace();
		}
	}
}