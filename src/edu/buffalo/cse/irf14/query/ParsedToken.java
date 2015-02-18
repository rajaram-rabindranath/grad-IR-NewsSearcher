package edu.buffalo.cse.irf14.query;
import java.util.ArrayList;


public class ParsedToken {
	
	public String tokenString;
	public int tokenType;
	
	public static final int TOKEN_AUTHOR = 1;
	public static final int TOKEN_CATEGORY = 2;
	public static final int TOKEN_PLACE = 3;
	public static final int TOKEN_TERM = 4;
	public static final int OPEN_PARENTHESES = 5;
	public static final int CLOSED_PARENTHESES = 6;
	public static final int OPEN_QUERY = 7;
	public static final int CLOSE_QUERY = 8;
	public static final int OPERATOR_AND = 9;
	public static final int OPERATOR_OR = 10;
	public static final int OPERATOR_NOT_OPEN = 11;
	public static final int OPERATOR_NOT_CLOSE = 12;
	
	public ParsedToken(String tokenString, int tokenIndex){
		this.tokenString = tokenString;
		this.tokenType = tokenIndex;
	}
}