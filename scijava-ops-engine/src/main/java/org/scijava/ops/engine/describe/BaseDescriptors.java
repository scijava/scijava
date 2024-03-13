
package org.scijava.ops.engine.describe;

import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;
import org.scijava.priority.Priority;
import org.scijava.types.Nil;
import org.scijava.types.Types;

import java.util.List;
import java.util.function.Function;

/**
 * {@code engine.describe} Ops pertaining to built-in Java classes.
 *
 * @param <T>
 * @param <N>
 * @author Gabriel Selzer
 */
public class BaseDescriptors<T, N extends Number> implements OpCollection {

	@OpField(names = "engine.describe")
	public final TypeDescriptor<N> boxedPrimitiveDescriptor = in -> "number";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<byte[]> byteArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<short[]> shortArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<int[]> intArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<long[]> longArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<float[]> floatArrayDescriptor = in -> "number[]";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<double[]> doubleArrayDescriptor =
		in -> "number[]";

	@OpMethod(names = "engine.describe", type = Function.class)
	public static <T> String arrayDescriptor( //
		@OpDependency(name = "engine.describe") Function<Nil<T>, String> dep, //
		Nil<T[]> in //
	) {
		return dep.apply(new Nil<>() {}) + "[]";
	}

	@OpMethod(names = "engine.describe", type = Function.class)
	public static <T> String listDescriptor( //
		@OpDependency(name = "engine.describe") Function<Nil<T>, String> dep, //
		Nil<List<T>> in //
	) {
		return "list<" + dep.apply(new Nil<>() {}) + ">";
	}

}
