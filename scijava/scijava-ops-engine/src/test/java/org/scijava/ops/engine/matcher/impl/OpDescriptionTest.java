
package org.scijava.ops.engine.matcher.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpRef;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.reduce.ReducedOpInfo;
import org.scijava.ops.engine.simplify.Identity;
import org.scijava.ops.engine.simplify.InfoSimplificationGenerator;
import org.scijava.ops.engine.simplify.PrimitiveArraySimplifiers;
import org.scijava.ops.engine.simplify.PrimitiveSimplifiers;
import org.scijava.ops.engine.simplify.SimplifiedOpInfo;
import org.scijava.types.Types;

public class OpDescriptionTest extends AbstractTestEnvironment {

	@BeforeAll
	public static void addNeededOps() {
		ops.registerInfosFrom(new PrimitiveSimplifiers());
		ops.registerInfosFrom(new PrimitiveArraySimplifiers());
		ops.registerInfosFrom(new CopyOpCollection<>());
		ops.register(new Identity<>());
	}

	static class ClassOp implements BiFunction<Double, Double, Double> {

		@Override
		public Double apply(Double t, Double u) {
			return t + u;
		}
	}

	@Test
	public void testOpClassDescription() {
		OpClassInfo info = new OpClassInfo(ClassOp.class, new DefaultHints(),
			"test.classDescription");

		String expected = "test.classDescription(\n\t " //
			+ "Inputs:\n\t\tjava.lang.Double input1\n\t\tjava.lang.Double input2\n\t " //
			+ "Outputs:\n\t\tjava.lang.Double output1\n)\n";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	public static Double methodOp(Double in1, Double in2) {
		return in1 + in2;
	}

	@Test
	public void testOpMethodDescription() throws NoSuchMethodException {
		Method method = OpDescriptionTest.class.getMethod("methodOp", Double.class,
			Double.class);
		OpMethodInfo info = new OpMethodInfo(method, BiFunction.class,
			new DefaultHints(), "test.methodDescription");
		String expected = "test.methodDescription(\n\t " //
			+ "Inputs:\n\t\tjava.lang.Double input1\n\t\tjava.lang.Double input2\n\t " //
			+ "Outputs:\n\t\tjava.lang.Double output1\n)\n";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	public final BiFunction<Double, Double, Double> fieldOp = Double::sum;

	@Test
	public void testOpFieldDescription() throws NoSuchFieldException {
		Field field = OpDescriptionTest.class.getDeclaredField("fieldOp");
		OpFieldInfo info = new OpFieldInfo(this, field, new DefaultHints(),
			"test.fieldDescription");
		String expected = "test.fieldDescription(\n\t " //
			+ "Inputs:\n\t\tjava.lang.Double input1\n\t\tjava.lang.Double input2\n\t " //
			+ "Outputs:\n\t\tjava.lang.Double output1\n)\n";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testAdaptedDescription() {
		OpClassInfo info = new OpClassInfo(ClassOp.class, new DefaultHints(),
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

	static class SimpleOp implements BiFunction<Double, Double, Double[]> {

		@Override
		public Double[] apply(Double t, Double u) {
			return new Double[] { t, u };
		}
	}

	@Test
	public void testSimplifiedDescription() {
		OpClassInfo info = new OpClassInfo(SimpleOp.class, new DefaultHints(),
			"test.simplifiedDescription");
		// NB it's a lot easier to let the framework create our SimplifiedOpInfo
		Type opType = Types.parameterize(BiFunction.class, new Type[] {
			Integer.class, Integer.class, Integer[].class });
		OpRef simpleRef = new DefaultOpRef("test.adaptationDescription", opType,
			Integer[].class, new Type[] { Integer.class, Integer.class });
		SimplifiedOpInfo simplified = new InfoSimplificationGenerator(info, ops)
			.generateSuitableInfo(ops, simpleRef, new DefaultHints());
		String expected = "test.simplifiedDescription(\n\t " //
			+
			"Inputs:\n\t\tjava.lang.Integer input1\n\t\tjava.lang.Integer input2\n\t " //
			+ "Outputs:\n\t\tjava.lang.Integer[] output1\n)\n";
		String actual = simplified.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testReducedDescription() {
		OpClassInfo info = new OpClassInfo(ClassOp.class, new DefaultHints(),
			"test.reductionDescription");

		Type opType = Types.parameterize(Function.class, new Type[] { Double.class,
			Double.class });
		ReducedOpInfo reduced = new ReducedOpInfo(info, opType, 1);
		String expected = "test.reductionDescription(\n\t " //
			+ "Inputs:\n\t\tjava.lang.Double input1\n\t " //
			+ "Outputs:\n\t\tjava.lang.Double output1\n)\n";
		String actual = reduced.toString();
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testMultiNameOp() {
		OpClassInfo info = new OpClassInfo(ClassOp.class, new DefaultHints(),
			"test.classDescription", "test.otherName");

		String expected = "test.classDescription(\n\t " //
			+ "Inputs:\n\t\tjava.lang.Double input1\n\t\tjava.lang.Double input2\n\t " //
			+ "Outputs:\n\t\tjava.lang.Double output1\n)\n" //
			+ "Aliases: [test.otherName]\n";
		String actual = info.toString();
		Assertions.assertEquals(expected, actual);

	}

}
