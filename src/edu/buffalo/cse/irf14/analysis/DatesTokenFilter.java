package edu.buffalo.cse.irf14.analysis;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.runner.Computer;

public class DatesTokenFilter extends TokenFilter 
{
	TokenStream stream = null;
	private static HashMap<String,String> month_numMap = new HashMap<String, String>();
	
	private static final String DEFAULT_YEAR= "1900";
	private static final String DEFAULT_MONTH= "01";
	private static final String DEFAULT_DAY= "01";

	//private static final Pattern num_pattern= Pattern.compile(REGEXCONSTANTS.REGEX_IS_NUM_PRESENT);
	private static final Pattern num_pattern= Pattern.compile(REGEXCONSTANTS.REGEX_DOES_START_WITH_NUM);
	
	private static final Pattern time_pattern= Pattern.compile(REGEXCONSTANTS.REGEX_IS_TIME);
	
	private boolean isfirstEntity;
	private boolean isAD;
	private boolean isWeekDay;
	private String month="";
	private String year="";
	private String day="";
	private String next_has_year="";
	private boolean is_punctuated;
	private String punc_token;
	
	static // a lookup with month to number mapping
	{
		month_numMap.put("january","01");
		month_numMap.put("february","02");
		month_numMap.put("march","03");
		month_numMap.put("april","04");
		month_numMap.put("may","05");
		month_numMap.put("june","06");
		month_numMap.put("july","07");
		
		month_numMap.put("august","08");
		month_numMap.put("september","09");
		month_numMap.put("october","10");
		month_numMap.put("november","11");
		
		month_numMap.put("december","12");
		month_numMap.put("jan","01");
		month_numMap.put("feb","02");
		month_numMap.put("mar","03");
		
		month_numMap.put("apr","04");
		month_numMap.put("aug","08");
		month_numMap.put("sep","09");
		month_numMap.put("oct","10");
		
		month_numMap.put("nov","11");
		month_numMap.put("dec","12");
	}
	
	public DatesTokenFilter(TokenStream stream) 
	{
		super(stream);
		this.stream = stream;
	}

