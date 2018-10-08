package org.scijava.ops.transform.impl;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;
import org.scijava.ops.transform.OpTransformer;
import org.scijava.ops.transform.TypeModUtils;
import org.scijava.ops.util.Maps;
import org.scijava.plugin.Plugin;

@Plugin(type = OpTransformer.class)
public class LiftFunctionToListTransformer implements OpTransformer {

	@Override
	public Object transform(OpService opService, OpRef fromRef, Object src) {
		if (src instanceof Function) {
			try {
				return Maps.Lift.Functions.list((Function) src);
			} catch (Exception e) {
				// TODO
			}
		}
		return null;
	}

	@Override
	public OpRef getFromTransformTo(OpRef toRef) {
		// TODO put the code below into a utils method
		Type[] refTypes = toRef.getTypes();
		boolean typesChanged = TypeModUtils.unliftParameterizedTypes(refTypes, Function.class, List.class);
		// TODO We assume here that the functional input is the first Type in
		// this list and all others are secondary args if there are any (we do
		// not want to touch them as they are not part of op transformations).
		// Should always be the case as during structification of the ops,
		// the functional args are always checked first. Hence, they always
		// need to be requested first and are thus at the beginning of the list.
		// From the functional type we know how many there must be.
		Type[] args = toRef.getArgs();
		boolean argsChanged = TypeModUtils.unliftTypes(args, List.class);
		Type[] outs = toRef.getOutTypes();
		boolean outsChanged = TypeModUtils.unliftTypes(outs, List.class);
		
		if (typesChanged && argsChanged && outsChanged) {
			return OpRef.fromTypes(toRef.getName(), refTypes, outs, args);
		}
		return null;
	}

}
