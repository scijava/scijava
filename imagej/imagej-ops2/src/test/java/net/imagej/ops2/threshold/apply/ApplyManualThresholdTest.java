/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package net.imagej.ops2.threshold.apply;

import java.util.Comparator;

import net.imagej.ops2.threshold.AbstractThresholdTest;
import net.imglib2.exception.IncompatibleTypeException;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.ops.api.OpBuilder;
import org.scijava.types.Nil;

/**
 * Tests {@link ApplyManualThreshold}.
 * 
 * @author Curtis Rueden
 */
public class ApplyManualThresholdTest extends AbstractThresholdTest {

	@Test
	public void testApplyThreshold() throws IncompatibleTypeException {
		Computers.Arity3<Img<UnsignedShortType>, UnsignedShortType, Comparator<UnsignedShortType>, Iterable<BitType>> createFunc =
			OpBuilder.matchComputer(ops, "threshold.apply",
				new Nil<Img<UnsignedShortType>>()
				{}, new Nil<UnsignedShortType>() {},
				new Nil<Comparator<UnsignedShortType>>()
				{}, new Nil<Iterable<BitType>>() {});

		final Img<BitType> out = bitmap();
		final UnsignedShortType threshold = new UnsignedShortType(30000);
		Comparator<UnsignedShortType> comparator = (c1, c2) -> (int) Math.ceil(c1.getRealDouble() - c2.getRealDouble());
		createFunc.compute(in, threshold, comparator, out);
		assertCount(out, 54);
	}

}
