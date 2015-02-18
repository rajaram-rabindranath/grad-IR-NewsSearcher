/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * @author nikhillo
 * Class that emulates reading data back from a written index
 */
public class IndexReader
{
	/**
	 * Default constructor
	 * @param indexDir : The root directory from which the index is to be read.
	 * This will be exactly the same directory as passed on IndexWriter. In case 
	 * you make subdirectories etc., you will have to handle it accordingly.
	 * @param type The {@link IndexType} to read from
	 */
	private String indexDir;
	private IndexType type;
	private String fname_index, fname_Dictionary, fname_documentDictionary;
	private static HashMap<Integer,DocumentStats> documentDictionary = new HashMap<Integer,DocumentStats>();
	private HashMap<String,Integer> indexTypeDictionary; 
	private HashMap<Integer,ArrayList<Posting>> postingsListMap;
	private HashMap<Integer,Integer> documentFrequencyMap;
	private TreeMap<Integer,ArrayList<String>> topIndexTerms;
	private BufferedReader br;
	
	public IndexReader(String indexDir, IndexType type) 
	{
		//TODO
		this.indexDir = indexDir;
		this.type = type;
		getFileNames();
		
		indexTypeDictionary = new HashMap<String,Integer>();
		postingsListMap = new HashMap<Integer, ArrayList<Posting>>();
		documentFrequencyMap = new HashMap<Integer,Integer>();
		
		topIndexTerms = new TreeMap<Integer,ArrayList<String>>();
		
		br = null;
		
		readIndexTypeDictionary();
		readIndexPostings();
	}
	
	public static String getDocName(int docID)
	{
		DocumentStats Doc = documentDictionary.get(docID);
		return Doc.documentName;
	}
	
	public static int NumDocs()
	{
		return documentDictionary.size();
	}
	
	public static HashMap<Integer,DocumentStats> getDocDictionary(String indexDir)
	{
		String fname_documentDictionary = new String(indexDir + File.separator + FileNames.fname_documentDictionary);
		System.out.println("Dictionary name"+fname_documentDictionary);
		readDocumentDictionary(fname_documentDictionary);
		return documentDictionary;
	}
	
	private void getFileNames() 
	{
		fname_index= new String(indexDir + File.separator + type.toString()+"Index");
		fname_Dictionary=new String(indexDir + File.separator + type.toString()+"Dictionary");
		//fname_documentDictionary = new String(indexDir + File.separator + FileNames.fname_documentDictionary);
	}
	
