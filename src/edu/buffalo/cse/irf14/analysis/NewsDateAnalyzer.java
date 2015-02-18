package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;

public class NewsDateAnalyzer implements Analyzer {

	TokenStream stream = null;
	private ArrayList<TokenFilter> Filters = new ArrayList<TokenFilter>();
	protected NewsDateAnalyzer(TokenStream stream)
	{
		this.stream = stream;
		Filters.add(new SymbolsTokenFilter(stream));
		Filters.add(new DatesTokenFilter(stream));
		Filters.add(new NumbersTokenFilter(stream));
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
	public TokenStream getStream() {
		// TODO Auto-generated method stub
		return null;
	}

}
