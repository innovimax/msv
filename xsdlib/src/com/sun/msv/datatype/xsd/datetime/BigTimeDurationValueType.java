package com.sun.tranquilo.datatype.datetime;

import java.math.BigInteger;
import java.math.BigDecimal;
import com.sun.tranquilo.datatype.Comparator;

public class BigTimeDurationValueType implements ITimeDurationValueType
{
	protected BigInteger year;
	protected BigInteger month;
	protected BigInteger day;
	protected BigInteger hour;
	protected BigInteger minute;
	protected BigDecimal second;

	private static final BigDateTimeValueType[] testInstance =
		new BigDateTimeValueType[]{
			new BigDateTimeValueType(
				new BigInteger("1696"), 8/*Sep*/, 0/*1st*/, 0,0, new BigDecimal(0), TimeZone.GMT ),
			new BigDateTimeValueType(
				new BigInteger("1697"), 1/*Feb*/, 0/*1st*/, 0,0, new BigDecimal(0), TimeZone.GMT ),
			new BigDateTimeValueType(
				new BigInteger("1903"), 2/*Mar*/, 0/*1st*/, 0,0, new BigDecimal(0), TimeZone.GMT ),
			new BigDateTimeValueType(
				new BigInteger("1903"), 6/*Jul*/, 0/*1st*/, 0,0, new BigDecimal(0), TimeZone.GMT ) };
																				

	public boolean equals( Object o )
	{ return equals( (ITimeDurationValueType)o ); }
	public boolean equals( ITimeDurationValueType o )
	{
		return compare(o)==Comparator.EQUAL;
	}
	
	private BigInteger nullAsZero(BigInteger o)
	{
		if(o==null)	return BigInteger.ZERO;
		else		return o;
	}
	
	public int hashCode()
	{// hashCode is very complex because it has to consistent with the behavior of equals method.
		return nullAsZero(day).multiply(Util.the24)
			.add( nullAsZero(hour) ).multiply(Util.the60)
				.add( nullAsZero(minute) ).hashCode();
	}

	public int compare( ITimeDurationValueType o )
	{
		if(!(o instanceof BigTimeDurationValueType) )
			o = o.getBigValue();

		return compare( this, (BigTimeDurationValueType)o );
	}
	
	static private int compare( BigTimeDurationValueType lhs, BigTimeDurationValueType rhs )
	{
		boolean less=false,greater=false,noDeterminate=false;
		
		for( int i=0; i<testInstance.length; i++ )
		{
			BigDateTimeValueType l = (BigDateTimeValueType)testInstance[i].add(lhs);
			BigDateTimeValueType r = (BigDateTimeValueType)testInstance[i].add(rhs);
			
			int v = BigDateTimeValueType.compare(l,r);
			
			if(v<0)						less=true;
			if(v>0)						greater=true;
			if(v==0)
			{
				if(!l.equals(r))		noDeterminate=true;
			}
		}
		
		if(noDeterminate)		return Comparator.UNDECIDABLE;
		if(less && greater)		return Comparator.UNDECIDABLE;
		if(less)				return Comparator.LESS;		// lhs<rhs
		if(greater)				return Comparator.GREATER;	// lhs>rhs
		return Comparator.EQUAL;
	}

	public BigTimeDurationValueType getBigValue() { return this; }

	public BigTimeDurationValueType(
		BigInteger year, BigInteger month, BigInteger day,
		BigInteger hour, BigInteger minute, BigDecimal second )
	{
		this.year	= year!=null?year:BigInteger.ZERO;
		this.month	= month!=null?month:BigInteger.ZERO;
		this.day	= day!=null?day:BigInteger.ZERO;
		this.hour	= hour!=null?hour:BigInteger.ZERO;
		this.minute	= minute!=null?minute:BigInteger.ZERO;
		this.second	= second!=null?second:new BigDecimal(0);
	}
	
	public static BigTimeDurationValueType fromMinutes( int minutes )
	{ return fromMinutes(Util.int2bi(minutes)); }
	public static BigTimeDurationValueType fromMinutes( BigInteger minutes )
	{ return new BigTimeDurationValueType(null,null,null,null,minutes,null); }
	
}