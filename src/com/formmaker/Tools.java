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
}
