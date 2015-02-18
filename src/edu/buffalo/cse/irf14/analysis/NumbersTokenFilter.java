package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumbersTokenFilter extends TokenFilter 
{
	/**	The following REGEX shall also recognize - 4th 2nd 3rd as well along with 89 90 1987 **/
	//private static Pattern num_pattern = Pattern.compile(REGEXCONSTANTS.REGEX_IS_NUM_PRESENT);
	private static Pattern num_pattern = Pattern.compile(REGEXCONSTANTS.REGEX_NUM_VALUE);
	private static Matcher matcher = null ;
	TokenStream stream = null;
	
	/**
	 * 
	 * @param stream
	 */
	public NumbersTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream = stream;
	}

	/**
	 * 
	 * @return
	 * @throws TokenizerException
	 */
	public boolean increment() throws TokenizerException 
	{
		boolean retVal = false;
		if(stream.hasNext())
		{
			retVal=  true;
			Token tok = stream.next();
			processNumber(tok);
		}
		return retVal;
	}

	private boolean processNumber(Token tok)
	{
		boolean retVal= false;
		String word= tok.toString();
		matcher = num_pattern.matcher(word);
		
		// must not be a date element
		if(matcher.find() && !tok.getDateStatus())
		{
			//System.out.println("found "+word);
			// check if the word contains valid number suffix
			if(word.matches(REGEXCONSTANTS.REGEX_NUMBERS_WITH_SUFFIX))
			{
				return false;
			}
			if(word.contains("-")) return false;
			word = word.replaceAll(REGEXCONSTANTS.REGEX_NUM_REMOVER,"");
			if(word.isEmpty())
			{
				stream.remove();
			}
			else
			{
				tok.setTermBuffer(word.toCharArray());
			}
			
		}
		return retVal;
	}
	
	@Override
	/**
	 * 
	 * @return
	 */
	public TokenStream getStream() 
	{
		return stream;
	}
}
