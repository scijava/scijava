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

package org.scijava.ops.engine.stats;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.api.OpBuilder;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.matcher.convert.IdentityCollection;
import org.scijava.ops.engine.matcher.convert.PrimitiveConverters;
import org.scijava.ops.engine.math.Add;
import org.scijava.ops.engine.math.MathOpCollection;
import org.scijava.types.Nil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link Size}
 */
public class SizeTest<N extends Number> extends AbstractTestEnvironment {

	private static List<Integer> nums;

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new Size());
		nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
	}

	@Test
	public void testSizeUntyped() {
		// Make sure we can call stats without specifying output type
		Long size = (Long) ops.op("stats.size").input(nums).apply();

		assertEquals(10, size);
	}

	@Test
	public void testSizeLong() {
		// Make sure we can call stats with Long output type
		Long size = ops.op("stats.size").input(nums).outType(Long.class).apply();

		assertEquals(10, size);
	}

	@Test
	public void testSizeDouble() {
		// Make sure we can call stats with Double output type
		Double size = ops.op("stats.size").input(nums).outType(Double.class).apply();

		assertEquals(10, size);
	}
}
