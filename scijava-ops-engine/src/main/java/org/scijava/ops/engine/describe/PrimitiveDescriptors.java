
package org.scijava.ops.engine.describe;

import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.priority.Priority;
import org.scijava.types.Types;

import java.util.List;

public class PrimitiveDescriptors<T, N extends Number> implements OpCollection {

	@OpField(names = "engine.describe")
	public final TypeDescriptor<N> boxedPrimitiveDescriptor = in -> "number";

	@OpField(names = "engine.describe")
	public final TypeDescriptor<N[]> boxedPrimitiveArrayDescriptor =
		in -> "number[]";

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

	@OpField(names = "engine.describe")
	public final TypeDescriptor<List<N>> boxedPrimitiveListDescriptor =
		in -> "list<number>";

	@OpField(names = "engine.describe", priority = Priority.LAST)
	public final TypeDescriptor<T> identityDescriptor = in -> Types.raw(in
		.getType()).getSimpleName();

}
