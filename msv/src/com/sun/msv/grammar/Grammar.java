/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.grammar;

/**
 * base interface of the "grammar".
 * 
 * This interface characterizes very basic part of grammar.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public interface Grammar {
	/**
	 * gets top-level expression.
	 * This expression shall be the constraint over the document element.
	 * Never return null.
	 */
	Expression getTopLevel();
	
	/**
	 * gets ExpressionPool object which was used to construct this grammar.
	 * Never return null.
	 */
	ExpressionPool getPool();
}