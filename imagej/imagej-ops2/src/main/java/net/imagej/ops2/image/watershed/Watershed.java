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
package net.imagej.ops2.image.watershed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import net.imglib2.Cursor;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.outofbounds.OutOfBounds;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.IntervalIndexer;
import net.imglib2.util.Intervals;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;

import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.OpDependency;

/**
 * <p>
 * The Watershed algorithm segments and labels a grayscale image analogous to a
 * heightmap. In short, a drop of water following the gradient of an image flows
 * along a path to finally reach a local minimum.
 * </p>
 * <p>
 * Lee Vincent, Pierre Soille, Watersheds in digital spaces: An efficient
 * algorithm based on immersion simulations, IEEE Trans. Pattern Anal. Machine
 * Intell., 13(6) 583-598 (1991)
 * </p>
 * <p>
 * Input is a grayscale image with arbitrary number of dimensions, defining the
 * heightmap. It needs to be defined whether a neighborhood with eight- or
 * four-connectivity (respective to 2D) is used. A binary image can be set as
 * mask which defines the area where computation shall be done. If desired, the
 * watersheds are drawn and labeled as 0. Otherwise the watersheds will be
 * labeled as one of their neighbors.
 * </p>
 * <p>
 * Output is a labeling of the different catchment basins.
 * </p>
 *
 * @param <T>
 *            element type of input
 * @param <B>
 *            element type of mask
 *
 * @author Simon Schmid (University of Konstanz)
 *@implNote op names='image.watershed'
 */
