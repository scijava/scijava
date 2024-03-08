
package org.scijava.ops.engine.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.describe.PrimitiveDescriptors;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import java.util.List;
import java.util.function.Function;

public class DefaultOpDescriptionGeneratorTest extends AbstractTestEnvironment
	implements OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register( //
			new DefaultOpDescriptionGeneratorTest(), //
			new PrimitiveDescriptors() //
		);
	}

	@OpField(names = "test.coalesceDescription")
	public final Function<Double, Double> func1 = in -> in + 1.;

	@OpField(names = "test.coalesceDescription")
	public final Function<Long, Long> func2 = in -> in + 1;

	@OpField(names = "test.coalesceDescription")
	public final Computers.Arity1<List<Long>, List<Long>> comp1 = (in, out) -> {
		out.clear();
		out.addAll(in);
	};

	@OpField(names = "test.coalesceDescription")
	public final Inplaces.Arity2_1<List<Long>, Long> inplace1 = (in1, in2) -> {
		in1.clear();
		in1.add(in2);
	};

	@Test
	public void testCoalescedDescriptions() {
		String actual = ops.unary("test.coalesceDescription").helpVerbose();
		String expected = "test.coalesceDescription:\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$comp1\n" +
			"\t\t> input1 : java.util.List<java.lang.Long>\n" +
			"\t\t> container1 : @CONTAINER java.util.List<java.lang.Long>\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$func1\n" +
			"\t\t> input1 : java.lang.Double\n" + "\t\tReturns : java.lang.Double\n" +
			"\t- org.scijava.ops.engine.impl.DefaultOpDescriptionGeneratorTest$func2\n" +
			"\t\t> input1 : java.lang.Long\n" + "\t\tReturns : java.lang.Long";

		Assertions.assertEquals(expected, actual);

		actual = ops.unary("test.coalesceDescription").help();
		expected = //
			"test.coalesceDescription:\n\t- (list<number>, @CONTAINER list<number>) -> None\n\t- (number) -> number";
		Assertions.assertEquals(expected, actual);

		// Test that with 2 inputs we do get the binary inplace Op, but no others
		actual = ops.binary("test.coalesceDescription").help();
		expected = //
			"test.coalesceDescription:\n\t- (@MUTABLE list<number>, number) -> None";
		Assertions.assertEquals(expected, actual);

		// Finally test that with no inputs we don't get any of the Ops
		actual = ops.nullary("test.coalesceDescription").help();
		expected = "No Ops found matching this request.";
		Assertions.assertEquals(expected, actual);
	}

}
