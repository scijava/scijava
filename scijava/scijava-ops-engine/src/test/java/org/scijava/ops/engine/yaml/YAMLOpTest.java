
package org.scijava.ops.engine.yaml;

import java.util.Arrays;
import java.util.ServiceLoader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.discovery.Discoverer;
import org.scijava.ops.api.InfoChainGenerator;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfoGenerator;
import org.scijava.ops.api.OpWrapper;
import org.scijava.ops.api.features.MatchingRoutine;
import org.scijava.ops.engine.impl.DefaultOpEnvironment;

/**
 * Tests discovery via YAML metadata.
 *
 * @author Gabriel Selzer
 */
public class YAMLOpTest {

	private static OpEnvironment env;

	/**
	 * Create an {@link OpEnvironment} that discovers only YAML-declared Ops
	 */
	@BeforeAll
	public static void setup() {
		// Discover internal stuff normally
		Discoverer serviceLoading = Discoverer.using(ServiceLoader::load) //
			.onlyFor( //
				OpWrapper.class, //
				MatchingRoutine.class, //
				OpInfoGenerator.class, //
				InfoChainGenerator.class //
			);
		// Add in a YAML discoverer for Ops
		env = new DefaultOpEnvironment(Arrays.asList(new YAMLOpInfoDiscoverer(),
			serviceLoading));
	}

	@Test
	public void testYAMLClass() {
		Double sum = env.op("example.add").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(5., sum, 1e-6);
	}

	@Test
	public void testYAMLInnerClass() {
		Double quot = env.op("example.div").arity2().input(24., 8.).outType(Double.class)
			.apply();
		Assertions.assertEquals(3., quot, 1e-6);
	}

	@Test
	public void testYAMLMethod() {
		Double sum = env.op("example.sub").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(-1., sum, 1e-6);
	}

	@Test
	public void testYAMLFunction() {
		Double sum = env.op("example.mul").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(6., sum, 1e-6);
	}

}
