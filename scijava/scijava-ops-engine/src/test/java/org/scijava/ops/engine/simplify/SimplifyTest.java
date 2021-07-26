
package org.scijava.ops.engine.simplify;

import static org.junit.Assert.assertEquals;

import java.util.function.BiFunction;

import org.junit.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * Basic simplify test
 * 
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
@Plugin(type = OpCollection.class)
public class SimplifyTest extends AbstractTestEnvironment {

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Double, Double, Double> powOp = (b, e) -> Math.pow(b,
		e);

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Long, Long, Double> powOpL = (b, e) -> Math.pow(b, e);

	@OpField(names = "test.math.powDouble", params = "base, exponent, result")
	public final BiFunction<Integer[], Double, Double> powOpArray = (b, e) -> Math.pow(b[0], e);

	@Test
	public void testSimplify() {
		Integer number = 2;
		Integer exponent = 2;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}

	@Test
	public void testSimplifySome() {
		Integer number = 2;
		Double exponent = 2.;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(4.0, result, 0);
	}
	
	@Test
	public void testSimplifyArray() {
		Byte[] number = {2};
		Double exponent = 3.;
		Double result = ops.op("test.math.powDouble").input(number, exponent)
			.outType(Double.class).apply();
		assertEquals(8.0, result, 0);
	}

	@Test
	public void testSimplifiedOp() {
		BiFunction<Number, Number, Double> numFunc = ops.op("test.math.powDouble")
			.inType(Number.class, Number.class).outType(Double.class).function();
		
		Double result = numFunc.apply(3., 4.);
		assertEquals(81., result, 0);
	}

}
