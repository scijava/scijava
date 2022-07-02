package org.scijava.ops.engine;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
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
		final List<Op> discoveries = d.discover(Op.class);
		Assertions.assertEquals(235, discoveries.size());

		@SuppressWarnings("unused")
		final Logger l = new StderrLoggerFactory().create();
		final OpInfoGenerator g = new OpClassBasedClassOpInfoGenerator();
		final List<OpInfo> infos = discoveries.stream() //
				.flatMap(c -> g.generateInfosFrom(c).stream()) //
				.collect(Collectors.toList());
		Assertions.assertEquals(235, infos.size());
	}

	@Test
	public void opCollectionDiscoveryRegressionIT() {
		final Discoverer d = Discoverer.using(ServiceLoader::load);
		final List<OpCollection> discoveries = d.discover(OpCollection.class);
		Assertions.assertEquals(16, discoveries.size());
		@SuppressWarnings("unused")
		final Logger l = new StderrLoggerFactory().create();
		final OpInfoGenerator g = new OpCollectionInfoGenerator();
		final List<OpInfo> infos = discoveries.stream() //
				.flatMap(c -> g.generateInfosFrom(c).stream()) //
				.collect(Collectors.toList());
		Assertions.assertEquals(264, infos.size());
	}

}
