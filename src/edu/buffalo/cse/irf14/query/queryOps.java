package edu.buffalo.cse.irf14.query;

public enum queryOps 
{
	
	AND, 
	OR,
	NOT,
	BAD_OPS;
	public static queryOps toOps(String str)
    {
        try
        {
            return valueOf(str);
        } 
        catch (Exception ex) 
        {
            return BAD_OPS;
        }
    }   	
}
