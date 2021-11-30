
package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.ManualDiscoverer;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLoggerFactory;
import org.scijava.ops.api.*;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;
import org.scijava.ops.engine.impl.DefaultOpHistory;
import org.scijava.types.DefaultTypeReifier;
import org.scijava.types.TypeReifier;

public abstract class AbstractTestEnvironment {

	protected static OpEnvironment ops;
	protected static OpHistory history;
	protected static Logger logger;
	protected static TypeReifier types;

	@BeforeAll
	public static void setUp() {
		logger = new StderrLoggerFactory().create();
		types = new DefaultTypeReifier(logger, Discoverer.using(ServiceLoader::load));
		ops = barebonesEnvironment();
	}

	@AfterAll
	public static void tearDown() {
		ops = null;
		logger = null;
	}

	protected static <T> Optional<T> objFromNoArgConstructor(Class<T> c) {
		try {
			return Optional.of(c.getDeclaredConstructor().newInstance());
		} catch (Throwable t) {
			return Optional.empty();
		}
	}

	protected static Object[] objsFromNoArgConstructors(Class<?>[] arr) {
		return Arrays.stream(arr) //
				.map(AbstractTestEnvironment::objFromNoArgConstructor) //
				.filter(Optional::isPresent) //
				.map(Optional::get) //
				.toArray();
	}

	protected static OpEnvironment barebonesEnvironment() {
		// register needed classes in StaticDiscoverer
		ManualDiscoverer discoverer = new ManualDiscoverer();

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

	protected static <T> void assertIterationsEqual(final Iterable<T> expected, final Iterable<T> actual) {
		final Iterator<T> e = expected.iterator();
		final Iterator<T> a = actual.iterator();
		while (e.hasNext()) {
			assertTrue(a.hasNext(), "Fewer elements than expected");
			assertEquals(e.next(), a.next());
		}
		assertFalse(a.hasNext(), "More elements than expected");
	}

}
