package com.sun.tranquilo.verifier.regexp;

import com.sun.tranquilo.util.StartTagInfo;
import com.sun.tranquilo.grammar.ExpressionPool;

/**
 * StartTagInfo plus AttributeTokens.
 */
public class StartTagInfoEx extends StartTagInfo
{
	public final AttributeToken[] attTokens;
	
	public StartTagInfoEx( StartTagInfo base, REDocumentDeclaration docDecl )
	{
		super( base.namespaceURI, base.localName, base.qName,
			   base.attributes, base.context );
		
		attTokens = new AttributeToken[attributes.getLength()];
		for( int i=0; i<attTokens.length; i++ )
			attTokens[i] = new AttributeToken(
				docDecl.getPool(),
				attributes.getURI(i),
				attributes.getLocalName(i),
				attributes.getValue(i),
				context, docDecl.getResidualCalculator() );
	}
}