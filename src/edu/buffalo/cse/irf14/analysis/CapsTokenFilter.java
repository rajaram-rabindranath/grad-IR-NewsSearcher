package edu.buffalo.cse.irf14.analysis;


import java.util.regex.Matcher;
import java.util.regex.Pattern;





public class CapsTokenFilter extends TokenFilter
{
	/**
	 * Different types of Caps items  
	 */
	private enum capsTypes
	{
		initCaps,
		allCaps,
		camelCase,
		bothInitCaps,
		notValid,
	};
	
	
	private boolean isTitleData=true;
	private boolean isAuthorData=false;
	
	//private static final String someCaps=; // FIXME -- might want to look for catMANhero
	//private String[] regexCaps ={REGEXCONSTANTS.REGEX_MATCH_POSSESSIVE,REGEXCONSTANTS.REGEX_ALL_CAPS,REGEXCONSTANTS.REGEX_INIT_CAPS,REGEXCONSTANTS.REGEX_CAMEL_CASE,REGEXCONSTANTS.REGEX_BOTH_INIT_CAPS};
	private String[] regexCaps ={REGEXCONSTANTS.REGEX_ALL_CAPS,REGEXCONSTANTS.REGEX_INIT_CAPS,REGEXCONSTANTS.REGEX_CAMEL_CASE,REGEXCONSTANTS.REGEX_BOTH_INIT_CAPS};
	
	private TokenStream stream=null;
	private Pattern pattern;
	private Matcher matcher;
	
	public CapsTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream = stream;
	}
	
	
	public void setAuthorData(boolean isAuthorData)
	{
		this.isAuthorData = isAuthorData;
	}
	
	public void setTitleData(boolean isTitleData)
	{
		this.isTitleData= isTitleData;
	}
	
	@Override
	/**
	 * All tokens should be lowercased unless:
	 * 1. The whole word is in caps (AIDS etc.) and the whole sentence is not in caps
	 * 2. The word is camel cased and is not the first word in a sentence. 
	 * 3. If adjacent tokens satisfy the above rule, they should be combined into a single token 
	 * 		(San Francisco, Brad Pitt, etc.)
	 * therefore purpose --- Identification and processing based on same
	 */
	public boolean increment() throws TokenizerException 
	{
		boolean retValue = false;
		Token curr_tok = null;
		Token next_tok = null;
		
		if(stream.hasNext())
		{
			retValue = true;
			curr_tok = stream.next();
			
			/** IS THERE A CAPS CHAR IN THIS TOKEN ? **/
			pattern = Pattern.compile(REGEXCONSTANTS.REGEX_IS_CAPS);
			matcher = pattern.matcher(curr_tok.toString());
			if(!matcher.find()) return retValue;
					
			capsTypes type = capsIDing(curr_tok);
			
			switch(type)
			{
				case initCaps:
				case camelCase:
				case bothInitCaps:
					//if((curr_tok.isSentenceStart && !isAuthorData) && type==capsTypes.initCaps )
					
					if((curr_tok.isSentenceStart) && type==capsTypes.initCaps )
					{
						curr_tok.setTermBuffer((curr_tok.toString()).toLowerCase().toCharArray());
						break;
					}
					/**
					 *  Aggressive Capitalization token appending Iowa Beef Producers Inc.
					 *  but only till the end of the sentence ..... !! tricky
					 *  but how to we handle stuff like 
					 *  Thus Rajaram Rabindranath went home. !!!! tricky
					 */
					boolean compoundWord=false;
					
					while(stream.hasNext() && !curr_tok.isSentenceEnd)
					{
						if((next_tok = stream.lookForward()) !=null && (next_tok.toString().matches(REGEXCONSTANTS.REGEX_INIT_CAPS) || next_tok.toString().matches(REGEXCONSTANTS.REGEX_CAMEL_CASE))
								|| next_tok.toString().matches(REGEXCONSTANTS.REGEX_BOTH_INIT_CAPS))
						{
							/** advance --> remove --> merge **/
							stream.next();
							stream.remove(); 
							curr_tok.isSentenceEnd= next_tok.isSentenceEnd;
							curr_tok.merge(next_tok);
							compoundWord=true;
						}
						else
						{
							// instead of sending the data via stopwrds jst to remove "and" i do it
							if(isAuthorData && next_tok.toString().equalsIgnoreCase("and")) 
							{
								stream.next();
								stream.remove();
							}
							break;
						}
					}
					
					/** KEEP COMPOUND WORDs INTACT EVEN IF STARTING OF SENTENCE **/
					/*if(curr_tok.isSentenceStart && !compoundWord)
					{
						curr_tok.setTermBuffer((curr_tok.toString()).toLowerCase().toCharArray());
						
					}*/
					break;
				case allCaps:
					if(isTitleData) // the whole sentence will be in caps
					{
						break;
					}
				case notValid:
					if(curr_tok.isSentenceStart)
					{
						curr_tok.setTermBuffer((curr_tok.toString()).toLowerCase().toCharArray());
					}
					break;
				default:
				 break;
			}
		}
		return retValue;
	}

	/**
	 * ID the token with has at least one char
	 * as CAPS 
	 * @param tok
	 * @return
	 * @throws TokenizerException
	 */
	private capsTypes capsIDing(Token tok) throws TokenizerException
	{
		/** check with all the regex at disposal **/
		for(int j =0;j<regexCaps.length;j++)
		{
			pattern = Pattern.compile(regexCaps[j]);
			matcher = pattern.matcher(tok.toString());
			
			if(matcher.matches())
			{
				switch(j)
				{
					case 0:
						return capsTypes.allCaps;
					case 1:
						return capsTypes.initCaps;
					case 2:
						return capsTypes.camelCase;
					case 3:
						return capsTypes.bothInitCaps;
					default:
						break;
				}
			}
		}
		return capsTypes.notValid;
	}
		
	
	
	@Override
	public TokenStream getStream() 
	{
		return stream;
	}

}
