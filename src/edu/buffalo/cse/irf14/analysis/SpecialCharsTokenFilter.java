package edu.buffalo.cse.irf14.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class SpecialCharsTokenFilter extends TokenFilter 
{

	private TokenStream stream = null;
	
	private static final Pattern pattern = Pattern.compile(REGEXCONSTANTS.REGEX_SPL_CHARS);
	private static Matcher matcher = null;
	public SpecialCharsTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream = stream;
	}

		
	@Override
	public boolean increment() throws TokenizerException 
	{
		boolean retVal = false;
		
		if(stream.hasNext())
		{
			retVal = true;
			Token tok = stream.next();
			
			if(tok.getDateStatus())
			{
				// scrub spl chars at end of sentence
				return true;
			} 
		
			String word = tok.toString();
			
			
			matcher = pattern.matcher(word);
			if(matcher.find())
			{
				// remove all spl chars except "-" since we need to do further processing on the same
				word = word.replaceAll(REGEXCONSTANTS.REGEX_SPLCHARS_REMOVER, "");
				
				if(word.contains("-"))
					word=hyphenProcessing(word);
			
				
				word=word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_CHAR_BEGIN,"");
				
				if(word.isEmpty())
				{
					stream.remove();
				}
				else
				{
					tok.setTermBuffer(word.toCharArray());	
				}
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param word
	 * @return
	 */
	private String hyphenProcessing(String word)
	{
		int pureNumberCnt = 0;
		int pureAlphaCnt = 0;
		Pattern pattern=null;
		//System.out.println("THE word is "+word);
		String[] tokens = word.split("-");
		if(tokens.length == 2)
		{
			for(int i=0;i<tokens.length;i++)
			{
				
				//System.out.println(i+" is "+tokens[i]);
				pattern = Pattern.compile(REGEXCONSTANTS.REGEX_IS_NUM_PRESENT);
				matcher = pattern.matcher(tokens[i]);
				if(matcher.find())
					pureNumberCnt++;

				pattern = Pattern.compile(REGEXCONSTANTS.REGEX_PURE_ALPHA);
				matcher = pattern.matcher(tokens[i]);
				if(matcher.matches())
					pureAlphaCnt++;
			}

			if((pureNumberCnt == 1 && pureAlphaCnt == 1) || (pureNumberCnt == 2)) // 6-6 B-54 / b-52
			{
				//do nothing
			} 
			else if(pureAlphaCnt == 2) // week-day
			{
				word=tokens[0].trim()+tokens[1].trim();	
			}
			else
			{
				//System.out.println("HYPEN -- don't know what to do::"+word);
			}
		}
		else // more than 2 tokens generated state-of-the-art
		{
			word="";
			for(String a: tokens)
			{
				word+=a.trim();
			}
		}
		return word;
	}

	
	@Override
	public TokenStream getStream() 
	{
		return stream;
	}
}
/*if(word.contains("@"))
{
	if(word.matches(REGEXCONSTANTS.REGEX_IS_EMAIL))
	{
		String[] tokens = word.split("@");
		tok.setTermBuffer(tokens[0].toCharArray());
		stream.add(tokens[1]);
	}
	else
		tok.setTermBuffer(word.replaceAll("@","").toCharArray());
	
	return true;
}*/