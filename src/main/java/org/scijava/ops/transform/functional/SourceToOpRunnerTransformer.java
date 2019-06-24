package org.scijava.ops.transform.functional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Stream;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Source;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.OpRunners;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

@Plugin(type = OpTransformer.class)
public class SourceToOpRunnerTransformer implements FunctionalTypeTransformer {

	@Override
	public Object transform(OpService opService, OpRef ref, Object src) {
		return OpRunners.Sources.toRunner((Source) src);
	}

	@Override
	public Class<?> srcClass() {
		return Source.class;
	}

	@Override
	public Class<?> targetClass() {
		return OpRunner.class;
	}

	@Override
	public Type[] getTransformedArgTypes(OpRef toRef) {
		return toRef.getArgs();
	}

	@Override
	public Type getTransformedOutputType(OpRef toRef) {
		return toRef.getOutType();
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {

		// concatenate the input and output types of the BiFunction (as described by the
		// OpRunner) into one array so that we can use them to parameterize the
		// BiFunction.
		Type[] toParamTypes = Stream
				.concat(Arrays.stream(getTransformedArgTypes(toRef)), Arrays.stream(new Type[] {getTransformedOutputType(toRef)}))
				.toArray(Type[]::new);

		// if toParamTypes does not contain the number of types needed to parameterize
		// this op type, return null.
		int expectedNumTypes = ((ParameterizedType) Types.parameterizeRaw(srcClass())).getActualTypeArguments().length;
		if (toParamTypes.length != expectedNumTypes)
			return null;

		// parameterize the OpRef types with the type parameters of the op
		Type[] refTypes = Arrays.stream(toRef.getTypes())
				.map(refType -> Types.parameterize(Types.raw(refType), toParamTypes)).toArray(Type[]::new);

		// if we can make an OpRef out of this, return it. Otherwise return null.
		boolean hit = TypeModUtils.replaceRawTypes(refTypes, targetClass(), srcClass());
		if (hit) {
			return OpRef.fromTypes(toRef.getName(), refTypes, getTransformedOutputType(toRef),
					getTransformedArgTypes(toRef));
		}
		return null;
	}
}
