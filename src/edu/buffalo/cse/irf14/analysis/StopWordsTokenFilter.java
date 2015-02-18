package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;

public class StopWordsTokenFilter extends TokenFilter
{

	private static final String STOP_WORDS_COLLECTION = "a,able,about,across,after,all,almost,also,am,among,an,and,any,"
			+ "are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,"
			+ "either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,"
			+ "his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,"
			+ "me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,"
			+ "our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,"
			+ "them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,"
			+ "when,where,which,while,who,whom,why,will,with,would,yet,you,your";
	private HashMap<String,String> stopWords =  null;
	TokenStream stream = null;
	
	public StopWordsTokenFilter(TokenStream stream)
	{
		super(stream);
		this.stream = stream;
		stopWords =  new HashMap<String, String>();
		String[] tokens = STOP_WORDS_COLLECTION.split(",");
		// populate the hashmap with stopwords
		for(int i=0;i<tokens.length;i++)
			stopWords.put(tokens[i],"");
	}

	@Override
	public boolean increment() throws TokenizerException
	{
		boolean retValue = false;
		if(stream.hasNext())
		{
			retValue = true;
			Token tok = null;
			tok = stream.next();
			if(stopWords.get(tok.toString().toLowerCase()) != null)
				stream.remove();
		}
		return retValue;
	}

	@Override
	public TokenStream getStream() 
	{
		return stream;
	}

}
