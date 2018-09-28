/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2018 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops;

import java.util.function.BiFunction;

import org.junit.Test;
import org.scijava.param.ValidityException;
import org.scijava.types.Nil;

public class OpCollectionTest extends AbstractTestEnvironment {

	Nil<Double> nilDouble = new Nil<Double>() {
	};
	
	@Test
	public void testOpCollection() throws ValidityException {
		BiFunction<Double, Double, Double> divFunction = ops().findOp( //
				"math.div", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 1.0 == divFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> addFunction = ops().findOp( //
				"math.add", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 4.0 == addFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> mulFunction = ops().findOp( //
				"math.mul", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 4.0 == mulFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> subFunction = ops().findOp( //
				"math.sub", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 0.0 == subFunction.apply(2.0, 2.0);
	}
	
	@Test
	public void testOpAliases() throws ValidityException {
		BiFunction<Double, Double, Double> divFunction = ops().findOp( //
				"div", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 1.0 == divFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> addFunction = ops().findOp( //
				"add", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 4.0 == addFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> mulFunction = ops().findOp( //
				"mul", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 4.0 == mulFunction.apply(2.0, 2.0);
		
		BiFunction<Double, Double, Double> subFunction = ops().findOp( //
				"sub", new Nil<BiFunction<Double, Double, Double>>() {
				}, //
				new Nil[] { nilDouble, nilDouble }, //
				nilDouble//
		);
		assert 0.0 == subFunction.apply(2.0, 2.0);
	}
}
