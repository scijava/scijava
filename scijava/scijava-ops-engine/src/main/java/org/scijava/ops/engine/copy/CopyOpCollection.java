package org.scijava.ops.engine.copy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.scijava.priority.Priority;
import org.scijava.function.Computers;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class CopyOpCollection <T> implements OpCollection{

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<T[], T[]> copyGenericArrayFunction = //
		from -> Arrays.copyOf(from, from.length);

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<T[], T[]> copyGenericArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0, to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<byte[], byte[]> copyByteArrayFunction =
		byte[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<byte[], byte[]> copyByteArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<short[], short[]> copyShortArrayFunction =
			short[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<short[], short[]> copyShortArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<int[], int[]> copyIntArrayFunction =
			int[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<int[], int[]> copyIntArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<long[], long[]> copyLongArrayFunction =
			long[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<long[], long[]> copyLongArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<float[], float[]> copyFloatArrayFunction =
			float[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<float[], float[]> copyFloatArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<double[], double[]> copyDoubleArrayFunction =
			double[]::clone;

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<double[], double[]> copyDoubleArray = (from, to) -> {
		int arrMax = Math.max(from.length, to.length);
		System.arraycopy(from, 0,to, 0, arrMax);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Computers.Arity1<List<T>, List<T>> copyList = (from, to) -> {
		to.clear();
		to.addAll(from);
	};

	@OpField(names = "cp, copy", params = "array, arrayCopy")
	public final Function<List<T>, List<T>> copyListFunction = ArrayList::new;
}
