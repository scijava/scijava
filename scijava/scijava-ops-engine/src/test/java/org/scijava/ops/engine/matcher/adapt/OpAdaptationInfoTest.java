
package org.scijava.ops.engine.matcher.adapt;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.matcher.impl.OpClassInfo;
import org.scijava.types.Types;

public class OpAdaptationInfoTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.registerInfosFrom(new CopyOpCollection<>());
	}

	static class ClassOp implements BiFunction<Double, Double, Double> {

		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	@Test
	public void testAdaptedDescription() {
		OpClassInfo info = new OpClassInfo(ClassOp.class, new Hints(),
				"test.adaptationDescription");
		Type opType = Types.parameterize(Computers.Arity2.class, new Type[] {
				Double.class, Double.class, Double.class });
		OpAdaptationInfo adapted = new OpAdaptationInfo(info, opType, null);
		String expected = "test.adaptationDescription(\n\t " //
				+ "Inputs:\n\t\tjava.lang.Double input1\n\t\tjava.lang.Double input2\n\t " //
				+ "Containers (I/O):\n\t\tjava.lang.Double output1\n)\n";
		String actual = adapted.toString();
		Assertions.assertEquals(expected, actual);
	}
}