public class Watershed<T extends RealType<T>, B extends BooleanType<B>> implements
		Computers.Arity4<RandomAccessibleInterval<T>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> {

	// @SuppressWarnings("rawtypes")
	// private UnaryFunctionOp<Interval, ImgLabeling> createOp;

	@OpDependency(name = "create.img")
	BiFunction<Dimensions, IntType, RandomAccessibleInterval<IntType>> imgCreator;

	/** Default label for watershed */
	private static final int WSHED = -1;

	/** Default label for initialization */
	private static final int INIT = -2;

	/** Default label for mask */
	private static final int MASK = -3;

	/**
	 * TODO
	 *
	 * @param input
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param mask
	 * @param outputLabeling
	 */
	@Override
	public void compute(final RandomAccessibleInterval<T> in, final Boolean useEightConnectivity,
			final Boolean drawWatersheds, final RandomAccessibleInterval<B> mask,
			final ImgLabeling<Integer, IntType> out) {
		final RandomAccess<T> raIn = in.randomAccess();

		RandomAccess<B> raMask = null;
		if (mask != null) {
			raMask = mask.randomAccess();
		}
		// stores the size of each dimension
		final long[] dimensSizes = new long[in.numDimensions()];
		in.dimensions(dimensSizes);

		// calculates the number of points in the n-d space
		long numPixels = Intervals.numElements(in);

		// the pixels indices are stored in an array, which is sorted depending
		// on the pixel values
		final List<Long> imiList = new ArrayList<>();

		if (mask != null) {
			final Cursor<Void> c = Regions.iterable(mask).localizingCursor();
			while (c.hasNext()) {
				c.next();
				imiList.add(IntervalIndexer.positionToIndex(c, in));
			}
		} else {
			for (long i = 0; i < numPixels; i++) {
				imiList.add(i);
			}
		}
		final Long[] imi = imiList.toArray(new Long[imiList.size()]);
		
		/*
		 * Sort the pixels of imi in the increasing order of their grey value
		 * (only the pixel indices are stored)
		 */
		Arrays.sort(imi, (o1, o2) -> {
			IntervalIndexer.indexToPosition(o1, in, raIn);
			final T value = raIn.get().copy();
			IntervalIndexer.indexToPosition(o2, in, raIn);
			return value.compareTo(raIn.get());
		});

		// lab and dist store the values calculated after each phase
		final RandomAccessibleInterval<IntType> lab = imgCreator.apply(in, new IntType());
		// extend border to be able to do a quick check, if a voxel is inside
		final ExtendedRandomAccessibleInterval<IntType, RandomAccessibleInterval<IntType>> labExt = Views
				.extendBorder(lab);
		final OutOfBounds<IntType> raLab = labExt.randomAccess();
		final RandomAccessibleInterval<IntType> dist = imgCreator.apply(in, new IntType());
		final RandomAccess<IntType> raDist = dist.randomAccess();

		// initial values
		for (final IntType pixel : Views.flatIterable(lab)) {
			pixel.set(INIT);
		}
		int current_label = 0;
		int current_dist;
		final ArrayList<Long> fifo = new ArrayList<>();

		// RandomAccess for Neighborhoods
		final Shape shape;
		if (useEightConnectivity) {
			shape = new RectangleShape(1, true);
		} else {
			shape = new DiamondShape(1);
		}
		final RandomAccessible<Neighborhood<T>> neighborhoods = shape.neighborhoodsRandomAccessible(in);
		final RandomAccess<Neighborhood<T>> raNeighbor = neighborhoods.randomAccess();

		/*
		 * Start flooding
		 */
		for (int j = 0; j < imi.length; j++) {
			IntervalIndexer.indexToPosition(imi[j], in, raIn);
			final T actualH = raIn.get().copy();
			int i = j;
			while (actualH.compareTo(raIn.get()) == 0) {
				final long p = imi[i];
				IntervalIndexer.indexToPosition(p, in, raIn);
				raLab.setPosition(raIn);
				raLab.get().set(MASK);
				raNeighbor.setPosition(raIn);
				final Cursor<T> neighborHood = raNeighbor.get().cursor();

				while (neighborHood.hasNext()) {
					neighborHood.fwd();
					raLab.setPosition(neighborHood);
					if (!raLab.isOutOfBounds()) {
						final int f = raLab.get().get();
						if (f > 0 || f == WSHED) {
							raDist.setPosition(raIn);
							raDist.get().set(1);
							fifo.add(p);
							break;
						}
					}
				}
				i++;
				if (i == imi.length) {
					break;
				}
				IntervalIndexer.indexToPosition(imi[i], in, raIn);
			}

			current_dist = 1;
			fifo.add(-1l); // add fictitious pixel
			while (true) {
				long p = fifo.remove(0);
				if (p == -1) {
					if (fifo.isEmpty()) {
						break;
					}
					fifo.add(-1l);
					current_dist++;
					p = fifo.remove(0);
				}

				IntervalIndexer.indexToPosition(p, in, raNeighbor);

				final Cursor<T> neighborHood = raNeighbor.get().cursor();

				raLab.setPosition(raNeighbor);
				int labp = raLab.get().get();

				final long[] posNeighbor = new long[neighborHood.numDimensions()];
				while (neighborHood.hasNext()) {
					neighborHood.fwd();
					neighborHood.localize(posNeighbor);
					raLab.setPosition(posNeighbor);
					if (!raLab.isOutOfBounds()) {
						raDist.setPosition(posNeighbor);
						final int labq = raLab.get().get();
						final int distq = raDist.get().get();
						if (distq < current_dist && (labq > 0 || labq == WSHED)) {
							// i.e. q belongs to an already labeled basin or to
							// the watersheds
							if (labq > 0) {
								if (labp == MASK || labp == WSHED) {
									labp = labq;
								} else {
									if (labp != labq) {
										labp = WSHED;
									}
								}
							} else {
								if (labp == MASK) {
									labp = WSHED;
								}
							}
							raLab.setPosition(raNeighbor);
							raLab.get().set(labp);
						} else {
							if (labq == MASK && distq == 0) {
								raDist.setPosition(posNeighbor);
								raDist.get().set(current_dist + 1);
								fifo.add(IntervalIndexer.positionToIndex(posNeighbor, dimensSizes));
							}
						}
					}
				}
			}

			// checks if new minima have been discovered
			IntervalIndexer.indexToPosition(imi[j], in, raIn);
			i = j;
			while (actualH.compareTo(raIn.get()) == 0) {
				final long p = imi[i];
				IntervalIndexer.indexToPosition(p, dist, raDist);
				// the distance associated with p is reseted to 0
				raDist.get().set(0);
				raLab.setPosition(raDist);

				if (raLab.get().get() == MASK) {
					current_label++;
					fifo.add(p);
					raLab.get().set(current_label);
					while (!fifo.isEmpty()) {
						final long q = fifo.remove(0);
						IntervalIndexer.indexToPosition(q, in, raNeighbor);
						final Cursor<T> neighborHood = raNeighbor.get().cursor();

						final long[] posNeighbor = new long[neighborHood.numDimensions()];
						while (neighborHood.hasNext()) {
							neighborHood.fwd();
							neighborHood.localize(posNeighbor);
							raLab.setPosition(posNeighbor);
							if (!raLab.isOutOfBounds()) {
								final long r = IntervalIndexer.positionToIndex(posNeighbor, dimensSizes);
								if (raLab.get().get() == MASK) {
									fifo.add(r);
									raLab.get().set(current_label);
								}
							}
						}
					}
				}
				i++;
				if (i == imi.length) {
					break;
				}
				IntervalIndexer.indexToPosition(imi[i], in, raIn);
			}
			j = i - 1;
		}

		/*
		 * Draw output and remove as the case may be the watersheds
		 */
		final Cursor<LabelingType<Integer>> cursorOut = out.cursor();
		while (cursorOut.hasNext()) {
			cursorOut.fwd();
			boolean maskValue = true;
			if (mask != null) {
				raMask.setPosition(cursorOut);
				if (!raMask.get().get()) {
					maskValue = false;
				}
			}
			raLab.setPosition(cursorOut);
			if (!maskValue) {
				cursorOut.get().clear();
			} else {
				if (!drawWatersheds && raLab.get().get() == WSHED) {
					raNeighbor.setPosition(cursorOut);
					final Cursor<T> neighborHood = raNeighbor.get().cursor();
					int newLab = WSHED;
					while (neighborHood.hasNext()) {
						neighborHood.fwd();
						raLab.setPosition(neighborHood);
						if (!raLab.isOutOfBounds()) {
							newLab = raLab.get().get();
							if (newLab > WSHED) {
								break;
							}
						}
					}
					if (newLab == WSHED) {
						cursorOut.get().clear();
					} else {
						cursorOut.get().add(newLab);
					}
				} else {
					cursorOut.get().add(raLab.get().get());
				}
			}
		}

		/*
		 * Merge already present labels before calculation of watershed
		 */
		if (out != null) {
			final Cursor<LabelingType<Integer>> cursor = out.cursor();
			final RandomAccess<LabelingType<Integer>> raOut = out.randomAccess();
			while (cursor.hasNext()) {
				cursor.fwd();
				raOut.setPosition(cursor);
				final List<Integer> labels = new ArrayList<>();
				cursor.get().iterator().forEachRemaining(labels::add);
				raOut.get().addAll(labels);
			}
		}
	}

}

