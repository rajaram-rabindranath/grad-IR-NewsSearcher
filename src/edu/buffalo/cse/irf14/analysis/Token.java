/**
 * 
 */
package edu.buffalo.cse.irf14.analysis;

/**
 * @author nikhillo
 * This class represents the smallest indexable unit of text.
 * At the very least it is backed by a string representation that
 * can be interchangeably used with the backing char array.
 * 
 * You are encouraged to add more data structures and fields as you deem fit. 
 */
public class Token 
{
	
	//The backing string representation -- can contain extraneous information
	private String termText;
	//The char array backing termText
	private char[] termBuffer;
	int position=0;
	int paraCnt=0;
	boolean isDate=false;
	boolean isSentenceStart=false;
	boolean isSentenceEnd=false;
	boolean isNewParaStart=false;
	boolean isUnitofAllCaps=false;
	boolean isCompoundWord=false;
	
	public Token()
	{}
	
	
	public Token(String str)
	{
		termText = str;
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}
	
	// overloaded constructor
	public Token(String str,boolean isSentenceStart, boolean isSentenceEnd,boolean isUnitofAllCaps,boolean isNewParaStart,int position,int paraCnt)
	{
		termText = str;
		termBuffer = (termText != null) ? termText.toCharArray() : null;
		this.isSentenceEnd=isSentenceEnd;
		this.isSentenceStart=isSentenceStart;
		this.isUnitofAllCaps=isUnitofAllCaps;
		this.isNewParaStart=isNewParaStart;
		this.position=position;
		this.paraCnt=paraCnt;
	}
	
	public void setDateStatus(boolean status)
	{
		isDate=status;
	}
	
	public boolean getDateStatus()
	{
		return isDate;
	}
	
	/**
	 * Method to set the termText to given text.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param text
	 */
	protected void setTermText(String text) 
	{
		termText = text;
		termBuffer = (termText != null) ? termText.toCharArray() : null;
	}
	
	/**
	 * Getter for termText
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @return the underlying termText
	 */
	protected String getTermText()
	{
		return termText;
	}
	
	/**
	 * Method to set the termBuffer to the given buffer.
	 * This is a sample implementation and you CAN change this
	 * to suit your class definition and data structure needs.
	 * @param buffer: The buffer to be set
	 */
	protected void setTermBuffer(char[] buffer) 
	{
		termBuffer = buffer;
		termText = new String(buffer);
	}
	
	/**
	 * Getter for the field termBuffer
	 * @return The termBuffer
	 */
	protected char[] getTermBuffer() 
	{
		return termBuffer;
	}
	
	/**
	 * Method to merge this token with the given array of Tokens
	 * You are free to update termText and termBuffer as you please
	 * based upon your Token implementation. But the toString() method
	 * below must return whitespace separated value for all tokens combined
	 * Also the token order must be maintained.
	 * @param tokens The token array to be merged
	 */
	protected void merge(Token...tokens) 
	{
		if(tokens != null)
		{
			String token_str = getTermText();
			for(int i =0;i<tokens.length;i++)
			{
				token_str+=" "+tokens[i].toString();
			}
			setTermText(token_str);
		}
	}
	
	/**
	 * Returns the string representation of this token. It must adhere to the
	 * following rules:
	 * 1. Return only the associated "text" with the token. Any other information 
	 * must be suppressed.
	 * 2. Must return a non-empty value only for tokens that are going to be indexed
	 * If you introduce special token types (sentence boundary for example), return
	 * an empty string
	 * 3. IF the original token A (with string as "a") was merged with tokens B ("b"),
	 * C ("c") and D ("d"), toString should return "a b c d"
	 * @return The raw string representation of the token
	 */
	@Override
	public String toString()
	{
		return this.termText;
	}
}
