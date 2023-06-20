
package org.scijava.ops.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

public class OpMethodDependencyPositionTest extends AbstractTestEnvironment
		implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpMethodDependencyPositionTest());
	}

	@OpField(names = "test.stringToLong")
	public final Function<String, Long> parser = in -> Long.parseLong(in);

	@OpMethod( //
		names = "test.dependencyBeforeOutput", //
		type = Computers.Arity1.class //
	)
	public static void OpMethodFoo( //
		@OpDependency(name = "test.stringToLong") Function<String, Long> op, //
		List<String> in, //
		List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBeforeOutput() {
		List<String> in = new ArrayList<>();
		in.add("1");
		List<Long> out = new ArrayList<>();
		ops.op("test.dependencyBeforeOutput").arity1().input(in).output(out).compute();
		List<Long> expected = Arrays.asList(1l);
		assertIterationsEqual(expected, out);
	}

}
