package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;

public class AuthorAnalyzer implements Analyzer
{

	TokenStream stream = null;
	private ArrayList<TokenFilter> Filters = new ArrayList<TokenFilter>();
	
	public AuthorAnalyzer(TokenStream stream)
	{
		
		this.stream = stream;
		
		Filters.add(new SymbolsTokenFilter(stream));
		Filters.add(new SpecialCharsTokenFilter(stream));
		Filters.add(new NumbersTokenFilter(stream));
		CapsTokenFilter caps = new CapsTokenFilter(stream);
		caps.setAuthorData(true);
		Filters.add(caps);
		Filters.add(new StopWordsTokenFilter(stream));
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
		// TODO Auto-generated method stub
		return null;
	}

}
