
package org.scijava.ops.engine.struct;

import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;

public class LazyJavadocParameterData implements ParameterData {

	public static final Map<Method, MethodParamInfo> paramDataMap =
		new ConcurrentHashMap<>();

	private final Method m;

	public LazyJavadocParameterData(Method m) {
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
		return paramDataMap.get(m);
	}

	public static synchronized void generateMethodParamInfo(
		List<FunctionalMethodType> fmts, Method m)
	{
		if (paramDataMap.containsKey(m)) return;

		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = m.getParameterCount();
		long numReturns = m.getReturnType() == void.class ? 0 : 1;
		// ensure the method declares a complete set of tags
		if (hasVanillaJavadoc(doc, numOpParams, numReturns)) {
			paramDataMap.put(m, javadocMethodParamInfo(doc, m));
		}
		else {
			paramDataMap.put(m, synthesizedMethodParamInfo(fmts));
		}
	}

	private static MethodParamInfo javadocMethodParamInfo(MethodJavadoc doc,
		Method m)
	{
		// scrape the conventional javadoc tags
		Parameter[] methodParams = m.getParameters();
		List<String> paramNames = new ArrayList<>(methodParams.length);
		List<String> paramDescriptions = new ArrayList<>(methodParams.length);
		IntStream.range(0, m.getParameterCount()).forEach(i -> {
			if (!methodParams[i].isAnnotationPresent(OpDependency.class)) {
				paramNames.add(doc.getParams().get(i).getName());
				paramDescriptions.add(doc.getParams().get(i).getComment().toString());
			}
		});
		String returnDescription = doc.getReturns().toString();
		return new MethodParamInfo(paramNames, paramDescriptions,
			returnDescription);
	}

	private static MethodParamInfo synthesizedMethodParamInfo(
		List<FunctionalMethodType> fmts)
	{
		List<String> paramNames = new ArrayList<>(fmts.size());
		List<String> paramDescriptions = new ArrayList<>(fmts.size());
		String returnDescription = "the output";

		int ins, outs, containers, mutables;
		ins = outs = containers = mutables = 1;
		for (FunctionalMethodType fmt : fmts) {
			String key;
			String description = "";
			switch (fmt.itemIO()) {
				case INPUT:
					key = "input" + ins++;
					break;
				case OUTPUT:
					key = "output" + outs++;
					break;
				case CONTAINER:
					key = "container" + containers++;
					break;
				case MUTABLE:
					key = "mutable" + mutables++;
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}
			paramNames.add(key);
			paramDescriptions.add(description);
			if (fmt.itemIO() != ItemIO.INPUT) returnDescription = description;
		}
		return new MethodParamInfo(paramNames, paramDescriptions,
			returnDescription);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		List<SynthesizedParameterMember<?>> params = new ArrayList<>();
		int ins = 0;

		for (FunctionalMethodType fmt : fmts) {
			Producer<MethodParamInfo> p = () -> LazyJavadocParameterData.getInfo(fmts,
				m);
			int paramNo = fmt.itemIO() == ItemIO.OUTPUT ? -1 : ins++;

			params.add(new SynthesizedParameterMember<>(fmt.type(), p, fmt.itemIO(),
				paramNo));
		}
		return params;
	}

}

class MethodParamInfo {

	private final List<String> paramNames;
	private final List<String> paramDescriptions;
	private final String outputName = "output";
	private final String outputDescription;

	public MethodParamInfo(final List<String> paramNames,
		final List<String> paramDescriptions, final String outputDescription)
	{
		this.paramNames = paramNames;
		this.paramDescriptions = paramDescriptions;
		this.outputDescription = outputDescription;
	}

	public String name(int index) {
		return index == -1 ? //
			outputName : //
			paramNames.get(index);
	}

	public String description(int index) {
		return index == -1 ? //
			outputDescription : //
			paramDescriptions.get(index);
	}
}

class AutogeneratedParamInfo {

}
