/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
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

package org.scijava.ops.engine.matcher;

import java.util.Arrays;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.engine.DependencyMatchingException;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.OpDescriptionGenerator;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;

public class OpMatchingExceptionTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpMatchingExceptionTest());
		ops.register(new DependentOp());
		ops.register(new FurtherDependentOp());
		ops.register(new MissingDependencyOp());
		ops.register(new MissingDependencyOpArr1());
		ops.register(new MissingDependencyOpArr2());
		ops.register(
			new ComputersToFunctionsViaFunction.Computer1ToFunction1ViaFunction());
		ops.register(new CreateOpCollection());
	}

	@OpField(names = "test.duplicateOp")
	public final Function<Double, Double> duplicateA = (in) -> in;

	@OpField(names = "test.duplicateOp")
	public final Function<Double, Double> duplicateB = (in) -> in;

	/**
	 * Assert a relevant (i.e. informational) message is thrown when multiple Ops
	 * conflict for a given OpRef.
	 */
	@Test
	public void duplicateErrorRegressionTest() {
		try {
			ops.op("test.duplicateOp").inType(Double.class).outType(Double.class)
				.function();
			Assertions.fail();
		}
		catch (OpMatchingException e) {
			Assertions.assertTrue(e.getMessage().startsWith(
				"Multiple ops of equal priority detected for request"));
			Assertions.assertTrue(e.getMessage().contains("test.duplicateOp"));
		}
	}

	/**
	 * Assert that a relevant error is thrown when a dependency is missing
	 */
	@Test
	public void missingDependencyRegressionTest() {
		try {
			ops.op("test.missingDependencyOp").input(1.).outType(Double.class)
				.apply();
			Assertions.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assertions.assertTrue(message.contains(
				"Name: \"test.missingDependencyOp\""));
		}
	}

	/**
	 * Assert the DependencyMatchingException contains both dependencies in the
	 * chain
	 */
	@Test
	public void missingNestedDependencyRegressionTest() {
		try {
			ops.op("test.outsideOp").input(1.).outType(Double.class).apply();
			Assertions.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assertions.assertTrue(message.contains("Name: \"test.outsideOp\""));
		}
	}

	/**
	 * Assert that DependencyMatchingExceptions do not omit relevant information
	 * when an adaptation is involved.
	 */
	@Test
	public void missingDependencyViaAdaptationTest() {
		Double[] d = new Double[0];
		try {
			ops.op("test.adaptMissingDep").input(d).outType(Double[].class).apply();
			Assertions.fail("Expected DependencyMatchingException");
		}
		catch (DependencyMatchingException e) {
			String message = e.getMessage();
			Assertions.assertTrue(message.contains("Name: \"test.adaptMissingDep\""));

		}
	}

}

@OpClass(names = "test.furtherOutsideOp")
class FurtherDependentOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.outsideOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.outsideOp")
class DependentOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.missingDependencyOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.missingDependencyOp")
class MissingDependencyOp implements Function<Double, Double>, Op {

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}

@OpClass(names = "test.adaptMissingDep")
class MissingDependencyOpArr1 implements Computers.Arity1<Double[], Double[]>,
	Op
{

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @param out the output
	 */
	@Override
	public void compute(Double[] t, Double[] out) {}

}

@OpClass(names = "test.adaptMissingDep")
class MissingDependencyOpArr2 implements Function<Double, Double>, Op {

	@OpDependency(name = "test.nonexistingOp")
	private Function<Double, Double> op;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return op.apply(t);
	}

}
