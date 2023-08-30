package org.scijava.ops.engine.impl;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.OpInfo;
import org.scijava.parse2.Parser;

public class TherapiDiscoveryIntegrationTest {

	@Test
	public void opDiscoveryRegressionIT() {
		Parser p = ServiceLoader.load(Parser.class).findFirst().get();
		final Discoverer d = new TherapiOpInfoDiscoverer();
		final List<OpInfo> discoveries = d.discover(OpInfo.class);
		Assertions.assertEquals(5, discoveries.size());
	}

}
