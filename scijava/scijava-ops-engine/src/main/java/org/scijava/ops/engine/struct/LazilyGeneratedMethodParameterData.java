
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.common3.validity.ValidityProblem;
import org.scijava.function.Producer;
import org.scijava.ops.engine.OpUtils;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;


/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 * 
 * @author Gabriel Selzer
 */
public class LazilyGeneratedMethodParameterData implements ParameterData {

	private static final Map<Method, MethodParamInfo> paramDataMap =
		new HashMap<>();

	private final Method m;
	private final Class<?> opType;

	public LazilyGeneratedMethodParameterData(Method m, Class<?> opType) {
		this.m = m;
		this.opType = opType;
	}

	public static MethodParamInfo getInfo(List<FunctionalMethodType> fmts,
		Method m, Class<?> opType)
	{
		if (!paramDataMap.containsKey(m)) generateMethodParamInfo(fmts, m, opType);
		MethodParamInfo info = paramDataMap.get(m);
		if (!info.containsAll(fmts)) updateMethodParamInfo(info, fmts, m, opType);
		return info;
	}

	private static void updateMethodParamInfo(MethodParamInfo info,
		List<FunctionalMethodType> fmts, Method m, Class<?> opType)
	{
		Map<FunctionalMethodType, String> fmtNames = info.getFmtNames();
		Map<FunctionalMethodType, String> fmtDescriptions = info
			.getFmtDescriptions();
		Map<FunctionalMethodType, Boolean> fmtNullability = info
			.getFmtNullability();
		// determine the Op inputs/outputs
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;
		Boolean[] paramNullability = getParameterNullability(m, opType,
			(int) numOpParams, new ArrayList<>());

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
			fmtNullability, paramNullability);
	}

	public static synchronized void generateMethodParamInfo(
		List<FunctionalMethodType> fmts, Method m, Class<?> opType)
	{
		if (paramDataMap.containsKey(m)) return;

		// determine the Op inputs/outputs
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;

		opType = OpUtils.findFunctionalInterface(opType);
		Boolean[] paramNullability = getParameterNullability(m, opType,
			(int) numOpParams, new ArrayList<>());

		paramDataMap.put(m, synthesizedMethodParamInfo(fmts, paramNullability));
	}


	private static void addSynthesizedMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts,
		Map<FunctionalMethodType, Boolean> fmtNullability,
		Boolean[] paramNullability)
	{
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		int nullableIndex = 0;
		for (FunctionalMethodType fmt : fmts) {
			fmtDescriptions.put(fmt, "");
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.put(fmt, "input" + ins++);
					fmtNullability.put(fmt, paramNullability[nullableIndex++]);
					break;
				case OUTPUT:
					fmtNames.put(fmt, "output" + outs++);
					break;
				case CONTAINER:
					fmtNames.put(fmt, "container" + containers++);
					break;
				case MUTABLE:
					fmtNames.put(fmt, "mutable" + mutables++);
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
		}
	}

	private static MethodParamInfo synthesizedMethodParamInfo(
		List<FunctionalMethodType> fmts, Boolean[] paramNullability)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());
		Map<FunctionalMethodType, Boolean> fmtNullability = new HashMap<>(fmts
			.size());

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
			fmtNullability, paramNullability);

		return new MethodParamInfo(fmtNames, fmtDescriptions, fmtNullability);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Producer<MethodParamInfo> p = //
			() -> LazilyGeneratedMethodParameterData.getInfo(fmts, m, opType);

		return fmts.stream() //
			.map(fmt -> new SynthesizedParameterMember<>(fmt, p)) //
			.collect(Collectors.toList());
	}

	private static Boolean[] getParameterNullability(Method m, Class<?> opType,
		int opParams, List<ValidityProblem> problems)
	{
		boolean opMethodHasNullables = FunctionalParameters.hasNullableAnnotations(m);
		List<Method> fMethodsWithNullables = FunctionalParameters.fMethodsWithNullable(
			opType);
		// the number of parameters we need to determine

		// Ensure only the Op method OR ONE of its op type's functional methods have
		// Nullables
		if (opMethodHasNullables && !fMethodsWithNullables.isEmpty()) {
			problems.add(new ValidityProblem(
				"Both the OpMethod and its op type have nullable parameters!"));
			return FunctionalParameters.generateAllRequiredArray(opParams);
		}
		if (fMethodsWithNullables.size() > 1) {
			problems.add(new ValidityProblem(
				"Multiple methods from the op type have nullable parameters!"));
			return FunctionalParameters.generateAllRequiredArray(opParams);
		}

		// return the nullability of each parameter of the Op
		if (opMethodHasNullables) return getOpMethodNullables(m, opParams);
		if (fMethodsWithNullables.size() > 0) return FunctionalParameters
			.findParameterNullability(fMethodsWithNullables.get(0));
		return FunctionalParameters.generateAllRequiredArray(opParams);
	}

	private static Boolean[] getOpMethodNullables(Method m, int opParams) {
		int[] paramIndex = mapFunctionalParamsToIndices(m.getParameters());
		Boolean[] arr = FunctionalParameters.generateAllRequiredArray(opParams);
		// check parameters on m
		Boolean[] mNullables = FunctionalParameters.findParameterNullability(m);
		for (int i = 0; i < mNullables.length; i++) {
			int index = paramIndex[i];
			if (index == -1) continue;
			arr[index] |= mNullables[i];
		}
		return arr;
	}

	/**
	 * Since Ops written as methods can have an {@link OpDependency} (or multiple)
	 * as parameters, we need to determine which parameter indices correspond to
	 * the inputs of the Op.
	 *
	 * @param parameters the list of {@link Parameter}s of the Op
	 * @return an array of ints where the value at index {@code i} denotes the
	 *         position of the parameter in the Op's signature. Values of
	 *         {@code -1} designate an {@link OpDependency} at that position.
	 */
	private static int[] mapFunctionalParamsToIndices(Parameter[] parameters) {
		int[] paramNo = new int[parameters.length];
		int paramIndex = 0;
		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].isAnnotationPresent(OpDependency.class)) {
				paramNo[i] = -1;
			}
			else {
				paramNo[i] = paramIndex++;
			}
		}
		return paramNo;
	}

}
