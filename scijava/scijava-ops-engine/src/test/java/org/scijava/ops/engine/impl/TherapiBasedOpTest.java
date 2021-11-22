package org.scijava.ops.engine.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.StaticDiscoverer;
import org.scijava.discovery.therapi.TherapiDiscoverer;
import org.scijava.function.Producer;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLoggerFactory;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.matcher.impl.OpWrappers;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.parse2.Parser;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public class TherapiBasedOpTest {

	protected static OpEnvironment ops;
	protected static OpHistory history;
	protected static Logger logger;
	protected static TypeReifier types;
	protected static Parser parser;
	protected static TherapiDiscoverer discoverer;

	@BeforeClass
	public static void setUp() {
		logger = new StderrLoggerFactory().create();
		types = new DefaultTypeReifier(logger, Discoverer.using(
			ServiceLoader::load));
		parser = ServiceLoader.load(Parser.class).findFirst().get();
		ops = barebonesEnvironment();
	}

	@AfterClass
	public static void tearDown() {
		ops = null;
		logger = null;
	}

	protected static OpEnvironment barebonesEnvironment()

	{
		// register needed classes in StaticDiscoverer
		discoverer = new TagBasedOpInfoDiscoverer();
		Discoverer d2 = Discoverer.using(ServiceLoader::load).onlyFor( //
				OpWrapper.class, //
				MatchingRoutine.class //
		);

		history = new DefaultOpHistory();
		// return Op Environment
		return new DefaultOpEnvironment(types, logger, history,
			discoverer, d2);
	}

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
