/*-
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.yaml.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		Double sum = ops.op("example.add").input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(5., sum, 1e-6);
	}

	@Test
	public void testYAMLInnerClass() {
		Double quot = ops.op("example.div").input(24., 8.).outType(Double.class)
			.apply();
		Assertions.assertEquals(3., quot, 1e-6);
	}

	@Test
	public void testYAMLMethodFunction() {
		Double sum = ops.op("example.sub").input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(-1., sum, 1e-6);
	}

	@Test
	public void testYAMLMethodFunctionNullable() {
		Double sum = ops.op("example.sub").input(2.).outType(Double.class).apply();
		Assertions.assertEquals(2., sum, 1e-6);
	}

	@Test
	public void testYAMLMethodInplaceShortType() {
		List<Integer> l1 = Arrays.asList(1);
		List<Integer> l2 = Arrays.asList(3);
		ops.op("example.xor").input(l1, l2).mutate1();
		Assertions.assertEquals(2, l1.get(0), 1e-6);
	}

	@Test
	public void testYAMLMethodComputerShortType() {
		List<Integer> l1 = Arrays.asList(1);
		List<Integer> l2 = Arrays.asList(3);
		List<Integer> out = new ArrayList<>();
		ops.op("example.and").input(l1, l2).output(out).compute();
		Assertions.assertEquals(1, out.get(0), 1e-6);
	}

	@Test
	public void testYAMLMethodExtraComputerShortType() {
		List<Integer> l1 = Arrays.asList(1);
		List<Integer> l2 = Arrays.asList(2);
		List<Integer> out = new ArrayList<>();
		ops.op("example.or").input(l1, l2).output(out).compute();
		Assertions.assertEquals(3, out.get(0), 1e-6);
	}

	@Test
	public void testYAMLField() {
		Double sum = ops.op("example.mul").input(2., 3.).outType(Double.class)
			.apply();
		Assertions.assertEquals(6., sum, 1e-6);
	}

	@Test
	public void testYAMLDescription() {
		var actual = ops.help("example.mul");
		var expected = "example.mul:\n\t- (Double, Double) -> Double";
		Assertions.assertEquals(expected, actual);
	}

	@Test
	public void testYAMLHints() {
		var infos = ops.infos("example.xor");
		Assertions.assertEquals(1, infos.size());
		var info = infos.iterator().next();
		var hints = info.declaredHints();
		Assertions.assertTrue(hints.containsAll( //
				"Adaptation.FORBIDDEN", //
				"Conversion.FORBIDDEN" //
		));
	}

}
