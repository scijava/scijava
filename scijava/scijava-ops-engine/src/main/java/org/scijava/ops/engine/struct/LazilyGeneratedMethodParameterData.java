
package org.scijava.ops.engine.struct;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.function.Producer;
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

	public LazilyGeneratedMethodParameterData(Method m) {
		this.m = m;
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
		Method m)
	{
		if (!paramDataMap.containsKey(m)) generateMethodParamInfo(fmts, m);
		MethodParamInfo info = paramDataMap.get(m);
		if (!info.containsAll(fmts)) updateMethodParamInfo(info, fmts, m);
		return info;
	}

	private static void updateMethodParamInfo(MethodParamInfo info,
		List<FunctionalMethodType> fmts, Method m)
	{
		Map<FunctionalMethodType, String> fmtNames = info.getFmtNames();
		Map<FunctionalMethodType, String> fmtDescriptions = info
			.getFmtDescriptions();
		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;

		if (hasVanillaJavadoc(doc, numOpParams, numReturns)) {
			addJavadocMethodParamInfo(fmtNames, fmtDescriptions, fmts, doc, m);
		}
		else {
			addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts);
		}
	}

	public static synchronized void generateMethodParamInfo(
		List<FunctionalMethodType> fmts, Method m)
	{
		if (paramDataMap.containsKey(m)) return;

		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;

		if (hasVanillaJavadoc(doc, numOpParams, numReturns)) {
			paramDataMap.put(m, javadocMethodParamInfo(fmts, doc, m));
		}
		else {
			paramDataMap.put(m, synthesizedMethodParamInfo(fmts));
		}
	}

	private static void addJavadocMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts, MethodJavadoc doc, Method m)
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
		List<FunctionalMethodType> fmts, MethodJavadoc doc, Method m)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());

		addJavadocMethodParamInfo(fmtNames, fmtDescriptions, fmts, doc, m);

		return new MethodParamInfo(fmtNames, fmtDescriptions);
	}

	private static void addSynthesizedMethodParamInfo(
		Map<FunctionalMethodType, String> fmtNames,
		Map<FunctionalMethodType, String> fmtDescriptions,
		List<FunctionalMethodType> fmts)
	{
		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		for (FunctionalMethodType fmt : fmts) {
			fmtDescriptions.put(fmt, "");
			switch (fmt.itemIO()) {
				case INPUT:
					fmtNames.put(fmt, "input" + ins++);
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
		List<FunctionalMethodType> fmts)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts
			.size());

		addSynthesizedMethodParamInfo(fmtNames, fmtDescriptions, fmts);

		return new MethodParamInfo(fmtNames, fmtDescriptions);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Producer<MethodParamInfo> p = //
			() -> LazilyGeneratedMethodParameterData.getInfo(fmts, m);

		return fmts.stream() //
			.map(fmt -> new SynthesizedParameterMember<>(fmt, p)) //
			.collect(Collectors.toList());
	}

}
