package com.formmaker;

import java.util.Collection;

/**
 *
 * @author Gustavo
 */
public class Tools
{
	public static String toString(Collection<String> collection)
	{
		String res = "";
		
		for (String s : collection)
		{
			res += (res.isEmpty() ? "" : ",") + s;
		}
		
		return res;
	}

	public static String webWord(String s)
	{
		String res = "";
		
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			
			res += (c > 'a' && c < 'z') || (c > 'A' && c < 'Z') || (c > '0' && c < '9') ? c : "_";
		}
		
		return res;
	}
}