package com.sun.tranquilo.datatype;

/**
 * Unicode-related utility functions
 */
public class UnicodeUtil
{
	/**
	 * Count the number of "character" in Unicode string.
	 * 
	 * "character" here is defined by http://www.w3.org/TR/REC-xml#NT-Char
	 * Basically, all the work this function will do is
	 * to take care of surrogate pairs.
	 * 
	 * If string contains any char ('char' in Java datatype) other than those
	 * allowed in XML spec, the behavior is undefined. However, we can safely
	 * assume that XML parser performs this check before we receive the value.
	 */
	public static int countLength( String str )
	{
		final int len = str.length();
		int count=0;
		
		for( int i=0; i<len; i++ )
		{
			final char ch = str.charAt(i);
			// skip the first half of surrogate pair
			// we can safely assume that the last half of surrogate pair follows.
			// because that's a requirement for XML parser
			if( 0xD800 <= ch && ch < 0xDC00 )	continue;
			count++;
		}
		
		return count;
	}
}