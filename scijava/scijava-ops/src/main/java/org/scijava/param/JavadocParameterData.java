
package org.scijava.param;

import com.github.therapi.runtimejavadoc.Comment;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.ParamJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;

import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
		if (hasCustomJavadoc(doc.getOther(), sam)) populateViaCustomTaglets(doc
			.getOther());
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
		for(Member<?> m : inputs) {
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
		MethodJavadoc doc = RuntimeJavadoc.getJavadoc(m);
		if (hasVanillaJavadoc(doc, m))
			populateViaParamAndReturn(doc.getParams(), doc.getReturns());
		else if (hasCustomJavadoc(doc.getOther(), m))
			populateViaCustomTaglets(doc.getOther());
		else throw new IllegalArgumentException("Method " + m +
			" has no suitable tag(lets) to scrape documentation from");
	}

	private boolean hasVanillaJavadoc(MethodJavadoc doc, Method m) {
		// We require a @param tag for each of the method parameters
		boolean sufficientParams = doc.getParams().size() == m.getParameterCount();
		// We require a @return tag for the method return iff not null
		boolean sufficientReturn = !((doc.getReturns() != null) ^ (m
			.getReturnType() != void.class));
		return sufficientParams && sufficientReturn;
	}

	private boolean hasCustomJavadoc(List<OtherJavadoc> doc, Method m) {
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
		// We require as many input/container/mutable taglets as there are parameters
		boolean sufficientIns = ins == m.getParameterCount();
		// We require one container/mutable/output taglet
		boolean sufficientOuts = outs == 1;
		return sufficientIns && sufficientOuts;
	}

	private void populateViaParamAndReturn(List<ParamJavadoc> params, Comment returnDoc) {
		paramNames = params.stream().map(param -> param.getName()).collect(Collectors.toList());
		paramDescriptions = params.stream().map(param -> param.getComment().toString()).collect(Collectors.toList());
		returnDescription = returnDoc.toString();
	}

	private void populateViaCustomTaglets(List<OtherJavadoc> doc) {
		paramNames = new ArrayList<>();
		paramDescriptions = new ArrayList<>();
		for (OtherJavadoc other : doc) {
			String name = other.getName();
			if (!validParameterTag(name)) continue;
			// add to params if not a pure output
			if (!name.equals("output")) {
				String param = other.getComment().toString();
				paramNames.add(param.substring(0, param.indexOf(" ")));
				paramDescriptions.add(param.substring(param.indexOf(" ") + 1));
			}
			// add return description if an I/O 
			if (!name.equals("input")) {
				if (returnDescription != null) throw new IllegalArgumentException(
					"Op cannot have multiple returns!");
				returnDescription = other.getComment().toString();
			}
		}
	}

	private boolean validParameterTag(String tagType) {
		if (tagType.equals("input")) return true;
		if (tagType.equals("mutable")) return true;
		if (tagType.equals("container")) return true;
		if (tagType.equals("output")) return true;
		return false;
	}

	@Override
	public List<Parameter> synthesizeAnnotations(List<FunctionalMethodType> fmts) {
		List<Parameter> params = new ArrayList<>();
		int ins = 0;
		int outs = 0;

		Map<String, Object> paramValues = new HashMap<>();
		for (FunctionalMethodType fmt : fmts) {
			paramValues.clear();
			paramValues.put(Parameter.ITEMIO_FIELD_NAME, fmt.itemIO());
			
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
			
			paramValues.put(Parameter.KEY_FIELD_NAME, key);
			paramValues.put(Parameter.DESCRIPTION_FIELD_NAME, description);
			
			try {
				params.add(TypeFactory.annotation(Parameter.class, paramValues));
			} catch (AnnotationFormatException e) {
				throw new RuntimeException("Error during Parameter annotation synthetization. This is "
						+ "most likely an implementation error.", e);
			}
		}
		return params;
	}

}
