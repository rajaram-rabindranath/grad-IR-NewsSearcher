package edu.buffalo.cse.irf14.analysis;

public class StemmersTokenFilter extends TokenFilter 
{
	private TokenStream stream = null;
	private String stemWord;
	
	public StemmersTokenFilter(TokenStream stream) 
	{
		// TODO Auto-generated constructor stub
		super(stream);
		this.stream = stream;
		stemWord = null;
	}

	/**
	 * 
	 */
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
			stemWord = doStemming(currentToken.getTermBuffer());
			
			if(stemWord!=null)
			{
				currentToken.setTermBuffer(stemWord.toCharArray());
			}
			else
			{
				stream.remove();
			}
		}
		return retValue;
	}

	private String doStemming(char[] termBuffer) 
	{
		// TODO Auto-generated method stub
		if(termBuffer.length > 0)
		{
			Stemmer s = new Stemmer();
			s.add(termBuffer, termBuffer.length);
			if(!Character.isLetter(termBuffer[0]))
			{
				return new String(termBuffer);
			}
			else
			{
				s.stem();
			}
			return s.toString();
		}
		else
		{
			return null;
		}
		
	}

	@Override
	public TokenStream getStream() 
	{
		return stream;
	}

}