package com.sun.tranquilo.reader.datatype.xsd;

import com.sun.tranquilo.datatype.BadTypeException;
import com.sun.tranquilo.datatype.DataType;
import com.sun.tranquilo.datatype.StringType;
import com.sun.tranquilo.grammar.Expression;
import com.sun.tranquilo.reader.State;
import com.sun.tranquilo.reader.ExpressionWithChildState;
import com.sun.tranquilo.reader.datatype.TypeOwner;

/**
 * State which has at most one TypeState as its child.
 */
abstract class TypeWithOneChildState extends TypeState implements TypeOwner
{
	protected DataType type;

	TypeWithOneChildState( XSDVocabulary voc )	{ super(voc); }

	/** receives a Pattern object that is contained in this element. */
	public void onEndChild( DataType child )
	{
		if( type!=null )
			reader.reportError( reader.ERR_MORE_THAN_ONE_CHILD_TYPE );
			// recover by ignoring this child
		else
			type = child;
	}
	
	protected final DataType makeType() throws BadTypeException
	{
		if( type==null )
		{
			reader.reportError( reader.ERR_MISSING_CHILD_TYPE );
			return StringType.theInstance;	// recover by supplying a dummy DataType
		}
		return annealType(type);
	}

	/**
	 * performs final wrap-up and returns a fully created DataType object
	 * that represents this element.
	 */
	protected DataType annealType( DataType dt ) throws BadTypeException
	{
		// default implementation do nothing.
		return dt;
	}
}