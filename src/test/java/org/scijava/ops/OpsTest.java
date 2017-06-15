
package org.scijava.ops;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.scijava.ItemIO;
import org.scijava.ValidityException;
import org.scijava.command.Command;
import org.scijava.param.Parameter;
import org.scijava.param.ParameterItem;
import org.scijava.param.ParameterStructs;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInfo;
import org.scijava.struct.Structs;

public class OpsTest {

	@Test
	public void testParameterized() throws ValidityException {
		final StructInfo<ParameterItem<?>> info = //
			ParameterStructs.infoOf(TruncAndMultiply.class);
		assertEquals(3, info.items().size());
		for (final ParameterItem<?> item : info) {
			System.out.println(item.getKey());
		}
	}

	@Test
	public void testBijection() throws ValidityException {
		final StructInfo<ParameterItem<?>> info = //
			ParameterStructs.infoOf(VariousParameters.class);

		final VariousParameters vp = new VariousParameters();
		vp.a = 5;
		vp.b = 3.3;
		vp.c = 2;
		vp.d = "Hello";
		vp.o = 12.3;
		vp.p = "Goodbye";

		final Struct<VariousParameters> struct = Structs.create(info, vp);
		assertEquals(5, struct.get("a"));
		assertEquals(3.3, struct.get("b"));
		assertEquals((byte) 2, struct.get("c"));
		assertEquals("Hello", struct.get("d"));
		assertEquals(12.3, struct.get("o"));
		assertEquals("Goodbye", struct.get("p"));

		struct.set("a", 6);
		assertEquals(6, vp.a);

		struct.set("p", "Yo");
		assertEquals("Yo", vp.p);
	}

	// -- Helper classes --

	@Parameter(key = "input")
	@Parameter(type = ItemIO.OUTPUT, key = "result")
	public static class TruncAndMultiply implements FunctionOp<Double, Long> {

		@Parameter
		long multiplier;

		@Override
		public Long apply(final Double t) {
			return t.intValue() * multiplier;
		}
	}

	@Parameter(key = "s")
	@Parameter(key = "number")
	public static class VariousParameters implements FunctionOp<String, Double> {

		@Parameter
		public int a;
		@Parameter
		public Double b;
		@Parameter
		public byte c;
		@Parameter
		public Object d;
		@Parameter(type = ItemIO.OUTPUT)
		public double o;
		@Parameter(type = ItemIO.OUTPUT)
		public String p;

		public void go() {}

		@Override
		public Double apply(final String s) {
			return Double.parseDouble(s);
		}
	}

	public static class Greeting implements Command {

		@Parameter(choices = { "Hello", "Greetings", "Salutations", "Good morning",
			"Good day", "Good evening", "Hallo", "Guten Morgen", "Guten Tag" })
		public String salutation;

		@Parameter
		public String name;

		@Parameter
		public int bangCount;

		@Parameter(type = ItemIO.OUTPUT)
		public String greeting;

		@Override
		public void run() {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bangCount; i++)
				sb.append("!");
			greeting = salutation + ", " + name + sb;
		}
	}
}
