package org.scijava.ops.transform.functional;

import java.lang.reflect.Type;
import java.util.Arrays;

import org.scijava.ops.OpService;
import org.scijava.ops.core.inplace.Inplace3Second;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpRunner;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.OpRunners;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.types.TypeService;
import org.scijava.util.Types;

@Plugin(type = OpTransformer.class)
public class Inplace3SecondToOpRunnerTransformer implements FunctionalTypeTransformer {

	@Parameter
	private TypeService typeService;

	@SuppressWarnings("unchecked")
	@Override
	public Object transform(OpService opService, OpRef ref, Object src) {
//		Type[] outTypes = ref.getOutTypes();
		return OpRunners.Inplaces.toRunner((Inplace3Second) src);
	}

	@Override
	public Class<?> srcClass() {
		return Inplace3Second.class;
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
	public Type[] getTransformedOutputTypes(OpRef toRef) {
		return toRef.getOutTypes();
	}

	@Override
	public OpRef getRefTransformingTo(OpRef toRef) {

		// concatenate the input and output types of the BiFunction (as described by the
		// OpRunner) into one array so that we can use them to parameterize the
		// BiFunction.
		Type[] toParamTypes = getTransformedArgTypes(toRef);

		// parameterize the OpRef types with the 3 BiFunction type parameters
		Type[] refTypes = Arrays.stream(toRef.getTypes())
				.map(refType -> Types.parameterize(Types.raw(refType), toParamTypes)).toArray(Type[]::new);

		boolean hit = TypeModUtils.replaceRawTypes(refTypes, targetClass(), srcClass());
		if (hit) {
			return OpRef.fromTypes(toRef.getName(), refTypes, getTransformedOutputTypes(toRef),
					getTransformedArgTypes(toRef));
		}
		return null;
	}
}