	private void readIndexPostings() 
	{
		// TODO Auto-generated method stub
		try {
			br = new BufferedReader(new FileReader(fname_index));
			StringTokenizer currentLineTokens;
			String currentLine;
			String[] infoStored = new String[2];
			while((currentLine = br.readLine()) != null)
			{
				currentLineTokens = new StringTokenizer(currentLine.trim(),Delimiters.delimTermPosting);
				while(currentLineTokens.hasMoreElements())
				{
					infoStored[0] = currentLineTokens.nextToken();
					infoStored[1] = currentLineTokens.nextToken();
				}
				
				postingsListMap.put(Integer.parseInt(infoStored[0]), convertToArrayList(infoStored[1]));
			}
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<Posting> convertToArrayList(String string) 
	{
		// TODO Auto-generated method stub
		String remainingString = string.trim().substring(0,string.length() - 1);
		String postingInfo;
		int lastDocID = 0;
		int originalDocID = 0;
		int fileCount = 0;
		StringTokenizer postingsTokens = new StringTokenizer(remainingString,",");
		Posting currentPosting;
		ArrayList<Posting> postingList = new ArrayList<Posting>();
		TreeMap<Integer,Posting> sortedPostingMap = new TreeMap<Integer,Posting>();;
		
		
		while(postingsTokens.hasMoreElements())
		{
			postingInfo = postingsTokens.nextToken();
			StringTokenizer postingToken = new StringTokenizer(postingInfo.trim(),Delimiters.delimPosting);
			if(fileCount == 0)
			{
				lastDocID = Integer.parseInt(postingToken.nextToken());
				originalDocID = lastDocID;
			}
			else
			{
				originalDocID = Integer.parseInt(postingToken.nextToken()) + lastDocID;
				lastDocID = originalDocID;
			}
			currentPosting = new Posting(originalDocID,Integer.parseInt(postingToken.nextToken()));
			
			if(type.equals(IndexType.CATEGORY))
			{
				sortedPostingMap.put(originalDocID, currentPosting);
			}
			else
			{
				postingList.add(currentPosting);
			}

			//postingList.add(currentPosting);
			fileCount++;
		}
		if(type.equals(IndexType.CATEGORY))
		{
			for(int key : sortedPostingMap.keySet())
			{
				postingList.add(sortedPostingMap.get(key));
			}
		}
		return postingList;
	}

	@SuppressWarnings("null")
	private void readIndexTypeDictionary()
	{
		// TODO Auto-generated method stub
		try
		{
			br = new BufferedReader(new FileReader(fname_Dictionary));
			StringTokenizer currentLineTokens;
			String currentLine;
			String[] infoStored = new String[4];
			int count;
			while((currentLine = br.readLine()) != null)
			{
				currentLineTokens = new StringTokenizer(currentLine.trim(),Delimiters.delimDictionary);
				count = 0;
				while(currentLineTokens.hasMoreTokens())
				{
					infoStored[count] = currentLineTokens.nextToken();
					count += 1;
				}
				indexTypeDictionary.put(infoStored[0], Integer.parseInt(infoStored[1]));
				documentFrequencyMap.put(Integer.parseInt(infoStored[1]), Integer.parseInt(infoStored[2]));
				if(topIndexTerms.containsKey(Integer.parseInt(infoStored[3]))){
					ArrayList<String> tokenList = topIndexTerms.get(Integer.parseInt(infoStored[3]));
					tokenList.add(infoStored[0]);
				}else{
					ArrayList<String> tokenList = new ArrayList<String>();
					tokenList.add(infoStored[0]);
					topIndexTerms.put(Integer.parseInt(infoStored[3]), tokenList);
				}
			}
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void readDocumentDictionary(String fname_documentDictionary) 
	{
		// TODO Auto-generated method stub
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fname_documentDictionary));
			StringTokenizer currentLineTokens;
			String currentLine;
			DocumentStats doc=null;
			while((currentLine = br.readLine()) != null)
			{
				currentLineTokens = new StringTokenizer(currentLine.trim(),Delimiters.delimDictionary);
				while(currentLineTokens.hasMoreElements())
				{
					String documentName = currentLineTokens.nextToken();
					String documentID = currentLineTokens.nextToken();
					String docLength = currentLineTokens.nextToken();
					String docEuc = currentLineTokens.nextToken();
					String docMaxFreq= currentLineTokens.nextToken();
					
					doc=new DocumentStats(Integer.parseInt(documentID),documentName,Integer.valueOf(docLength),Double.parseDouble(docEuc),Integer.valueOf(docMaxFreq));
					documentDictionary.put(Integer.parseInt(documentID),doc);
				}
			}
			br.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Get total number of terms from the "key" dictionary associated with this 
	 * index. A postings list is always created against the "key" dictionary
	 * @return The total number of terms
	 */
	public int getTotalKeyTerms() 
	{	
		//TODO : YOU MUST IMPLEMENT THIS
		return indexTypeDictionary.size();
		//return -1;
	}
	
	/**
	 * Get total number of terms from the "value" dictionary associated with this 
	 * index. A postings list is always created with the "value" dictionary
	 * @return The total number of terms
	 */
	public int getTotalValueTerms() 
	{
		//TODO: YOU MUST IMPLEMENT THIS
		return documentDictionary.size();
		//return -1;
	}
	
	/**
	 * Method to get the postings for a given term. You can assume that
	 * the raw string that is used to query would be passed through the same
	 * Analyzer as the original field would have been.
	 * @param term : The "analyzed" term to get postings for
	 * @return A Map containing the corresponding fileid as the key and the 
	 * number of occurrences as values if the given term was found, null otherwise.
	 */
	public Map<String, Integer> getPostings(String term)
	{
		//TODO:YOU MUST IMPLEMENT THIS
		if(indexTypeDictionary.containsKey(term))
		{
			HashMap<String,Integer> result = new HashMap<String,Integer>();
			int termID = indexTypeDictionary.get(term);
			ArrayList<Posting> indexPosting = postingsListMap.get(termID);
			for(Posting p : indexPosting)
			{
				String documentName = documentDictionary.get(p.docID).documentName;
				int termFrequency = p.termFrequency;
				result.put(documentName, termFrequency);
			}
			return result;
		}
		return null;
	}
	
	/**
	 * Method to get the top k terms from the index in terms of the total number
	 * of occurrences.
	 * @param k : The number of terms to fetch
	 * @return : An ordered list of results. Must be <=k fr valid k values
	 * null for invalid k values
	 */
	public List<String> getTopK(int k) 
	{
		//TODO YOU MUST IMPLEMENT THIS
		if(k < 1)
		{
			return null;
		}
		System.out.println("Get Top " + k);
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<String> indexTerms;
		for(Integer key : topIndexTerms.descendingKeySet())
		{
			indexTerms = topIndexTerms.get(key);
			for(String index : indexTerms)
			{
				result.add(index);
				if(result.size() == k){
					return result;
				}
			}
		}
		return result;
	}
	
	/**
	 * Method to implement a simple boolean AND query on the given index
	 * @param terms The ordered set of terms to AND, similar to getPostings()
	 * the terms would be passed through the necessary Analyzer.
	 * @return A Map (if all terms are found) containing FileId as the key 
	 * and number of occurrences as the value, the number of occurrences 
	 * would be the sum of occurrences for each participating term. return null
	 * if the given term list returns no results
	 * BONUS ONLY
	 */
	public Map<String, Integer> query(String...terms)
	{
		System.out.println("Query for " + terms.length);  
		
		/**
		 * Since doing AND operation on PostingsList 
		 * we need to start with postingList that is the smallest -- 
		 * DOC_FREQ is a value that could
		 * help us do this hence the following MAP
		 */
		TreeMap<Integer,ArrayList<String>> df_TermMap = new TreeMap<Integer,ArrayList<String>>();
		
		HashMap<String,Integer> result = new HashMap<String,Integer>(); // 
		ArrayList<Posting> intersectionList = new ArrayList<Posting>(); // List of docs which contain terms
		ArrayList<String> remainingTerms = new ArrayList<String>(); 	// hack  
		
		int first = 0;

		/**
		 * Search for all terms in dictionary
		 * 1. Get term ID
		 * 2. Add to df_TermMap
		 */
		for(String queryTerm : terms)
		{
			if(indexTypeDictionary.containsKey(queryTerm))
			{
				int termID = indexTypeDictionary.get(queryTerm);
				int documentFrequency = documentFrequencyMap.get(termID);
				System.out.println("Term Found " + queryTerm);
				addToDocumentFrequencyToTermMap(df_TermMap, documentFrequency, queryTerm);
			}
			else
			{
				System.out.println(queryTerm + " Not Found");
				return null;
			}
		}
		
		/*
		 * Get the smallest postings list -- Use Doc Freq
		 * For that -- many terms can have the smallest DOC_FREQ value
		 * choose the first 
		 */
		intersectionList = postingsListMap.get(indexTypeDictionary.get(df_TermMap.get(df_TermMap.firstKey()).get(first)));
		
		for(int documentFrequency : df_TermMap.keySet())
		{
			remainingTerms.addAll(df_TermMap.get(documentFrequency));
		}
		remainingTerms.remove(first); // we already have the postings list for the first one 
		
		
		
		while(!remainingTerms.isEmpty() && !intersectionList.isEmpty())
		{
			String word = remainingTerms.remove(first);
			int termID = indexTypeDictionary.get(word);
			ArrayList<Posting> list = postingsListMap.get(termID);
			intersectionList = intersection(intersectionList,list);
		}
		
		if(intersectionList.isEmpty())
		{
			System.out.println("No result");
			return null;
		}
		
		result = convertIntersectionListToMap(intersectionList); 	 		
		printIntersectionList(result);
		return result;
	}
	
	/**
	 * 
	 * @param result
	 */
	private void printIntersectionList(HashMap<String, Integer> result) 
	{
		for(String fileID : result.keySet())
		{
			System.out.println("File ID " + fileID + " Occurrences " + result.get(fileID));
		}
	}

	/**
	 * 
	 * @param intersectionList
	 * @return
	 */
	private HashMap<String, Integer> convertIntersectionListToMap(ArrayList<Posting> intersectionList) 
	{
		// TODO Auto-generated method stub
		HashMap<String,Integer> queryResult = new HashMap<String,Integer>();
		for(Posting posting : intersectionList)
		{
			int docID = posting.docID;
			String fileName = documentDictionary.get(docID).documentName;
			queryResult.put(fileName,posting.termFrequency);
		}
		return queryResult;
	}

	/**
	 * 
	 * @param intersectionList
	 * @param list
	 * @return
	 */
	private ArrayList<Posting> intersection(ArrayList<Posting> intersectionList, ArrayList<Posting> candidatelist) 
	{
		
		if(intersectionList.isEmpty() || candidatelist.isEmpty())
		{
			System.out.println("Empty List received");
			return null;
		}
		
		Posting result;
		ArrayList<Posting> intersectionResult = new ArrayList<Posting>();
		int indexIntersectionList = 0, indexList = 0;
		
		while(indexIntersectionList < intersectionList.size() && indexList < candidatelist.size())
		{
			if(intersectionList.get(indexIntersectionList).equals(candidatelist.get(indexList)))
			{
				result = addResult(intersectionList.get(indexIntersectionList),candidatelist.get(indexList));
				intersectionResult.add(result);
				indexIntersectionList++;
				indexList++;
			}
			else if((intersectionList.get(indexIntersectionList)).docID <(candidatelist.get(indexList)).docID)
			{
				indexIntersectionList++;
			}
			else
			{
				indexList++;
			}
		}
		return intersectionResult;
	}

	/**
	 * 
	 * @param posting1
	 * @param posting2
	 * @return
	 */
	private Posting addResult(Posting posting1, Posting posting2) 
	{
		// TODO Auto-generated method stub
		Posting result = new Posting(posting1.docID, posting1.termFrequency);
		result.termFrequency+=posting2.termFrequency;
		return result;
	}

	/**
	 * 
	 * @param documentFrequencyToTermMap
	 * @param documentFrequency
	 * @param queryTerm
	 */
	private void addToDocumentFrequencyToTermMap(TreeMap<Integer, ArrayList<String>> df_TermMap,
			int documentFrequency, String queryTerm) 
	{
		if(df_TermMap.containsKey(documentFrequency))
		{
			ArrayList<String> list = df_TermMap.get(documentFrequency);
			list.add(queryTerm);
		}
		else
		{
			ArrayList<String> list = new ArrayList<String>();
			list.add(queryTerm);
			df_TermMap.put(documentFrequency, list);
		}
	}
	
	/**
	 * Creates an index bundle and sends it to caller
	 * @return
	 */
	public IndexBundle getIndexBundle()
	{
		return new IndexBundle(indexTypeDictionary,postingsListMap,documentFrequencyMap);
	}
}