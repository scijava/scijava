package org.scijava.ops.provenance;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.Priority;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.function.Producer;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class ProvenanceTest extends AbstractTestEnvironment {

	@OpField(names = "test.provenance")
	public final Producer<String> foo = () -> "provenance";

	@Test
	public void testProvenance() {
		String s = ops.op("test.provenance").input().outType(String.class).create();
		List<OpExecutionSummary> l = OpHistory.executionsUpon(s);
		Assert.assertEquals(1, l.size());
		Assert.assertEquals(l.get(0).executor(), foo);
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
		List<OpExecutionSummary> history1 = OpHistory.executionsUpon(out1);
		List<OpExecutionSummary> history2 = OpHistory.executionsUpon(out2);
		Assert.assertEquals(1, history1.size());
		Assert.assertEquals(baz, history1.get(0).executor());
		Assert.assertEquals(1, history2.size());
		Assert.assertEquals(bar, history2.get(0).executor());

	}

}
