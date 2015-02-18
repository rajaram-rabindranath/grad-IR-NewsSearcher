/**
 * 
 */
package edu.buffalo.cse.irf14;

import java.io.File;
import java.util.ArrayList;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenFilter;
import edu.buffalo.cse.irf14.analysis.TokenFilterType;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Parser;
import edu.buffalo.cse.irf14.document.ParserException;
import edu.buffalo.cse.irf14.index.IndexReader;
import edu.buffalo.cse.irf14.index.IndexType;
import edu.buffalo.cse.irf14.index.IndexWriter;
import edu.buffalo.cse.irf14.index.IndexerException;
import edu.buffalo.cse.irf14.query.QueryParser;

/**
 * @author nikhillo
 *
 */
public class Runner 
{
	/**
	 * 
	 */
	public Runner() 
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * 1. Input directory -- Corpus
	 * 2. Output directory -- index
	 */
	public static void main(String[] args) 
	{
		String ipDir = args[0];
		String indexDir = args[1];
		//more? idk!
		boolean dbg = true;
		if(dbg)
		{
			System.out.println(ipDir);
			System.out.println(indexDir);
		}
		
		long startTime = System.currentTimeMillis();
		
		
		// category directories
		File ipDirectory = new File(ipDir);
		String[] catDirectories = ipDirectory.list();
		
		String[] files;
		File dir;
		Document d = null;
		IndexWriter writer = new IndexWriter(indexDir);
		
		// test both modes -- Eval mode & Query mode
		
		try 
		{			
			for (String cat : catDirectories) 
			{
				dir = new File(ipDir+ File.separator+ cat);
				files = dir.list();
				
				if (files == null)
					continue;
				
				for (String f : files)
				{
					try
					{
						d = Parser.parse(dir.getAbsolutePath() + File.separator +f);
						writer.addDocument(d);
					} 
					catch (ParserException e) 
					{
						System.out.println("PARSE EXCEPTION");
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
			System.out.println("total"+Parser.total);
			System.out.println("author count"+Parser.withAuthor);
			writer.close();
			System.out.println("Total time::"+(System.currentTimeMillis() - startTime));
			callReader(indexDir);
		}
		catch (IndexerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void callReader(String indexDir) 
	{
		// TODO Auto-generated method stub
		IndexReader termReader = new IndexReader(indexDir,IndexType.TERM);
		IndexReader authorReader = new IndexReader(indexDir,IndexType.AUTHOR);
		IndexReader placeReader = new IndexReader(indexDir,IndexType.PLACE);
		IndexReader categoryReader = new IndexReader(indexDir,IndexType.CATEGORY);
		
		System.out.println("Total Key Terms = " + termReader.getTotalKeyTerms());
		System.out.println("Total Value Terms = " + termReader.getTotalValueTerms());
		System.out.println("Total Key Author = " + authorReader.getTotalKeyTerms());
		System.out.println("Total Value Author = " + authorReader.getTotalValueTerms());
		System.out.println("Total Key Place = " + placeReader.getTotalKeyTerms());
		System.out.println("Total Value Place = " + placeReader.getTotalValueTerms());
		System.out.println("Total Key Category = " + categoryReader.getTotalKeyTerms());
		System.out.println("Total Value Category = " + categoryReader.getTotalValueTerms());
		System.out.println();
		System.out.println("Top 10 terms");
		ArrayList<String> topTerms = (ArrayList<String>)termReader.getTopK(10);
		for(String term: topTerms){
			System.out.println(term);
		}
		
		System.out.println();
		System.out.println("Top 10 author");
		ArrayList<String> authorTerms = (ArrayList<String>)authorReader.getTopK(10);
		for(String author: authorTerms){
			System.out.println(author);
		}
		System.out.println();
		System.out.println("Top 10 places");
		ArrayList<String> placeTerms = (ArrayList<String>)placeReader.getTopK(10);
		for(String place: placeTerms){
			System.out.println(place);
		}
		System.out.println();
		System.out.println("Top 10 category");
		ArrayList<String> categoryTerms = (ArrayList<String>)categoryReader.getTopK(10);
		for(String category: categoryTerms){
			System.out.println(category);
		}
		
	/*	System.out.println();
		String[] queryTerms = {"aluminium","plant","alloy","china"};
		for(int i = 0; i < queryTerms.length; i++)
		{
			queryTerms[i] = getAnalyzedTerm(queryTerms[i],FieldNames.CONTENT);
		}
		termReader.query(queryTerms);*/
		
		/*./trade/0009594
		./money-fx/0009594
		./cpi/0009594
		./dlr/0009594
		./money-supply/0009594
		*/
		System.out.println("");
		String query[]={"trade","money-fx","cpi","dlr","money-supply"};
		for(int i = 0; i < query.length; i++)
		{
			query[i] = getAnalyzedTerm(query[i],FieldNames.CATEGORY);
		}
		categoryReader.query(query);
		
		String query_[]={"central"};
		for(int i = 0; i < query_.length; i++)
		{
			query_[i] = getAnalyzedTerm(query_[i],FieldNames.CONTENT);
		}
		termReader.query(query_);
	}
	
	private static String getAnalyzedTerm(String string,FieldNames type)
	{
		Tokenizer tknizer = new Tokenizer();
		AnalyzerFactory fact = AnalyzerFactory.getInstance();
		try 
		{
			TokenStream stream = tknizer.consume(string);
			Analyzer analyzer = fact.getAnalyzerForField(type, stream);
			
			while (analyzer.increment()) 
			{
				
			}
			
			stream.reset();
			return stream.next().toString();
		} catch (TokenizerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
