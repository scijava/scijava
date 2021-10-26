
package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.Priority;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.InfoChain;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.RichOp;
import org.scijava.ops.engine.BarebonesTestEnvironment;
import org.scijava.ops.engine.adapt.functional.ComputersToFunctionsViaFunction;
import org.scijava.ops.engine.adapt.lift.FunctionToArrays;
import org.scijava.ops.engine.conversionLoss.impl.PrimitiveLossReporters;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.create.CreateOpCollection;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.AdaptationInfoChainGenerator;
import org.scijava.ops.engine.simplify.PrimitiveArraySimplifiers;
import org.scijava.ops.engine.simplify.PrimitiveSimplifiers;
import org.scijava.ops.engine.simplify.SimplificationInfoChainGenerator;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.types.Nil;

public class ProvenanceTest extends BarebonesTestEnvironment implements OpCollection {

	private static final Collection<? extends Class<?>> INFO_CHAIN_GENERATORS =
		Arrays.asList( //
			AdaptationInfoChainGenerator.class, //
			DefaultInfoChainGenerator.class, //
			SimplificationInfoChainGenerator.class //
		);

	@BeforeClass
	public static void AddNeededOps() {
		discoverer.register(ProvenanceTest.class, "opcollection");
		discoverer.register(FunctionToArrays.class, "opcollection");
		discoverer.register(PrimitiveSimplifiers.class, "opcollection");
		discoverer.register(PrimitiveArraySimplifiers.class, "opcollection");
		discoverer.register(PrimitiveLossReporters.class, "opcollection");
		discoverer.register(CopyOpCollection.class, "opcollection");
		discoverer.register(CreateOpCollection.class, "opcollection");
		discoverer.registerAll(ComputersToFunctionsViaFunction.class.getDeclaredClasses(), "op");
		discoverer.registerAll(INFO_CHAIN_GENERATORS, "infochaingenerator");
	}

	@OpField(names = "test.provenance")
	public final Producer<String> foo = () -> "provenance";

	@Test
	public void testProvenance() {
		String s = ops.op("test.provenance").input().outType(String.class).create();
		List<RichOp<?>> executionsUpon = history.executionsUpon(s);
		Assert.assertEquals(1, executionsUpon.size());
		// Assert only one info in the execution hierarchy
		InfoChain executionHierarchy = history.opExecutionChain(executionsUpon
			.get(0));
		Assert.assertEquals(0, executionHierarchy.dependencies().size());
		OpInfo info = executionHierarchy.info();
		Assert.assertTrue(info.implementationName().contains(this.getClass()
			.getPackageName()));
	}

	@OpField(names = "test.provenance")
	public final Function<List<? extends Number>, Double> bar = l -> {
		return l.stream().map(n -> n.doubleValue()).reduce((d1, d2) -> d1 + d2)
			.orElse(0.);
	};

	@OpField(names = "test.provenance", priority = Priority.HIGH)
	public final Function<List<Double>, Double> baz = l -> {
		return l.stream().reduce((d1, d2) -> d1 + d2).orElse(0.);
	};

	@Test
	public void testPriorityProvenance() {
		List<Double> l1 = new ArrayList<>();
		l1.add(1.0);
		l1.add(2.0);
		l1.add(3.0);
		l1.add(4.0);
		Double out1 = ops.op("test.provenance").input(l1).outType(Double.class)
			.apply();

		List<Long> l2 = new ArrayList<>();
		l2.add(5l);
		l2.add(6l);
		l2.add(7l);
		l2.add(8l);
		Double out2 = ops.op("test.provenance").input(l2).outType(Double.class)
			.apply();

		List<RichOp<?>> history1 = history.executionsUpon(out1);
		List<RichOp<?>> history2 = history.executionsUpon(out2);

		Assert.assertEquals(1, history1.size());
		InfoChain opExecutionChain = history.opExecutionChain(history1.get(
			0));
		Assert.assertEquals(0, opExecutionChain.dependencies().size());
		String expected =
			"public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.baz";
		Assert.assertEquals(expected, opExecutionChain.info().getAnnotationBearer()
			.toString());

		Assert.assertEquals(1, history2.size());
		opExecutionChain = history.opExecutionChain(history2.get(0));
		Assert.assertEquals(0, opExecutionChain.dependencies().size());
		expected =
			"public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.bar";
		Assert.assertEquals(expected, opExecutionChain.info().getAnnotationBearer()
			.toString());
	}

