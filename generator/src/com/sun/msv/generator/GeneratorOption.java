/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.generator;

import java.util.Random;
import com.sun.tranquilo.grammar.trex.TREXPatternPool;

/**
 * set of options that controls generation behavior.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class GeneratorOption
{
	/** random number generator. */
	public Random random;
	
	/**
	 * if the generated element exceeds this depth,
	 * the generator tries to cut back.
	 */
	public int cutBackDepth = 0;
	
	/**
	 * this object is responsible to calculate how many times '*' or '+' is repeated.
	 */
	public Rand width;

	public NameGenerator nameGenerator;
	public DataTypeGenerator dtGenerator;
	
	public TREXPatternPool pool;
	
	/**
	 * probability of "sequencing error" per # of sequences.
	 * 0: no error, 1.0: error at every occurence of sequence.
	 * 
	 * sequencing error is to generate B,A where its content model is A,B.
	 */
	public double probSeqError = 0;
	
	/**
	 * probability of "missing element error" per # of elements.
	 * Missing element error is to generate epsilon where its content model is A.
	 */
	public double probMissingElemError = 0;
	
	/**
	 * probability of "missing plus error" per # of '+' operator.
	 * Missing plus error is to generate epsilon where the content model is A+.
	 * Similar to "missing element error"
	 */
	public double probMissingPlus = 0;
	
	/**
	 * probability of "slip-in element error" per # of elements.
	 * 
	 * slip-in element error is to generate a random element before valid element X.
	 */
	public double probSlipInElemError = 0;

	/**
	 * probability of "mutated element error" per # of elements.
	 * 
	 * mutated element error is to replace a valid element by a random element.
	 * Or this error can be considered as combination of "missing element error"
	 * and "slip-in element error".
	 */
	public double probMutatedElemError = 0;
	
	/**
	 * probability of "missing attribute error" per # of attributes.
	 */
	public double probMissingAttrError = 0;
	
	/**
	 * probability of "slip-in attribute error" per # of attributes.
	 * 
	 * slip-in attribute error is to generate a random attribute before valid attribute X.
	 */
	public double probSlipInAttrError = 0;

	/**
	 * probability of "mutated attribute error" per # of attributes.
	 * 
	 * mutated attribute error is to replace a valid attribute by a random attribute.
	 * Or this error can be considered as combination of "missing attribute error"
	 * and "slip-in attribute error".
	 */
	public double probMutatedAttrError = 0;
	
	/**
	 * probability of "greedy choice error" per # of choice.
	 * 
	 * greedy choice error is to generate A,B (or B,A) where its content model is (A|B).
	 */
	public double probGreedyChoiceError = 0;
	
	/**
	 * fills unspecified parameters by default values.
	 */
	public void fillInByDefault()
	{
		if( random==null )			random = new Random();
		if( cutBackDepth==0 )		cutBackDepth=5;
		if( width==null )			width = new Rand.UniformRand( random, 3 );
		if( nameGenerator==null )	nameGenerator = new NameGenerator(random);
		if( dtGenerator==null )		dtGenerator = new DataTypeGeneratorImpl();
		if( pool==null )			pool = new TREXPatternPool();
	}
	
	public boolean errorSpecified()
	{
		return probGreedyChoiceError!=0
			|| probMissingAttrError!=0
			|| probMissingElemError!=0
			|| probMissingPlus!=0
			|| probMutatedAttrError!=0
			|| probMutatedElemError!=0
			|| probSeqError!=0
			|| probSlipInAttrError!=0
			|| probSlipInElemError!=0;
	}
}
