package edu.buffalo.cse.irf14;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Result;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AuthorAnalyzer;
import edu.buffalo.cse.irf14.analysis.CategoryAnalyzer;
import edu.buffalo.cse.irf14.analysis.ContentAnalyzer;
import edu.buffalo.cse.irf14.analysis.PlaceAnalyzer;
import edu.buffalo.cse.irf14.analysis.REGEXCONSTANTS;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.index.DocVec_Element;
import edu.buffalo.cse.irf14.index.DocVector;
import edu.buffalo.cse.irf14.index.DocumentStats;
import edu.buffalo.cse.irf14.index.IndexBundle;
import edu.buffalo.cse.irf14.index.IndexBundleType;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexSearcher;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.termStats;
import edu.buffalo.cse.irf14.query.DocInfo;
import edu.buffalo.cse.irf14.query.Query;
import edu.buffalo.cse.irf14.query.QueryParser;
import edu.buffalo.cse.irf14.query.QueryProcessor;
import edu.buffalo.cse.irf14.query.QueryResultSet;
import edu.buffalo.cse.irf14.query.queryOps;
import edu.buffalo.cse.irf14.query.resultElement;

/**
 * Main class to run the searcher.
 * As before implement all TODO methods unless marked for bonus
 * @author nikhillo
 *
 */

class QueryBlocks
{
	queryOps prevOps;
	queryOps nextOps;
	blockType type;
	IndexBundleType bundleType;
	String block;
	
	public String toString()
	{
		return block;
	}
}

enum blockType
{
	CLAUSE,
	PHRASE,
	BOOL_OPS,
	NORMAL,
}



public class SearchRunner 
{
	public enum ScoringModel {TFIDF, OKAPI};
	String indexDir="";
	String corpusDir="";
	char mode;
	PrintStream printer;
	double numDocs=0;
	double avgDocLenth=0;
	
	double weightForCat=0.8;
	double weightForAuthor=0.9;
	double weightForPlace=0.5;
	double weightForTerm=1;
	
	/**
	 * TERM, AUTHOR, CATEGORY, PLACE
	 */
	IndexBundle bundle_term,bundle_author,bundle_category,bundle_place;
	HashMap<Integer, DocumentStats> docDictonary;
	/**
	 * Default (and only public) constuctor
	 * @param indexDir : The directory where the index resides
	 * @param corpusDir : Directory where the (flattened) corpus resides
	 * @param mode : Mode, one of Q or E
	 * @param stream: Stream to write output to
	 */
	public SearchRunner(String indexDir, String corpusDir,char mode, PrintStream printer) 
	{
		this.indexDir =indexDir;
		this.corpusDir=corpusDir;
		this.mode = mode;
		this.printer=printer;
		System.out.println("Index Dir is"+indexDir);
		docDictonary = IndexReader.getDocDictionary(indexDir);
		numDocs = docDictonary.size();
		
		for(Integer a : docDictonary.keySet())
		{
			avgDocLenth=docDictonary.get(a).doclength;
		}
		avgDocLenth=avgDocLenth/numDocs;
		
		getBundles();
	}
	
	public String getDocName(int docID)
	{
		DocumentStats Doc = docDictonary.get(docID);
		return Doc.documentName;
	}
	
	public int NumDocs()
	{
		return docDictonary.size();
	}
	
