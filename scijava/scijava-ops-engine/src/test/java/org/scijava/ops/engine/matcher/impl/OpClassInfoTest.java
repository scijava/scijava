
package org.scijava.ops.engine.matcher.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Container;
import org.scijava.function.Producer;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FinalOpDependencyFieldException;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.spi.OpDependency;

public class OpClassInfoTest {

	static class FinalOpDependency implements Producer<String> {

		@OpDependency(name = "foo")
		public final Producer<Boolean> foo = null;

		@Override
		public String create() {
			return "This Op has a final OpDependency";
		}
	}

	@Test
	public void testFinalDependency() {
		Assertions.assertThrows(FinalOpDependencyFieldException.class, () -> //
		new OpClassInfo( //
			FinalOpDependency.class, //
			new Hints(), //
			"finalDependency" //
		));
	}

	static class ImmutableOutput implements Computers.Arity1<Double, Double> {

		@Override
		public void compute(Double in, @Container Double out) {}
	}

	@Test
	public void testImmutableOutput() {
		Assertions.assertThrows(FunctionalTypeOpException.class, () ->
			new OpClassInfo( //
					ImmutableOutput.class, //
					new Hints(), //
					"finalDependency" //
			));
	}

}
