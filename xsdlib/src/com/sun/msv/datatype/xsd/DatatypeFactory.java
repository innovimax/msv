/*
 * Tranquilo : RELAX Verifier           written by Kohsuke Kawaguchi
 *                                           k-kawa@bigfoot.com
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.sun.tranquilo.datatype;

import java.util.Map;

/**
 * DataType object factory
 *
 * <p>
 * Applications use this class to get a reference to built-in DataType object.
 *
 */
public class DataTypeFactory
{
	/** a map that contains all built in types */
	private static final Map builtinType = createStaticTypes();
	
	
	/**
	 * derives a new type by list.
	 *
	 * See http://www.w3.org/TR/xmlschema-2#derivation-by-list for
	 * what "derivation by list" means.
	 *
	 * @param newTypeName
	 *		name of the new type. it can be set to null for indicating an anonymous type.
	 * @param itemType
	 *		Type of the list item. It must be an atom type. You cannot use
	 *		your own DataType implementation here.
	 *
	 * @exception BadTypeException
	 *		this exception is thrown when the derivation is illegal.
	 *		For example, when you try to derive a type from non-atom type.
	 */
	public static DataType deriveByList( String newTypeName, DataType itemType )
		throws BadTypeException
	{
		return new ListType(newTypeName,(DataTypeImpl)itemType);
	}
	
	/**
	 * derives a new type by union.
	 *
	 * See http://www.w3.org/TR/xmlschema-2#derivation-by-union for
	 * what "derivation by union" means.
	 * 
	 * @param newTypeName
	 *		name of the new type. it can be set to null for indicating an anonymous type.
	 * @param memberTypes
	 *		Types of the union member. It can be any type that implements DataType.
	 *
	 * @exception BadTypeException
	 *		this exception is thrown when the derivation is illegal.
	 */
	public static DataType deriveByUnion( String newTypeName, DataType[] memberTypes )
		throws BadTypeException
	{
		return new UnionType(newTypeName,(DataTypeImpl[])memberTypes);
	}
	
	/**
	 * obtain a built-in DataType object from its name
	 * 
	 * @return null	if DataType is not found
	 */
	public static DataType getTypeByName( String dataTypeName )
	{
		return (DataType)builtinType.get(dataTypeName);
	}
	
	private static void add( Map m, DataType type )
	{
		if( m.containsKey(type.getName()) )
			// this error is considered as an assertion,
			// since this object doesn't allow external programs to
			// add types to the object.
			throw new IllegalArgumentException("multiple definition");
		
		m.put( type.getName(), type );
	}
	
	/** creates a map that contains all static types */
	private static Map createStaticTypes()
	{
		try
		{
			Map m = new java.util.HashMap();

			// missing types are noted inline.

			add( m, StringType.theInstance );
			add( m, BooleanType.theInstance );
			add( m, DecimalType.theInstance );
			add( m, FloatType.theInstance );
			add( m, DoubleType.theInstance );
			add( m, DurationType.theInstance );
			add( m, DateTimeType.theInstance );
			add( m, TimeType.theInstance );
			add( m, DateType.theInstance );
			add( m, YearMonthType.theInstance );
			add( m, YearType.theInstance );
			add( m, MonthDayType.theInstance );
			add( m, DayType.theInstance );
			add( m, MonthType.theInstance );
			add( m, HexBinaryType.theInstance );
			add( m, Base64BinaryType.theInstance );
			add( m, UriReferenceType.theInstance );
	//		ID, IDREF
			add( m, EntityType.theInstance );
			add( m, QnameType.theInstance );
			add( m, NormalizedStringType.theInstance );
			add( m, TokenType.theInstance );
			add( m, LanguageType.theInstance );
	//		IDREFS
			add( m, new ListType("ENTITIES",EntityType.theInstance) );
			add( m, NmtokenType.theInstance );
			add( m, new ListType("NMTOKENS",NmtokenType.theInstance) );
			add( m, NameType.theInstance );
			add( m, NcnameType.theInstance );
	//		NOTATION
			add( m, new StringType("NOTATION", WhiteSpaceProcessor.theCollapse) );
			
			add( m, IntegerType.theInstance );
			add( m, NonPositiveIntegerType.theInstance );
			add( m, NegativeIntegerType.theInstance );
			add( m, LongType.theInstance );
			add( m, IntType.theInstance );
			add( m, ShortType.theInstance );
			add( m, ByteType.theInstance );
			add( m, NonNegativeIntegerType.theInstance );
			add( m, UnsignedLongType.theInstance );
			add( m, UnsignedIntType.theInstance );
			add( m, UnsignedShortType.theInstance );
			add( m, UnsignedByteType.theInstance );
			add( m, PositiveIntegerType.theInstance );
			return m;
		}
		catch( BadTypeException bte )
		{// assertion failed
			throw new IllegalStateException();
		}
	}
}