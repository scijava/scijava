package org.scijava.ops.serviceloader;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.discovery.Discoverer;
import org.scijava.ops.spi.Op;

public class ServiceLoaderDiscovererTest {

	@Test
	public void testServiceLoader() {
		Discoverer d = new ServiceLoaderDiscoverer();
		List<Class<Op>> implementingClasses = d.implementingClasses(Op.class);
		Assert.assertTrue(implementingClasses.contains(ServiceBasedAdder.class));
		Assert.assertEquals(implementingClasses.size(), 1);
	}
	

}

