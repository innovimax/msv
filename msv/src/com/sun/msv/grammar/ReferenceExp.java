package com.sun.tranquilo.grammar;

/**
 * Reference to the other expression.
 * 
 * <p>
 * In RELAX grammar, this class is used as a base class of elementRule reference
 * and hedgeRule reference.
 * TREX uses this class directly.
 * 
 * <p>
 * This object is created and controlled by TREXGrammar/RELAXModule object,
 * rather than ExpressionPool. Therefore, this object is not a subject to unification.
 */
public class ReferenceExp extends Expression
{
	/** child expression. Due to the possible forward reference,
	 * this variable is not available when the object is instanciated.
	 * 
	 * Actual expression will be set once if the definition is parsed.
	 */
	public Expression exp;
	
	/** name of the referenced expression.
	 * 
	 * can be null for anonymously referenced expression.
	 */
	public final String name;
	
	public ReferenceExp( String name )
	{
		super( hashCode(name==null?"":name,HASHCODE_REF) );
		this.name = name;
	}
	
	public boolean equals( Object o )
	{
		return this==o;
	}
	
	protected boolean calcEpsilonReducibility()
	{
		if(exp==null)
//			// actual expression is not supplied yet.
//			// actual definition of the referenced expression must be supplied
//			// before any computation over the grammar.
//			throw new Error();	// assertion failed.
			return false;
		// this method can be called while parsing a grammar.
		// in that case, epsilon reducibility is just used for approximation.
		// therefore we can safely return false.
		
		return exp.isEpsilonReducible();
	}
	
	// derived class must be able to behave as a ReferenceExp
	public final Object visit( ExpressionVisitor visitor )				{ return visitor.onRef(this); }
	public final Expression visit( ExpressionVisitorExpression visitor ){ return visitor.onRef(this); }
	public final boolean visit( ExpressionVisitorBoolean visitor )		{ return visitor.onRef(this); }
	public final void visit( ExpressionVisitorVoid visitor )			{ visitor.onRef(this); }
}