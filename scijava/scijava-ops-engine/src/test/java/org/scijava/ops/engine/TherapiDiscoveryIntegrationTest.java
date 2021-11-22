package org.scijava.ops.engine;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.impl.TagBasedOpInfoDiscoverer;
import org.scijava.parse2.Parser;

public class TherapiDiscoveryIntegrationTest {

	@Test
	public void opDiscoveryRegressionIT() {
		Parser p = ServiceLoader.load(Parser.class).findFirst().get();
		final Discoverer d = new TagBasedOpInfoDiscoverer();
		final List<OpInfo> discoveries = d.discover(OpInfo.class);
		Assert.assertEquals(5, discoveries.size());
	}

}
