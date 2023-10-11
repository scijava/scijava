
package org.scijava.ops.engine.matcher.impl;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.exceptions.impl.InstanceOpMethodException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;

public class OpMethodInfoTest {

	private static Boolean privateOp() {
		return true;
	}

	@Test
	public void testPrivateMethod() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("privateOp");
		Assertions.assertThrows(PrivateOpException.class, //
			() -> new OpMethodInfo( //
				m, //
				Producer.class, //
				new Hints(), //
				"privateOp"));
	}

	public Boolean instanceOp() {
		return true;
	}

	@Test
	public void testInstanceMethod() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("instanceOp");
		Assertions.assertThrows(InstanceOpMethodException.class, //
			() -> new OpMethodInfo( //
				m, //
				Producer.class, //
				new Hints(), //
				"instanceOp"));
	}

	public static Boolean staticOp() {
		return true;
	}

	@Test
	public void testNonFuncIFace() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("staticOp");
		Assertions.assertThrows(FunctionalTypeOpException.class, () ->
			new OpMethodInfo( //
				m, //
				getClass(), //
				new Hints(), //
				"staticOp"));
	}

	@Test
	public void testWrongOpType() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("staticOp");
		Assertions.assertThrows(FunctionalTypeOpException.class, () ->
			new OpMethodInfo( //
					m, //
					Function.class, //
					new Hints(), //
					"staticOp"));
	}

	public static void mutateDouble(final Double input, final Double io) {}

	@Test
	public void testImmutableOutput() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod("mutateDouble", Double.class, Double.class);
		Assertions.assertThrows(FunctionalTypeOpException.class, () ->
				new OpMethodInfo( //
						m, //
						Computers.Arity1.class, //
						new Hints(), //
						"mutateDouble"));
	}
}
