/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.reader.trex.ng;

import com.sun.msv.grammar.*;
import com.sun.msv.grammar.util.ExpressionWalker;
import com.sun.msv.grammar.util.NameClassCollisionChecker;
import org.xml.sax.Locator;
import java.util.Vector;

/**
 * Checks RELAX NG contextual restrictions defined in the section 7.
 * 
 * <p>
 * ExpressionWalker is used to walk the content model thoroughly.
 * Depending on the current context, different walkers are used so that
 * we can detect contextual restrictions properly.
 * 
 * <p>
 * For each ElementExp and AttributeExp, its name class is checked to detect
 * the constraint set out in the section 7.1.6. Also, a set is used to avoid
 * redundant checks.
 * 
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class RestrictionChecker {
	
	private RestrictionChecker( RELAXNGReader _reader ) {
		this.reader = _reader;
	}
	
	public static void check( RELAXNGReader reader ) {
		reader.getGrammar().visit(new RestrictionChecker(reader).inStart);
	}
	
	/** Reader object to which errors are reported. */
	private final RELAXNGReader reader;
	
	/**
	 * The source location of this expression should be also reported in case of error.
	 */
	private Expression errorContext;
	
	private void reportError( Expression exp, String errorMsg ) {
		reportError(exp,errorMsg,null);
	}
	private void reportError( Expression exp, String errorMsg, Object[] args ) {
		reader.reportError(
			new Locator[]{
				reader.getDeclaredLocationOf(exp),
				reader.getDeclaredLocationOf(errorContext)
			}, errorMsg, args );
	}
	
	/** Visited ElementExp/AttributeExps. */
	private final java.util.Set visitedExps = new java.util.HashSet();

	/** Object that checks duplicate attributes in a content model. */
	private DuplicateAttributesChecker attDupChecker;
	
	/** Object that checks conflicting elements in interleave. */
	private DuplicateElementsChecker elemDupChecker;
	
	/** Object that checks conflicting values in &lt;interleave> in &lt;list>. */
	private final DuplicateValueChecker valueDupChecker = new DuplicateValueChecker();
	
