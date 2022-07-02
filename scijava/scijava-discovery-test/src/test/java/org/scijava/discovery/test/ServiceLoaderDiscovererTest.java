
package org.scijava.discovery.test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

public class ServiceLoaderDiscovererTest {

	@Test
	public void testServiceLoaderWithBoth() {
		Discoverer d = Discoverer.using(ServiceLoader::load);
		assertDiscoveryRequirements(d, OpCollection.class, ServiceBasedMultipliers.class);
		assertDiscoveryRequirements(d, Op.class, ServiceBasedAdder.class);
	}

	@SafeVarargs
	private static <T> void assertDiscoveryRequirements(Discoverer d, Class<T> discovery, Class<? extends T>... impls) {
		List<Class<T>> implementingClasses = d.discover(discovery).stream().map(o -> (Class<T>) o.getClass()).collect(
				Collectors.toList());
		for(Class<? extends T> cls : impls)
			Assertions.assertTrue(implementingClasses.contains(cls));

			Assertions.assertEquals(impls.length, implementingClasses.size());
	}
}
