
package org.scijava.ops.engine;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.types.Nil;

/**
 * Ensures correct behavior in {@link OpBuilder} calls <b>where no output type
 * or <code>Object</code> is given</b>.
 *
 * @author Gabriel Selzer
 * @see OpBuilderTest
 */
public class OpBuilderNoOutputTest<T extends Number> extends
	BarebonesTestEnvironment implements OpCollection
{

	@BeforeClass
	public static void addNeededOps() {
		discoverer.register(OpBuilderNoOutputTest.class, "opcollection");
	}

	public final String opName = "test.noOutput";

	// private wrapper class
	private static class WrappedList<E> extends ArrayList<E> {}

	/**
	 * @input in
	 * @output out
	 */
	@OpField(names = opName)
	public final Function<T, WrappedList<T>> func = in -> {

		WrappedList<T> out = new WrappedList<>();
		out.add(in);
		return out;
	};

	@Test
	public void testNoParameterizedTypeOutputGiven() {
		Object output = ops.op(opName).input(5.).apply();
		Type expectedOutputType = new Nil<WrappedList<Double>>() {}.getType();
		Assert.assertEquals(ops.genericType(output), expectedOutputType);
	}
}
