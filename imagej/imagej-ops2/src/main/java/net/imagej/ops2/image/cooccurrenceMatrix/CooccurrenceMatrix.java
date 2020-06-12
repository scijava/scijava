package net.imagej.ops2.image.cooccurrenceMatrix;

import java.util.function.Function;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Pair;

import org.scijava.ops.OpDependency;
import org.scijava.ops.core.Op;
import org.scijava.ops.function.Functions;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;

/**
 * Handler Op delegating between {@link CooccurrenceMatrix2D} and
 * {@link CooccurrenceMatrix3D}.
 * 
 * @author Gabriel Selzer
 *
 * @param <T> - the input {@link RealType}.
 */
@Plugin(type = Op.class, name = "image.cooccurrenceMatrix")
@Parameter(key = "iterableInput")
@Parameter(key = "nrGreyLevels", min = "0", max = "128", stepSize = "1", initializer = "32")
@Parameter(key = "distance", min = "0", max = "128", stepSize = "1", initializer = "1")
@Parameter(key = "matrixOrientation")
@Parameter(key = "cooccurrenceMatrix", itemIO = ItemIO.OUTPUT)
public class CooccurrenceMatrix<T extends RealType<T>>
		implements Functions.Arity4<IterableInterval<T>, Integer, Integer, MatrixOrientation, double[][]> {

	@OpDependency(name = "stats.minMax")
	private Function<IterableInterval<T>, Pair<T, T>> minmax;

	@Override
	public double[][] apply(IterableInterval<T> input, Integer nrGreyLevels, Integer distance,
			MatrixOrientation orientation) {
		if (input.numDimensions() == 3 && orientation.isCompatible(3)) {
			return CooccurrenceMatrix3D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else if (input.numDimensions() == 2 && orientation.isCompatible(2)) {
			return CooccurrenceMatrix2D.apply(input, nrGreyLevels, distance, minmax, orientation);
		} else
			throw new IllegalArgumentException("Only 2 and 3-dimensional inputs are supported!");
	}

}
