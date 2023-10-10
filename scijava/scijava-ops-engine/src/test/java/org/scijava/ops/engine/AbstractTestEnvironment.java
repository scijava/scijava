
package org.scijava.ops.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

public abstract class AbstractTestEnvironment {

	protected static OpEnvironment ops;

	@BeforeAll
	public static void setUp() {
		ops = barebonesEnvironment();
	}

	@AfterAll
	public static void tearDown() {
		ops = null;
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
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load).except( //
			Op.class, //
			OpInfo.class, //
			OpCollection.class //
		);
		return OpEnvironment.getEnvironment(serviceLoading);
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
