package com.sun.tranquilo.datatype;

import com.sun.tranquilo.datatype.datetime.ISO8601Parser;
import com.sun.tranquilo.datatype.datetime.IDateTimeValueType;

/**
 * "day" type.
 * 
 * See http://www.w3.org/TR/xmlschema-2/#day for the spec
 */
public class DayType extends DateTimeBaseType
{
	public static final DayType theInstance = new DayType();
	private DayType() { super("day"); }

	protected void runParserL( ISO8601Parser p ) throws Exception
	{
		p.dayTypeL();
	}

	protected IDateTimeValueType runParserV( ISO8601Parser p ) throws Exception
	{
		return p.dayTypeV();
	}
}