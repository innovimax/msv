package com.sun.tranquilo.datatype;

/**
 * "unsignedInt" and unsignedInt-derived types
 * 
 * See http://www.w3.org/TR/xmlschema-2/#unsignedInt for the spec
 *
 * We don't have language support for unsigned datatypes, so things are not so easy.
 * UnsignedIntType uses a LongType as a base implementation, for the convenience and
 * faster performance.
 */
public class UnsignedIntType extends LongType
{
	public static final UnsignedIntType theInstance = new UnsignedIntType();
	private UnsignedIntType() { super("unsignedInt"); }

    /** upper bound value. this is the maximum possible valid value as an unsigned int */
    private static final long upperBound = 4294967295L;
	
	public Object convertToValue( String lexicalValue, ValidationContextProvider context )
	{
		// Implementation of JDK1.2.2/JDK1.3 is suitable enough
		try
		{
			Long v = (Long)super.convertToValue(lexicalValue,context);
			if( v==null )						return null;
			if( v.longValue()<0 )               return null;
			if( v.longValue()>upperBound )      return null;
			return v;
		}
		catch( NumberFormatException e )
		{
			return null;
		}
	}
}