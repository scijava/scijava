/*
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

package org.scijava.ops.image.threshold.apply;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import net.imglib2.RandomAccessibleInterval;
import org.junit.jupiter.api.Assertions;
import org.scijava.ops.api.OpMatchingException;
import org.scijava.ops.image.threshold.AbstractThresholdTest;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.jupiter.api.Test;
import org.scijava.types.Nil;

/**
 * Tests {@link ApplyConstantThreshold} and its wrappers.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class ApplyConstantThresholdTest extends AbstractThresholdTest {

	@Test
	public void testApplyThreshold() throws IncompatibleTypeException {
        final var out = bitmap();
		final var threshold = new UnsignedShortType(30000);
		final Comparator<UnsignedShortType> comparator =  //
				(c1, c2) -> (int) Math.ceil(c1 .getRealDouble() - c2.getRealDouble());
		ops.op("threshold.apply").input(in, threshold, comparator).output(out).compute();
		assertCount(out, 54);
	}

	@Test
	public void testApplyThresholdRAIs() {
		// Test as Computer
		final var buffer = bitmap();
		Assertions.assertDoesNotThrow(() -> ops.op("threshold.mean") //
				.input(in) //
				.output(buffer) //
				.compute());
		// Test as Function
		Assertions.assertDoesNotThrow(() -> ops.op("threshold.mean") //
				.input(in) //
				.apply());
	}

	@Test
	public void testApplyThresholdIterables() {
		List<UnsignedShortType> itr = in.stream().collect(Collectors.toList());
		List<UnsignedShortType> output = new ArrayList<>(itr.size());

		ops.op("threshold.mean") //
				.input(in) //
				.output(output) //
				.compute();
	}

}
