/*-
 * #%L
 * Interactive tutorial for SciJava Ops.
 * %%
 * Copyright (C) 2023 - 2024 SciJava developers.
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

package org.scijava.ops.tutorial;

import org.scijava.Context;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.types.Nil;

import io.scif.img.ImgOpener;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class OpsIntro {

	public static void main(String... args) {
		/*
		SciJava Ops Image is a library for scientific image processing. The
		fundamental currency of this library are individual algorithms called Ops.

		All Ops share a few traits that establish a baseline of expected behavior:

		1. Ops are stateless - with no internal state, calling an Op two times on
		the same inputs will produce the same output.

		2. Ops are limited to one output - by limiting Ops to one output, we ensure
		that the algorithms we write are reusable building blocks. Of course,
		algorithms always need inputs, and there are no limitations on the number
		of inputs.

		3. Ops have a strong functional type - Java has a strong type system, and
		Ops utilizes this to maximize expressiveness and efficiency.

		4. Ops have at least one name - this name conveys an Op's purpose
		 */

		/*
		The OpEnvironment is the core of SciJava Ops. It is a collection of Ops,
		along with API for accessing Ops, adding new Ops, and other utilities.

		The easiest way to obtain an OpEnvironment is to use the following static
		method.
		*/
        var ops = OpEnvironment.build();

		/*
		OpEnvironments contain Ops, and there are a lot of them.
		 */
        var numberOfOps = count(ops.infos());
		System.out.println("There are " + numberOfOps +
			" ops in the OpEnvironment");

		/*
		Because each Op has at least one name, we can search for all Ops by name:
		 */
        var numberOfGaussianOps = count(ops.infos("filter.gauss"));
		System.out.println("There are " + numberOfGaussianOps +
			" gaussian filters in the OpEnvironment");

		/*
		The OpEnvironment also provides an access point into the OpBuilder syntax.
		(For an explanation of the OpBuilder syntax, please see the OpBuilder
		tutorial).
		 */
        var result = ops.op("math.add") //
			.input(1., 1.) //
			.outType(Double.class) //
			.apply();
		System.out.println("1+1=" + result);

		// open an image and do ops with it
		var img = openLymp();
		var shape = new RectangleShape(5, false);
		var uByteType = new Nil<Img<UnsignedByteType>>() {};
		var mean = ops.op("filter.mean").input(img, shape).outType(uByteType)
			.apply();
		var multResult = ops.op("create.img").input(img, new IntType()).apply();
		ops.op("math.multiply").input(img, mean).output(multResult).compute();

		// To see information about available ops, we use the
		// OpEnvironment#descriptions method
		// First, print all available ops
		System.out.println("--- All Ops!!! ---");
		System.out.println(ops.help());

		// Printing all the ops is a lot, so usually it's more helpful to check the
		// available signatures for a particular op:
		System.out.println("--- Available 'math.multiply' ops ---");
		System.out.println(ops.help("math.multiply"));
	}

	/**
	 * Static utility function to count the number of elements in an
	 * {@link Iterable}.
	 *
	 * @param iterable the {@link Iterable} to enumerate
	 * @return the number of elements in {@code iterable}
	 */
	private static int count(Iterable<?> iterable) {
        var num = 0;
		for (var ignored : iterable) {
			num++;
		}
		return num;
	}

	/**
	 * Helper method to open the lymp.tif ImageJ sample data. It encapsulates an
	 * IJ2 context.
	 *
	 * @return The opened image
	 */
	private static Img<?> openLymp() {
		Img<?> img = null;
		try (var ctx = new Context()) {
            var opener = new ImgOpener(ctx);
			img = opener.openImgs("https://imagej.net/images/lymp.tif").get(0)
				.getImg();
		}
		return img;
	}
}
