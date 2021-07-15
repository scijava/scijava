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

package net.imagej.ops2.filter.vesselness;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.scif.img.IO;

import java.net.URL;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.Context;
import org.scijava.cache.CacheService;
import org.scijava.ops.OpService;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.ops.provenance.OpHistoryService;
import org.scijava.plugin.PluginService;
import org.scijava.script.ScriptService;
import org.scijava.thread.ThreadService;
import org.scijava.types.TypeService;

/**
 * Tests the Frangi Vesselness operation.
 * 
 * @author Gabe Selzer
 */
public class FrangiVesselnessTest{
	
	protected static Context context;
	protected static OpService ops;

	@BeforeAll
	public static void setUp() {
		context = new Context(OpService.class, OpHistoryService.class, CacheService.class,
			ThreadService.class, ScriptService.class, PluginService.class,
			TypeService.class);
		ops = context.service(OpService.class);
	}

	@AfterAll
	public static void tearDown() {
		context.dispose();
		context = null;
		ops = null;
	}
	
	private static OpBuilder op(String name) {
		return ops.op(name);
	}
	
	private Img<FloatType> openFloatImg(
		final String resourcePath)
	{
		final URL url = getClass().getResource(resourcePath);
		return IO.openFloat(url.getPath()).getImg();
	}

	@Test
	public void regressionTest() throws Exception {

		// load in input image and expected output image.
		Img<DoubleType> inputImg = ArrayImgs.doubles(256, 256);
		ops.op("image.equation")
				.input("Math.tan(0.3*p[0]) + Math.tan(0.1*p[1])", context.getService(ScriptService.class))
				.output(inputImg).compute();
		Img<FloatType> expectedOutput = ((Img<FloatType>) openFloatImg("Result.tif"));

		// create ouput image
		long[] dims = new long[inputImg.numDimensions()];
		inputImg.dimensions(dims);
		Img<FloatType> actualOutput = ArrayImgs.floats(dims);

		// scale over which the filter operates (sensitivity)
		int scale = 1;

		// physical spacing between data points (1,1 since I got it from the
		// computer)
		double[] spacing = { 1, 1 };

		// run the op
		ops.op("filter.frangiVesselness").input(inputImg, spacing, scale).output(actualOutput).compute();

		// compare the output image data to that stored in the file.
		Cursor<FloatType> cursor = Views.iterable(actualOutput).localizingCursor();
		RandomAccess<FloatType> actualRA = actualOutput.randomAccess();
		RandomAccess<FloatType> expectedRA = expectedOutput.randomAccess();

		while (cursor.hasNext()) {
			cursor.fwd();
			actualRA.setPosition(cursor);
			expectedRA.setPosition(cursor);
			assertEquals(expectedRA.get().get(), actualRA.get().get(), 0);
		}
	}

}