	@OpField(names = "test.provenanceMapped")
	public final Function<Double, Thing> mappedFunc = in -> new Thing(in);

	@OpMethod(names = "test.provenanceMapper", type = Function.class)
	public static Thing mapperFunc(@OpDependency(
		name = "test.provenanceMapped") Function<Double, Thing> func, Double[] arr)
	{
		return Arrays.stream(arr).map(func).reduce((t1, t2) -> t1.append(t2))
			.orElseGet(() -> null);
	}

	class Thing {

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

	@Test
	public void testMappingProvenance() {
		// Run the mapper
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Thing out = ops.op("test.provenanceMapper").input(array).outType(
			Thing.class).apply();

		// Assert two executions upon this Object, once from the mapped function, once from the mapper
		List<RichOp<?>> executionsUpon = history.executionsUpon(out);
		Assert.assertEquals(2, executionsUpon.size());
	}

	@Test
	public void testMappingExecutionChain() {
		// Run an Op call
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Function<Double[], Thing> mapper = ops.op("test.provenanceMapper").input(
			array).outType(Thing.class).function();

		// Get the Op execution chain associated with the above call
		InfoChain executionChain = history.opExecutionChain(mapper);

		// Assert the mapper is in the execution chain
		Iterator<OpInfo> mapperInfos = ops.infos("test.provenanceMapper")
			.iterator();
		OpInfo mapperInfo = mapperInfos.next();
		Assert.assertTrue(executionChain.info().equals(mapperInfo));
		// Assert mapped is in the execution chain
		Iterator<OpInfo> mappedInfos = ops.infos("test.provenanceMapped")
			.iterator();
		OpInfo mappedInfo = mappedInfos.next();
		Assert.assertEquals("Expected only one dependency of the mapper Op!", 1,
			executionChain.dependencies().size());
		Assert.assertTrue(executionChain.dependencies().get(0).info().equals(
			mappedInfo));
	}

	@Test
	public void testMappingProvenanceAndCaching() {
		// call (and run) the Op
		Hints hints = new DefaultHints();
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Thing out = ops.op("test.provenanceMapper").input(array).outType(
			Thing.class).apply(hints);

		// Assert that two Ops operated on the return.
		List<RichOp<?>> mutators = history.executionsUpon(out);
		Assert.assertEquals(2, mutators.size());

		// Run the mapped Op, assert still two runs on the mapper
		Thing out1 = ops.op("test.provenanceMapped").input(2.).outType(Thing.class)
			.apply(hints);
		mutators = history.executionsUpon(out);
		Assert.assertEquals(2, mutators.size());
		// Assert one run on the mapped Op as well
		mutators = history.executionsUpon(out1);
		Assert.assertEquals(1, mutators.size());

	}

	@Test
	public void testDependencylessOpRecoveryFromString() {
		Hints hints = new DefaultHints();
		Function<Double, Thing> mapper = ops.op("test.provenanceMapped").input(5.0)
			.outType(Thing.class).function(hints);
		InfoChain chain = history.opExecutionChain(mapper);
		Assert.assertEquals(0, chain.dependencies().size());
		String signature = chain.signature();
		Nil<Function<Double, Thing>> special = new Nil<>() {};
		@SuppressWarnings("unused")
		Function<Double, Thing> actual = ops.opFromSignature(signature,
			special);
	}