	private void getBundles()
	{
		for(IndexType type : IndexType.values())
		{
			IndexReader reader = new IndexReader(indexDir, type);
			switch (type)
			{
				case TERM:
					bundle_term=reader.getIndexBundle();
					break;
				case AUTHOR:
					bundle_author=reader.getIndexBundle();
					break;
				case PLACE:
					bundle_place=reader.getIndexBundle();
					break;
				case CATEGORY:
					bundle_category=reader.getIndexBundle();
					break;
				default:
					break;
			}
		}
	}
	
	
	/**
	 * Method to execute given query in the Q mode
	 * @param userQuery : Query to be parsed and executed
	 * @param model : Scoring Model to use for ranking results
	 */
	public void query(String userQuery, ScoringModel model) 
	{
		// fetch query object 
		Query qObj = QueryParser.parse(userQuery,"OR");
		
		long start=0,end=0,totalTime=0; 

		start= System.currentTimeMillis();
		execQueryOutput result =executeQuery(qObj);
		end= System.currentTimeMillis();
		totalTime = end - start;
			
		ArrayList<DocVec_Element> result_docs =null;
		
		/**
		 *  get TOP K
		 */
		switch (model)
		{
			case TFIDF:
				result_docs=computeTFIDF(result.resultVector, result.qTF);
				break;
			case OKAPI:
				result_docs=computeOKAPI(result.resultVector);
				break;
			default:
				break;
		}
		
		/**
		 * The results out please
		 */
		printer.println("USER QUERY: "+qObj.userQuery);
		printer.println("QUERY TIME: "+totalTime);
		printer.println();
		for(int i=0;i<result_docs.size();i++)
		{
			resultElement n =DocInfo.fetchDocDetails(corpusDir,result_docs.get(i));
			if(n.relevanceScore > 1.0) n.relevanceScore=0.99;
			printer.println("RANK: "+(i+1)+"\t\t"+"SCORE "+n.relevanceScore);
			printer.println("");
			
			printer.println(n.title);
			printer.println("");
			printer.println(n.snippet);
			
			printer.println("\n");
		}
	}
	