/*
	
	content model checker
	=====================
*/
	
	/**
	 * The base class of all other context-specific checker.
	 * This class performs the context switching.
	 */
	private class DefaultChecker extends ExpressionWalker {
		public void onElement( ElementExp exp ) {
			if( !visitedExps.add(exp) )		return;
			
			// check conflicting elements
			if(elemDupChecker!=null)
				elemDupChecker.add(exp);
			
			// push context element,
			final Expression oldContext = errorContext;
			final DuplicateAttributesChecker oldADC = attDupChecker;
			final DuplicateElementsChecker oldEDC = elemDupChecker;
			
			errorContext = exp;
			attDupChecker = new DuplicateAttributesChecker();
			elemDupChecker = new DuplicateElementsChecker();

			exp.getNameClass().visit(inNameClass);	// check the name
			
			// it is important to use the expanded exp because
			// section 7 has to be applied after patterns are expanded.
			exp.contentModel.getExpandedExp(reader.pool).visit(inElement);
			errorContext = oldContext;
			attDupChecker = oldADC;
			elemDupChecker = oldEDC;
		}
		public void onAttribute( AttributeExp exp ) {
			if( !visitedExps.add(exp) )		return;
			
			// check duplicate attributes
			attDupChecker.add(exp);
			
			final Expression oldContext = errorContext;
			
			errorContext = exp;
			
			exp.getNameClass().visit(inNameClass);	// check the name
			
			exp.exp.getExpandedExp(reader.pool).visit(inAttribute);
			errorContext = oldContext;
		}
		public void onList( ListExp exp ) {
			exp.exp.visit(inList);
		}
		public void onData( DataExp exp ) {
			exp.except.visit(inExcept);
		}
		public void onChoice( ChoiceExp exp ) {
			if(attDupChecker==null)
				// if a 'choice' appears at the top level,
				// there is no enclosing element, so no attDupChecker is present.
				super.onChoice(exp);
			else {
				int idx = attDupChecker.start();
				exp.exp1.visit(this);
				attDupChecker.endLeftBranch(idx);
				exp.exp2.visit(this);
				attDupChecker.endRightBranch();
			}
		}
		public void onInterleave( InterleaveExp exp ) {
			if(elemDupChecker==null)
				super.onInterleave(exp);
			else {
				int idx = elemDupChecker.start();
				exp.exp1.visit(this);
				elemDupChecker.endLeftBranch(idx);
				exp.exp2.visit(this);
				elemDupChecker.endRightBranch();
			}
		}
		public void onAnyString() {
			super.onAnyString();
		}
	}
	
	/**
	 * Used to visit children of the 'except' clause of data.
	 */
	private final ExpressionWalker inExcept = new DefaultChecker() {
		public void onAttribute( AttributeExp exp ) {
			reportError( exp, ERR_ATTRIBUTE_IN_EXCEPT );
		}
		public void onElement( ElementExp exp ) {
			reportError( exp, ERR_ELEMENT_IN_EXCEPT );
		}
		public void onList( ListExp exp ) {
			reportError( exp, ERR_LIST_IN_EXCEPT );
		}
		public void onAnyString() {
			reportError( null, ERR_TEXT_IN_EXCEPT );
		}
		public void onEpsilon() {
			reportError( null, ERR_EMPTY_IN_EXCEPT );
		}
		public void onSequence( SequenceExp exp ) {
			reportError( exp, ERR_SEQUENCE_IN_EXCEPT );
		}
		public void onInterleave( InterleaveExp exp ) {
			reportError( exp, ERR_INTERLEAVE_IN_EXCEPT );
		}
		public void onOneOrMore( OneOrMoreExp exp ) {
			reportError( exp, ERR_ONEORMORE_IN_EXCEPT );
		}
	};
	
	/**
	 * Used to visit children of group/interleave in oneOrMore in elements.
	 */
	private final ExpressionWalker inGroupInOneOrMoreInElement = new DefaultChecker() {
		public void onAttribute( AttributeExp exp ) {
			reportError( exp, ERR_REPEATED_GROUPED_ATTRIBUTE );
		}
	};
	
	/**
	 * Used to visit children of oneOrMore in elements.
	 */
	private final ExpressionWalker inOneOrMoreInElement = new DefaultChecker() {
		public void onSequence( SequenceExp exp ) {
			exp.visit(inGroupInOneOrMoreInElement);
		}
		public void onInterleave( InterleaveExp exp ) {
			exp.visit(inGroupInOneOrMoreInElement);
		}
	};
	
	/**
	 * Used to visit children of elements.
	 */
	private final ExpressionWalker inElement = new DefaultChecker() {
		public void onOneOrMore( OneOrMoreExp exp ) {
			exp.exp.visit(inOneOrMoreInElement);
		}
	};
	
	/**
	 * Used to visit children of attributes.
	 */
	private final ExpressionWalker inAttribute = new DefaultChecker(){
		public void onElement( ElementExp exp ) {
			reportError( exp, ERR_ELEMENT_IN_ATTRIBUTE );
		}
		public void onAttribute( AttributeExp exp ) {
			reportError( exp, ERR_ATTRIBUTE_IN_ATTRIBUTE );
		}
	};
	
	
	private class ListChecker extends DefaultChecker {
		public void onAttribute( AttributeExp exp ) {
			reportError( exp, ERR_ATTRIBUTE_IN_LIST );
		}
		public void onElement( ElementExp exp ) {
			reportError( exp, ERR_ELEMENT_IN_LIST );
		}
		public void onList( ListExp exp ) {
			reportError( exp, ERR_LIST_IN_LIST );
		}
		public void onAnyString() {
			reportError( null, ERR_TEXT_IN_LIST );
		}
	}
	/**
	 * Used to visit children of interleaves in lists.
	 */
	private final ExpressionWalker inInterleaveInList = new ListChecker() {
		public void onData( DataExp exp ) {
			reportError( exp, ERR_DATA_IN_INTERLEAVE_IN_LIST );
		}
		public void onValue( ValueExp exp ) {
			// check <value> in <interleave>, which can only happen
			// inside <list>.
			valueDupChecker.add(exp);
		}
		public void onInterleave( InterleaveExp exp ) {
			int idx = valueDupChecker.start();
			exp.exp1.visit(this);
			valueDupChecker.endLeftBranch(idx);
			exp.exp2.visit(this);
			valueDupChecker.endRightBranch();
		}
	};
	/**
	 * Used to visit children of lists.
	 */
	private final ExpressionWalker inList = new ListChecker() {
		public void onInterleave( InterleaveExp exp ) {
			valueDupChecker.reset();
			inInterleaveInList.onInterleave(exp);
		}
	};
	
	/**
	 * Used to visit the start pattern.
	 */
	private final ExpressionWalker inStart = new DefaultChecker() {
		public void onAttribute( AttributeExp exp ) {
			reportError( exp, ERR_ATTRIBUTE_IN_START );
		}
		public void onList( ListExp exp ) {
			reportError( exp, ERR_LIST_IN_START );
		}
		public void onAnyString() {
			reportError( null, ERR_TEXT_IN_START );
		}
		public void onEpsilon() {
			reportError( null, ERR_EMPTY_IN_START );
		}
		public void onSequence( SequenceExp exp ) {
			reportError( exp, ERR_SEQUENCE_IN_START );
		}
		public void onInterleave( InterleaveExp exp ) {
			reportError( exp, ERR_INTERLEAVE_IN_START );
		}
		public void onData( DataExp exp ) {
			reportError( exp, ERR_DATA_IN_START );
		}
		public void onValue( ValueExp exp ) {
			reportError( exp, ERR_DATA_IN_START );
		}
		public void onOneOrMore( OneOrMoreExp exp ) {
			reportError( exp, ERR_ONEORMORE_IN_START );
		}
	};
	

