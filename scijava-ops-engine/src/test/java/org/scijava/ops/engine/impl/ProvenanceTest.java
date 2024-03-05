/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.collections.ObjectArray;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoTree;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.BaseOpHints;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.adapt.lift.FunctionToArrays;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.engine.matcher.simplify.PrimitiveArraySimplifiers;
import org.scijava.ops.engine.matcher.simplify.PrimitiveLossReporters;
import org.scijava.ops.engine.matcher.simplify.PrimitiveSimplifiers;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.priority.Priority;
import org.scijava.types.Nil;

public class ProvenanceTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new ProvenanceTest());
		ops.register(new MapperFunc());
		ops.register(new FunctionToArrays());
		ops.register(new PrimitiveSimplifiers());
		ops.register(new PrimitiveArraySimplifiers());
		ops.register(new PrimitiveLossReporters());
		ops.register(new CopyOpCollection());
		ops.register(new CreateOpCollection());
		Object[] adaptors = objsFromNoArgConstructors(
			ComputersToFunctionsViaFunction.class.getDeclaredClasses());
		ops.register(adaptors);
		ops.setDefaultHints(new Hints(BaseOpHints.History.RECORD));
	}

	// -- Test Ops -- //

	@OpField(names = "test.provenance")
	public final Producer<String> foo = () -> "provenance";

	@OpField(names = "test.provenanceComputer")
	public final Computers.Arity1<Double[], Double[]> op = (in, out) -> {
		for (int i = 0; i < in.length && i < out.length; i++)
			out[i] = in[i];
	};

	@OpField(names = "test.provenance")
	public final Function<List<? extends Number>, Double> bar = //
		l -> l.stream() //
			.map(Number::doubleValue) //
			.reduce(Double::sum) //
			.orElse(0.);

	@OpField(names = "test.provenance", priority = Priority.HIGH)
	public final Function<List<Double>, Double> baz = //
		l -> l.stream() //
			.reduce(Double::sum) //
			.orElse(0.);

	@OpField(names = "test.provenanceMapped")
	public final Function<Double, Thing> mappedFunc = Thing::new;

	@OpClass(names = "test.provenanceMapper")
	public static class MapperFunc implements Function<Double[], Thing>, Op {

		@OpDependency(name = "test.provenanceMapped")
		public Function<Double, Thing> func;

		@Override
		public Thing apply(Double[] doubles) {
			return Arrays.stream(doubles).map(func).reduce(Thing::append).orElse(
				null);

		}
	}

	static class Thing {

		private Double d;

		public Thing(Double d) {
			this.d = d;
		}

		private Thing append(Thing other) {
			d += other.getDouble();
			return this;
		}

		public Double getDouble() {
			return d;
		}
	}

	// -- Tests -- //

	@Test
	public void testProvenance() {
		String s = ops.op("test.provenance").arity0().outType(String.class)
			.create();
		List<RichOp<?>> executionsUpon = ops.history().executionsUpon(s);
		Assertions.assertEquals(1, executionsUpon.size());
		// Assert only one info in the execution hierarchy
		InfoTree executionHierarchy = ops.history().infoTree(executionsUpon.get(0));
		Assertions.assertEquals(0, executionHierarchy.dependencies().size());
		OpInfo info = executionHierarchy.info();
		Assertions.assertTrue(info.implementationName().contains(this.getClass()
			.getPackageName()));
	}

	@Test
	public void testPriorityProvenance() {
		List<Double> l1 = new ArrayList<>();
		l1.add(1.0);
		l1.add(2.0);
		l1.add(3.0);
		l1.add(4.0);
		Double out1 = ops.op("test.provenance").arity1().input(l1).outType(
			Double.class).apply();

		List<Long> l2 = new ArrayList<>();
		l2.add(5L);
		l2.add(6L);
		l2.add(7L);
		l2.add(8L);
		Double out2 = ops.op("test.provenance").arity1().input(l2).outType(
			Double.class).apply();

		List<RichOp<?>> history1 = ops.history().executionsUpon(out1);
		List<RichOp<?>> history2 = ops.history().executionsUpon(out2);

		Assertions.assertEquals(1, history1.size());
		InfoTree opExecutionChain = ops.history().infoTree(history1.get(0));
		Assertions.assertEquals(0, opExecutionChain.dependencies().size());
		String expected =
			"public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.baz";
		Assertions.assertEquals(expected, opExecutionChain.info()
			.getAnnotationBearer().toString());

		Assertions.assertEquals(1, history2.size());
		opExecutionChain = ops.history().infoTree(history2.get(0));
		Assertions.assertEquals(0, opExecutionChain.dependencies().size());
		expected =
			"public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.bar";
		Assertions.assertEquals(expected, opExecutionChain.info()
			.getAnnotationBearer().toString());
	}

	@Test
	public void testMappingProvenance() {
		// Run the mapper
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Thing out = ops.op("test.provenanceMapper").arity1().input(array).outType(
			Thing.class).apply();

		// Assert two executions upon this Object, once from the mapped function,
		// once from the mapper
		List<RichOp<?>> executionsUpon = ops.history().executionsUpon(out);
		Assertions.assertEquals(2, executionsUpon.size());
	}

	@Test
	public void testMappingInfoTree() {
		// Run an Op call
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Function<Double[], Thing> mapper = ops.op("test.provenanceMapper").arity1()
			.input(array).outType(Thing.class).function();

		// Get the InfoTree associated with the above call
		InfoTree tree = ops.history().infoTree(mapper);

		// Assert the mapper is in the tree
		Iterator<OpInfo> mapperInfos = ops.infos("test.provenanceMapper")
			.iterator();
		OpInfo mapperInfo = mapperInfos.next();
		Assertions.assertEquals(mapperInfo, tree.info());
		// Assert mapped is in the tree
		Iterator<OpInfo> mappedInfos = ops.infos("test.provenanceMapped")
			.iterator();
		OpInfo mappedInfo = mappedInfos.next();
		Assertions.assertEquals(1, tree.dependencies().size(),
			"Expected only one dependency of the mapper Op!");
		Assertions.assertEquals(mappedInfo, tree.dependencies().get(0).info());
	}

	@Test
	public void testMappingProvenanceAndCaching() {
		// call (and run) the Op
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Thing out = ops.op("test.provenanceMapper").arity1().input(array).outType(
			Thing.class).apply();

		// Assert that two Ops operated on the return.
		List<RichOp<?>> mutators = ops.history().executionsUpon(out);
		Assertions.assertEquals(2, mutators.size());

		// Run the mapped Op, assert still two runs on the mapper
		Thing out1 = ops.op("test.provenanceMapped").arity1().input(2.).outType(
			Thing.class).apply();
		mutators = ops.history().executionsUpon(out);
		Assertions.assertEquals(2, mutators.size());
		// Assert one run on the mapped Op as well
		mutators = ops.history().executionsUpon(out1);
		Assertions.assertEquals(1, mutators.size());

	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op.
	 */
	@Test
	public void testDependencylessOpRecoveryFromString() {
		// Get the Op
		Function<Double, Thing> mapper = ops //
			.op("test.provenanceMapped") //
			.arity1() //
			.input(5.0) //
			.outType(Thing.class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(mapper);
		// Generate the Op from the signature and an Op type
		Nil<Function<Double, Thing>> specialType = new Nil<>() {};
		Function<Double, Thing> actual = ops //
			.opFromSignature(signature, specialType);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(mapper, actual));
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op with dependencies.
	 */
	@Test
	public void testOpWithDependencyRecoveryFromString() {
		// Get the Op
		Function<Double[], Thing> mapper = ops //
			.op("test.provenanceMapper") //
			.arity1() //
			.input(new Double[] { 5.0, 10.0, 15.0 }) //
			.outType(Thing.class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(mapper);
		// Generate the Op from the signature and an Op type
		Nil<Function<Double, Thing>> specialType = new Nil<>() {};
		Function<Double, Thing> actual = ops //
			.opFromSignature(signature, specialType);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(mapper, actual));
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op that has been adapted.
	 */
	@Test
	public void testAdaptationRecoveryFromString() {
		// Get the Op
		Function<Double[], Thing[]> f = ops //
			.op("test.provenanceMapped") //
			.arity1() //
			.inType(Double[].class) //
			.outType(Thing[].class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(f);
		// Generate the Op from the signature and an Op type
		Nil<Function<Double[], Thing[]>> special = new Nil<>() {};
		Function<Double[], Thing[]> actual = ops. //
			opFromSignature(signature, special);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(f, actual));
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op, with dependencies, that has been adapted.
	 */
	@Test
	public void testAdaptedOpWithDependencies() {
		// Get the Op
		Function<Double[][], Thing[]> f = ops //
			.op("test.provenanceMapper") //
			.arity1() //
			.inType(Double[][].class) //
			.outType(Thing[].class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(f);
		// Generate the Op from the signature and an Op type
		Nil<Function<Double[][], Thing[]>> special = new Nil<>() {};
		Function<Double[][], Thing[]> actual = ops //
			.opFromSignature(signature, special);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(f, actual));
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op that has been adapted and simplified.
	 */
	@Test
	public void testSimplificationRecovery() {
		// Get the Op
		Computers.Arity1<ObjectArray<Number>, ObjectArray<Number>> c = ops //
			.op("test.provenanceComputer") //
			.arity1() //
			.inType(new Nil<ObjectArray<Number>>()
			{}) //
			.outType(new Nil<ObjectArray<Number>>()
			{}) //
			.computer();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(c);
		// Generate the Op from the signature and an Op type
		Nil<Computers.Arity1<ObjectArray<Number>, ObjectArray<Number>>> special =
			new Nil<>()
			{};
		Computers.Arity1<ObjectArray<Number>, ObjectArray<Number>> fromString = ops
			.opFromSignature(signature, special);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(c, fromString));
		// Assert Op functionality similarity
		ObjectArray<Number> in = new ObjectArray<>(new Number[] { 1, 2, 3 });
		ObjectArray<Number> actual = new ObjectArray<>(new Number[] { 0, 0, 0 });
		fromString.compute(in, actual);
		ObjectArray<Number> expected = new ObjectArray<>(new Number[] { 1., 2.,
			3. });
		Assertions.assertEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op that has been adapted and simplified.
	 */
	@Test
	public void testFocusedRecovery() {
		// Get the Op
		Computers.Arity1<Integer[], Integer[]> c = ops //
			.op("test.provenanceComputer") //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.computer();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(c);
		// Generate the Op from the signature and an Op typ
		Nil<Computers.Arity1<Integer[], Integer[]>> special = new Nil<>() {};
		Computers.Arity1<Integer[], Integer[]> fromString = ops.opFromSignature(
			signature, special);
		// Assert Op functionality similarity
		Integer[] in = { 1, 2, 3 };
		Integer[] actual = { 0, 0, 0 };
		fromString.compute(in, actual);
		Integer[] expected = { 1, 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an Op that has been adapted <b>and</b> simplified.
	 */
	@Test
	public void testSimplificationAdaptationRecovery() {
		// Get the Op
		Function<Integer[], Integer[]> c = ops //
			.op("test.provenanceComputer") //
			.arity1() //
			.inType(Integer[].class) //
			.outType(Integer[].class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(c);
		// Generate the Op from the signature and the Op type
		Nil<Function<Integer[], Integer[]>> special = new Nil<>() {};
		Function<Integer[], Integer[]> fromString = ops //
			.opFromSignature(signature, special);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(c, fromString));
		// Assert Op functionality similarity
		Integer[] in = { 1, 2, 3 };
		Integer[] actual = fromString.apply(in);
		Integer[] expected = { 1, 2, 3 };
		Assertions.assertArrayEquals(expected, actual);
	}

	/**
	 * Tests the ability of {@link OpEnvironment#opFromSignature(String, Nil)} to
	 * generate an adapted Op, with dependencies, from a signature.
	 */
	@Test
	public void testAdaptationWithDependencies() {
		// Get the Op
		Function<Double[], Double[]> f = ops //
			.op("test.provenanceComputer") //
			.arity1() //
			.inType(Double[].class) //
			.outType(Double[].class) //
			.function();
		// Get the signature from the Op
		String signature = ops.history().signatureOf(f);
		// Generate the Op from the signature and the Op type
		Nil<Function<Double[], Double[]>> special = new Nil<>() {};
		Function<Double[], Double[]> actual = ops //
			.opFromSignature(signature, special);
		// Assert Op similarity
		Assertions.assertTrue(wrappedOpEquality(f, actual));
	}

	// -- Helper Methods -- //

	/**
	 * This method returns {@code true} iff:
	 * <ol>
	 * <li><b>Both</b> {@code op1} and {@code op2} are {@link RichOp}s</li>
	 * <li>The backing Op {@link Class}es are equal</li>
	 * </ol>
	 *
	 * @param op1 an Op
	 * @param op2 another Op
	 * @return true iff the two conditions above are true
	 */
	private boolean wrappedOpEquality(Object op1, Object op2) {
		boolean isRichOp1 = op1 instanceof RichOp;
		boolean isRichOp2 = op2 instanceof RichOp;
		if (isRichOp1 && isRichOp2) {
			var backingCls1 = ((RichOp<?>) op1).op().getClass();
			var backingCls2 = ((RichOp<?>) op2).op().getClass();
			return backingCls1 == backingCls2;
		}
		return false;
	}

}