	@Test
	public void testOpWithDependencyRecoveryFromString() {
		OpInfo info = singularInfoOfName("test.provenanceMapper");
		OpInfo dependency = singularInfoOfName("test.provenanceMapped");
		InfoChain depChain = new InfoChain(dependency);
		InfoChain chain = new InfoChain(info, Collections.singletonList(depChain));
		String signature = chain.signature();
		Nil<Function<Double[], Thing>> special = new Nil<>() {};
		@SuppressWarnings("unused")
		Function<Double[], Thing> actual = ops.opFromSignature(signature,
			special);
	}

	@Test
	public void testAdaptationRecoveryFromString() {
		Function<Double[], Thing[]> f = ops.op("test.provenanceMapped").inType(
			Double[].class).outType(Thing[].class).function();
		String signature = history.signatureOf(f);
		Nil<Function<Double[], Thing[]>> special = new Nil<>() {};
		Function<Double[], Thing[]> actual = ops.opFromSignature(signature,
			special);
		@SuppressWarnings("unused")
		Thing[] apply = actual.apply(new Double[] { 1., 2., 3. });
	}

	@Test
	public void testAdaptedOpWithDependencies() {
		Function<Double[][], Thing[]> f = ops.op("test.provenanceMapper").inType(
			Double[][].class).outType(Thing[].class).function();
		String signature = history.signatureOf(f);
		Nil<Function<Double[][], Thing[]>> special = new Nil<>() {};
		Function<Double[][], Thing[]> actual = ops.opFromSignature(signature,
			special);
		@SuppressWarnings("unused")
		Thing[] apply = actual.apply(new Double[][] { new Double[] { 1., 2.,
			3. } });
	}

	@Test
	public void testSimplificationRecovery() {
		Computers.Arity1<Integer[], Integer[]> c = ops.op("test.provenanceComputer")
			.inType(Integer[].class).outType(Integer[].class).computer();
		String signature = history.signatureOf(c);
		Nil<Computers.Arity1<Integer[], Integer[]>> special = new Nil<>() {};
		Computers.Arity1<Integer[], Integer[]> fromString = ops
			.opFromSignature(signature, special);
		Integer[] in = { 1, 2, 3 };
		Integer[] actual = { 0, 0, 0 };
		fromString.compute(in, actual);
		Integer[] expected = { 1, 2, 3 };
		Assert.assertArrayEquals(expected, actual);
	}

	@Test
	public void testSimplificationAdaptationRecovery() {
		Function<Integer[], Integer[]> c = ops.op("test.provenanceComputer").inType(
			Integer[].class).outType(Integer[].class).function();
		String signature = history.signatureOf(c);
		Nil<Function<Integer[], Integer[]>> special = new Nil<>() {};
		Function<Integer[], Integer[]> fromString = ops.opFromSignature(
			signature, special);
		Integer[] in = { 1, 2, 3 };
		Integer[] actual = fromString.apply(in);
		Integer[] expected = { 1, 2, 3 };
		Assert.assertArrayEquals(expected, actual);
	}

	private OpInfo singularInfoOfName(String name) {
		Iterator<OpInfo> infos = ops.infos(name).iterator();
		Assert.assertTrue(infos.hasNext());
		OpInfo info = infos.next();
		Assert.assertFalse(infos.hasNext());
		return info;
	}

	@OpField(names = "test.provenanceComputer")
	public final Computers.Arity1<Double[], Double[]> op = (in, out) -> {
		for (int i = 0; i < in.length && i < out.length; i++)
			out[i] = in[i];
	};

	@Test
	public void testAdaptatorWithDependencies() {
		Function<Double[], Double[]> f = ops.op("test.provenanceComputer").inType(
			Double[].class).outType(Double[].class).function();
		@SuppressWarnings("unchecked")
		InfoChain chain = ((RichOp<Function<Double[], Double[]>>) f).infoChain();
		String signature = chain.signature();
		Nil<Function<Double[], Double[]>> special = new Nil<>() {};
		Function<Double[], Double[]> actual = ops.opFromSignature(signature,
			special);
		@SuppressWarnings("unused")
		Double[] apply = actual.apply(new Double[] { 1., 2., 3. });
	}

}
