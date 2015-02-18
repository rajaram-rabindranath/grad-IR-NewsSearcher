/**
 * 
 */
package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.buffalo.cse.irf14.analysis.Analyzer;
import edu.buffalo.cse.irf14.analysis.AnalyzerFactory;
import edu.buffalo.cse.irf14.analysis.TokenStream;
import edu.buffalo.cse.irf14.analysis.Tokenizer;
import edu.buffalo.cse.irf14.analysis.TokenizerException;
import edu.buffalo.cse.irf14.document.Document;
import edu.buffalo.cse.irf14.document.FieldNames;
import java.math.*;

/**
 * @author nikhillo
 * Class responsible for writing indexes to disk
 */
public class IndexWriter
{
	String indexDir;
	
	// index & dictionary files for persistent store
	File fileTermIndex,fileAuthorIndex, fileCategoryIndex, filePlaceIndex;
    File fileTermDictionary,fileAuthorDictionary,fileCategoryDictionary,filePlaceDictionary,fileDocumentDictionary,fileMasterDictionary;
      
    // The indices and dictionaries
    private static ArrayList<String> documentDictionary;
    private static HashMap<String,Integer> masterDictionary;
    
    static IndexConstructor termIndex_;
    static IndexConstructor categoryIndex_;
    static IndexConstructor authorIndex_;
    static IndexConstructor placeIndex_;

    /** Just keeps a track of the document added **/
    HashMap<String,Integer> documentCatalog = new HashMap<String, Integer>();
    
    ArrayList<Integer> DocLength=new ArrayList<Integer>();
    ArrayList<Double> DocEuclidean=new ArrayList<Double>();
    ArrayList<Integer> DocMaxFreq=new ArrayList<Integer>();
    
    private static int docID;
    
    /**
	 * Default constructor
	 * @param indexDir : The root directory to be used for indexing
	 */
	public IndexWriter(String indexDir)
	{
		this.indexDir = indexDir;
		createIndexFiles();
	    
		/** create index processors and provide them with file to write to **/
		termIndex_=new IndexConstructor(IndexType.TERM,fileTermIndex,fileTermDictionary);
	    categoryIndex_=new IndexConstructor(IndexType.CATEGORY,fileCategoryIndex, fileCategoryDictionary);
	    authorIndex_=new IndexConstructor(IndexType.AUTHOR,fileAuthorIndex,fileAuthorDictionary);
	    placeIndex_=new IndexConstructor(IndexType.PLACE,filePlaceIndex,filePlaceDictionary);
	    
	    
	    documentDictionary=new ArrayList<String>();
	    docID=1;
	} 
	
	private void createIndexFiles()
	{
		File[] indexNdictionaryFiles=new File[10];
		/* --- create index files for storage ---*/
		fileTermIndex=new File(indexDir+ File.separator+ FileNames.fname_termIndex);
		fileAuthorIndex=new File(indexDir+ File.separator+ FileNames.fname_authorIndex);
		filePlaceIndex=new File(indexDir+ File.separator+ FileNames.fname_placeIndex);
		fileCategoryIndex=new File(indexDir+ File.separator+ FileNames.fname_categoryIndex);
		/* --- create dictionary files for storage ---*/
		fileTermDictionary=new File(indexDir+ File.separator+ FileNames.fname_termDictionary);
		fileAuthorDictionary=new File(indexDir+ File.separator+ FileNames.fname_authorDictionary);
		filePlaceDictionary=new File(indexDir+ File.separator+ FileNames.fname_placeDictionary);
		fileCategoryDictionary=new File(indexDir+ File.separator+ FileNames.fname_categoryDictionary);
		
		/* The Document dictionary and master dictionary */
		fileDocumentDictionary=new File(indexDir+ File.separator+ FileNames.fname_documentDictionary);
		fileMasterDictionary=new File(indexDir+ File.separator+ FileNames.fname_masterDictionary);
		
		indexNdictionaryFiles[0]=fileTermIndex;indexNdictionaryFiles[1]= fileCategoryIndex;
		indexNdictionaryFiles[2]=fileAuthorIndex;indexNdictionaryFiles[3]=filePlaceIndex;
		indexNdictionaryFiles[4]=fileAuthorDictionary;indexNdictionaryFiles[5]=fileTermDictionary;
		indexNdictionaryFiles[6]=filePlaceDictionary;indexNdictionaryFiles[7]=fileCategoryDictionary;
		indexNdictionaryFiles[8]=fileDocumentDictionary;indexNdictionaryFiles[9]=fileMasterDictionary;
		
		try
		{
			for(File f : indexNdictionaryFiles)
			{
				if(f.createNewFile());
				else
				{
					f.delete(); f.createNewFile();
				}
			}
		}
		catch (IOException e) 
		{
		      e.printStackTrace();
		}
	}
	
