package com.sun.tranquilo.datatype;

/** base class of FloatType and DoubleType
 */
abstract class FloatingNumberType extends ConcreteType implements Comparator
{
	protected FloatingNumberType( String typeName ) { super(typeName); }
	
	final protected boolean checkFormat( String lexicalValue, ValidationContextProvider context )
	{// FloatType and DoubleType checks format by trying to convert it to value object
		return convertToValue(lexicalValue,context)!=null;
	}
	
	protected static boolean isDigitOrPeriodOrSign( char ch )
	{
		if( '0'<=ch && ch<='9' )	return true;
		if( ch=='+' || ch=='-' || ch=='.' )	return true;
		return false;
	}
	
	public final int compare( Object lhs, Object rhs )
	{// float and double type has total order.
		// implementation of Float.compareTo/Double.compareTo is
		// consistent with the spec
		int r = ((Comparable)lhs).compareTo(rhs);
		if(r<0)	return -1;
		if(r>0)	return +1;
		return 0;
	}

	public final int isFacetApplicable( String facetName )
	{
		// TODO : should we allow scale facet, or not?
		if( facetName.equals(FACET_PATTERN)
		||	facetName.equals(FACET_ENUMERATION)
		||	facetName.equals(FACET_MAXINCLUSIVE)
		||	facetName.equals(FACET_MININCLUSIVE)
		||	facetName.equals(FACET_MAXEXCLUSIVE)
		||	facetName.equals(FACET_MINEXCLUSIVE) )
			return APPLICABLE;
		else
			return NOT_ALLOWED;
	}

}