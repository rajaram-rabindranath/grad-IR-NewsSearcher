package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;

public class CategoryAnalyzer implements Analyzer 
{

	TokenStream stream = null;
	// will contain all the token filters
	private ArrayList<TokenFilter> Filters=new ArrayList<TokenFilter>();
	
	public CategoryAnalyzer(TokenStream stream)
	{
		this.stream = stream;
		Filters.add(new SymbolsTokenFilter(stream));
		Filters.add(new AccentsTokenFilter(stream));
		Filters.add(new SpecialCharsTokenFilter(stream));
	}
	
	@Override
	public boolean increment() throws TokenizerException 
	{
		for(TokenFilter filter:Filters)
		{
			stream.reset();
			while(filter.increment());
		}
		return false;
	}

	@Override
	public TokenStream getStream() 
	{
		return stream;
	}

}
