
package org.scijava.ops.engine.struct;

import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scijava.function.Producer;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.types.inference.InterfaceInference;

/**
 * Lazily generates the parameter data for a {@link List} of
 * {@link FunctionalMethodType}s. <b>If</b> there exists a <b>full</b> set of
 * {@code @param} and {@code @return} tags, the javadoc will be used to create
 * the parameter names and descriptions. Otherwise, reasonable defaults will be
 * used.
 * 
 * @author Gabriel Selzer
 */
public class LazilyGeneratedFieldParameterData implements ParameterData {

	private static final Map<Field, MethodParamInfo> paramDataMap =
		new HashMap<>();

	private final Field f;

	public LazilyGeneratedFieldParameterData(Field f) {
		this.f = f;
	}

	/**
	 * Determines whether {@code doc} has enough
	 * {@code @input/@container/@mutable} tags to satisfy the number of inputs to
	 * the Op ({@code numIns}), as well as enough
	 * {@code @container/@mutable/@output} taglets to satisfy the number of Op
	 * outputs ({@code numOuts}).
	 * 
	 * @param doc the {@link OtherJavadoc}s found by the javadoc parser.
	 * @param numIns the desired number of inputs
	 * @param numOuts the desired number of outputs
	 * @return true iff there are {@code numIns} inputs and {@code numOuts}
	 *         outputs
	 */
	private static boolean hasCustomJavadoc(List<OtherJavadoc> doc, long numIns,
		long numOuts)
	{
		int ins = 0, outs = 0;
		for (OtherJavadoc other : doc) {
			switch (other.getName()) {
				case "input":
					ins++;
					break;
				case "container":
				case "mutable":
					ins++;
					outs++;
					break;
				case "output":
					outs++;
					break;
			}
		}
		// We require as many input/container/mutable taglets as there are
		// parameters
		boolean sufficientIns = ins == numIns;
		// We require one container/mutable/output taglet
		boolean sufficientOuts = outs == numOuts;
		return sufficientIns && sufficientOuts;
	}

	public static MethodParamInfo getInfo(List<FunctionalMethodType> fmts,
		Field f)
	{
		if (!paramDataMap.containsKey(f)) generateFieldParamInfo(fmts, f);
		return paramDataMap.get(f);
	}

	public static synchronized void generateFieldParamInfo(
		List<FunctionalMethodType> fmts, Field f)
	{
		if (paramDataMap.containsKey(f)) return;

		Method sam = InterfaceInference.singularAbstractMethod(f.getType());
		FieldJavadoc doc = RuntimeJavadoc.getJavadoc(f);
		long numIns = sam.getParameterCount();
		long numOuts = 1; // There is always one output

		// determine the Op inputs/outputs

		if (hasCustomJavadoc(doc.getOther(), numIns, numOuts)) {
			paramDataMap.put(f, javadocFieldParamInfo(fmts, doc));
		}
		else {
			paramDataMap.put(f, synthesizedMethodParamInfo(fmts));
		}
	}

	private static MethodParamInfo javadocFieldParamInfo(
		List<FunctionalMethodType> fmts, FieldJavadoc doc)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts.size());

		List<OtherJavadoc> others = doc.getOther();
		int otherIndex = 0;
		int outputs = 0;
		for(int i = 0; i < fmts.size(); i++) {
			FunctionalMethodType fmt = fmts.get(i);
			OtherJavadoc other = others.get(otherIndex++);
			String name = other.getName();
			while (!validParameterTag(name)) {
				other = others.get(otherIndex++);
				name = other.getName();
			}

			// if the taglet is not output, it should have a name and description
				String param = other.getComment().toString();
				int space = param.indexOf(" ");
			if (!name.equals("output")) {
				if (space != -1) {
					fmtNames.put(fmt, param.substring(0, param.indexOf(" ")));
					fmtDescriptions.put(fmt, param.substring(param.indexOf(" ") + 1));
				}
				else {
					fmtNames.put(fmt, param);
					fmtDescriptions.put(fmt, "");
				}
			}
			// if the taglet is an output, it should just have a description
			else {
				fmtNames.put(fmt, "output" + ++outputs);
				fmtDescriptions.put(fmt, param);
			}
		}
		return new MethodParamInfo(fmtNames, fmtDescriptions);
	}

	/**
	 * Determines if {@code tagType} is one of the tags that we are interested in
	 * scraping.
	 * 
	 * @param tagType the tag we might need to scrape
	 * @return true iff it is interesting to us
	 */
	private static boolean validParameterTag(String tagType) {
		if (tagType.equals("input")) return true;
		if (tagType.equals("mutable")) return true;
		if (tagType.equals("container")) return true;
		if (tagType.equals("output")) return true;
		return false;
	}

	private static MethodParamInfo synthesizedMethodParamInfo(
		List<FunctionalMethodType> fmts)
	{
		Map<FunctionalMethodType, String> fmtNames = new HashMap<>(fmts.size());
		Map<FunctionalMethodType, String> fmtDescriptions = new HashMap<>(fmts.size());

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
		return new MethodParamInfo(fmtNames, fmtDescriptions);
	}

	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(
		List<FunctionalMethodType> fmts)
	{
		Producer<MethodParamInfo> p = //
			() -> LazilyGeneratedFieldParameterData.getInfo(fmts, f);

		return fmts.stream() //
			.map(fmt -> new SynthesizedParameterMember<>(fmt, p)) //
			.collect(Collectors.toList());
	}

}
