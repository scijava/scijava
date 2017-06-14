
package org.scijava.ops;

import org.junit.Test;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.param.Parameter;

public class TestOps {

	@Test
	public void testParameterized() {
	}

	// -- Helper classes --

	public static class TruncFunction implements FunctionOp<Double, Integer> {

		@Override
		public Integer apply(final Double t) {
			return t.intValue();
		}
	}

	public static class VariousParameters {

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
