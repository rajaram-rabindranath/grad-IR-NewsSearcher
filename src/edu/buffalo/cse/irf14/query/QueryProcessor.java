package edu.buffalo.cse.irf14.query;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryProcessor 
{
	 ArrayList<String> parsedTokens;
	 ArrayList<ParsedToken> parsedTokenList; 
	 static Stack<String> operandStack;
	 static Stack<String> operatorStack;
	 LinkedList<ParsedTokenInfo> rulesSet;
	 Stack<String> finalExpression;
	
	
	private class ParsedTokenInfo
	{
		private final Pattern regex;
		private final int tokenIndex;

		public ParsedTokenInfo(Pattern regex, int tokenIndex)
		{
			this.regex = regex;
			this.tokenIndex = tokenIndex;
		}
	}
	
	private void addToRulesSet(String pattern, int tokenIndex){
		rulesSet.add(new ParsedTokenInfo(Pattern.compile("^("+pattern+")"), tokenIndex));
	}
	
	public QueryProcessor()
	{
		operandStack = new Stack<String>();
		operatorStack = new Stack<String>();
		parsedTokens = new ArrayList<String>();
		rulesSet = new LinkedList<ParsedTokenInfo>();
		parsedTokenList = new ArrayList<ParsedToken>();
		finalExpression = new Stack<String>();
		addRules();
	}
	
	private void addRules()
	{
		this.addToRulesSet("^(.*)Author:(.*)",ParsedToken.TOKEN_AUTHOR);
		this.addToRulesSet("^(.*)Category:(.*)",ParsedToken.TOKEN_CATEGORY);
		this.addToRulesSet("^(.*)Place:(.*)",ParsedToken.TOKEN_PLACE);
		this.addToRulesSet("^(.*)Term:(.*)",ParsedToken.TOKEN_TERM);
		this.addToRulesSet("(\\s*)\\[(\\s*)",ParsedToken.OPEN_PARENTHESES);
		this.addToRulesSet("(.*)\\](\\s*)",ParsedToken.CLOSED_PARENTHESES);
		this.addToRulesSet("(\\s*)\\{(\\s*)",ParsedToken.OPEN_QUERY);
		this.addToRulesSet("(\\s*)\\}(\\s*)",ParsedToken.CLOSE_QUERY);
		this.addToRulesSet("AND",ParsedToken.OPERATOR_AND);
		this.addToRulesSet("OR",ParsedToken.OPERATOR_OR);	}
	
	public String processNested(String query) throws ParseException
	{
		parsedTokens = splitTokens(query);
		createTokenList();
		return infixQueryProcessing(parsedTokenList);
	}


	private String infixQueryProcessing(ArrayList<ParsedToken> tokenList) 
	{
		// TODO Auto-generated method stub
		String notTerm = null;
		for(ParsedToken parsedToken : tokenList){
			switch(parsedToken.tokenType){
			case ParsedToken.OPEN_QUERY:
				break;
			case ParsedToken.TOKEN_AUTHOR:
			case ParsedToken.TOKEN_CATEGORY:
			case ParsedToken.TOKEN_PLACE:
			case ParsedToken.TOKEN_TERM:
					operandStack.push(parsedToken.tokenString);
				break;
			case ParsedToken.OPERATOR_AND:
			case ParsedToken.OPERATOR_OR:
				if(operatorStack.isEmpty() || operatorStack.peek().equals("[")){
					operatorStack.push(parsedToken.tokenString);
				}else{
					String term1 = operandStack.pop();
					String term2 = operandStack.pop();
					String postfixExpression = concatenateQuery(term2,term1,operatorStack.pop());
					operandStack.push(postfixExpression);
					operatorStack.push(parsedToken.tokenString);
				}
				break;
			case ParsedToken.OPEN_PARENTHESES:
				operatorStack.push(parsedToken.tokenString);
				break;
			case ParsedToken.CLOSED_PARENTHESES:
				while(!operatorStack.peek().equals("[")){
					String term1 = operandStack.pop();
					String term2 = operandStack.pop();
					String postfixExpression = concatenateQuery(term2,term1,operatorStack.pop());
					operandStack.push(postfixExpression);
				}
				operatorStack.pop();
				break;
			case ParsedToken.CLOSE_QUERY:
				break;
			}
		}
		while(!operatorStack.isEmpty()){
			String term1 = operandStack.pop();
			String term2 = operandStack.pop();
			String postfixExpression = concatenateQuery(term2,term1,operatorStack.pop());
			operandStack.push(postfixExpression);
		}
		
		System.out.println("--------------Postfix---------------");
		System.out.println(operandStack.peek());
		return operandStack.peek();
	}

	private String concatenateQuery(String term1, String operator, String term2) 
	{
		// TODO Auto-generated method stub
		StringBuilder expression = new StringBuilder();
		expression.append(term1);
		expression.append(" ");
		expression.append(operator);
		expression.append(" ");
		expression.append(term2);
		return expression.toString();
	}

	public ArrayList<String> splitTokens(String parsedQuery) 
	{
		ArrayList<String> matchList = new ArrayList<String>();
		Pattern regex = Pattern.compile("[^\\s\"]+|\"[^\"]*(\"|$)");
		Matcher regexMatcher = regex.matcher(parsedQuery);
		while (regexMatcher.find()) {
		    matchList.add(regexMatcher.group());
		}
		Pattern typeRegex = Pattern.compile("^(.*)Author:|^(.*)Category:|^(.*)Place:|^(.*)Term:");
		for(int i = 1; i < matchList.size(); i++){
			Matcher regexTypeMatcher = typeRegex.matcher(matchList.get(i - 1));
			if(matchList.get(i).contains("\"")){
				if(regexTypeMatcher.find()){
					String newEntry = matchList.get(i-1) + matchList.get(i);
					matchList.set(i, newEntry);
					matchList.remove(i-1);
				}
			}
		}
		return matchList;
	}
	
	private void createTokenList()
	{
		for(String word : parsedTokens)
		{
			boolean match = false;
			for(ParsedTokenInfo info : rulesSet)
			{
				Matcher m = info.regex.matcher(word);
				if(m.find())
				{
					match = true;
					String token = m.group().trim();
					ParsedToken parsedToken = new ParsedToken(token,info.tokenIndex);
					parsedTokenList.add(parsedToken);
				}
			}
		}
	}
}