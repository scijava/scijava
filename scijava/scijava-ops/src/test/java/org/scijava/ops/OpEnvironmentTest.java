package org.scijava.ops;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.Priority;
import org.scijava.functions.Producer;
import org.scijava.ops.matcher.OpClassInfo;
import org.scijava.param.Parameter;
import org.scijava.struct.ItemIO;
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

	@Test
	public void testClassOpification() {
		OpInfo opifyOpInfo = ops.env().opify(OpifyOp.class);
		Assert.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assert.assertEquals(Priority.NORMAL, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testClassOpificationWithPriority() {
		OpInfo opifyOpInfo = ops.env().opify(OpifyOp.class, Priority.HIGH);
		Assert.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assert.assertEquals(Priority.HIGH, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testRegister() {
		String opName = "test.opifyOp";
		OpInfo opifyOpInfo = ops.env().opify(OpifyOp.class, Priority.HIGH);
		ops.env().register(opifyOpInfo, opName);

		String actual = ops.op(opName).input().outType(String.class).create();

		String expected = new OpifyOp().getString();
		Assert.assertEquals(expected, actual);
	}

}

/**
 * Test class to be opified (and added to the {@link OpEnvironment})
 *
 * TODO: remove @Parameter annotation when it is no longer necessary
 *
 * @author Gabriel Selzer
 */
@Parameter(key = "output")
class OpifyOp implements Producer<String> {

	@Override
	public String create() {
		return getString();
	}

	public String getString() {
		return "This Op tests opify!";
	}

}
