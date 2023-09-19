package org.scijava.ops.engine.impl;

import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.function.Producer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.parse2.Parser;

public class TherapiBasedOpTest {

	protected static OpEnvironment ops;
	protected static Parser parser;

	@BeforeAll
	public static void setUp() {
		parser = ServiceLoader.load(Parser.class).findFirst().get();
		ops = barebonesEnvironment();
		ops.registerInfosFrom(new TherapiBasedOpTest());
		ops.registerInfosFrom(new TherapiOpClass());
	}

	@AfterAll
	public static void tearDown() {
		ops = null;
	}

	protected static OpEnvironment barebonesEnvironment()
	{
		// register needed classes in StaticDiscoverer
		Discoverer d2 = Discoverer.using(ServiceLoader::load).onlyFor( //
				OpWrapper.class, //
				MatchingRoutine.class, //
				OpInfoGenerator.class //
		);
		ops = OpEnvironment.getEnvironment(d2);
		return ops;
	}

	private static final String FIELD_STRING = "This OpField is discoverable using Therapi!";
	static final String CLASS_STRING = "This OpClass is discoverable using Therapi!";
	private static final String METHOD_STRING = "This OpMethod is discoverable using Therapi!";

	/**
	 * @implNote op names='test.therapiOpField'
	 */
	public final Producer<String> therapiFunction = () -> FIELD_STRING;

	@Test
	public void therapiOpFieldTest() throws NoSuchFieldException {
		String actual = ops.op("test.therapiOpField").arity0().outType(String.class).create();
		String expected = FIELD_STRING;
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void therapiOpClassTest() {
		String actual = ops.op("test.therapiOpClass").arity0().outType(String.class).create();
		String expected = CLASS_STRING;
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void therapiOpMethodTest() {
		String actual = ops.op("test.therapiOpMethod").arity0().outType(String.class).create();
		String expected = METHOD_STRING;
		Assertions.assertEquals(expected, actual);
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
		String actual = ops.op("test.therapiPriority").arity0().outType(String.class).create();
		String expected = HIGH_PRIORITY_STRING;
		Assertions.assertEquals(expected, actual);
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
