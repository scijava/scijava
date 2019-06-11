package org.scijava.ops.stats;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Functions;

public class MeanTest <N extends Number> extends AbstractTestEnvironment{

	@Test
	public void regressionTest() {

		Function<Iterable<Integer>, Double> goodFunc = Functions.unary(ops(), "stats.mean", new Nil<Iterable<Integer>>() {}, new Nil<Double>() {});

		List<Integer> goodNums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
		double expected = 5.5;

		double actual = goodFunc.apply(goodNums);

		assertEquals(expected, actual, 0);
	}

}