	/**
	 * Method to add the given Document to the index
	 * This method should take care of reading the filed values, passing
	 * them through corresponding analyzers and then indexing the results
	 * for each indexable field within the document. 
	 * @param d : The Document to be added
	 * @throws IndexerException : In case any error occurs
	 */
	public void addDocument(Document d) throws IndexerException
	{
		/**
		 * For all elements of the document
		 * filter and pass the data to index 
		 */
		String[] data;
		Tokenizer tkizer=new Tokenizer();
		AnalyzerFactory analyzerFactory=AnalyzerFactory.getInstance();
		Analyzer dataAnalyzer=null;
		TokenStream dataStream=null;
		String FileName = d.getField(FieldNames.FILEID)[0];
		
		/** Have we parsed this document already **/
		if(documentDictionary.contains(FileName))
		{
			int index=documentDictionary.indexOf(FileName);
			data=d.getField(FieldNames.CATEGORY);
			if(data != null)
			{
				try
				{
					dataStream=tkizer.consume(data[0]);
					dataAnalyzer=analyzerFactory.getAnalyzerForField(FieldNames.CATEGORY,dataStream);
					while(dataAnalyzer.increment());
					categoryIndex_.addTermsInBulk(dataStream,index+1);
				}
				catch(TokenizerException tex)
				{
					tex.printStackTrace();
					System.out.println("Tokenizer exception while adding category");
				}
			}
			return;
		}
		else // need to add this
		{
			documentDictionary.add(FileName);
		}
		
		int docLength=0;
		HashMap<String,Integer> aid_DocNormCalc = new HashMap<String, Integer>();
		for (FieldNames field : FieldNames.values())
		{
			try
			{
				if(field !=  FieldNames.FILEID)
				{
					data=d.getField(field);
					if(data != null)
					{
						dataStream=tkizer.consume(data[0]);
						dataAnalyzer=analyzerFactory.getAnalyzerForField(field,dataStream);
						while(dataAnalyzer.increment());
						
						// add doc lenght
						if(field != FieldNames.CATEGORY)
						{
							docLength+=dataStream.size();
						}
						
						if(field != FieldNames.AUTHOR && field != FieldNames.AUTHORORG)
						{
							dataStream.reset();
							String term=null;
							while(dataStream.hasNext())
							{
								term=dataStream.next().toString().toLowerCase();
								if(aid_DocNormCalc.containsKey(term))
								{
									aid_DocNormCalc.put(term,aid_DocNormCalc.get(term)+1);
								}
								else
								{
									aid_DocNormCalc.put(term,1);
								}
							}
							dataStream.reset();
						}
						addToIndex(dataStream,field);
					}
				}
			}
			catch(TokenizerException TKex)
			{
				String[] cat=d.getField(FieldNames.CATEGORY);
				String[] fileID=d.getField(FieldNames.FILEID);
				System.out.println("Tokenizer exception while analyzing file:"+fileID[0]+" cat:"+cat[0]+" cotent"+field);
				TKex.printStackTrace();
			}
		}
		double DocEuc=0.0f;
		int MaxFreq=0;
		int tmp=0;
		
		
		//System.out.println(docLength+" Processed "+d.getField(FieldNames.FILEID)[0]);
		for(String a :aid_DocNormCalc.keySet())
		{
			tmp=aid_DocNormCalc.get(a);
			if(MaxFreq<tmp)
			{
				MaxFreq = tmp; 
			}
			DocEuc+=Math.pow(tmp,2);
		}
		/*double smooth=0.4;
		double tmp2;
		for(String a :aid_DocNormCalc.keySet())
		{
			tmp2=aid_DocNormCalc.get(a);
			tmp2=1+Math.log(tmp2);//smooth+((1-smooth)*(tmp2/MaxFreq));
			DocEuc+=Math.pow(tmp2,2);
		}
		*/
		DocEuc=Math.sqrt(DocEuc);
		DocMaxFreq.add(MaxFreq);
		DocEuclidean.add(DocEuc);
		DocLength.add(docLength);
		
		docID++;
	}
	
	
	/**
	 * 
	 * @param dataStream
	 * @param type
	 */
	private void addToIndex(TokenStream dataStream, FieldNames type)
	{	
		switch(type)
		{
			case TITLE:
			case NEWSDATE:
			case CONTENT:
				termIndex_.addTermsInBulk(dataStream, docID);
				break;
			case AUTHOR:
			case AUTHORORG:
				authorIndex_.addTermsInBulk(dataStream, docID);
				break;
			case PLACE:
				placeIndex_.addTermsInBulk(dataStream, docID);
				break;
			case CATEGORY:
				categoryIndex_.addTermsInBulk(dataStream, docID);
				break;
			default:
				System.out.println("Field id="+type+" does not have a corresponding index");
				break;
		}
	}
	
	
	/**
	 * Method that indicates that all open resources must be closed
	 * and cleaned and that the entire indexing operation has been completed.
	 * @throws IndexerException : In case any error occurs
	 */
	public void close() throws IndexerException
	{
		try
		{
			WriteDictionary docDictionaryWriter = new WriteDictionary(documentDictionary,DocLength,DocEuclidean,DocMaxFreq,fileDocumentDictionary);
			termIndex_.start();
			authorIndex_.start();
			categoryIndex_.start();
			placeIndex_.start();
			docDictionaryWriter.start();
			
			termIndex_.join();
			authorIndex_.join();
			categoryIndex_.join();
			placeIndex_.join();
			docDictionaryWriter.join();
			
		}
		catch (InterruptedException e) 
		{
			System.out.println("INTERRUPTED AADADASDADADADDA");
			e.printStackTrace();
		}
		
		System.out.println("All Indexe and Dictionaries have been written!!");	
	}
	