/*
	
	name class checker
	==================
*/
	
	class NameClassWalker implements NameClassVisitor {
		public Object onAnyName( AnyNameClass nc ) { return null; }
		public Object onSimple( SimpleNameClass nc ) { return null; }
		public Object onNsName( NamespaceNameClass nc ) { return null; }
		public Object onNot( NotNameClass nc ) { throw new Error(); }	// should not be used
		public Object onDifference( DifferenceNameClass nc ) {
			nc.nc1.visit(this);
			if(nc.nc1 instanceof AnyNameClass)
				nc.nc2.visit(inAnyNameClass);
			else
			if(nc.nc1 instanceof NamespaceNameClass)
				nc.nc2.visit(inNsNameClass);
			else
				throw new Error();	// this is not possible in RELAX NG.
			return null;
		}
		public Object onChoice( ChoiceNameClass nc ) {
			nc.nc1.visit(this);
			nc.nc2.visit(this);
			return null;
		}
	}
	
	/**
	 * Used to visit name classes.
	 */
	private final NameClassWalker inNameClass = new NameClassWalker();
		
	/**
	 * Used to visit children of AnyNameClass
	 */
	private final NameClassVisitor inAnyNameClass = new NameClassWalker(){
		public Object onAnyName( AnyNameClass nc ) {
			reportError(null,ERR_ANYNAME_IN_ANYNAME);
			return null;
		}
	};
	
	/**
	 * Used to visit children of NamespaceNameClass
	 */
	private final NameClassVisitor inNsNameClass = new NameClassWalker(){
		public Object onAnyName( AnyNameClass nc ) {
			reportError(null,ERR_ANYNAME_IN_NSNAME);
			return null;
		}
		public Object onNsName( NamespaceNameClass nc ) {
			reportError(null,ERR_NSNAME_IN_NSNAME);
			return null;
		}
	};

	
	
	
