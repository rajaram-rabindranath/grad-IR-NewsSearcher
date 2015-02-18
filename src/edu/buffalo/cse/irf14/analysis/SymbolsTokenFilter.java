package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolsTokenFilter extends TokenFilter
{
	
	private static final Pattern num_pattern=Pattern.compile(REGEXCONSTANTS.REGEX_IS_NUM_PRESENT);
	private static final Pattern alpha_pattern=Pattern.compile(REGEXCONSTANTS.REGEX_PURE_ALPHA);
	private static final Pattern punc_pattern=Pattern.compile(REGEXCONSTANTS.REGEX_PUNCTUATIONS);
	private static final Pattern sentenceEnd_punc_pattern = Pattern.compile(REGEXCONSTANTS.REGEX_SENTENCE_ENDING);
	private static final Pattern find_possessive=Pattern.compile(REGEXCONSTANTS.REGREX_FIND_POSSESSIVE);	
	private Matcher matcher = null;
	private TokenStream stream =  null; 
	/** <english_contractions, expansion> pairs **/
	private HashMap<String, String> eng_cont;

	
	
	
	public SymbolsTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream= stream;
		eng_cont = new HashMap<String, String>();
		eng_cont.put("ve","have");
		eng_cont.put("t","not");
		eng_cont.put("ll","will");
		eng_cont.put("m","am");
		eng_cont.put("re","are");
		eng_cont.put("d","would");
		eng_cont.put("em","them");
		eng_cont.put("won","will");
		eng_cont.put("shan","shall");
		eng_cont.put("s","is");
	}

	@Override
	/**
	 * Method to indicate that the implementing class must complete 
	 * all its processing on the current {@link Token} and proceed to next 
	 * @throws TokenizerException : If any exception occurs during the operation
	 */
	public boolean increment() throws TokenizerException
	{
		boolean retValue = false;
		Token curr_tok = null;
		
		if(stream.hasNext())
		{
			retValue = true;
			curr_tok = stream.next();
			symbolProcessing(curr_tok);
		}
		return retValue;
	}

	/**
	 * 1. Punctuation processing
	 * 2. Possessive processing
	 * 3. Apostrophe processing
	 * 4. Hyphen processing
	 * @param tok
	 */
	public void symbolProcessing(Token tok)
	{
		punctuationProcessing(tok);
		possessiveProcessing(tok);
		apostropheProcessing(tok);
		if(!hyphenProcessing(tok)) // many hyphens in the word -- "state-of-the-art"
		{
			
			//System.out.println("Too many hypens "+tok.toString());
			// state-of-the-art / ten-year-old-boy / --will
			String tokens[] = tok.toString().split("-");
			//stream.remove();
			/**
			 *  if the user queries "state of the art" then we shall be find it here--
			 *  but if we were to do stateoftheart of the example then when queried we
			 *  shall not be able to retrieve this information
			 *  -- but however since we are maintaining positional index we must concat
			 *  individual components 
			 */
			StringBuilder compoundToken= new StringBuilder();
			for(int i=0;i<tokens.length;i++) // FIXME -- could have concat all the elements and set it to the token 
			{
				if(!tokens[i].isEmpty())
				{
					compoundToken.append(tokens[i]);
				}
			}
			
			if(!compoundToken.toString().isEmpty())
			{
				tok.setTermBuffer(compoundToken.toString().toCharArray());
			}
			else
			{
				stream.remove();
			}
		}
	}
	/**
	 * Receives a token and evaluates if it is
	 * legit Punctuation
	 * @param tok
	 * @return
	 */
	private void punctuationProcessing(Token tok)
	{	
		String word = tok.toString();
		/**************************************************************
		 * PUNCTUATIONS
		 * Check is present
		 * Check if Line ending just to ID -- make the token special
		 **************************************************************/
		matcher = punc_pattern.matcher(word);
		if(matcher.find())
		{
			/*matcher = sentenceEnd_punc_pattern.matcher(word);
			if(matcher.matches())
			{
				if(stream.hasNext())
				{
					// handling abbreviations
					if(!tok.isSentenceEnd || word.matches(REGEXCONSTANTS.REGEX_ALL_CAPS)) return;
					//if(!tok.isSentenceEnd) return;
				}
				else
				{
					// last word in the whole document so....
				}
			}*/
			// remove all punctuation at the begining or the end of the word -- FIXME
			word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,"");
			word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
			if(!word.isEmpty())
			{
				tok.setTermText(word);
			}
			else
			{
				stream.remove();
			}
		}
		return;
	}
	
	/**
	 * Receives a token and checks if it is
	 * a legit Possessive
	 * @param tok
	 * @return
	 */
	private void possessiveProcessing(Token tok)
	{
		String word = tok.toString();
		 
		matcher = find_possessive.matcher(word);
		if(matcher.find())
		{
			if(!word.substring(0,matcher.start()).equalsIgnoreCase("let"))
			{
				word =  word.replaceAll(REGEXCONSTANTS.REGREX_FIND_POSSESSIVE,"");
				if(word.isEmpty()) stream.remove();
				else
				{
					tok.setTermBuffer(word.toCharArray());
				}
			}
		}
		return;
	}
	
	/**
	 * 
	 * @param tok
	 */
	private void apostropheProcessing(Token tok)
	{
		String word= tok.toString();
		String expansion=  null;
		String initCaps = "^[A-Z][a-z]*$"; // * there for names like O'Niel or MAC
		// i think ^[A-Z]+'[a-z]* would be a better way to look at these things
		
		if(word.contains("'"))
		{
			String tokens[]= word.split("'");
			//System.out.println("Lenght of the tokens"+tokens.length);
			/** check if both words are init caps -- NAME **/
			if(tokens.length == 2)
			{
				if(tokens[0].matches(REGEXCONSTANTS.REGEX_INIT_CAPS_IRISH) && tokens[1].matches(REGEXCONSTANTS.REGEX_INIT_CAPS_IRISH) )
				{
					tok.setTermText(tokens[0]+tokens[1]); // A NAME!!! O'Niel
					return;
				}
				else if((expansion= eng_cont.get(tokens[1])) != null)
				{
					if(expansion.equals("not"))
					{
						if(eng_cont.get(tokens[0])!=null)
						{
							tokens[0]=eng_cont.get(tokens[0]);
						}
						else
						{
							tokens[0] =tokens[0].substring(0,tokens[0].length()-1);	
						}
					}
					tok.setTermBuffer((tokens[0]+" "+expansion).trim().toCharArray());
					return;
				}
			}
			if(word.replaceAll(REGEXCONSTANTS.REGEX_MANY_APOSTROPHES, "").trim().isEmpty()) stream.remove();
			else
			{
				tok.setTermBuffer(word.replaceAll(REGEXCONSTANTS.REGEX_MANY_APOSTROPHES, "").trim().toCharArray());
			}
		}
		return;
	}
	
	/**
	 * 
	 * @param tok
	 */
	private boolean hyphenProcessing(Token tok)
	{
		
		/**
		 * If a hyphen occurs within a alphanumeric token it should be retained 
		 * (B-52,at least one of the two constituents must have A number). 
		 * If both are alphabetic, it should be replaced with a whitespace 
		 * and retained as a single token (week-day => week day). 
		 * Any other hyphens padded by spaces on either or both sides should be removed.
		 */
		boolean retvalue = true;
		String word = tok.toString();
		int numPresentCnt= 0;
		int pureAlphaCnt= 0;
		
		if(word.contains("-"))
		{
			/**
			 * Sanity check if word in format " - "
			 */
			if(word.trim().equals("-"))
			{
				stream.remove();
				return true;
			}
			
			if(word.matches("^\\(?-[0-9\\.]+\\)?$"))
			{
				//System.out.println(word+" this is it");
				tok.setTermBuffer(tok.toString().replaceAll("-","").toCharArray());
			}
			
			String[] tokens = word.split("-");
			if(tokens.length == 2)
			{
				/**
				 * either one of these must have a number 
				 */
				for(int i=0;i<tokens.length;i++)
				{
					matcher = num_pattern.matcher(tokens[i]);
					if(matcher.find())
					{
						numPresentCnt++;
						continue;
					}
					matcher = alpha_pattern.matcher(tokens[i].replaceAll("[/|<|>|(|)|\"|\\.|,]", ""));
					if(matcher.matches())
					{
						pureAlphaCnt++;
					}
				}
				
				// take decisions based on discovery
				if((numPresentCnt == 1 && pureAlphaCnt == 1) || (numPresentCnt > 0)) // 6-6 B-54 / b-52
				{
					/*if(pureAlphaCnt == 1 && tokens[0].isEmpty())
					{
						tok.setTermBuffer(tok.toString().replaceAll("-", "").toCharArray());
					}
					else if(pureAlphaCnt == 1) //alum/0005316: +NON-COMMUNIST
					{
						tok.setTermText(tokens[0].trim()+" "+tokens[1].trim());
					}*/
				} 
				else if(pureAlphaCnt == 2) // week-day
				{
					tok.setTermText(tokens[0].trim()+" "+tokens[1].trim());	
				}
				else
				{
					//System.out.println("HYPEN -- don't know what to do::"+word+":PureAlpha="+pureAlphaCnt+":Numpresent="+numPresentCnt);
					if(pureAlphaCnt == 1 && tokens[0].isEmpty())
					{
						tok.setTermBuffer(tok.toString().replaceAll("-", "").toCharArray());
					}
					else if(pureAlphaCnt == 1) //alum/0005316: +NON-COMMUNIST
					{
						tok.setTermText(tokens[0].trim()+" "+tokens[1].trim());
					}
				}
			}
			else // something like state-of-the-art
				return false;
		}
		return retvalue;
	}
	
	@Override
	/**
	 * Return the underlying {@link TokenStream} instance
	 * @return The underlying stream
	 */
	public TokenStream getStream() 
	{
		return stream;
	}

}
