package org.scijava.ops.engine.impl;

import com.google.common.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.Priority;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.Plugin;
import org.scijava.types.Nil;

@Plugin(type = OpCollection.class)
public class ProvenanceTest extends AbstractTestEnvironment {

	@OpField(names = "test.provenance")
	public final Producer<String> foo = () -> "provenance";

	@Test
	public void testProvenance() {
		String s = ops.op("test.provenance").input().outType(String.class).create();
		List<UUID> executionsUpon = ops.history().executionsUpon(s);
		Assert.assertEquals(1, executionsUpon.size());
		// Assert only one info in the execution hierarchy
		Graph<OpInfo> executionHierarchy = ops.history().opExecutionChain(executionsUpon.get(0));
		Assert.assertEquals(1, executionHierarchy.nodes().size());
		OpInfo info = executionHierarchy.nodes().iterator().next();
		Assert.assertTrue(info.implementationName().contains(this.getClass().getPackageName()));
	}

	@OpField(names = "test.provenance")
	public final Function<List<? extends Number>, Double> bar = l -> {
		return l.stream().map(n -> n.doubleValue()).reduce((d1, d2)-> d1 + d2).orElse(0.);
	};

	@OpField(names = "test.provenance", priority = Priority.HIGH)
	public final Function<List<Double>, Double> baz = l -> {
		return l.stream().reduce((d1, d2)-> d1 + d2).orElse(0.);
	};

	@Test
	public void testPriorityProvenance() {
		List<Double> l1 = new ArrayList<>();
		l1.add(1.0);
		l1.add(2.0);
		l1.add(3.0);
		l1.add(4.0);
		Double out1 = ops.op("test.provenance").input(l1).outType(Double.class).apply();

		List<Long> l2 = new ArrayList<>();
		l2.add(5l);
		l2.add(6l);
		l2.add(7l);
		l2.add(8l);
		Double out2 = ops.op("test.provenance").input(l2).outType(Double.class).apply();

		List<UUID> history1 = ops.history().executionsUpon(out1);
		List<UUID> history2 = ops.history().executionsUpon(out2);

		Assert.assertEquals(1, history1.size());
		Graph<OpInfo> opExecutionChain = ops.history().opExecutionChain(history1.get(0));
		Assert.assertEquals(1, opExecutionChain.nodes().size());
		String expected = "public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.baz";
		for (OpInfo i : opExecutionChain.nodes()) {
			Assert.assertEquals(expected, i.getAnnotationBearer().toString());
		}

		Assert.assertEquals(1, history2.size());
		opExecutionChain = ops.history().opExecutionChain(history2.get(0));
		Assert.assertEquals(1, opExecutionChain.nodes().size());
		expected = "public final java.util.function.Function org.scijava.ops.engine.impl.ProvenanceTest.bar";
		for (OpInfo i : opExecutionChain.nodes()) {
			Assert.assertEquals(expected, i.getAnnotationBearer().toString());
		}
	}

	@OpField(names = "test.provenanceMapped")
	public final Function<Double, Thing> mappedFunc = in -> new Thing(in);

	@OpMethod(names = "test.provenanceMapper", type = Function.class)
	public static Thing mapperFunc(@OpDependency(name = "test.provenanceMapped") Function<Double, Thing> func, Double[] arr) {
		return Arrays.stream(arr).map(func).reduce((t1, t2) -> t1.append(t2)).orElseGet(() -> null);
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
		Thing out = ops.op("test.provenanceMapper").input(array).outType(Thing.class).apply();

		// Assert only one execution upon this Object
		List<UUID> executionsUpon = ops.history().executionsUpon(out);
		Assert.assertEquals(1, executionsUpon.size());
	}

	@Test
	public void testMappingExecutionChain() {
		// Run an Op call
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Function<Double[], Thing> mapper = ops.op("test.provenanceMapper").input(array).outType(Thing.class).function();

		// Get the Op execution chain associated with the above call
		Graph<OpInfo> executionChain = ops.history().opExecutionChain(mapper);

		// Assert only two Ops are called (the Op we asked for, and its dependency)
		Assert.assertEquals(2, executionChain.nodes().size());
		// Assert the mapper is in the execution chain
		Iterator<OpInfo> mapperInfos = ops.env().infos("test.provenanceMapper").iterator();
		Assert.assertTrue(mapperInfos.hasNext());
		OpInfo mapperInfo = mapperInfos.next();
		Assert.assertFalse(mapperInfos.hasNext());
		Assert.assertTrue(executionChain.nodes().contains(mapperInfo));
		// Assert mapped is in the execution chain
		Iterator<OpInfo> mappedInfos = ops.env().infos("test.provenanceMapped").iterator();
		Assert.assertTrue(mappedInfos.hasNext());
		OpInfo mappedInfo = mappedInfos.next();
		Assert.assertFalse(mappedInfos.hasNext());
		Assert.assertTrue(executionChain.nodes().contains(mappedInfo));
		// Assert that there is an edge from the mapper OpInfo to the mapped OpInfo
		Assert.assertEquals(1, executionChain.edges().size());
		Assert.assertTrue(executionChain.hasEdgeConnecting(mapperInfo, mappedInfo));
	}

	@Test
	public void testMappingProvenanceAndCaching() {
		// call (and run) the Op
		Hints hints = new DefaultHints();
		int length = 200;
		Double[] array = new Double[length];
		Arrays.fill(array, 1.);
		Thing out = ops.op("test.provenanceMapper").input(array).outType(Thing.class).apply(hints);

		// Assert only one run of the Base Op
		List<UUID> history = ops.history().executionsUpon(out);
		Assert.assertEquals(1, history.size());

		// Run the mapped Op, assert still one run on the mapper
		Thing out1 = ops.op("test.provenanceMapped").input(2.).outType(Thing.class).apply(hints);
		history = ops.history().executionsUpon(out);
		Assert.assertEquals(1, history.size());
		// Assert one run on the mapped Op as well
		history = ops.history().executionsUpon(out1);
		Assert.assertEquals(1, history.size());

	}

	@Test
	public void testDependencylessOpRecoveryFromString() {
		Hints hints = new DefaultHints();
		Function<Double, Thing> mapper = ops.op("test.provenanceMapped").input(5.0).outType(Thing.class).function(hints);
		Graph<OpInfo> g = ops.history().opExecutionChain(mapper);
		Assert.assertEquals(1, g.nodes().size());
		OpInfo info = g.nodes().iterator().next();
		Nil<Function<Double, Thing>> special = new Nil<>() {};
		Nil<Double> inType = Nil.of(Double.class);
		Nil<Thing> outType = Nil.of(Thing.class);
		Function<Double, Thing> actual = ops.env().opFromID(info.id(), special, new Nil[] {inType}, outType);
	}

}