/*
	
	duplicate attributes check
	==========================
*/
	protected abstract class DuplicateNameChecker {
		/** ElementExps will be added into this array. */
		protected NameClassAndExpression[] exps = new NameClassAndExpression[16];
		/** Number of items in the atts array. */
		protected int expsLen=0;

		/**
		 * areas.
		 * 
		 * <p>
		 * An area is a range of index designated by the start and end.
		 * 
		 * Areas are stored as:
		 * <pre>{ start, end, start, end, ... }</pre>
		 * 
		 * <p>
		 * The start method gives the index. The endLeftBranch method creates
		 * an area by using the start index given by the start method.
		 * The endRightBranch method will remove the area.
		 * 
		 * <p>
		 * When testing duplicate attributes, areas are created by ChoiceExp
		 * and used to exclude test candidates (as two attributes can share the
		 * same name if they are in different branches of choice.)
		 * 
		 * <p>
		 * When testing duplicate elements, areas are created by InterleaveExp
		 * and used to include test candidates (as two elements cannot share
		 * the same name if they are in different branches of interleave.)
		 */
		protected int[] areas = new int[8];
		protected int areaLen=0;
		
		/**
		 * Adds newly found element or attribute.
		 */
		public void add( NameClassAndExpression exp ) {
			check(exp);	// perform duplication check
			
			// add it to the array
			if(exps.length==expsLen) {
				// expand buffer
				NameClassAndExpression[] n = new NameClassAndExpression[expsLen*2];
				System.arraycopy(exps,0,n,0,expsLen);
				exps = n;
			}
			exps[expsLen++] = exp;
		}
		
		/**
		 * tests a given exp against existing expressions (which are stored in
		 * the exps field.)
		 */
		protected abstract void check( NameClassAndExpression exp );
		
		public int start() {
			return expsLen;
		}
		
		public void endLeftBranch( int start ) {
			if( areas.length==areaLen ) {
				// expand buffer
				int[] n = new int[areaLen*2];
				System.arraycopy(areas,0,n,0,areaLen);
				areas = n;
			}
			// create an area
			areas[areaLen++] = start;
			areas[areaLen++] = expsLen;
		}
		
		public void endRightBranch() {
			// remove an area
			areaLen-=2;
		}

		
		/**
		 * Name class checker object. One object is reused throughout the test.
		 */
		private final NameClassCollisionChecker checker =
			new NameClassCollisionChecker();
		
		/** Tests two name classes to see if they collide. */
		protected void check(
			NameClassAndExpression newExp,
			NameClassAndExpression oldExp ) {
			
			if(checker.check( newExp.getNameClass(), oldExp.getNameClass() )) {
				// two attributes/elements collide
				reader.reportError( 
					new Locator[]{
						reader.getDeclaredLocationOf(errorContext),	// the parent element
						reader.getDeclaredLocationOf(newExp),
						reader.getDeclaredLocationOf(oldExp)},
					getErrorMessage(), null );
			}
		}
		
		/** Gets the error message resource name. */
		protected abstract String getErrorMessage();
	}
	
	private class DuplicateElementsChecker extends DuplicateNameChecker {
		protected void check( NameClassAndExpression exp ) {
			// check this element with elements in the area
			for( int i=0; i<areaLen; i+=2 )
				for( int j=areas[i]; j<areas[i+1]; j++ )
					this.check(exp,exps[j]);
		}
		
		protected String getErrorMessage() { return ERR_DUPLICATE_ELEMENTS; }
	}
	
	private class DuplicateAttributesChecker extends DuplicateNameChecker {
		protected void check( NameClassAndExpression exp ) {
			// check the consistency with attributes NOT in the area.
			int j=0;
			for( int i=0; i<areaLen; i+=2 ) {
				while( j<areas[i] )
					this.check(exp,exps[j++]);
				j=areas[i+1];
			}
			while(j<expsLen)
				this.check(exp,exps[j++]);
		}
		
		protected String getErrorMessage() { return ERR_DUPLICATE_ATTRIBUTES; }
	}
	
	/**
	 * Checks &lt;value>s in &lt;interleave> in &lt;list>.
	 * 
	 * The algorithm here is basically the same as DuplicateNameChecker.
	 */
	private class DuplicateValueChecker {
		
		/** ValueExps will be added into this array. */
		protected ValueExp[] exps = new ValueExp[16];
		/** Number of items in the atts array. */
		protected int expsLen=0;

		/** areas. */
		protected int[] areas = new int[8];
		protected int areaLen=0;
		
		/** Adds newly found value. */
		public void add( ValueExp exp ) {
			check(exp);	// perform duplication check
			
			// add it to the array
			if(exps.length==expsLen) {
				// expand buffer
				ValueExp[] n = new ValueExp[expsLen*2];
				System.arraycopy(exps,0,n,0,expsLen);
				exps = n;
			}
			exps[expsLen++] = exp;
		}
		
		/** Tests a new value against existing values. */
		private void check( ValueExp exp ) {
			// make sure that this new exp has the correct type name.
			if( expsLen!=0 && !exps[0].getName().equals(exp.getName()) ) {
				// datatype names are different
				reportError( exp, 
					ERR_DIFFERENT_VALUE_TYPES_IN_INTERLEAVE,
					new Object[]{
						exps[0].getName().localName,
						exp.getName().localName} );
				return;
			}
			
			// check this value with all values in active areas
			for( int i=0; i<areaLen; i+=2 )
				for( int j=areas[i]; j<areas[i+1]; j++ )
					check(exp,exps[j]);
		}
		
		private void check( ValueExp v1, ValueExp v2 ) {
			if( v1.dt.sameValue( v1.value, v2.value ) )
				reportError( v1, ERR_SAME_VALUE_IN_INTERLEAVE );
		}
		
		public int start() { return expsLen; }
		
		public void endLeftBranch( int start ) {
			if( areas.length==areaLen ) {
				// expand buffer
				int[] n = new int[areaLen*2];
				System.arraycopy(areas,0,n,0,areaLen);
				areas = n;
			}
			// create an area
			areas[areaLen++] = start;
			areas[areaLen++] = expsLen;
		}
		
		/** Removes an area */
		public void endRightBranch() { areaLen-=2; }
		
		/**
		 * Resets all stored expressions.
		 * This is just an optimization to keep the array small
		 * by purging unnecessary ValueExps.
		 */
		public void reset() {
			if(areaLen!=0)	throw new Error();	// assertion failed
			expsLen=0;
		}
	}
	
	
