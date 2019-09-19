/*-
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

package net.imagej.ops.filter.tubeness;

import java.util.concurrent.ExecutorService;

import net.imagej.ops.AbstractOpTest;
import net.imagej.ops.OpMatchingService;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.cache.CacheService;
import org.scijava.thread.ThreadService;

/**
 * Tests {@link DefaultTubeness}.
 * 
 * @author Gabe Selzer
 */
public class TubenessTest extends AbstractOpTest {

	@Override
	protected Context createContext() {
		return new Context(OpService.class, OpMatchingService.class,
			CacheService.class, StatusService.class, ThreadService.class);
	}

	@Test
	public void testTubeness() {
		Img<UnsignedByteType> input = openUnsignedByteType(DefaultTubeness.class,
			"TubesInput.png");
		Img<DoubleType> expected = openDoubleImg("tube.tif");

		final double scale = 5;
		final double sigma = scale / Math.sqrt(2);

		ExecutorService es = context.getService(ThreadService.class).getExecutorService();
		Img<DoubleType> actual = (Img<DoubleType>) ops.run("create.img", input, new DoubleType());
		ops.run("filter.tubeness", input, es, sigma, actual);

		assertIterationsEqual(expected, actual);

	}

}
