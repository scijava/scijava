
package org.scijava.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpCollection;
import org.scijava.ops.api.OpDependency;
import org.scijava.ops.api.OpMethod;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class OpMethodDependencyPositionTest extends AbstractTestEnvironment {

	@OpField(names = "test.stringToLong")
	public final Function<String, Long> parser = in -> Long.parseLong(in);

	@OpMethod(names = "test.dependencyBeforeOutput",
		type = Computers.Arity1.class)
	public static void OpMethodFoo(List<String> in, @OpDependency(
		name = "test.stringToLong") Function<String, Long> op, List<Long> out)
	{
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBeforeOutput() {
		List<String> in = new ArrayList<>();
		in.add("1");
		List<Long> out = new ArrayList<>();
		ops.op("test.dependencyBeforeOutput").input(in).output(out).compute();
		List<Long> expected = Arrays.asList(1l);
		assertIterationsEqual(expected, out);
	}

	@OpMethod(names = "test.dependencyAfterOutput", type = Computers.Arity1.class)
	public static void OpMethodFoo(List<String> in, List<Long> out, @OpDependency(
		name = "test.stringToLong") Function<String, Long> op)
	{
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyAfterOutput() {
		List<String> in = new ArrayList<>();
		in.add("1");
		List<Long> out = new ArrayList<>();
		ops.op("test.dependencyAfterOutput").input(in).output(out).compute();
		List<Long> expected = Arrays.asList(1l);
		assertIterationsEqual(expected, out);
	}

	@OpField(names = "test.squareList")
	public final Computers.Arity1<List<Long>, List<Long>> squareOp = (in,
		out) -> {
		out.clear();
		for (Long l : in)
			out.add(l * l);
	};

	@OpMethod(names = "test.dependencyBeforeAndAfterOutput",
		type = Computers.Arity1.class)
	public static void OpMethodBar(List<String> in, @OpDependency(
		name = "test.stringToLong") Function<String, Long> op1, List<Long> out,
		@OpDependency(
			name = "test.squareList") Computers.Arity1<List<Long>, List<Long>> op2)
	{
		List<Long> temp = new ArrayList<>();
		for (String s : in)
			temp.add(op1.apply(s));
		out.clear();
		op2.compute(temp, out);
	}

	@Test
	public void testOpDependencyBeforeAndAfterOutput() {
		List<String> in = new ArrayList<>();
		in.add("2");
		List<Long> out = new ArrayList<>();
		ops.op("test.dependencyBeforeAndAfterOutput").input(in).output(out).compute();
		List<Long> expected = Arrays.asList(4l);
		assertIterationsEqual(expected, out);
	}
}
