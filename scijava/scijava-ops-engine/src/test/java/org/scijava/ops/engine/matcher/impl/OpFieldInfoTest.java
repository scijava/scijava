
package org.scijava.ops.engine.matcher.impl;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.meta.Versions;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.exceptions.impl.FunctionalTypeOpException;
import org.scijava.ops.engine.exceptions.impl.PrivateOpException;
import org.scijava.ops.spi.OpCollection;

public class OpFieldInfoTest implements OpCollection {

	private final BiFunction<Double, Double, Double> foo = Double::sum;

	@Test
	public void testPrivateField() throws NoSuchFieldException {
		var field = this.getClass().getDeclaredField("foo");
		Assertions.assertThrows(PrivateOpException.class, //
			() -> new OpFieldInfo(//
				this, //
				field, //
				Versions.getVersion(this.getClass()), //
				new Hints(), //
				"foo" //
			));
	}

	public final Computers.Arity1<Double, Double> bar = (in, out) -> out = in;
	
	@Test
	public void testImmutableOutput() throws NoSuchFieldException {
		var field = this.getClass().getDeclaredField("bar");
		Assertions.assertThrows(FunctionalTypeOpException.class, //
				() -> new OpFieldInfo(//
						this, //
						field, //
						Versions.getVersion(this.getClass()), //
						new Hints(), //
						"bar" //
				));
	}
}
