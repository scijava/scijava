package org.scijava.ops.engine;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.discovery.Discovery;
import org.scijava.log2.Logger;
import org.scijava.log2.StderrLoggerFactory;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.engine.impl.OpClassBasedClassOpInfoGenerator;
import org.scijava.ops.engine.impl.OpCollectionInfoGenerator;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpCollection;

public class ServiceLoaderDiscoveryIntegrationTest {

	@Test
	public void opDiscoveryRegressionIT() {
		final Discoverer d = Discoverer.using(ServiceLoader::load);
		final List<Discovery<Class<Op>>> discoveries = d.discoveriesOfType(Op.class);
		Assert.assertEquals(235, discoveries.size());

		final Logger l = new StderrLoggerFactory().create();
		final OpInfoGenerator g = new OpClassBasedClassOpInfoGenerator(l, d);
		final List<OpInfo> infos = g.generateInfos();
		Assert.assertEquals(235, infos.size());
	}

	@Test
	public void opCollectionDiscoveryRegressionIT() {
		final Discoverer d = Discoverer.using(ServiceLoader::load);
		final List<Discovery<Class<OpCollection>>> discoveries = d.discoveriesOfType(
				OpCollection.class);
		Assert.assertEquals(16, discoveries.size());
		final Logger l = new StderrLoggerFactory().create();
		final OpInfoGenerator g = new OpCollectionInfoGenerator(l, d);
		final List<OpInfo> infos = g.generateInfos();
		Assert.assertEquals(264, infos.size());
	}

}
