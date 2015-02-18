/**
 * 
 */
package edu.buffalo.cse.irf14.document;

/**
 * @author nikhillo
 * Generic wrapper exception class for parsing exceptions
 */
public class ParserException extends Exception 
{
	/**
	 * 
	 */
	// check to see if branch stuff works
	
	private static final long serialVersionUID = 4691717901217832517L;
	String reason = null;
	public ParserException(){}
	public ParserException(String reason)
	{
		this.reason = reason;
	}
	
	/**
	 * 
	 * @return String -- reason for the exception
	 */
	public String getReason()
	{
		return reason;
	}
}
