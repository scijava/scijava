
package org.scijava.ops.engine.impl;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.exceptions.impl.OpDependencyPositionException;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;

public class OpMethodDependencyPositionTest extends AbstractTestEnvironment
		implements OpCollection {

	public static void goodDep( //
			@OpDependency(name = "someDep") Function<String, Long> op, //
			List<String> in, //
			List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBefore() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
				"goodDep", //
				Function.class, //
				List.class, //
				List.class //
		);
		var info = new OpMethodInfo( //
				m, //
				Computers.Arity1.class, //
				new Hints(), //
				"test.dependencyBeforeInput" //
		);
	}

	public static void badDep( //
			List<String> in, //
			@OpDependency(name = "someDep") Function<String, Long> op, //
			List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyAfter() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
				"badDep", //
				List.class, //
				Function.class, //
				List.class //
		);
		createInvalidInfo(m, Computers.Arity1.class, "test.dependencyAfterInput");
	}

	public static void goodThenBadDep( //
			@OpDependency(name = "someDep") Function<String, Long> op, //
			List<String> in, //
			@OpDependency(name = "someOtherDep") Function<String, Long> op2, //
			List<Long> out //
	) {
		out.clear();
		for (String s : in)
			out.add(op.apply(s));
	}

	@Test
	public void testOpDependencyBeforeAndAfter() throws NoSuchMethodException {
		var m = this.getClass().getDeclaredMethod(//
				"goodThenBadDep", //
				Function.class, //
				List.class, //
				Function.class, //
				List.class //
		);
		createInvalidInfo(m, Computers.Arity1.class, "test.dependencyBeforeAndAfterInput");
	}

	/**
	 * Helper method for testing ops with dependencies before other params
	 */
	private void createInvalidInfo(Method m, Class<?> arity, String... names) {
		Assertions.assertThrows(OpDependencyPositionException.class,
			() -> new OpMethodInfo( //
				m, //
				arity, //
				new Hints(), //
				names));
	}
}
