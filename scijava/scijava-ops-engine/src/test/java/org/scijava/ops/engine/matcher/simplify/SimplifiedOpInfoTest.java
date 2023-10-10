
package org.scijava.ops.engine.matcher.simplify;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.Hints;
import org.scijava.ops.api.OpRequest;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.impl.DefaultOpRequest;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.types.Types;

public class SimplifiedOpInfoTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.registerInfosFrom(new PrimitiveSimplifiers());
		ops.registerInfosFrom(new PrimitiveArraySimplifiers());
		ops.register(new Identity<>());
	}

	static class SimpleOp implements BiFunction<Double, Double, Double[]> {

		@Override
		public Double[] apply(Double t, Double u) {
			return new Double[] { t, u };
		}
	}

	@Test
	public void testSimplifiedDescription() {
		OpClassInfo info = new OpClassInfo(SimpleOp.class, new Hints(),
				"test.simplifiedDescription");
		// NB it's a lot easier to let the framework create our SimplifiedOpInfo
		Type opType = Types.parameterize(BiFunction.class, new Type[] {
				Integer.class, Integer.class, Integer[].class });
		OpRequest
				simpleReq = new DefaultOpRequest("test.adaptationDescription", opType,
				Integer[].class, new Type[] { Integer.class, Integer.class });
		SimplifiedOpInfo simplified = new InfoSimplificationGenerator(info, ops)
				.generateSuitableInfo(ops, simpleReq, new Hints());
		String expected = "test.simplifiedDescription(\n\t " //
				+
				"Inputs:\n\t\tjava.lang.Integer input1\n\t\tjava.lang.Integer input2\n\t " //
				+ "Outputs:\n\t\tjava.lang.Integer[] output1\n)\n";
		String actual = simplified.toString();
		Assertions.assertEquals(expected, actual);
	}

}
