/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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

package org.scijava.ops.image.transform.piv;

import java.util.Arrays;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.DoubleType;

/**
 * The result of {@code transform.piv}: a vector field of displacements on a
 * regular grid of interrogation windows.
 * <p>
 * Grid point {@code g} (a position in the grid, of the same dimensionality as
 * the frames) belongs to the interrogation window whose minimum is
 * {@code min + g * step} in the coordinates of the first frame. The vector of
 * a window is the displacement {@code u} such that the pattern at {@code x}
 * in the first frame appears at {@code x + u} in the second frame.
 * </p>
 * <p>
 * The result is immutable: each getter returns a copy.
 * </p>
 *
 * @author Curtis Rueden
 * @author Claude Opus 5.5
 */
public class PIVResult {

	private final long[] gridSize;
	private final long[] min;
	private final int[] windowSize;
	private final int[] step;
	private final double[] displacements;
	private final double[] correlations;
	private final boolean[] valid;

	/**
	 * Creates a result.
	 *
	 * @param gridSize the number of windows in each dimension
	 * @param min the minimum of the first window, in frame coordinates
	 * @param windowSize the size of the windows in each dimension
	 * @param step the distance between the windows in each dimension
	 * @param displacements the displacement vectors, flattened with the vector
	 *          component varying fastest, then grid dimension 0, and so on
	 * @param correlations the correlation peak of each window, flattened with
	 *          grid dimension 0 varying fastest
	 * @param valid whether each vector was measured and passed validation,
	 *          flattened like the correlations
	 */
	public PIVResult(final long[] gridSize, final long[] min,
		final int[] windowSize, final int[] step, final double[] displacements,
		final double[] correlations, final boolean[] valid)
	{
		this.gridSize = gridSize.clone();
		this.min = min.clone();
		this.windowSize = windowSize.clone();
		this.step = step.clone();
		this.displacements = displacements.clone();
		this.correlations = correlations.clone();
		this.valid = valid.clone();
	}

	/** Gets the dimensionality of the frames and of the vectors. */
	public int numDimensions() {
		return gridSize.length;
	}

	/** Gets the number of interrogation windows in each dimension. */
	public long[] getGridSize() {
		return gridSize.clone();
	}

	/** Gets the size of the interrogation windows. */
	public int[] getWindowSize() {
		return windowSize.clone();
	}

	/** Gets the distance between neighboring interrogation windows. */
	public int[] getStep() {
		return step.clone();
	}

	/**
	 * Gets the center, in the coordinates of the first frame, of the
	 * interrogation window at the given grid point.
	 */
	public double[] getPosition(final long... gridPoint) {
		final var n = numDimensions();
		final var position = new double[n];
		for (var d = 0; d < n; d++)
			position[d] = min[d] + gridPoint[d] * step[d] + (windowSize[d] - 1) /
				2.0;
		return position;
	}

	/**
	 * Gets the displacement vector of the window at the given grid point, whose
	 * components are {@code NaN} if it could not be measured.
	 */
	public double[] getDisplacement(final long... gridPoint) {
		final var n = numDimensions();
		final var i = index(gridPoint) * n;
		return Arrays.copyOfRange(displacements, i, i + n);
	}

	/**
	 * Gets the normalized cross-correlation at the displacement peak of the
	 * window at the given grid point, or {@code NaN} if it could not be
	 * measured.
	 */
	public double getCorrelation(final long... gridPoint) {
		return correlations[index(gridPoint)];
	}

	/**
	 * Gets whether the vector of the window at the given grid point was
	 * measured and passed validation. Otherwise, it was replaced by the median
	 * of its neighbors (if validation was enabled) or is {@code NaN}.
	 */
	public boolean isValid(final long... gridPoint) {
		return valid[index(gridPoint)];
	}

	/**
	 * Gets the window centers as an image of the grid size plus one last
	 * dimension holding the coordinates.
	 */
	public RandomAccessibleInterval<DoubleType> getPositions() {
		final var n = numDimensions();
		final var data = new double[displacements.length];
		final var gridPoint = new long[n];
		for (var i = 0; i < correlations.length; i++) {
			System.arraycopy(getPosition(gridPoint), 0, data, i * n, n);
			increment(gridPoint);
		}
		return vectorImage(data);
	}

	/**
	 * Gets the displacement vectors as an image of the grid size plus one last
	 * dimension holding the components.
	 */
	public RandomAccessibleInterval<DoubleType> getDisplacements() {
		return vectorImage(displacements);
	}

	/** Gets the correlation peaks as an image of the grid size. */
	public RandomAccessibleInterval<DoubleType> getCorrelations() {
		return ArrayImgs.doubles(correlations.clone(), gridSize);
	}

	/** Gets the validity of the vectors as an image of the grid size. */
	public RandomAccessibleInterval<BitType> getValid() {
		final var img = ArrayImgs.bits(gridSize);
		final var cursor = img.cursor();
		for (final var v : valid)
			cursor.next().set(v);
		return img;
	}

	/**
	 * Copies vector data, flattened with the component fastest, into an image
	 * with the component as its last dimension.
	 */
	private RandomAccessibleInterval<DoubleType> vectorImage(
		final double[] data)
	{
		final var n = numDimensions();
		final var count = correlations.length;
		final var transposed = new double[data.length];
		for (var i = 0; i < count; i++)
			for (var d = 0; d < n; d++)
				transposed[d * count + i] = data[i * n + d];
		final var dims = Arrays.copyOf(gridSize, n + 1);
		dims[n] = n;
		return ArrayImgs.doubles(transposed, dims);
	}

	private int index(final long[] gridPoint) {
		if (gridPoint.length != numDimensions()) throw new IllegalArgumentException(
			"Expected a grid point with " + numDimensions() + " coordinates.");
		long index = 0;
		for (var d = numDimensions() - 1; d >= 0; d--) {
			if (gridPoint[d] < 0 || gridPoint[d] >= gridSize[d])
				throw new IndexOutOfBoundsException("Grid point " + Arrays.toString(
					gridPoint) + " is outside the grid " + Arrays.toString(gridSize));
			index = index * gridSize[d] + gridPoint[d];
		}
		return (int) index;
	}

	private void increment(final long[] gridPoint) {
		for (var d = 0; d < gridPoint.length; d++) {
			if (++gridPoint[d] < gridSize[d]) return;
			gridPoint[d] = 0;
		}
	}

	@Override
	public String toString() {
		return "PIVResult[grid = " + Arrays.toString(gridSize) + ", window = " +
			Arrays.toString(windowSize) + ", step = " + Arrays.toString(step) + "]";
	}
}