	private void writeMasterDictionary() throws FileNotFoundException 
	{
		StringBuilder masterDictionaryData=new StringBuilder();
		try 
		{
			BufferedWriter masterDictionaryWriter= new BufferedWriter(new FileWriter(fileMasterDictionary));
			for(String term : masterDictionary.keySet())
			{
				masterDictionaryData.append(term);
				masterDictionaryData.append(":");
				masterDictionaryData.append(masterDictionary.get(term).toString());
				masterDictionaryData.append(":");
				masterDictionaryData.append("\n");
			}
			
			masterDictionaryWriter.write(masterDictionaryData.toString());
			masterDictionaryWriter.flush();
			masterDictionaryWriter.close();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void writeDocumentDictionary() throws FileNotFoundException
	{
		StringBuilder docDictionaryData=new StringBuilder();
		try 
		{
			BufferedWriter docDictonaryWriter=new BufferedWriter(new FileWriter(fileDocumentDictionary));
			int index=0;
			for(;index<documentDictionary.size()-1;index++)
			{
				docDictionaryData.append(documentDictionary.get(index)); 
				docDictionaryData.append(":");
				docDictionaryData.append(index+1);
				docDictionaryData.append("\n");
			}
			docDictionaryData.append(documentDictionary.get(index));
			docDictionaryData.append(":");
			docDictionaryData.append(index+1);
			
			docDictonaryWriter.write(docDictionaryData.toString());
			docDictonaryWriter.flush();
			docDictonaryWriter.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
