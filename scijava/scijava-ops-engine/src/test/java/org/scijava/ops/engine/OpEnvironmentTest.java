
package org.scijava.ops.engine;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.priority.Priority;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;

/**
 * Test class for {@link OpEnvironment} methods. NB this class does not test any
 * <em>particular</em> implementation of {@link OpEnvironment}, but instead
 * ensures expected behavior in the {@link OpEnvironment} returned by the
 * {@link OpService} (which will be nearly exclusively the only OpEnvironment
 * implementation used)
 * 
 * @author Gabriel Selzer
 */
public class OpEnvironmentTest extends AbstractTestEnvironment {

	@Test
	public void testClassOpification() {
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class);
		Assertions.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.NORMAL, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testClassOpificationWithPriority() {
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class, Priority.HIGH);
		Assertions.assertEquals(OpifyOp.class.getName(), opifyOpInfo.implementationName());
		// assert default priority
		Assertions.assertEquals(Priority.HIGH, opifyOpInfo.priority(), 0.);
	}

	@Test
	public void testRegister() {
		String opName = "test.opifyOp";
		OpInfo opifyOpInfo = ops.opify(OpifyOp.class, Priority.HIGH, opName);
		ops.register(opifyOpInfo);

		String actual = ops.op(opName).input().outType(String.class).create();

		String expected = new OpifyOp().getString();
		Assertions.assertEquals(expected, actual);
	}

}

/**
 * Test class to be opified (and added to the {@link OpEnvironment})
 *
 * TODO: remove @Parameter annotation when it is no longer necessary
 *
 * @author Gabriel Selzer
 */
class OpifyOp implements Producer<String> {

	@Override
	public String create() {
		return getString();
	}

	public String getString() {
		return "This Op tests opify!";
	}

}
