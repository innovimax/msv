package com.sun.tranquilo.grammar.xmlschema;

public class GroupDeclExp extends RedefinableExp {
	
	public GroupDeclExp( String typeLocalName ) {
		super(typeLocalName);
	}
	
	/** clone this object. */
	public RedefinableExp getClone() {
		RedefinableExp exp = new GroupDeclExp(super.name);
		exp.redefine(this);
		return exp;
	}
}