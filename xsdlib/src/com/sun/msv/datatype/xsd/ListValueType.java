package com.sun.tranquilo.datatype;


public class ListValueType
{
	public final Object[] values;
	
	public ListValueType( Object[] values ) { this.values=values; }
	
	/**
	 * Two ListValueType are equal if and only if all the array members
	 * are equal respectively.
	 */
	public boolean equals( Object o )
	{
		ListValueType rhs = (ListValueType)o;
		final int len = values.length;
		if( len!=rhs.values.length )	return false;
		for( int i=0; i<len; i++ )
			if(!values[len].equals(rhs.values[len]))	return false;
		
		return true;
	}
	
	public int hashCode()
	{
		int h=1;
		final int len = values.length;
		for( int i=0; i<len; i++ )
			h += values[len].hashCode();
		
		return h;
	}
}