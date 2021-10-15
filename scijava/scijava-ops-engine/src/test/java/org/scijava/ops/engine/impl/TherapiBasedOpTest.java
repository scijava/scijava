package org.scijava.ops.engine.impl;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.function.Producer;
import org.scijava.ops.engine.AbstractTestEnvironment;

public class TherapiBasedOpTest extends AbstractTestEnvironment {

	private static final String FIELD_STRING = "This OpField is discoverable using Therapi!";
	static final String CLASS_STRING = "This OpClass is discoverable using Therapi!";
	private static final String METHOD_STRING = "This OpMethod is discoverable using Therapi!";

	/**
	 * @implNote op names='test.therapiOpField'
	 */
	public final Producer<String> therapiFunction = () -> FIELD_STRING;

	@Test
	public void therapiOpFieldTest() {
		String actual = ops.op("test.therapiOpField").input().outType(String.class).create();
		String expected = FIELD_STRING;
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void therapiOpClassTest() {
		String actual = ops.op("test.therapiOpClass").input().outType(String.class).create();
		String expected = CLASS_STRING;
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void therapiOpMethodTest() {
		String actual = ops.op("test.therapiOpMethod").input().outType(String.class).create();
		String expected = METHOD_STRING;
		Assert.assertEquals(expected, actual);
	}

	/**
	 * @implNote op names='test.therapiOpMethod',
	 *           type='org.scijava.function.Producer'
	 * @return a {@link String}
	 */
	public static String therapiMethod() {
		return METHOD_STRING;
	}

	private static final String HIGH_PRIORITY_STRING = "High Priority";
	private static final String LOW_PRIORITY_STRING = "Low Priority";

	/**
	 * @implNote op names='test.therapiPriority', priority='10.0'
	 */
	public final Producer<String> therapiHighPriorityFunction = () -> HIGH_PRIORITY_STRING;

	/**
	 * @implNote op names='test.therapiPriority', priority='1.0'
	 */
	public final Producer<String> therapiLowPriorityFunction = () -> LOW_PRIORITY_STRING;

	@Test
	public void therapiOpFieldPriorityTest() {
		String actual = ops.op("test.therapiPriority").input().outType(String.class).create();
		String expected = HIGH_PRIORITY_STRING;
		Assert.assertEquals(expected, actual);
	}

}

/**
 * @implNote op names='test.therapiOpClass'
 */
class TherapiOpClass implements Producer<String> {

	@Override
	public String create() {
		return TherapiBasedOpTest.CLASS_STRING;
	}
	
}
