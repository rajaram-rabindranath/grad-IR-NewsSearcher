package edu.buffalo.cse.irf14.index;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WriteDictionary extends Thread
{
	ArrayList<String> documentDictionary=null;
	File file_docDictionary=null;
	ArrayList<Integer> docLenghts=null;
	ArrayList<Double> docEuc=null;
	ArrayList<Integer> docMaxFreq=null;
	public WriteDictionary(ArrayList<String> docDictionary,ArrayList<Integer> doclengths,
			ArrayList<Double> docEuc,ArrayList<Integer> docMaxFreq,File file_docDictionary)
	{
		this.documentDictionary = docDictionary;
		this.file_docDictionary=file_docDictionary;
		this.docLenghts=doclengths;
		this.docEuc=docEuc;
		this.docMaxFreq=docMaxFreq;
		
	}
	
	public void run()
	{
		
		StringBuilder docDictionaryData=new StringBuilder();
		try 
		{
			BufferedWriter docDictonaryWriter=new BufferedWriter(new FileWriter(file_docDictionary));
			int index=0;
			for(;index<documentDictionary.size()-1;index++)
			{
				docDictionaryData.append(documentDictionary.get(index)); 
				
				docDictionaryData.append(Delimiters.delimDictionary);
				docDictionaryData.append(index+1);
				
				docDictionaryData.append(Delimiters.delimDictionary);
				docDictionaryData.append(docLenghts.get(index));
				
				docDictionaryData.append(Delimiters.delimDictionary);
				docDictionaryData.append(docEuc.get(index));
				
				docDictionaryData.append(Delimiters.delimDictionary);
				docDictionaryData.append(docMaxFreq.get(index));
				
				docDictionaryData.append("\n");
			}
			
			docDictionaryData.append(documentDictionary.get(index));
			
			docDictionaryData.append(Delimiters.delimDictionary );
			docDictionaryData.append(index+1);
			
			docDictionaryData.append(Delimiters.delimDictionary);
			docDictionaryData.append(docLenghts.get(index));
			
			docDictionaryData.append(Delimiters.delimDictionary);
			docDictionaryData.append(docEuc.get(index));
			
			docDictionaryData.append(Delimiters.delimDictionary);
			docDictionaryData.append(docMaxFreq.get(index));
			
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
