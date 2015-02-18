package edu.buffalo.cse.irf14.index;


public enum IndexBundleType
{
	PLACE,
	CATEGORY,
	TERM,
	AUTHOR,
	NONE;
	
	public static IndexBundleType toIndexBundleType(String str)
	{
		try
		{
			return valueOf(str);
		}
		catch(Exception ex)
		{
			return NONE;
		}
	}
}
