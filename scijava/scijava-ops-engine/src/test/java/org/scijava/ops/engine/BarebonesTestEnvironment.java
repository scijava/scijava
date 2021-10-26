
package org.scijava.ops.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.StaticDiscoverer;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLogFactory;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpHistory;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;
import org.scijava.ops.engine.impl.DefaultOpHistory;
import org.scijava.ops.engine.impl.OpClassBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.OpCollectionInfoGenerator;
import org.scijava.ops.engine.impl.PluginBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.TagBasedOpInfoGenerator;
import org.scijava.ops.engine.matcher.impl.AdaptationMatchingRoutine;
import org.scijava.ops.engine.matcher.impl.OpWrappers;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.ops.engine.matcher.impl.SimplificationMatchingRoutine;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public abstract class BarebonesTestEnvironment {

	protected static List<Class<? extends MatchingRoutine>> routines() {
		return Arrays.asList( //
			RuntimeSafeMatchingRoutine.class, //
			AdaptationMatchingRoutine.class, //
			SimplificationMatchingRoutine.class //
		);
	}

	protected static List<Class<?>> opContainingClasses() {
		return Collections.emptyList();
	}

	protected static OpEnvironment ops;
	protected static OpHistory history;
	protected static Logger logger;
	protected static TypeReifier types;
	protected static StaticDiscoverer discoverer;

	@BeforeClass
	public static void setUp() {
		logger = new StderrLogFactory().create();
		types = new DefaultTypeReifier(logger, Discoverer.using(
			ServiceLoader::load));
		ops = barebonesEnvironment(routines(), opContainingClasses());
	}

	@AfterClass
	public static void tearDown() {
		ops = null;
		logger = null;
	}

	protected static OpEnvironment barebonesEnvironment(
		List<Class<? extends MatchingRoutine>> routines, List<Class<?>> opClasses)
	{
		// register needed classes in StaticDiscoverer
		discoverer = new StaticDiscoverer();
		discoverer.registerAll(routines, "matchingroutine");
		discoverer.registerAll(OpWrappers.class.getDeclaredClasses(), "opwrapper");
		discoverer.registerAll(opClasses, "");
		// register possibly useful OpInfoGenerators
		List<OpInfoGenerator> generators = new ArrayList<>();
		generators.add(new OpCollectionInfoGenerator(logger, discoverer));
		generators.add(new OpClassBasedClassOpInfoGenerator(logger, discoverer));
		generators.add(new PluginBasedClassOpInfoGenerator(logger, discoverer));
		generators.add(new TagBasedOpInfoGenerator(logger, discoverer));

		history = new DefaultOpHistory();
		// return Op Environment
		return new DefaultOpEnvironment(types, logger, history,
			generators, discoverer);
	}

	protected static boolean arrayEquals(double[] arr1, Double... arr2) {
		return Arrays.deepEquals(Arrays.stream(arr1).boxed().toArray(Double[]::new), arr2);
	}

	protected static <T> void assertIterationsEqual(final Iterable<T> expected,
		final Iterable<T> actual)
	{
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue("Fewer elements than expected", a.hasNext());
			assertEquals(e.next(), a.next());
		}
		assertFalse("More elements than expected", a.hasNext());
	}

}
