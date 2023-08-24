
package org.scijava.ops.engine.yaml.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.AbstractTestEnvironment;

/**
 * Tests discovery of YAML discovery implementations
 *
 * @author Gabriel Selzer
 */
public class YAMLOpTest extends AbstractTestEnvironment {

	/**
	 * Create an {@link OpEnvironment} that discovers only YAML-declared Ops
	 */
	@BeforeAll
	public static void setup() {
		ops.discoverUsing(new YAMLOpInfoDiscoverer());
	}

	@Test
	public void testYAMLClass() {
		Double sum = ops.op("example.add").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(5., sum, 1e-6);
	}

	@Test
	public void testYAMLInnerClass() {
		Double quot = ops.op("example.div").arity2().input(24., 8.).outType(Double.class)
			.apply();
		Assertions.assertEquals(3., quot, 1e-6);
	}

	@Test
	public void testYAMLMethod() {
		Double sum = ops.op("example.sub").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(-1., sum, 1e-6);
	}

	@Test
	public void testYAMLField() {
		Double sum = ops.op("example.mul").arity2().input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(6., sum, 1e-6);
	}

}
