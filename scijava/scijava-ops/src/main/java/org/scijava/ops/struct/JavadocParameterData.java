
package org.scijava.ops.struct;

import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scijava.ops.OpDependency;
import org.scijava.ops.OpInfo;
import org.scijava.ops.simplify.SimplificationUtils;
import org.scijava.struct.Member;
import org.scijava.types.Types;

/**
 * Class able to scrape Op parameter metadata off the Op's Javadoc.
 * 
 * @author Gabriel Selzer
 */
public class JavadocParameterData implements ParameterData {

	List<String> paramNames;
	List<String> paramDescriptions;
	String returnDescription;

	public JavadocParameterData(Method m) {
		parseMethod(m);
	}

	public JavadocParameterData(Class<?> c) {
		try {
			parseMethod(getOpMethod(c));
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Op class " + c +
				" does not declare a functional method!");
		}
	}

	/**
	 * Parses a {@link Field} for {@code @param}, {@code @return} tags. HACK:
	 * since {@code Field}s shouldn't normally have these tags, we have to parse
	 * these manually.
	 * 
	 * @param f the field
	 */
	public JavadocParameterData(Field f) {
		Method sam = ParameterStructs.singularAbstractMethod(f.getType());
		FieldJavadoc doc = RuntimeJavadoc.getJavadoc(f);
		long numIns = sam.getParameterCount();
		long numOuts = 1; // There is always one output
		if (hasCustomJavadoc(doc.getOther(), numIns, numOuts))
			populateViaCustomTaglets(doc.getOther());
		else throw new IllegalArgumentException("Field " + f +
			" does not have enough taglets to generate OpInfo documentation!");
	}

	public JavadocParameterData(OpInfo info, Type newType) {
		paramNames = new ArrayList<>();
		paramDescriptions = new ArrayList<>();
		List<Member<?>> inputs = new ArrayList<>(info.inputs());
		Member<?> output = info.output();

		// this method is called when the op is adapted/simplified. In the case of
		// adaptation, the op's output might shift from a pure output to an input,
		// or might shift from a container to a pure output. We
		Method sam = ParameterStructs.singularAbstractMethod(Types.raw(newType));
		if (sam.getParameterCount() > inputs.size()) {
			inputs.add(output);
		}
		else if (sam.getParameterCount() < inputs.size()) {
			// one of the inputs is an I/O and should be a pure output. We need to
			// remove it from the inputs.
			Optional<Member<?>> ioMember = inputs.parallelStream().filter(m -> m
				.isOutput()).findFirst();
			if (ioMember == null) throw new IllegalArgumentException(
				"Cannot transform Op of type " + info.opType() + " into type " +
					newType + "; at least one input must also be an output!");

			inputs.remove(ioMember.get());
		}
		for (Member<?> m : inputs) {
			paramNames.add(m.getKey());
			paramDescriptions.add(m.getDescription());
		}
		returnDescription = output.getDescription();
	}

	public List<String> paramNames() {
		return paramNames;
	}

	public List<String> paramDescriptions() {
		return paramDescriptions;
	}

	public String returnDescription() {
		return returnDescription;
	}

	// -- Helper methods -- //

	/**
	 * Finds the abstract {@link FunctionalInterface} method implemented by the Op
	 * {@code c}
	 * 
	 * @param c the Op {@link Class}
	 * @return the {@link Method} of the {@link FunctionalInterface} implemented
	 *         by {@code c}
	 * @throws NoSuchMethodException when {@code c} does not implement its
	 *           functional method
	 */
	private Method getOpMethod(Class<?> c) throws NoSuchMethodException {
		// NB this is the functional method w.r.t. the interface, not w.r.t. the Op
		Method fMethod = SimplificationUtils.findFMethod(c);
		Type[] paramTypes = Types.getExactParameterTypes(fMethod, c);
		Class<?>[] rawParamTypes = Arrays.stream(paramTypes).map(t -> Types.raw(t))
			.toArray(Class[]::new);
		return c.getMethod(fMethod.getName(), rawParamTypes);
	}

	/**
	 * Parses the {@code @param} and {@code @return} annotations of {@link Method}
	 * {@code m}.
	 * 
	 * @param m the method whose javadoc has tags we want to parse
	 */
	private void parseMethod(Method m) {
		// determine the Op inputs/outputs
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		long numOpParams = getOpParams(m);
		long numReturns = m.getReturnType() == void.class ? 0 : 1;
		// ensure the method declares a complete set of tags
		if (!hasVanillaJavadoc(doc, numOpParams, numReturns))
			throw new IllegalArgumentException("Method " + m +
				" has no suitable tag(lets) to scrape documentation from");
		// scrape the conventional javadoc tags
		populateViaParamAndReturn(doc.getParams(), doc.getReturns());
	}

	private long getOpParams(Method m) {
		return Arrays //
			.stream(m.getParameters()) //
			.filter(param -> param.getAnnotation(OpDependency.class) == null).count();
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
	private boolean hasVanillaJavadoc(MethodJavadoc doc, long numParams,
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
	private boolean hasCustomJavadoc(List<OtherJavadoc> doc, long numIns,
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

	/**
	 * Populates {@link JavadocParameterData#paramNames} and
	 * {@link JavadocParameterData#paramDescriptions} using {@code params}, and
	 * {@link JavadocParameterData#returnDescription} using {@code returnDoc}.
	 * 
	 * @param params the {@code @param} tags on the method of interest
	 * @param returnDoc the string following {@code @return}
	 */
	private void populateViaParamAndReturn(List<ParamJavadoc> params,
		Comment returnDoc)
	{
		paramNames = params.stream().map(param -> param.getName()).collect(
			Collectors.toList());
		paramDescriptions = params.stream().map(param -> param.getComment()
			.toString()).collect(Collectors.toList());
		returnDescription = returnDoc.toString();
	}

	/**
	 * Populates {@link JavadocParameterData#paramNames},
	 * {@link JavadocParameterData#paramDescriptions}, and, and
	 * {@link JavadocParameterData#returnDescription} using
	 * {@code @input/@output/@container/@mutable} taglets.
	 * 
	 * @param doc the {@link List} of {@link OtherJavadoc}s containing all of the
	 *          taglets we need to parse
	 */
	private void populateViaCustomTaglets(List<OtherJavadoc> doc) {
		paramNames = new ArrayList<>();
		paramDescriptions = new ArrayList<>();
		for (OtherJavadoc other : doc) {
			String name = other.getName();
			if (!validParameterTag(name)) continue;
			// add to params if not a pure output
			if (!name.equals("output")) {
				String param = other.getComment().toString();
				int space = param.indexOf(" ");
				if (space != -1) {
					paramNames.add(param.substring(0, param.indexOf(" ")));
					paramDescriptions.add(param.substring(param.indexOf(" ") + 1));
				}
				else {
					paramNames.add(param);
					paramDescriptions.add("");
				}
			}
			// add return description if an I/O
			if (!name.equals("input")) {
				if (returnDescription != null) throw new IllegalArgumentException(
					"Op cannot have multiple returns!");
				returnDescription = other.getComment().toString();
			}
		}
	}

	/**
	 * Determines if {@code tagType} is one of the tags that we are interested in
	 * scraping.
	 * 
	 * @param tagType the tag we might need to scrape
	 * @return true iff it is interesting to us
	 */
	private boolean validParameterTag(String tagType) {
		if (tagType.equals("input")) return true;
		if (tagType.equals("mutable")) return true;
		if (tagType.equals("container")) return true;
		if (tagType.equals("output")) return true;
		return false;
	}

	/**
	 * Synthesizes a set of {@link Member}s using the data present in the
	 * javadoc, as well as {@code fmts}.
	 * 
	 * @param fmts the list of inputs, outputs, and other types required by the Op
	 */
	@Override
	public List<SynthesizedParameterMember<?>> synthesizeMembers(List<FunctionalMethodType> fmts) {
		List<SynthesizedParameterMember<?>> params = new ArrayList<>();
		int ins = 0;
		int outs = 0;

		for (FunctionalMethodType fmt : fmts) {
			String key;
			String description;
			switch (fmt.itemIO()) {
				case INPUT:
					key = paramNames.get(ins);
					description = paramDescriptions.get(ins);
					ins++;
					break;
				case OUTPUT:
					// NB the @return tag does not provide a name, only a comment
					key = "output" + (outs == 0 ? "" : outs);
					description = returnDescription;
					outs++;
					break;
				case CONTAINER:
					key = paramNames.get(ins);
					description = paramDescriptions.get(ins);
					ins++;
					outs++;
					break;
				case MUTABLE:
					key = paramNames.get(ins);
					description = paramDescriptions.get(ins);
					ins++;
					outs++;
					break;
				default:
					throw new RuntimeException("Unexpected ItemIO type encountered!");
			}

			params.add(new SynthesizedParameterMember<>(fmt.type(), key, description,
				fmt.itemIO()));
		}
		return params;
	}

}
