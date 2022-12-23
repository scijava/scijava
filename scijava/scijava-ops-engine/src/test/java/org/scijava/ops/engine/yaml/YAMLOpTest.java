
package org.scijava.ops.engine.yaml;

import java.util.Arrays;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.ops.engine.yaml.ops.YAMLClassOp;

public class YAMLOpTest {

	@Test
	public void testYAMLClass() {
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
			.onlyFor( //
				OpWrapper.class, //
				MatchingRoutine.class, //
				OpInfoGenerator.class, //
				InfoChainGenerator.class //
			);
		OpEnvironment env = new DefaultOpEnvironment(Arrays.asList(
			new YAMLDiscoverer(), serviceLoading));
		Double sum = env.op("example.add").input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(5., sum, 1e-6);
	}

	@Test
	public void testYAMLInnerClass() {
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
				.onlyFor( //
						OpWrapper.class, //
						MatchingRoutine.class, //
						OpInfoGenerator.class, //
						InfoChainGenerator.class //
				);
		OpEnvironment env = new DefaultOpEnvironment(Arrays.asList(
				new YAMLDiscoverer(), serviceLoading));
		Double quot = env.op("example.div").input(24., 8.).outType(Double.class)
				.apply();
		Assertions.assertEquals(3., quot, 1e-6);
	}

	@Test
	public void testYAMLMethod() {
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
				.onlyFor( //
						OpWrapper.class, //
						MatchingRoutine.class, //
						OpInfoGenerator.class, //
						InfoChainGenerator.class //
				);
		OpEnvironment env = new DefaultOpEnvironment(Arrays.asList(
				new YAMLDiscoverer(), serviceLoading));
		Double sum = env.op("example.sub").input(2., 3.).outType(Double.class)
				.apply();
		Assertions.assertEquals(-1., sum, 1e-6);
	}

	@Test
	public void testYAMLFunction() {
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
				.onlyFor( //
						OpWrapper.class, //
						MatchingRoutine.class, //
						OpInfoGenerator.class, //
						InfoChainGenerator.class //
				);
		OpEnvironment env = new DefaultOpEnvironment(Arrays.asList(
				new YAMLDiscoverer(), serviceLoading));
		Double sum = env.op("example.mul").input(2., 3.).outType(Double.class)
				.apply();
		Assertions.assertEquals(6., sum, 1e-6);
	}

}
