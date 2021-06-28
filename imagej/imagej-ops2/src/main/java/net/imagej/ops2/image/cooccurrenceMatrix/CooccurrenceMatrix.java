package net.imagej.ops2.image.cooccurrenceMatrix;

import java.util.function.Function;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.function.Functions;
import org.scijava.ops.Op;
import org.scijava.ops.OpDependency;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Handler Op delegating between {@link CooccurrenceMatrix2D} and
 * {@link CooccurrenceMatrix3D}.
 * 
 * @author Gabriel Selzer
 *
 * @param <T> - the input {@link RealType}.
 */
@Plugin(type = Op.class, name = "image.cooccurrenceMatrix")
public class CooccurrenceMatrix<T extends RealType<T>>
		implements Functions.Arity4<RandomAccessibleInterval<T>, Integer, Integer, MatrixOrientation, double[][]> {

	@OpDependency(name = "stats.minMax")
	private Function<RandomAccessibleInterval<T>, Pair<T, T>> minmax;

	/**
	 * TODO
	 *
	 * @param iterableInput
	 * @param nrGreyLevels
	 * @param distance
	 * @param matrixOrientation
	 * @param cooccurrenceMatrix
	 */
	@Override
	public double[][] apply(RandomAccessibleInterval<T> input, Integer nrGreyLevels, Integer distance,
			MatrixOrientation orientation) {
		if (input.numDimensions() == 3 && orientation.isCompatible(3)) {
			return CooccurrenceMatrix3D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else if (input.numDimensions() == 2 && orientation.isCompatible(2)) {
			return CooccurrenceMatrix2D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else
			throw new IllegalArgumentException("Only 2 and 3-dimensional inputs are supported!");
	}

}
