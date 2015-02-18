/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.buffalo.cse.irf14.document.FieldNames;
import edu.buffalo.cse.irf14.document.Parser;

/**
 * @author nikhillo
 * Class that represents a stream of Tokens. All {@link Analyzer} and
 * {@link TokenFilter} instances operate on this to implement their
 * behavior
 */
public class TokenStream implements Iterator<Token>
{
	
//	private final static Logger logger = Logger.getLogger(Parser.class.getName());
	public ArrayList<Token> tokenList;
	private static final int EMPTY_LIST = Integer.MAX_VALUE; 
	private int curr_index = EMPTY_LIST;
	private boolean canRemove = false; // to safeguard against consecutive remove()
	
	
	public TokenStream()
	{
		tokenList = new ArrayList<Token>();
		curr_index = 0;
	}
	
	public int size()
	{
		return tokenList.size();
	}
	
	//public TokenStream(String[] wordList,FieldNames type)
	public TokenStream(String[] wordList)
	{
		/** wrap all strings into Token **/
		tokenList = new ArrayList<Token>();
		curr_index = 0;
	
		/**
		 *  transform strings into TOKENs and have them
		 *  added to the arrayList
		 */
		boolean isSentenceStart=false;
		boolean isSentenceEnd=false;
		boolean isNewPara=false;
		boolean isUnitOfAllCaps=false;
		boolean hack=false;
		int paraCount=0;
		
		int whiteSpaceCnt=0;
		int size = wordList.length;
		
		// iterate thru all tokens
		for(int i =0;i<size;i++)
		{
			// spot <new_para>
			if(wordList[i].isEmpty())
			{
				whiteSpaceCnt++;
				continue;
			}
			if(whiteSpaceCnt>=4 || i==0)
			{
				whiteSpaceCnt=0;
				isSentenceStart=true;isNewPara=true;
				paraCount++;
			}
			
			
			// finding the token markers -- sentence ending / begining etc 
			// normally U.S and U.K are followed by InitCaps words and are generally not ending of sentences
			if(wordList[i].matches(REGEXCONSTANTS.REGEX_SENTENCE_ENDING) && !wordList[i].matches(REGEXCONSTANTS.REGEX_ALL_CAPS))
			{
				if(i+1<size && (wordList[i+1].isEmpty() || wordList[i+1].matches(REGEXCONSTANTS.REGEX_INIT_CAPS)))
				{ 
					isSentenceEnd=true;
					// FIXME -- need to make the next word as sentence starter 
					if(!wordList[i+1].isEmpty())
					{
						hack= true;
					}
				}
				else if(i+1==size)
				{
					isSentenceEnd=true;
				}
				else if(wordList[i].matches("^.+[0-9]+\\.$"))
				{
					isSentenceEnd=true;
				}
			}
			tokenList.add(new Token(wordList[i].trim(),isSentenceStart,isSentenceEnd,isUnitOfAllCaps,isNewPara,i,paraCount));
			if(hack)
			{
				tokenList.add(new Token(wordList[i+1].trim(),true,false,false,isNewPara,i+1,paraCount));
				//System.out.println("SENTENCE START :::"+wordList[i+1]);
				i++;
				hack=false;
			}
			// reset discoveries
			isSentenceEnd=isSentenceStart=isNewPara=isUnitOfAllCaps=false;
		}
	}
	
	/*public void printAll(String str)
	{
		logger.log(Level.FINE,"=============="+str+"==================");
		this.printAll();
	}
	
	public void printAll()
	{
		logger.log(Level.FINE,"SIZE OF THE LIST::::::"+tokenList.size());
		Token tok;
		for(int i=0;i<tokenList.size();i++)
		{
			tok=tokenList.get(i);
			logger.log(Level.FINE, "TOKEN::"+tok.toString());
		}
	}
	*/
	/**
	 * Method that checks if there is any Token left in the stream
	 * with regards to the current pointer.
	 * DOES NOT ADVANCE THE POINTER
	 * @return true if at least one Token exists, false otherwise
	 */
	@Override
	public boolean hasNext() 
	{
		// !empty list + have not iterated all
		if(tokenList.size() != 0 && curr_index < tokenList.size()) return true;
		return false;
	}

	/**
	 * Method to return the next Token in the stream. If a previous
	 * hasNext() call returned true, this method must return a non-null
	 * Token.
	 * If for any reason, it is called at the end of the stream, when all
	 * tokens have already been iterated, return null
	 */
	@Override
	public Token next() 
	{
		Token t = null;
		if(this.hasNext())
		{
			t = tokenList.get(curr_index);
			curr_index++;
			canRemove = true;
		}
		else
		{
			canRemove = false;
		}
		return t;
	}
	
	
	/**
	 * Post-processing -- put back the token into the stream
	 */
	public boolean set(Token tok)
	{
		return true;
	}
	
	/**
	 * Method to remove the current Token from the stream.
	 * Note that "current" token refers to the Token just returned
	 * by the next method. 
	 * Must thus be NO-OP when at the beginning of the stream or at the end
	 */
	@Override
	public void remove() 
	{
		if(canRemove) // to safegaurd against consecutive remove(); ops
		{
			if(tokenList.size() != 0)
			{
				int get = (curr_index == 0) ? curr_index : curr_index-1;
				tokenList.remove(get);
				curr_index = get;
				/** are we at the tail **/
				if(curr_index >= tokenList.size())curr_index=tokenList.size();
			}
			
			/** is the list empty now**/
			if(tokenList.size() == 0) curr_index = EMPTY_LIST;
			canRemove = false;
		}
	}
	
	/**
	 * Method to reset the stream to bring the iterator back to the beginning
	 * of the stream. Unless the stream has no tokens, hasNext() after calling
	 * reset() must always return true.
	 */
	public void reset() 
	{
		 curr_index = 0;
	}
	
	/**
	 * Add tokens to the tokenStream object
	 */
	public void add(Token tok)
	{
		tokenList.add(tok);
		if(curr_index ==EMPTY_LIST) curr_index=0;
	}
	
	
	public void add(String str)
	{
		Token tok = new Token(str);
		this.add(tok);
	}
	
	
	/**
	 * Method to append the given TokenStream to the end of the current stream
	 * The append must always occur at the end irrespective of where the iterator
	 * currently stands. After appending, the iterator position must be unchanged
	 * Of course this means if the iterator was at the end of the stream and a 
	 * new stream was appended, the iterator hasn't moved but that is no longer
	 * the end of the stream.
	 * @param stream : The stream to be appended
	 */
	public void append(TokenStream stream)
	{
		if(stream != null)
		tokenList.addAll(stream.tokenList);
	}
	

	/**
	 * Method to get the current Token from the stream without iteration.
	 * The only difference between this method and {@link TokenStream#next()} is that
	 * the latter moves the stream forward, this one does not.
	 * Calling this method multiple times would not alter the return value of {@link TokenStream#hasNext()}
	 * @return The currlent {@link Token} if one exists, null if end of stream
	 * has been reached or the current Token was removed FIXME -- RAJARAM
	 */
	public Token getCurrent() 
	{
		Token t = null;
		if(canRemove)
		{
			t = tokenList.get(curr_index-1);
		}
		return t;
	}
	
	public Token lookForward()
	{
		Token t=null;
		if(this.hasNext())
		{
			t = tokenList.get(curr_index);
		}
		return t;
	}
	
}