/**
 *@implNote op names='image.watershed'
 */
class WatershedMaskless<T extends RealType<T>, B extends BooleanType<B>> implements
		Computers.Arity3<RandomAccessibleInterval<T>, Boolean, Boolean, ImgLabeling<Integer, IntType>> {
	
	@OpDependency(name = "image.watershed")
	private Computers.Arity4<RandomAccessibleInterval<T>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;

	/**
	 * TODO
	 *
	 * @param input
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param outputLabeling
	 */
	@Override
	public void compute(RandomAccessibleInterval<T> in, Boolean useEightConnectivity, Boolean drawWatersheds,
			ImgLabeling<Integer, IntType> outputLabeling) {
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, null, outputLabeling);
		
	}
}

/**
 *@implNote op names='image.watershed'
 */
class WatershedFunction<T extends RealType<T>, B extends BooleanType<B>>
		implements Functions.Arity4<RandomAccessibleInterval<T>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "image.watershed")
	private Computers.Arity4<RandomAccessibleInterval<T>, Boolean, Boolean, RandomAccessibleInterval<B>, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * TODO
	 *
	 * @param input
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @param mask
	 * @return the outputLabeling
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply(RandomAccessibleInterval<T> in, Boolean useEightConnectivity,
			Boolean drawWatersheds, RandomAccessibleInterval<B> mask) {
		ImgLabeling<Integer, IntType> outputLabeling = labelingCreator.apply(in, new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, mask, outputLabeling);
		return outputLabeling;
	}
}

/**
 *@implNote op names='image.watershed'
 */
class WatershedFunctionMaskless<T extends RealType<T>, B extends BooleanType<B>>
		implements Functions.Arity3<RandomAccessibleInterval<T>, Boolean, Boolean, ImgLabeling<Integer, IntType>> {

	@OpDependency(name = "image.watershed")
	private Computers.Arity3<RandomAccessibleInterval<T>, Boolean, Boolean, ImgLabeling<Integer, IntType>> watershedOp;
	@OpDependency(name = "create.imgLabeling")
	private BiFunction<Dimensions, IntType, ImgLabeling<Integer, IntType>> labelingCreator;

	/**
	 * TODO
	 *
	 * @param input
	 * @param useEightConnectivity
	 * @param drawWatersheds
	 * @return the outputLabeling
	 */
	@Override
	public ImgLabeling<Integer, IntType> apply(RandomAccessibleInterval<T> in, Boolean useEightConnectivity,
			Boolean drawWatersheds) {
		ImgLabeling<Integer, IntType> outputLabeling = labelingCreator.apply(in, new IntType());
		watershedOp.compute(in, useEightConnectivity, drawWatersheds, outputLabeling);
		return outputLabeling;
	}
}
