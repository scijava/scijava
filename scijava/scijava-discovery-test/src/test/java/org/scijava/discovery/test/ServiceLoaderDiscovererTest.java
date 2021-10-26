
package org.scijava.discovery.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.test.ServiceBasedAdder;
import org.scijava.discovery.test.ServiceBasedMultipliers;
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
		List<Class<T>> implementingClasses = d.implsOfType(discovery);
		for(Class<? extends T> cls : impls)
			Assert.assertTrue(implementingClasses.contains(cls));

			Assert.assertEquals(impls.length, implementingClasses.size());
	}
}
