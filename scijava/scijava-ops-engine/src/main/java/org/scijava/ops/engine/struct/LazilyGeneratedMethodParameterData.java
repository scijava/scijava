
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
import org.scijava.ops.engine.reduce.ReductionUtils;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;

import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

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

	/**
	 * Determines whether {@code doc} has enough {@code @param} tags to satisfy
	 * the number of inputs to the Op ({@code numParams}), as well as enough
	 * {@code @return} taglets to satisfy the number of Op outputs
	 * ({@code numReturns}).
	 * 
	 * @param doc the {@link OtherJavadoc}s found by the javadoc parser.
	 * @param numParams the desired number of inputs
	 * @param numReturns the desired number of outputs
	 * @return true iff there are {@code numParams} inputs and {@code numReturns}
	 *         outputs
	 */
	private static boolean hasVanillaJavadoc(MethodJavadoc doc, long numParams,
		long numReturns)
	{
		// We require a @param tag for each of the method parameters
		boolean sufficientParams = doc.getParams().size() == numParams;
		// We require a @return tag for the method return iff not null
		boolean javadocReturn = !doc.getReturns().toString().isEmpty();
		boolean methodReturn = numReturns == 1;
		boolean sufficientReturn = javadocReturn == methodReturn;
		return sufficientParams && sufficientReturn;
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
		Map<FunctionalMethodType, Boolean> fmtOptionality = info
			.getFmtOptionality();
		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;
		Boolean[] paramOptionality = getParameterOptionality(m, opType,
			(int) numOpParams, new ArrayList<>());

		if (hasVanillaJavadoc(doc, numOpParams, numReturns)) {
			addJavadocMethodParamInfo(fmtNames, fmtDescriptions, fmts, fmtOptionality,
				doc, m, paramOptionality);
		}
		else {
			addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
				fmtOptionality, paramOptionality);
		}
	}

	public static synchronized void generateMethodParamInfo(
		List<FunctionalMethodType> fmts, Method m, Class<?> opType)
	{
		if (paramDataMap.containsKey(m)) return;

		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;

		opType = OpUtils.findFunctionalInterface(opType);
		Boolean[] paramOptionality = getParameterOptionality(m, opType,
			(int) numOpParams, new ArrayList<>());

		if (hasVanillaJavadoc(doc, numOpParams, numReturns)) {
			paramDataMap.put(m, javadocMethodParamInfo(fmts, doc, m,
				paramOptionality));
		}
		else {
			paramDataMap.put(m, synthesizedMethodParamInfo(fmts, paramOptionality));
		}
	}

	private static void addJavadocMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts,
		Map<FunctionalMethodType, Boolean> fmtOptionality, MethodJavadoc doc,
		Method m, Boolean[] paramOptionality)
	{
		// Assigns fmt inputs to javadoc params
		List<FunctionalMethodType> params = fmts.stream().filter(fmt -> fmt
			.itemIO() != ItemIO.OUTPUT).collect(Collectors.toList());
		Parameter[] methodParams = m.getParameters();
		int fmtIndex = 0;
		for (int i = 0; i < methodParams.length; i++) {
			Parameter methodParam = methodParams[i];
			if (methodParam.isAnnotationPresent(OpDependency.class)) continue;
			FunctionalMethodType fmt = params.get(fmtIndex++);
			fmtNames.put(fmt, doc.getParams().get(i).getName());
			fmtDescriptions.put(fmt, doc.getParams().get(i).getComment().toString());
			fmtOptionality.put(fmt, paramOptionality[i]);
		}

		// Assigns (pure) fmt output to javadoc return
		List<FunctionalMethodType> returns = fmts.stream().filter(fmt -> fmt
			.itemIO() == ItemIO.OUTPUT).collect(Collectors.toList());
		if (returns.size() > 1) throw new IllegalStateException(
			"A method can only have one return but this one has multiple!");
		if (returns.size() == 1) {
			fmtNames.put(returns.get(0), "output");
			fmtDescriptions.put(returns.get(0), doc.getReturns().toString());
		}
	}

	private static MethodParamInfo javadocMethodParamInfo(
		List<FunctionalMethodType> fmts, MethodJavadoc doc, Method m,
		Boolean[] paramOptionality)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());
		Map<FunctionalMethodType, Boolean> fmtOptionality = new HashMap<>(fmts
			.size());

		addJavadocMethodParamInfo(fmtNames, fmtDescriptions, fmts, fmtOptionality,
			doc, m, paramOptionality);

		return new MethodParamInfo(fmtNames, fmtDescriptions, fmtOptionality);
	}

	private static void addSynthesizedMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts,
		Map<FunctionalMethodType, Boolean> fmtOptionality,
		Boolean[] paramOptionality)
	{
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		int optionalIndex = 0;
		for (FunctionalMethodType fmt : fmts) {
			fmtDescriptions.put(fmt, "");
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.put(fmt, "input" + ins++);
					fmtOptionality.put(fmt, paramOptionality[optionalIndex++]);
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
		List<FunctionalMethodType> fmts, Boolean[] paramOptionality)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());
		Map<FunctionalMethodType, Boolean> fmtOptionality = new HashMap<>(fmts
			.size());

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts,
			fmtOptionality, paramOptionality);

		return new MethodParamInfo(fmtNames, fmtDescriptions, fmtOptionality);
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

	private static Boolean[] getParameterOptionality(Method m, Class<?> opType,
		int opParams, List<ValidityProblem> problems)
	{
		boolean opMethodHasOptionals = ReductionUtils.hasOptionalAnnotations(m);
		List<Method> fMethodsWithOptionals = ReductionUtils.fMethodsWithOptional(
			opType);
		// the number of parameters we need to determine

		// Ensure only the Op method OR ONE of its op type's functional methods have
		// Optionals
		if (opMethodHasOptionals && !fMethodsWithOptionals.isEmpty()) {
			problems.add(new ValidityProblem(
				"Both the OpMethod and its op type have optional parameters!"));
			return ReductionUtils.generateAllRequiredArray(opParams);
		}
		if (fMethodsWithOptionals.size() > 1) {
			problems.add(new ValidityProblem(
				"Multiple methods from the op type have optional parameters!"));
			return ReductionUtils.generateAllRequiredArray(opParams);
		}

		// return the optionality of each parameter of the Op
		if (opMethodHasOptionals) return getOpMethodOptionals(m, opParams);
		if (fMethodsWithOptionals.size() > 0) return ReductionUtils
			.findParameterOptionality(fMethodsWithOptionals.get(0));
		return ReductionUtils.generateAllRequiredArray(opParams);
	}

	private static Boolean[] getOpMethodOptionals(Method m, int opParams) {
		int[] paramIndex = mapFunctionalParamsToIndices(m.getParameters());
		Boolean[] arr = ReductionUtils.generateAllRequiredArray(opParams);
		// check parameters on m
		Boolean[] mOptionals = ReductionUtils.findParameterOptionality(m);
		for (int i = 0; i < mOptionals.length; i++) {
			int index = paramIndex[i];
			if (index == -1) continue;
			arr[index] |= mOptionals[i];
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
