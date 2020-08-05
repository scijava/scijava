package org.scijava.ops;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.types.GenericTyped;
import org.scijava.types.Nil;

/**
 * Test class for {@link OpEnvironment} methods. NB this class does not test any
 * <em>particular</em> implementation of {@link OpEnvironment}, but instead
 * ensures expected behavior in the {@link OpEnvironment} returned by the
 * {@link OpService} (which will be nearly exclusively the only OpEnvironment
 * implementation used)
 * 
 * @author Gabriel Selzer
 */
public class OpEnvironmentTest extends AbstractTestEnvironment{
	
	@Test
	public void testBakeType() {
		Function<Double, Double> func = (in) -> in * 2;
		Type funcType = new Nil<Function<Double, Double>>() {}.getType();

		Function<Double, Double> wrappedFunction = ops.env().bakeLambdaType(func,
			funcType);

		Assert.assertTrue("wrappedFunction should be a GenericTyped but is not!",
			wrappedFunction instanceof GenericTyped);
		Type type = ((GenericTyped) wrappedFunction).getType();
		Assert.assertEquals("wrappedFunction type " + type +
			"is not equivalent to the provided type " + funcType + "!", funcType,
			type);
	}

}
