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

package net.imagej.ops2.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;

import net.imagej.ops2.AbstractOpTest;
import net.imagej.ops2.filter.sigma.DefaultSigmaFilter;
import net.imagej.testutil.TestImgGeneration;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.RectangleShape.NeighborhoodsIterableInterval;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory;
import net.imglib2.outofbounds.OutOfBoundsMirrorFactory.Boundary;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;

/**
 * Tests implementations of {@code MaxFilterOp}, {@code MeanFilterOp},
 * {@code MedianFilterOp}, {@code MinFilterOp}, {@code SigmaFilterOp},
 * {@code VarianceFilterOp}.
 * 
 * @author Jonathan Hale (University of Konstanz)
 */
public class NonLinearFiltersTest extends AbstractOpTest {

	Img<ByteType> in;
	Img<ByteType> out;
	RectangleShape shape;
	OutOfBoundsMirrorFactory<ByteType, Img<ByteType>> oobFactory = new OutOfBoundsMirrorFactory<>(Boundary.SINGLE);

	/**
	 * Initialize images.
	 *
	 * @throws Exception
	 */
	@BeforeEach
	public void before() throws Exception {
		in = TestImgGeneration.byteArray(true, new long[] { 10, 10 });
		out = TestImgGeneration.byteArray(false, new long[] { 10, 10 });
		shape = new RectangleShape(1, false);
	}

	/**
	 * @see NeighborhoodFilters#defaultMax(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMaxFilter() {
		ops.op("filter.max").arity3().input(in, shape, oobFactory).output(out).compute();

		byte max = Byte.MIN_VALUE;

		NeighborhoodsIterableInterval<ByteType> neighborhoods = shape
				.neighborhoods(Views.interval(Views.extendMirrorSingle(in), in));
		for (ByteType t : neighborhoods.firstElement()) {
			max = (byte) Math.max(t.getInteger(), max);
		}
		assertEquals(out.firstElement().get(), max);
	}

	/**
	 * @see NeighborhoodFilters#defaultMax(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMaxFilterNullable() {
		ops.op("filter.max").arity2().input(in, shape).output(out).compute();
	}

	/**
	 * @see NeighborhoodFilters#defaultMean(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMeanFilter() {
		ops.op("filter.mean").arity3().input(in, shape, oobFactory).output(out).compute();

		double sum = 0.0;

		NeighborhoodsIterableInterval<ByteType> neighborhoods = shape
				.neighborhoods(Views.interval(Views.extendMirrorSingle(in), in));
		for (ByteType t : neighborhoods.firstElement()) {
			sum += t.getRealDouble();
		}

		assertEquals(Util.round(sum / 9.0), out.firstElement().get());
	}

	/**
	 * @see NeighborhoodFilters#defaultMean(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMeanFilterNullable() {
		ops.op("filter.mean").arity2().input(in, shape).output(out).compute();
	}

	/**
	 * @see NeighborhoodFilters#defaultMedian(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMedianFilter() {
		ops.op("filter.median").arity3().input(in, shape, oobFactory).output(out).compute();

		ArrayList<ByteType> items = new ArrayList<>();
		NeighborhoodsIterableInterval<ByteType> neighborhoods = shape
				.neighborhoods(Views.interval(Views.extendMirrorSingle(in), in));
		for (ByteType t : neighborhoods.firstElement()) {
			items.add(t.copy());
		}

		Collections.sort(items);

		assertEquals(items.get(5).get(), out.firstElement().get());
	}

	/**
	 * @see NeighborhoodFilters#defaultMedian(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMedianFilterNullable() {
		ops.op("filter.median").arity2().input(in, shape).output(out).compute();
	}

	/**
	 * @see NeighborhoodFilters#defaultMinimum(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMinFilter() {
		ops.op("filter.min").arity3().input(in, shape, oobFactory).output(out).compute();

		byte min = Byte.MAX_VALUE;

		NeighborhoodsIterableInterval<ByteType> neighborhoods = shape
				.neighborhoods(Views.interval(Views.extendMirrorSingle(in), in));
		for (ByteType t : neighborhoods.firstElement()) {
			min = (byte) Math.min(t.getInteger(), min);
		}
		assertEquals(min, out.firstElement().get());
	}

	/**
	 * @see NeighborhoodFilters#defaultMinimum(Computers.Arity1, Neighborhood, Object)
	 */
	@Test
	public void testMinFilterNullable() {
		ops.op("filter.min").arity2().input(in, shape).output(out).compute();
	}

	/**
	 * @see SigmaFilterOp
	 * @see DefaultSigmaFilter
	 */
	@Test
	public void testSigmaFilter() {
		ops.op("filter.sigma").arity5().input(in, shape, 1.0, 0.0, oobFactory).output(out).compute();
	}

	@Test
	public void testSigmaFilterNullable() {
		ops.op("filter.sigma").arity4().input(in, shape, 1.0, 0.0).output(out).compute();
	}

	@Test
	public void testVarianceFilter() {
		ops.op("filter.variance").arity3().input(in, shape, oobFactory).output(out).compute();

		double sum = 0.0;
		double sumSq = 0.0;

		NeighborhoodsIterableInterval<ByteType> neighborhoods = shape
				.neighborhoods(Views.interval(Views.extendMirrorSingle(in), in));
		for (ByteType t : neighborhoods.firstElement()) {
			sum += t.getRealDouble();
			sumSq += t.getRealDouble() * t.getRealDouble();
		}

		assertEquals((byte) Util.round((sumSq - (sum * sum / 9)) / 8), out.firstElement().get());
	}

	@Test
	public void testVarianceFilterNullable() {
		ops.op("filter.variance").arity2().input(in, shape).output(out).compute();
	}

}
