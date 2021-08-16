package org.scijava.ops.engine.impl;

import com.google.common.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.Priority;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpExecutionSummary;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class ProvenanceTest extends AbstractTestEnvironment {

	@OpField(names = "test.provenance")
	public final Producer<String> foo = () -> "provenance";

	@Test
	public void testProvenance() {
		String s = ops.op("test.provenance").input().outType(String.class).create();
		List<OpExecutionSummary> l = ops.history().executionsUpon(s);
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(l.get(0).op().op(), foo);
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
		l2.add(1l);
		l2.add(2l);
		l2.add(3l);
		l2.add(4l);
		Double out2 = ops.op("test.provenance").input(l2).outType(Double.class).apply();

		Assert.assertEquals(out1, out2);
		List<OpExecutionSummary> history1 = ops.history().executionsUpon(out1);
		List<OpExecutionSummary> history2 = ops.history().executionsUpon(out2);
		Assert.assertEquals(1, history1.size());
		Assert.assertEquals(baz, history1.get(0).op().op());
		Assert.assertEquals(1, history2.size());
		Assert.assertEquals(bar, history2.get(0).op().op());
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
		List<OpExecutionSummary> history = ops.history().executionsUpon(out);
		Assert.assertEquals(1, history.size());
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
		List<OpExecutionSummary> history = ops.history().executionsUpon(out);
		Assert.assertEquals(1, history.size());

		// Run the mapped Op, assert still one run on the mapper
		Thing out1 = ops.op("test.provenanceMapped").input(2.).outType(Thing.class).apply(hints);
		history = ops.history().executionsUpon(out);
		Assert.assertEquals(1, history.size());
		// Assert one run on the mapped Op as well
		history = ops.history().executionsUpon(out1);
		Assert.assertEquals(1, history.size());

	}

}
