package edu.buffalo.cse.irf14.analysis;

public class REGEXCONSTANTS
{
	public static final String REGEX_PUNCTUATIONS="[\\?|!|\\.|:|;|,|\"|\']$";
	public static final String REGEX_PUNC_REMOVER="[\\?|!|\\.|:|;|,|\"|\'|)]+$";
	public static final String REGEX_SENTENCE_ENDING_PUNC= "[\\?|!|\\.]$";
	public static final String REGEX_SENTENCE_ENDING="^.+[\\?\"?|!\"?|\\.\"?]$";
	public static final String REGEX_WORD_BEGIN_PUNC_REMOVE= "^[\"|\'|(]+";
	
	public static final String REGEX_PUNC_CHAR_BEGIN="^[!|\\?|\\.]+";
	public static final String REGEX_SPL_CHARS = "[^A-Za-z0-9\\.\\?!'\\s]";// week day should not become weekday
	public static final String REGEX_SPLCHARS_REMOVER="[^A-Za-z0-9\\.\\?!'\\-\\s]+";
	public static final String REGEX_MATCH_POSSESSIVE="\\w+'s$|\\w+s'$";
	public static final String REGREX_FIND_POSSESSIVE="'s$|s'$";;
	public static final String REGEX_MANY_APOSTROPHES="'+";
	public static final String REGEX_NUM_VALUE="^[0-9\\.,]+";
	public static final String REGEX_PURE_NUM= "^[0-9]+$";// FIXME MADE SOME MODIFICATIONs
	public static final String REGEX_IS_EMAIL="^.+@\\w+.\\w+$";

	
	public static final String REGEX_PURE_ALPHA= "^(?i)[a-z\\.]+$";
	public static final String REGEX_IS_WORD_PUNCTUATED=".+[\\.|,|!|:|;|\\?]$";
	
	public static final String REGEX_TIME_RANGE="^(?i)(GMT|EDT|EST|CDT|CST|PST)/[0-9]{4}$";
	public static final String REGEX_NUMBERS_WITH_SUFFIX="^(?i)[0-9]+(th|rd|nd|st)$";
	public static final String REGEX_NUMBERS_WITH_SUFFIX_MAYBE= "^(?i)[0-9]+(th|rd|nd|ad|bc|am|pm)?$";
	public static final String REGEX_TIME_SUFFIX="^(?i)PM|AM|GMT|EDT|EST|CDT|CST|PST|hours|hrs|local|central|pacific|eastern$";
	public static final String REGEX_TIME_SUFFIX_EXT="^(?i)local|central|pacific|eastern$";
	
	public static final String REGEX_MILITARY_TIMINGS="^0[0-9]{3}$";
	public static final String REGEX_TO_OR_AND="(?i)to|and$";
	
	
	public static final String REGEX_NUMBER_SUFFIX="^(?i)st|th|rd|nd$";
	public static final String REGEX_YEAR_SUFFIX="^(?i)BC|AD$";
	
	public static final String REGEX_INIT_CAPS_IRISH= "^[A-Z]+'[a-z]*$"; // should change to ^[A-Z]+'[a-z]+
	public static final String REGEX_INIT_CAPS="^\"*[A-Z]+{2}[a-z'\\s?]+\\.?$"; // for stuff like Inc.
	public static final String REGEX_FRENCH_NAMES="^[A-Z]+[a-z]\\s[A-Z][a-z]$";
	public static final String REGEX_ALL_CAPS= "^[A-Z\\.\\s?]+$";
	public static final String REGEX_CAMEL_CASE="^[a-z]+[A-Z]+[a-z]+$";
	public static final String REGEX_BOTH_INIT_CAPS="^[A-Z][a-z]+\\s?[A-Z][a-z]+$";
	public static final String REGEX_IS_CAPS= "[A-Z]+";
	
	public static final String REGEX_NUM_REMOVER="[0-9]|\\.|,";
	
	public static final String REGEX_DOES_START_WITH_NUM="^[0-9]+"; 
	public static final String REGEX_IS_NUM_PRESENT="[0-9]+";
	public static final String REGEX_IS_TIME="^[0-9]+:[0-9]+";
	public static final String REGEX_OCLOCK="^(?i)o'clock$";
	
	public static final String REGEX_NUM_RANGE="^[0-9]{4}+[-|/][0-9]+$";
	
	public static final String REGEX_MONTH = "^(?i)january|jan|february|feb|march|mar|april|apr|may|"
			+ "june|july|august|aug|september|sep|october|oct|november|nov|december|dec$";
	
	
	public static final String REGEX_FIND_ROUGE_NEWSDATE="^.+\\.\\s.+$";
	
	// liberal regex which could also identify market as a valid month entity because of mar
	public static final String REGEX_MONTH_FIND = "(?i)january|jan|february|feb|march|mar|april|apr|may|"
			+ "june|july|august|aug|september|sep|october|oct|november|nov|december|dec";
	
	// Strict regex which defines word boundaries
	public static final String REGEX_MONTH_FIND_STRICT = "(?i)\\b(january|jan|february|feb|march|mar|april|apr|may|"
			+ "june|july|august|aug|september|sep\\s|october|oct|november|nov|december|dec)\\b";
	
	
	public static final String REGEX_WEEKDAYS = "^(?i)monday|mon|tuesday|tue|"
			+ "wednesday|wed|thursday|thur|friday|fri|sunday|sun|saturday|sat$";
	
	/**
	 * Query Regex
	 */
	
	public static final String REGEX_HAS_CLAUSE="^.*\\(.+\\).*$";
	public static final String REGEX_HAS_PHRASE="\".+\"";
	public static final String REGEX_ONLY_PHRASE="^\".+\"$";

	public static final String REGEX_IS_INDECES="(?i)(Author|Place|Category|Term):.+";
	public static final String REGEX_IS_BOOL ="AND|OR|NOT";
	
	public static final String REGEX_OPEN_BRACKS_SEQ="^\\(+.+";
	public static final String REGEX_CLOSE_BRACKS_SEQ="^.+\\)+";
	
	public static final String REGEX_OPEN_BRACKS="\\(+";
	public static final String REGEX_CLOSE_BRACKS="\\)+";
	public static final String REGEX_HAS_CLAUSE_START ="^.+\\(.+$";
}
