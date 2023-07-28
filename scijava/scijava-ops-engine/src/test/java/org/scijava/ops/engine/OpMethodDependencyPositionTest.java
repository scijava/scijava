
package org.scijava.ops.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.common3.validity.ValidityException;
import org.scijava.function.Computers;
import org.scijava.ops.engine.hint.DefaultHints;
import org.scijava.ops.engine.matcher.impl.OpMethodInfo;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

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
			new DefaultHints(), //
			"test.dependencyBeforeInput" //
		);
		Assertions.assertEquals(null, info.getValidityException());
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
		var info = new OpMethodInfo( //
					m, //
					Computers.Arity1.class, //
					new DefaultHints(), //
					"test.dependencyAfterInput" //
		);
		ValidityException exc = info.getValidityException();
		String expMsg = "java.lang.IllegalArgumentException: Op Dependencies in " +
			"static methods must come before any other parameters!";
		Assertions.assertEquals(expMsg, exc.problems().get(0).getMessage());
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
		var info = new OpMethodInfo( //
				m, //
				Computers.Arity1.class, //
				new DefaultHints(), //
				"test.dependencyBeforeAndAfterInput" //
		);
		ValidityException exc = info.getValidityException();
		String expMsg = "java.lang.IllegalArgumentException: Op Dependencies in " +
				"static methods must come before any other parameters!";
		Assertions.assertEquals(expMsg, exc.problems().get(0).getMessage());
	}

}
