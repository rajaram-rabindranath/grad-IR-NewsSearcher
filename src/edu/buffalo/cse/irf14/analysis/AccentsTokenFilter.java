package edu.buffalo.cse.irf14.analysis;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class AccentsTokenFilter extends TokenFilter 
{
	private TokenStream stream;
	private String removeAccent;
	public AccentsTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream = stream;
		removeAccent = null;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean increment() throws TokenizerException 
	{
		// TODO Auto-generated method stub
		boolean retValue = false;
		Token currentToken = null;
		if(stream.hasNext())
		{
			retValue = true;
			currentToken = stream.next();
			removeAccent = removeAccents(currentToken.getTermText());
			if(removeAccent != null)
			{
				currentToken.setTermText(removeAccent);
			}
			else
			{
				stream.remove();
			}
		}
		return retValue;
	}

	private String removeAccents(String termText)
	{
		// TODO Auto-generated method stub
		if(termText == null || termText.isEmpty())
		{
			return null;
		}
		else
		{
			String newWord = Normalizer.normalize(termText, Form.NFD).
					replaceAll("\\p{InCombiningDiacriticalMarks}+","");
			return newWord;
		}
		
	}

	@Override
	public TokenStream getStream() 
	{
		// TODO Auto-generated method stub
		return stream;
	}
}