	/**
	 * Method to execute queries in E mode
	 * @param queryFile : The file from which queries are to be read and executed
	 */
	public void query(File queryFile) 
	{
		/**
		 * 1. Get all queries from the file
		 * 2. Get results of all those queries
		 * 3. Write the results to a file 
		 */
		ArrayList<Query> querySet= 	getQuerySet(queryFile);
		Iterator<Query> it = querySet.iterator();
		ArrayList<QueryResultSet> resultSetBag = new ArrayList<QueryResultSet>();  
		
		QueryResultSet queryResult=null;
		
		execQueryOutput result=null;
		ArrayList<DocVec_Element> bag =null;
		
		// Get results of the query
		while(it.hasNext())
		{
			Query q = it.next();
		
			/**
			 * need information on query length and 
			 * result vector of all the operations
			 */
			ScoringModel model;
			result=executeQuery(q);
			if(result!=null)
			{
				// score the vector
				ArrayList<DocVec_Element> topResults = null;
				
				/**
				 * based on some metric figure out which model to use
				 */
				model=ScoringModel.TFIDF;
				
				if(result.resultVector == null)
				{
					continue;
				}
				
				
				switch(model)
				{
					case TFIDF:
						topResults=computeTFIDF(result.resultVector, result.qTF);
						break;
					case OKAPI:
						topResults=computeOKAPI(result.resultVector);
						break;
				}
				resultSetBag.add(new QueryResultSet(q.queryID,topResults));
			}
		}
		
		
		// print the results
		StringBuffer re= new StringBuffer();
		re.append("numResults="+resultSetBag.size());
		re.append("\n");
		
		// write to file
		for(int i=0;i<resultSetBag.size();i++)
		{
			re.append(resultSetBag.get(i).unroll());
		}
		printer.print(re.toString());
	}
	
	
	public ArrayList<Query> getQuerySet(File queryFile) 
	{
		BufferedReader br=null;
		Pattern qPattern = Pattern.compile("\\{.+\\}$");
		Matcher matcher;
		String usrQuery=null;
		Query query=null;
		ArrayList<Query> querySet = new ArrayList<Query>();
		try 
		{
			String sCurrentLine;
			br = new BufferedReader(new FileReader(queryFile));
			while ((sCurrentLine = br.readLine()) != null) 
			{
				//System.out.println(sCurrentLine);
				matcher = qPattern.matcher(sCurrentLine);
				if(matcher.find())
				{
					usrQuery= matcher.group();
					query=QueryParser.parse(usrQuery.substring(1,usrQuery.length()-1));
					query.queryID=sCurrentLine.substring(0,matcher.start()-1);
					querySet.add(query);
					
					//System.out.println(query.queryID+"::"+query);
				}
				else
				{
					// -- FIXME
					//System.out.println("This is a very bad query ::"+sCurrentLine);
				}
			}
		}
		catch (FileNotFoundException FEX)
		{
			FEX.printStackTrace();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (br != null)br.close();
			} 
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
		}
		return querySet;
	}
	
	
	// how to handle phrase queries
	private HashMap<String, queryTerm> getQTF(String qClear)
	{
		HashMap<String, queryTerm> a = new HashMap<String, queryTerm>();
		String toks[]=qClear.split(" ");
		String qT = null;
		for(int i =0;i<toks.length;i++)
		{
			
			String ab=toks[i];
			//System.out.println("PLEASE GOD"+ab);
			if(ab.matches("^<.+>$"))
			{
				ab=ab.substring(1,ab.length()-1);
			}
			if(ab.contains("\""))
			{
				qT = ab.split(":")[1].toLowerCase();
				//System.out.println("PLEASE GOD"+qT);
				if(ab.matches(".+\"$"))
				{
					qT=qT.substring(1,qT.length()-1);
				}
				else
				{
					int j =0;
					StringBuffer re = new StringBuffer();
					re.append(qT);
					re.append(" ");
					for(j=i+1;j<toks.length;j++)
					{
						re.append(toks[j]);
						if(toks[j].matches(".+\""))
						{
							break;
						}
						re.append(" ");
					}
					i=j;
					qT=re.toString();
					qT=qT.substring(1,qT.length()-1);
				}
				
				String facet=ab.split(":")[0];
				double weight=weightForTerm;
				if(facet.matches("(?i)(CATEGORY)"))
				{
					
					weight=weightForCat;
				}
				else if(facet.matches("(?i)(AUTHOR)"))
				{
					
					weight=weightForAuthor;
				}
				else if(facet.matches("(?i)(PLACE)"))
				{
					
					weight=weightForPlace;
				}
				
				if(a.containsKey(qT))
				{
					a.get(qT).count+=weight;
				}
				else
				{
					
					a.put(qT,new queryTerm(weight));
				}
			}
			else if(!ab.matches(REGEXCONSTANTS.REGEX_IS_BOOL) && !ab.matches("\\[|\\]"))
			{
				//System.out.println(ab);
				qT = ab.split(":")[1].toLowerCase();

				String facet=ab.split(":")[0];
				double weight=weightForTerm;
				if(facet.matches("(?i)(CATEGORY)"))
				{
					
					weight=weightForCat;
				}
				else if(facet.matches("(?i)(AUTHOR)"))
				{
					
					weight=weightForAuthor;
				}
				else if(facet.matches("(?i)(PLACE)"))
				{
					
					weight=weightForPlace;
				}
				
				if(a.containsKey(qT))
				{
					a.get(qT).count+=weight;
				}
				else
				{
					
					a.put(qT,new queryTerm(weight));
				}				//System.out.println("QT "+qT);
			}
		}
		return a;
	}
	
	
	//private QueryResultSet executeQuery(Query qObj)
	private execQueryOutput executeQuery(Query qObj)
	{
		System.out.println("The Query::"+qObj);
		// get the weight of queries -- find the qTF
		HashMap<String,queryTerm> qTFs=null;
		String clearQ=qObj.representation.substring(1,qObj.representation.length()-1).trim();
		
		/*
		 * get qTF
		 */
		//if(true) return null;
		qTFs=getQTF(clearQ);
		
		/*for(String a:qTFs.keySet())
		{
			//System.out.println("qTF: "+a);
		}*/
		
		ArrayList<QueryBlocks> chops = queryChop(clearQ);
		
		/*for(QueryBlocks q : chops)
		{
			System.out.println("QBLOCK "+q+" type:"+q.type);
		}
		*/

		int topK=10;
		
		
		//if(true) return null;
		QueryBlocks qBlock=null;
		DocVector resultVector=null;
		for(int i=0;i<chops.size();i++)
		{
			qBlock=chops.get(i);
			// part1 BOOL part2 BOOL part3
			// need to perform Intersection / Union / Negation
			if(qBlock.type == blockType.BOOL_OPS)
			{
				/**
				 * get the doc vector for part 2 and do the ops
				 */
				QueryBlocks qBlockRight = chops.get(i+1);
				// need to check if this term has a <> in it
				String searchTerm=qBlockRight.block;
				if(searchTerm.matches("^<.+>$"))
				{	
					qBlock.block="NOT";
					searchTerm=searchTerm.substring(1,searchTerm.length()-1);
				}
				
				i++;
				DocVector vectorRight=null;
				if(qBlockRight.type == blockType.CLAUSE)
				{
					vectorRight=executeClauseQuery(searchTerm,qTFs);
					
				}
				else if(qBlockRight.type == blockType.PHRASE)
				{	
					vectorRight=executePhraseQuery(searchTerm);
					
				}
				else // simple item could be <Term:something>
				{
					vectorRight=searchInBundle(searchTerm,qTFs);
				}
				
				if(vectorRight == null)
				{
					System.out.println("NO RESULTS for term "+searchTerm);
				}
				else if(resultVector == null)
				{
					System.out.println("Previous term is mt");
					resultVector=vectorRight;
				}
				else
				{
					vectorRight.printVector();
					resultVector=IndexSearcher.DoOperation(resultVector,vectorRight,qBlock.block);
				}
				
				//System.out.println("---- VECTOR FOR ----"+searchTerm);
			}
			else // probably the first amongst query blocks
			{
				String searchTerm=qBlock.block;
				if(searchTerm.matches("^<.+>$"))
				{	
					qBlock.block="NOT";
					searchTerm=searchTerm.substring(1,searchTerm.length()-1);
				}
				
				System.out.println("Looking at search term "+searchTerm);
				
				if(qBlock.type == blockType.CLAUSE)
				{
					resultVector=executeClauseQuery(searchTerm,qTFs);
				}
				else if(qBlock.type == blockType.PHRASE)
				{	
					resultVector=executePhraseQuery(searchTerm);
				}
				else // simple item could be <Term:something>
				{
					
					resultVector=searchInBundle(searchTerm,qTFs);;
				}
				
				//System.out.println("---- VECTOR FOR ----"+searchTerm);
				//resultVector=searchInBundle(qBlock.block);
			}
			//resultVector.printVector();
		}
		/**
		 * pack the out put and send it across
		 */
		return new execQueryOutput(resultVector, qTFs);
	}
	

	private String createPhraseModified(String clearQ) 
	{
		QueryProcessor qP = new QueryProcessor();
		ArrayList<String> queryTokensList = qP.splitTokens(clearQ);
		StringBuilder newSearchPhrase = new StringBuilder();
		for(String s : queryTokensList)
		{
			System.out.println("Modifying " + s);
			if(s.contains(":"))
			{
				String[] queryTokens = s.split(":");
				String type = queryTokens[0];
				String searchPhrase = queryTokens[1];
				searchPhrase = searchPhrase.substring(1,searchPhrase.length() - 1);
				String finalSearchQuery = addTypeToPhrase(searchPhrase,type);
				System.out.println(finalSearchQuery);
				newSearchPhrase.append(finalSearchQuery);
			}
			else
			{
				System.out.println("OP " + s);
				newSearchPhrase.append(s);
				newSearchPhrase.append(" ");
			}
		}
		System.out.println(clearQ + " Modified Query " + newSearchPhrase.toString());
		return newSearchPhrase.toString();
	}
	
	private DocVector executePhraseQuery(String phraseQuery)
	{
		
		DocVector result = null;
		HashMap<String, queryTerm> resultMap = new HashMap<String,queryTerm>();
		
		
		System.out.println("Phrase Query " + phraseQuery);
		
		String[] queryTokens = phraseQuery.split(":");
		String type = queryTokens[0];
		String searchPhrase = queryTokens[1];
		
		searchPhrase = searchPhrase.substring(1,searchPhrase.length() - 1);
		String finalSearchQuery = addTypeToPhrase(searchPhrase,type);
		System.out.println("Final " + finalSearchQuery);
		String[] finalSearchQueryTokens = finalSearchQuery.split(" ");
		resultMap = getQTF(finalSearchQuery);
		
		ArrayList<DocVector> candidateLists = new ArrayList<DocVector>();
		
		for(int i = 0; i < finalSearchQueryTokens.length ;i++)
		{
			DocVector docVector = searchInBundle(finalSearchQueryTokens[i],resultMap);
			
			if(docVector == null)
			{
				continue;
			}
			else
			{
				candidateLists.add(docVector);
			}
				
		}
		
		
		if(candidateLists.size() == 0)
		{
			return null;
		}
		
		DocVector[] arrayOfVectors = new DocVector[candidateLists.size()]; 
		arrayOfVectors = candidateLists.toArray(arrayOfVectors);
		
		result = IndexSearcher.Intersection(arrayOfVectors);
		return result;
	}
	
	private String addTypeToPhrase(String qClear , String type) 
	{
		// TODO Auto-generated method stub
		StringBuilder phraseTokens = new StringBuilder();
		String[] tokens = qClear.split(" ");
		for(int i = 0 ; i < tokens.length ; i++){
			if(tokens[i].contains("\"")){
				tokens[i] = tokens[i].replace("\"", "");
			}
			phraseTokens.append(type);
			phraseTokens.append(":");
			phraseTokens.append(tokens[i]);
			phraseTokens.append(" ");
		}
		return phraseTokens.toString();
	}
	
	private DocVector executeClauseQuery(String query,HashMap<String, queryTerm> qTFs)
	{
		
		// check if the clause given is nested or not
		int nestedCount = query.length() - query.replaceAll("\\[", "").length();
		DocVector resultVector = null;
		
		if(nestedCount > 1)
		{
			System.out.println("THIS IS A NESTED QUERY----------------------");
			QueryProcessor queryProcessor = new QueryProcessor();
			Stack<DocVector> docVectorStack = new Stack<DocVector>();
			try
			{	
				
				String postfixExpression = queryProcessor.processNested(query);
				ArrayList<String> postfixList = queryProcessor.splitTokens(postfixExpression);
				for(int i = 0 ; i < postfixList.size(); i++)
				{
					if(postfixList.get(i).contains("<"))
					{
						postfixList.set(i + 1, "NOT");
						String term = postfixList.get(i);
						term = new String(term.substring(1, term.length() - 1));
						postfixList.set(i, term);
					}
				}
				for(String searchTerm : postfixList)
				{
					if(searchTerm.equals("AND") || searchTerm.equals("OR") || searchTerm.equals("NOT"))
					{
						DocVector docVectorTerm1 = docVectorStack.pop();
						DocVector docVectorTerm2 = docVectorStack.pop();
						DocVector resultDocVector = IndexSearcher.DoOperation(docVectorTerm2,docVectorTerm1,searchTerm);
						docVectorStack.push(resultDocVector);
					}
					else
					{
						DocVector docVectorForTerm = searchInBundle(searchTerm ,qTFs);
						docVectorStack.push(docVectorForTerm);
					}
				}
				
			}
			catch(ParseException wrongToken)
			{
				
			}
			resultVector = docVectorStack.pop();
		}
		else
		{
			String clearQ=query.substring(1,query.length()-1).trim();
			ArrayList<QueryBlocks> chops = queryChop(clearQ);
			QueryBlocks qBlock=null;
			for(int i=0;i<chops.size();i++)
			{
				qBlock=chops.get(i);
				
				// part1 BOOL part2 BOOL part3
				// need to perform Intersection / Union / Negation
				if(qBlock.type == blockType.BOOL_OPS)
				{
					/**
					 * get the doc vector for part 2 and do the ops
					 */
					QueryBlocks qBlockRight = chops.get(i+1);
					// need to check if this term has a <> in it
					String searchTerm=qBlockRight.block;
					if(searchTerm.matches("^<.+>$"))
					{	
						qBlock.block="NOT";
						searchTerm=searchTerm.substring(1,searchTerm.length()-1);
					}
					
					i++;
					DocVector vectorRight=null;
					if(qBlockRight.type == blockType.PHRASE)
					{	
						vectorRight=executePhraseQuery(searchTerm);
						vectorRight.printVector();
					}
					else // simple item could be <Term:something>
					{
						vectorRight=searchInBundle(searchTerm,qTFs);
					}
					
					if(vectorRight == null)
					{
						System.out.println("NO RESULTS for term "+searchTerm);
					}
					else if(resultVector == null)
					{
						System.out.println("Previous term is mt");
					}
					else
					{
						resultVector=IndexSearcher.DoOperation(resultVector,vectorRight,qBlock.block);
					}
				}
				else // probably the first amongst query blocks
				{
					String searchTerm=qBlock.block;
					if(searchTerm.matches("^<.+>$"))
					{	
						qBlock.block="NOT";
						searchTerm=searchTerm.substring(1,searchTerm.length()-1);
					}
					if(qBlock.type == blockType.CLAUSE)
					{
						resultVector=executeClauseQuery(searchTerm,qTFs);
					}
					else if(qBlock.type == blockType.PHRASE)
					{	
						resultVector=executePhraseQuery(searchTerm);
					}
					else // simple item could be <Term:something>
					{
						resultVector=searchInBundle(searchTerm,qTFs);
					}
				}
			}
		}
		return resultVector;
	}
	
	private DocVector searchInBundle(String queryTerm,HashMap<String,queryTerm> qTFs)
	{
/*		for(String a: qTFs.keySet())
		{
			System.out.println(a+" "+qTFs.get(a));
		}*/
		
		DocVector v = null;
		String[] toks=queryTerm.split(":");
		/*System.out.println("Toks 1"+toks[0]);
		System.out.println("Toks 2"+toks[1]);
		*/
		double qTF=0;
		queryTerm q =qTFs.get(toks[1].toLowerCase());
		if(q == null)
		{
			qTF=0;
			System.out.println("is a facet");
		}
		else if(q.haveQueried)
		{
			return null;
		}
		else
		{
			q.haveQueried=true;
			qTF=q.count;
		}
		IndexBundleType type =IndexBundleType.toIndexBundleType(toks[0].toUpperCase());
		String searchThis = getAnalyzedTerm(toks[1],type);
		if(searchThis == null) 
		{
			// a stop word therefore should not figure in query as well
			qTFs.remove(toks[1].toLowerCase());
			return null;
		}
			searchThis=searchThis.toLowerCase();
		System.out.println("Searching for "+searchThis+" in "+type);
		
		switch(type)
		{
			case TERM:
				v=bundle_term.search(searchThis,qTF);
				break;
			case AUTHOR:
				v=bundle_author.search(searchThis,qTF);
				break;
			case CATEGORY:
				v=bundle_category.search(searchThis,qTF);
				break;
			case PLACE:
				v=bundle_place.search(searchThis,qTF);
				break;
			case NONE:
				System.out.println("SORRY "+toks[0]+" index does not exist");
				break;
		}
		if(v != null) 
		{
			v.type = IndexBundleType.toIndexBundleType(toks[0].toUpperCase());
			v.printVector();
		}
		return v;
	}
	
	
	private static String getAnalyzedTerm(String string,IndexBundleType type)
	{
		Tokenizer tknizer = new Tokenizer();
		Analyzer analyzer = null;
		TokenStream stream = null;
		try
		{
			stream = tknizer.consume(string);
			switch(type)
			{
				case AUTHOR:
					analyzer = new AuthorAnalyzer(stream);
					break ;
				case CATEGORY:
					analyzer= new CategoryAnalyzer(stream);
					break ;
				case PLACE:
					analyzer= new PlaceAnalyzer(stream);
					break ;
				case TERM:
					analyzer= new ContentAnalyzer(stream);
					break;
				case NONE:
					System.out.println("TYPE IS NONE ----!!");
					break ;
				default:
					System.out.println("BAD TYPE ----!!");
					break;
			}
			while (analyzer.increment()) 
			{
				
			}
			stream.reset();
			if(stream.size() > 1)
			{
				System.out.println("THIS IS NOT POSSIBLE!!!! MANY TOKENS");
			}
		}
		catch (TokenizerException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(stream.hasNext())
		{
			return stream.next().toString();
		}
		else
			return null;
	}
	//use recursion
	private DocVector queryClause(String subQ)
	{
		//System.out.println("The Clause to handle"+subQ);
		Matcher mat= Pattern.compile("\\[.+\\]").matcher(subQ);
		
		while(mat.find())
		{
			//System.out.println("Mat group"+mat.group());
		}/*
		DocVector d=null;
		String[] toks=subQ.substring(1,subQ.length()-1).trim().split(" ");*/
		return null;
	}
	
	static ArrayList<QueryBlocks> queryChop(String qClear)
	{
		ArrayList<QueryBlocks> qChunks = new ArrayList<QueryBlocks>();
		
		String clauseBegin="[";
		
		/**
		 * Makes query blocks given a query
		 */
		String[] toks=qClear.split(" ");
		
		/**
		 * Supposed to do the following 
		 * 1. Maintain the sanctity of the phrase queries
		 * 2. Maintain the clauses as a unit
		 * 3. of course the rest of it has to be kept as units as well
		 * 4. Identification of facets
		 * 5. Also capture Ops
		 */
		for(int i=0;i<toks.length;i++)
		{
			QueryBlocks qBlock= new QueryBlocks();
			/* CLAUSES */
			if(toks[i].equals(clauseBegin))
			{
				d D =getBraks(toks,i);
				i=D.i;
				qBlock.type=blockType.CLAUSE;
				qBlock.block=D.clause;
				/*if(D.clause.matches(REGEXCONSTANTS.REGEX_IS_INDECES))
				{
					qBlock.type=blockType.FACET_CLAUSE;
				}*/
				qChunks.add(qBlock);
			}
			/* PHRASE QUERIES AS A BLOCK */
			else if(toks[i].contains("\""))
			{
				qBlock.type=blockType.PHRASE;
				StringBuffer re= new StringBuffer();
				re.append(toks[i]);
				
				if(toks[i].matches(".+\"$"))
				{
					qBlock.block=re.toString();
					qChunks.add(qBlock);
				}
				else
				{
					System.out.println("I cam nbere");
					re.append(" ");
					i++;
					int j=0;
					for(j=i;j<toks.length;j++)
					{
						if(toks[j].matches("^.+\"$"))
						{
							re.append(toks[j]);
							re.append(" ");
							break;
						}
						re.append(toks[j]);
						re.append(" ");
					}
					i=j;
					qBlock.block=re.toString();
					qChunks.add(qBlock);
				}
			}
			/** FACETS **/
			/*else if(toks[i].matches(REGEXCONSTANTS.REGEX_IS_INDECES))
			{
				qBlock.type=blockType.FACET;
				qBlock.block=toks[i];
				qChunks.add(qBlock);
			}*/
			/* BOOLS */
			else if(toks[i].matches(REGEXCONSTANTS.REGEX_IS_BOOL))
			{
				qBlock.type=blockType.BOOL_OPS;
				qBlock.block=toks[i];
				qChunks.add(qBlock);
			}
			else // gen term
			{
				qBlock.type=blockType.NORMAL;
				qBlock.block=toks[i];
				qChunks.add(qBlock);
			}
			//System.out.println("Bundle Type "+toks[0].toUpperCase());
			if(i >= toks.length)
			{
				System.out.println("BIG PROBEMS");
			}
			qBlock.bundleType=IndexBundleType.toIndexBundleType(toks[i].split(":")[0].toUpperCase());
		}
		return qChunks;		
	}
	
	
	public static d getBraks(String[] toks,int index)
	{
		d D= new d();
		StringBuffer subQ= new StringBuffer(toks[index]);
		subQ.append(" ");
		
		int numOpen=1;
		int numclose=0;
		int i;
		for(i=index+1;i<toks.length;i++)
		{
			subQ.append(toks[i]);
			subQ.append(" ");
			if(toks[i].equals("]"))
			{
				numclose++;
				if(numOpen==numclose)
				{
					break;
				}
			}
			else if(toks[i].equals("["))
			{
				numOpen++;
			}
		}
		D.clause=subQ.toString().trim();
		D.i=i;
		return D;
	}
	
	void printA(DocVector a)
	{
		for(int i=0;i<a.size();i++)
		{
			DocVec_Element n = a.get(i);
			System.out.println("DOC "+n.docID);
			System.out.println(n.getTermStats());
		}
	}
	
	/**
	 * 
	 * @param vector
	 * @param qTFs
	 * @return
	 */
	private ArrayList<DocVec_Element> computeTFIDF(DocVector vector,HashMap<String,queryTerm> qTFs)
	{
		/**
		 * input resultant document vector
		 * Implementing TF-IDF
		 */
		ArrayList<DocVec_Element> n = vector.vector;
		
		double eucQuery=0.0f;
		// single term query TF-IDF does not help
		for(String j : qTFs.keySet())
		{
			//System.out.println(qTFs.get(j).count);
			eucQuery+=Math.pow(qTFs.get(j).count,2);
		}
		
		eucQuery=Math.sqrt(eucQuery);
		double smoothingVal=0.4;
		for(DocVec_Element a:n)
		{	
			double wTQ = 0.0f;
			double wTD = 0.0f;
			DocumentStats docStats= docDictonary.get(a.docID);
			double docEuc =docStats.docEuc;
			double maxTF=docStats.maxTermFreq;
			for(termStats b : a.statsList)
			{
				//wTD = 1+Math.log(b.tf); // normalize TF
				//if(b.tf !=0)
				{
					wTD = 1+Math.log(b.tf);
					//wTD=smoothingVal+((1-smoothingVal)*(b.tf/maxTF));
					if(wTD <0)
					{
						wTD=0;
					}
					wTQ = b.qtf*Math.log(numDocs/b.df);
					//System.out.println("df"+b.df);
					a.relevanceScore+= (wTD*wTQ);
				}
			}
			a.relevanceScore=a.relevanceScore/(docEuc*eucQuery);
		}
		ArrayList<DocVec_Element> bag =sortResult(vector);
		return bag;
	}
	
	/**
	 * 
	 * @param vector
	 */
	private ArrayList<DocVec_Element> computeOKAPI(DocVector vector)
	{
		/**
		 * Values of K1, b, and K3
		 */
		double k1=2;
		double 	k3=2;	
		double b_factor=0.75;//0.75;
		
		// for each element in the doc vector
		for(DocVec_Element doc : vector.vector)
		{
			double docLength=docDictonary.get(doc.docID).doclength;
			for(termStats b : doc.statsList)
			{
				double term_okapi=Math.log(numDocs/b.df);
				double document_okapi=((k1+1)*b.tf)/(k1*((1-b_factor)+b_factor*(docLength/avgDocLenth))+(double)b.tf);
				double query_okapi=((k3+1)*b.qtf)/(k3+b.qtf);
				doc.relevanceScore+=term_okapi*document_okapi*query_okapi;
			}
		}
		ArrayList<DocVec_Element> bag =sortResult(vector);
		return bag;
	}
	
	
	private ArrayList<DocVec_Element> sortResult(DocVector vector)
	{
		// sort the results
		TreeMap<Double,ArrayList<DocVec_Element>> a = new TreeMap<Double, ArrayList<DocVec_Element>>(Collections.reverseOrder());
		ArrayList<DocVec_Element> list = vector.vector;
		for(DocVec_Element b : list)
		{
			if(a.containsKey(b.relevanceScore))
			{
				a.get(b.relevanceScore).add(b);
			}
			else
			{
				ArrayList<DocVec_Element> c = new ArrayList<DocVec_Element>();
				c.add(b);
				a.put(b.relevanceScore,c);
			}
		}
		
		int i =0;
		ArrayList<DocVec_Element> bag=new ArrayList<DocVec_Element>();
		// get top 10
		for(Double rel : a.keySet())
		{
			ArrayList<DocVec_Element> take = a.get(rel);
			
			for(DocVec_Element candidate : take)
			{
				if(i>10)
					break;
				
				bag.add(candidate);
				
				i++;
			}
		}
		
		/*for(DocVec_Element chosen : bag)
		{
			System.out.println(chosen.docName+"#"+chosen.docID+"::"+chosen.relevanceScore);
		}*/
		return bag;
	}
	
	
	
	/**
	 * General cleanup method
	 */
	public void close() 
	{
		printer.flush();
		printer.close();
	}
	
	/**
	 * Method to indicate if wildcard queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean wildcardSupported() 
	{
		//TODO: CHANGE THIS TO TRUE ONLY IF WILDCARD BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get substituted query terms for a given term with wildcards
	 * @return A Map containing the original query term as key and list of
	 * possible expansions as values if exist, null otherwise
	 */
	public Map<String, List<String>> getQueryTerms() 
	{
		//TODO:IMPLEMENT THIS METHOD IFF WILDCARD BONUS ATTEMPTED
		return null;
		
	}
	
	/**
	 * Method to indicate if speel correct queries are supported
	 * @return true if supported, false otherwise
	 */
	public static boolean spellCorrectSupported() 
	{
		//TODO: CHANGE THIS TO TRUE ONLY IF SPELLCHECK BONUS ATTEMPTED
		return false;
	}
	
	/**
	 * Method to get ordered "full query" substitutions for a given misspelt query
	 * @return : Ordered list of full corrections (null if none present) for the given query
	 */
	public List<String> getCorrections() 
	{
		//TODO: IMPLEMENT THIS METHOD IFF SPELLCHECK EXECUTED
		return null;
	}
}

class j
{
	//String 
}

class d
{
	String clause;
	int i;
}

class queryTerm
{
	double count=0.0;
	boolean haveQueried=false;
	public queryTerm(double qTF) 
	{
		count=qTF;
	}
}

class execQueryOutput
{
	DocVector resultVector;
	HashMap<String, queryTerm> qTF;
	public execQueryOutput(DocVector resultVector,HashMap<String,queryTerm> qTF)
	{
		this.resultVector=resultVector;
		this.qTF=qTF;
	}
}