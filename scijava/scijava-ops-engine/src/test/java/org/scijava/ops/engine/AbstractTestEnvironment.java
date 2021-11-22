
package org.scijava.ops.engine;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.StaticDiscoverer;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLoggerFactory;
import org.scijava.ops.api.*;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;
import org.scijava.ops.engine.impl.DefaultOpHistory;
import org.scijava.ops.engine.matcher.impl.AdaptationMatchingRoutine;
import org.scijava.ops.engine.matcher.impl.OpWrappers;
import org.scijava.ops.engine.matcher.impl.RuntimeSafeMatchingRoutine;
import org.scijava.ops.engine.matcher.impl.SimplificationMatchingRoutine;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public abstract class AbstractTestEnvironment {

	protected static List<MatchingRoutine> routines() {
		return Arrays.asList( //
				new RuntimeSafeMatchingRoutine(), //
				new AdaptationMatchingRoutine(), //
				new SimplificationMatchingRoutine() //
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
		logger = new StderrLoggerFactory().create();
		types = new DefaultTypeReifier(logger, Discoverer.using(
			ServiceLoader::load));
		ops = barebonesEnvironment(routines(), opContainingClasses());
	}

	@AfterClass
	public static void tearDown() {
		ops = null;
		logger = null;
	}

	protected static <T> Optional<T> objFromNoArgConstructor(Class<T> c) {
		try {
			return Optional.of(c.getDeclaredConstructor().newInstance());
		}
		catch (Throwable t) {
			return Optional.empty();
		}
	}

	protected static Object[] objsFromNoArgConstructors(Class<?>[] arr) {
		return Arrays.stream(arr).map(c -> objFromNoArgConstructor(c).get()).toArray();
	}

	private static List<OpWrapper<?>> opWrappers() {
		return Arrays.stream((Class<OpWrapper<?>>[]) OpWrappers.class.getDeclaredClasses()) //
				.map(c -> objFromNoArgConstructor(c)) //
				.filter(o -> o.isPresent()) //
				.map(o -> o.get()) //
				.collect(Collectors.toList());
	}

	protected static OpEnvironment barebonesEnvironment(
		List<MatchingRoutine> routines, List<Class<?>> opClasses)
	{
		// register needed classes in StaticDiscoverer
		discoverer = new StaticDiscoverer();
		discoverer.register("", opClasses);

		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
				.onlyFor( //
						OpWrapper.class, //
						MatchingRoutine.class, //
						OpInfoGenerator.class, //
						InfoChainGenerator.class //
				);

		history = new DefaultOpHistory();
		// return Op Environment
		return new DefaultOpEnvironment(types, logger, history, discoverer, serviceLoading);
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