	@Override
	public boolean increment() throws TokenizerException 
	{
		/**
		 * some state variables
		 * becoz we shall be looking aheisAD
		 * and all that
		 */
		boolean retVal=  false;
		isfirstEntity= true;
		isWeekDay=false;
		isAD=true;
		is_punctuated=false;
		day="";year="";month="";punc_token="";next_has_year="";
		
		if(stream.hasNext())
		{	
			retVal= true;
			// if we find anything interesting following token is the one we shall be changing
			Token tok =  stream.next();
			Token firstToken = tok;
			
			// search for date related info going from specific ---- generic
			while(dateProcessing(tok))
			{
				if(isfirstEntity)
				{
					isfirstEntity=false;
					if(tok.isSentenceEnd)break;
				}
				else
				{
					stream.remove();
				}
				
				// don't be over aggressive
				if(!month.isEmpty() && !year.isEmpty() && !day.isEmpty())
				{
					break;
				}
				
				if(stream.hasNext())
				{
					if(stream.lookForward().isSentenceStart)
					{
						break;
					}
					tok=stream.next();
					
				}
				else
					break;
			}
			
			if(!isfirstEntity)
			{
				String compound =  compoundToken();
				if(compound!=null && !compound.isEmpty())
				{
					firstToken.setDateStatus(true);
					firstToken.setTermBuffer(compound.toCharArray());
				}
				if(!next_has_year.isEmpty())
				{
					StringBuilder s = new StringBuilder();
					int zeros = 4 - next_has_year.toCharArray().length; 
					while(zeros > 0)
					{
						s.append("0");
						zeros--;
					}
					s.append(next_has_year);
					s.append(DEFAULT_MONTH);
					s.append(DEFAULT_DAY);
					
					tok.setDateStatus(true);
					tok.setTermBuffer(s.toString().toCharArray());
				}
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 **/
	private String compoundToken()
	{
		/** 28  not valid **/
		if(year.isEmpty() && month.isEmpty()) return null;
		
		/** FIXME --
		 * eg. Jan/January not valid
		 * eg. Jan 24th valid
		 * eg Jan 1978 valid
		 */
		//if(!month.isEmpty() && year.isEmpty() && day.isEmpty()) return null;
		
		StringBuilder compTok=new StringBuilder();
		
		if(!year.isEmpty())
		{
			int zeros = 4 - year.toCharArray().length; 
			while(zeros > 0)
			{
				compTok.append("0");
				zeros--;
			}
			compTok.append(year);
			if(!isAD) compTok.insert(0, "-");
			
		}
		else
			compTok.append(DEFAULT_YEAR);
		
		if(!month.isEmpty())
		{
			compTok.append(month);
		}
		else
			compTok.append(DEFAULT_MONTH);
		
		if(!day.isEmpty())//&& !month.isEmpty())
		{
			compTok.append(day);
		}
		else
		{
			compTok.append(DEFAULT_DAY);
		}
		if(is_punctuated)
			compTok.append(punc_token);
		
		year="";month="";day="";
		return compTok.toString();
	}

	/**
	 * @param tok
	 * @return
	 */
	private boolean dateProcessing(Token tok)
	{		
		boolean retVal = false;
		if(isMonth(tok))
			retVal=true;
		else if(isWeekDay(tok))
		{
			isWeekDay=true;
			retVal=true;
		}
		else if(isTime(tok))
		{}
		else if(isYearRange(tok))
			retVal=true;
		else if(isNum(tok))
			retVal = true;
		
		return retVal;
	}
	
	/**
	 * 
	 * @param tok
	 * @return
	 */
	private boolean isWeekDay(Token tok)
	{
		boolean retVal =false;
		String word = tok.toString();
		

		word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,""); // remove punctuation
		word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
		
		if(word.matches(REGEXCONSTANTS.REGEX_WEEKDAYS))
		{
			retVal= true;
			extractPunctuation(tok.toString());
		}
		return retVal;
	}
	
	/**
	 * @param tok
	 * @return
	 */
	private boolean isMonth(Token tok)
	{
		boolean retVal =false;
		String word = tok.toString();
		
		word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,""); // remove punctuation
		word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
		
		if(word.matches(REGEXCONSTANTS.REGEX_MONTH_FIND))
		{
			
			// to counter adjacent months April Jan February
			if( !month.isEmpty() ||word.equals("may"))
			{
				retVal = false;
			}
			else
			{
				extractPunctuation(word);
				retVal = true; 
				month=month_numMap.get(word.toLowerCase());
			}
		}
		return retVal;
	}
	
	/**
	 * @param tok
	 * @return
	 */
	private boolean isTime(Token tok)
	{
		boolean retVal = false;
		String word = tok.toString();
		boolean isAMPM_known=false;
		boolean morning = true;
		
		word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,""); // remove punctuation
		word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
		Matcher matcher = time_pattern.matcher(word); // FIXME -- can be 8AM
		
		if(matcher.find())
		{
			retVal = true;
			String v[] = word.split(":");
			/**
			 * Check all components of time to 
			 * see if extraneous information is held 
			 */
			for(int i=0;i<v.length;i++)
			{
				System.out.println("Component::"+v[i]);
				// is it a pure number ?
				if(!v[i].matches(REGEXCONSTANTS.REGEX_PURE_NUM)) 
				{
					/**
					 * if not then check what is the extraneous data
					 * and plz validate if this is indeed a date component
					 */
					matcher = num_pattern.matcher(v[i]);
					if(matcher.find())
					{
						String xtra = v[i].substring(matcher.end());
						if(xtra.matches(REGEXCONSTANTS.REGEX_TIME_SUFFIX))
						{
							isAMPM_known=true;
							if(xtra.equalsIgnoreCase("PM"))
								morning = false;
							v[i]=v[i].substring(0,matcher.end());
						}
						else // some character but is !time_related so .....
							return false;
					}
				}
			}
			extractPunctuation(tok.toString());
			// we do not know if this time term is AM/PM let's check
			if(!isAMPM_known) morning=isMorning();
			// Construct clock time ----
			tok.setDateStatus(true);
			tok.setTermBuffer(constructClock(morning,v).toCharArray());
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param v
	 * @param morning
	 * @return
	 */
	private String constructClock(boolean morning,String... v)
	{
		StringBuilder clock=new StringBuilder();
		
		if(Integer.valueOf(v[0]) < 12 && !morning) 
		{ v[0] = Integer.toString(Integer.valueOf(v[0])+12);}
		
		if(v.length > 1)
		{
			for(int i=0;i<v.length-1;i++)
			{
				clock.append(v[i]);clock.append(":"); 
			}
			clock.append(v[v.length-1]);
			if(v.length<3)clock.append(":00");
		}
		else
		{
			clock.append(v[0]);clock.append(":00:00");
		}
		
		if(is_punctuated)
		{
			clock.append(punc_token);
			is_punctuated=false;
		}
		
		return clock.toString();
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean isMorning()
	{
		boolean retVal=true;
		Token next_tok= stream.lookForward();
		if(next_tok!= null)
		{
			String is_am_pm = next_tok.toString();
			is_am_pm = is_am_pm.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,"");
			is_am_pm = is_am_pm.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
			if(is_am_pm.matches(REGEXCONSTANTS.REGEX_TIME_SUFFIX))
			{
				if(is_am_pm.equalsIgnoreCase("PM"))
					retVal = false;
				extractPunctuation(stream.next().toString());
				stream.remove();
			}
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param tok
	 * @return
	 */
	private boolean isYearRange(Token tok)
	{
		boolean retVal = false;
		String word = tok.toString();

		word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,"");
		word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
		
		if(word.matches(REGEXCONSTANTS.REGEX_NUM_RANGE))
		{
			retVal = true;
			String[] a = word.split("-|/");
			StringBuilder dateBuild = new StringBuilder();
			if(a[0].length() != 4 && a[1].length() > 4) return false; // cannot be a year value -0- if "January 24-25"/"24-25 January" that crazy
			if(a[0].length() != a[1].length())
			{
				if(a[0].toCharArray().length > a[1].toCharArray().length && a[1].length() == 2 && Integer.valueOf(a[0]) < 2050)
				{
					String century = a[0].substring(0,2);
					a[1]=century+a[1];
				}
				else// 1995-19967 -- not possible
				{
					return false;
				}
			}
			dateBuild.append(a[0]);dateBuild.append(DEFAULT_MONTH);
			dateBuild.append(DEFAULT_DAY);dateBuild.append("-");
			dateBuild.append(a[1]);dateBuild.append(DEFAULT_MONTH);
			dateBuild.append(DEFAULT_DAY);
			
			extractPunctuation(tok.toString());
			if(is_punctuated)
			{
				dateBuild.append(punc_token);
			}
			tok.setDateStatus(true);
			tok.setTermBuffer(dateBuild.toString().toCharArray());
		}
		return retVal;
	}
	
	/**
	 * 
	 * @param tok
	 * @return
	 */
	private boolean isNum(Token tok)
	{
		boolean retVal = false;
		String word =  tok.toString();
		
		word = word.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,"");
		word = word.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
		
		Matcher matcher= num_pattern.matcher(word);
		boolean isADBC=false;
		boolean isAMPM=false;
		boolean isAM=true;
		
		if(word.matches(REGEXCONSTANTS.REGEX_NUMBERS_WITH_SUFFIX_MAYBE))
		{
			matcher.find();
			if(matcher.end() != word.length())
			{
				String part1= word.substring(0,matcher.end());
				String part2= word.substring(matcher.end());
				
				if(part2.matches(REGEXCONSTANTS.REGEX_YEAR_SUFFIX))
				{
					isADBC=true;
					if(part2.equalsIgnoreCase("bc"))  isAD=false;
				}
				else if(part2.matches(REGEXCONSTANTS.REGEX_NUMBER_SUFFIX))
				{
					// nobody ever said 1998th etc. etc.
					if(Integer.valueOf(part1) > 31) return false; 
				}
				else if(part2.matches(REGEXCONSTANTS.REGEX_TIME_SUFFIX))
				{
					isAMPM=true;
					if(part2.equalsIgnoreCase("pm")) isAM=false;
				}
				else
				{
					return false;
				}
				word = word.substring(0,matcher.end());
			}
			
			// too BIG a number -- FIXME must let number format exception
			if(word.length() > 4) return false;
			
			extractPunctuation(tok.toString());
			
			if(isADBC)
			{
				year=word;
				retVal=true;
			}
			else if(isAMPM)
			{
				tok.setDateStatus(true);
				tok.setTermBuffer(constructClock(isAM,word).toCharArray());
				retVal=false;
			}
			else
			{
				
			
				/*if(!year.isEmpty() && word.length() >2)
				{
					System.out.println(word);
					return false;
				}*/
				retVal=ascertain_dayORYearORHour(word,tok);
			}
		}
		return retVal;
	}

	/**
	 * 
	 * @param word
	 * @param isADBC_known
	 */
	private boolean ascertain_dayORYearORHour(String word,Token tok)
	{
		boolean retVal = false;
		boolean isADBC=false;
		boolean isAMPM=false;
		boolean isAM=true;
		
		 String isAD_BC_AM_PM="";
		
		//System.out.println("Got "+word+" need to ascertain if year or what not!");
		Token next_tok = stream.lookForward();
		if(next_tok != null)
		{
			isAD_BC_AM_PM= next_tok.toString();
			isAD_BC_AM_PM=isAD_BC_AM_PM.replaceAll(REGEXCONSTANTS.REGEX_PUNC_REMOVER,"");
			isAD_BC_AM_PM=isAD_BC_AM_PM.replaceAll(REGEXCONSTANTS.REGEX_WORD_BEGIN_PUNC_REMOVE,"");
			if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_YEAR_SUFFIX))
			{
				isADBC=true;
				if(isAD_BC_AM_PM.equalsIgnoreCase("bc")) isAD= false;
				extractPunctuation(stream.next().toString());
				stream.remove();
			}
			else if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_TIME_SUFFIX))
			{
			
				try
				{
					// to handle cases like 40 hours of work
					if(word.length() < 4) 
					{
						return false;
					}
				}
				catch(NumberFormatException nex)
				{
					nex.printStackTrace();
					return false;
				}
				
				isAMPM=true;
				if(isAD_BC_AM_PM.equalsIgnoreCase("pm")) isAM=false;
				extractPunctuation(next_tok.toString());
				stream.next();
				if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_TIME_SUFFIX_EXT))
				{
					if(stream.lookForward().toString().matches("^(?i)time$"))
					{
						
						stream.remove();
						extractPunctuation(stream.lookForward().toString());
						stream.next(); stream.remove();
					}
					else
					{
						stream.remove();
						isAMPM=false; // we have a false positive need to get rid of that
					}
				}
				else
				{
					stream.remove();
				}
				
			}
			else if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_OCLOCK))
			{
				isAMPM=true;
				stream.next();
				stream.remove();
			}
			if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_TIME_RANGE))
			{
				isAMPM=true;
				next_tok.setTermBuffer(isAD_BC_AM_PM.split("/")[1].toCharArray());
			}
		}
		
	
		
		/** if AM and PM is not known**/
		if(!isAMPM && !isADBC && word.matches(REGEXCONSTANTS.REGEX_MILITARY_TIMINGS))
		{
			if(isAD_BC_AM_PM.matches(REGEXCONSTANTS.REGEX_TO_OR_AND))
			{
				
				isAMPM=true;
			}
			else
			{
				return false; // avoid random 0635
			}
		}
		
		if(isAMPM)
		{
			tok.setDateStatus(true);
			if(word.length() > 2) // military style 0500 hrs|gmt some thing like that
			{
				String[] comp={word.substring(0,2),word.substring(2)};
				tok.setTermBuffer(constructClock(isAM,comp).toCharArray());
			}
			else
			{
				tok.setTermBuffer(constructClock(isAM,word).toCharArray());
			}	
			return false;
		}
		else if(isADBC)
		{
			year=word;
			retVal=true;
		}
		else // could be Year or a Day of a month
		{
			try
			{
				// news cannot refer to dates beyond this, right? 
				if(Integer.valueOf(word) > 2050)
				{
					return false;
				}
				// could it be a year value ?
				else if(Integer.valueOf(word) > 31)
				{
					if(word.length() < 4) // something like 50 days
						return false;	
					if(!year.isEmpty())
					{
						next_has_year=word;
						return false;
					}
					year+=word;
					retVal=true;
				}
				else // number is less than 31
				{
					day = word;
					if(Integer.valueOf(day)<10)
						day="0"+day;
					
					retVal=true;
				}
			}
			catch(NumberFormatException nex)
			{
				System.out.println("Number format exception"+word);
				nex.printStackTrace();
				retVal=false;
			}
		}
		return retVal;
	}

	/**
	 * 
	 * @param word
	 */
	private void extractPunctuation(String word)
	{
		if(word.matches(REGEXCONSTANTS.REGEX_IS_WORD_PUNCTUATED))
		{
			punc_token=word.substring(word.length()-1);
			is_punctuated=true;
		}
		else
		{
			is_punctuated=false;
		}
	}
	
	@Override
	public TokenStream getStream() 
	{
		return stream;
	}
}
