/*
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2023 ImageJ2 developers.
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

package org.scijava.ops.image;

import java.net.URL;
import java.util.stream.StreamSupport;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.spi.Op;

import io.scif.img.IO;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

/**
 * Base class for Op unit testing.
 * <p>
 * <i>All</i> {@link Op} unit tests need to have an {@link OpEnvironment} instance.
 * Following the DRY principle, we should implement it only once. Here.
 * </p>
 *
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
public abstract class AbstractOpTest{

	protected static final OpEnvironment ops = OpEnvironment.build();


	private int seed;

	private int pseudoRandom() {
		return seed = 3170425 * seed + 132102;
	}

	/**
	 * Open a sample image from a resource path relative to {@link AbstractOpTest}
	 */
	public Img<FloatType> openFloatImg(final String resourcePath) {
		return openRelativeFloatImg(getClass(), resourcePath);
	}

	/**
	 * Open a sample image from a resource path relative to {@link AbstractOpTest}
	 */
	public Img<DoubleType> openDoubleImg(final String resourcePath) {
		return openRelativeDoubleImg(getClass(), resourcePath);
	}

	/**
	 * Open a sample image from a resource path relative to {@link AbstractOpTest}
	 */
	public Img<UnsignedByteType> openUnsignedByteImg(final String resourcePath)
	{
		return openRelativeUnsignedByteImg(getClass(), resourcePath);
	}

	/**
	 * Open a sample image from a resource path relative to a specified base class
	 */
	public static Img<FloatType> openRelativeFloatImg(final Class<?> base,
		final String resourcePath)
	{
		final URL url = base.getResource(resourcePath);
		return IO.openFloat(url.getPath()).getImg();
	}

	/**
	 * Open a sample image from a resource path relative to a specified base class
	 */
	public static Img<DoubleType> openRelativeDoubleImg(final Class<?> base,
		final String resourcePath)
	{
		final URL url = base.getResource(resourcePath);
		return IO.openDouble(url.getPath()).getImg();
	}

	/**
	 * Open a sample image from a resource path relative to a specified base class
	 */
	public static Img<UnsignedByteType> openRelativeUnsignedByteImg(final Class<?> base,
		final String resourcePath)
	{
		final URL url = base.getResource(resourcePath);
		return IO.openUnsignedByte(url.getPath()).getImg();
	}

	public static <T> RandomAccessible<T> deinterval(
		RandomAccessibleInterval<T> input)
	{
		return Views.extendBorder(input);
	}

	public <T extends RealType<T>> boolean areCongruent(
		final IterableInterval<T> in, final RandomAccessible<T> out,
		final double epsilon)
	{
		Cursor<T> cin = in.localizingCursor();
		RandomAccess<T> raOut = out.randomAccess();

		while (cin.hasNext()) {
			cin.fwd();
			raOut.setPosition(cin);
			if (Math.abs(cin.get().getRealDouble() - raOut.get()
				.getRealDouble()) > epsilon) return false;
		}
		return true;
	}

	public <T extends RealType<T>> double[] asArray(final Iterable<T> image) {
		return StreamSupport.stream(image.spliterator(), false).mapToDouble(
				ComplexType::getRealDouble).toArray();
	}

}