// error messages
	
	private static final String ERR_ATTRIBUTE_IN_EXCEPT =
		"RELAXNGReader.AttributeInExcept";
	private static final String ERR_ELEMENT_IN_EXCEPT =
		"RELAXNGReader.ElementInExcept";
	private static final String ERR_LIST_IN_EXCEPT =
		"RELAXNGReader.ListInExcept";
	private static final String ERR_TEXT_IN_EXCEPT =
		"RELAXNGReader.TextInExcept";
	private static final String ERR_EMPTY_IN_EXCEPT =
		"RELAXNGReader.EmptyInExcept";
	private static final String ERR_SEQUENCE_IN_EXCEPT =
		"RELAXNGReader.SequenceInExcept";
	private static final String ERR_INTERLEAVE_IN_EXCEPT =
		"RELAXNGReader.InterleaveInExcept";
	private static final String ERR_ONEORMORE_IN_EXCEPT =
		"RELAXNGReader.OneOrMoreInExcept";
	private static final String ERR_REPEATED_GROUPED_ATTRIBUTE =
		"RELAXNGReader.RepeatedGroupedAttribute";
	private static final String ERR_ELEMENT_IN_ATTRIBUTE =
		"RELAXNGReader.ElementInAttribute";
	private static final String ERR_ATTRIBUTE_IN_ATTRIBUTE =
		"RELAXNGReader.AttributeInAttribute";
	private static final String ERR_ATTRIBUTE_IN_LIST =
		"RELAXNGReader.AttributeInList";
	private static final String ERR_ELEMENT_IN_LIST =
		"RELAXNGReader.ElementInList";
	private static final String ERR_LIST_IN_LIST =
		"RELAXNGReader.ListInList";
	private static final String ERR_TEXT_IN_LIST =
		"RELAXNGReader.TextInList";
	private static final String ERR_ATTRIBUTE_IN_START =
		"RELAXNGReader.AttributeInStart";
	private static final String ERR_LIST_IN_START =
		"RELAXNGReader.ListInStart";
	private static final String ERR_TEXT_IN_START =
		"RELAXNGReader.TextInStart";
	private static final String ERR_EMPTY_IN_START =
		"RELAXNGReader.EmptyInStart";
	private static final String ERR_SEQUENCE_IN_START =
		"RELAXNGReader.SequenceInStart";
	private static final String ERR_INTERLEAVE_IN_START =
		"RELAXNGReader.InterleaveInStart";
	private static final String ERR_DATA_IN_START =
		"RELAXNGReader.DataInStart";
	private static final String ERR_ONEORMORE_IN_START =
		"RELAXNGReader.OneOrMoreInStart";
	private static final String ERR_TEXT_IN_INTERLEAVE =
		"RELAXNGReader.TextInInterleave";
	private static final String ERR_DATA_IN_INTERLEAVE_IN_LIST =
		"RELAXNGReader.DataInInterleaveInList";
	
	private static final String ERR_ANYNAME_IN_ANYNAME =
		"RELAXNGReader.AnyNameInAnyName";
	private static final String ERR_ANYNAME_IN_NSNAME =
		"RELAXNGReader.AnyNameInNsName";
	private static final String ERR_NSNAME_IN_NSNAME =
		"RELAXNGReader.NsNameInNsName";
	
	private static final String ERR_DUPLICATE_ATTRIBUTES =
		"RELAXNGReader.DuplicateAttributes";
	private static final String ERR_DUPLICATE_ELEMENTS =
		"RELAXNGReader.DuplicateElements";
	
	private static final String ERR_DIFFERENT_VALUE_TYPES_IN_INTERLEAVE =
		"RELAXNGReader.DifferentValueTypesInInterleave";
	private static final String ERR_SAME_VALUE_IN_INTERLEAVE =
		"RELAXNGReader.SameValueInInterleave";
	
}
