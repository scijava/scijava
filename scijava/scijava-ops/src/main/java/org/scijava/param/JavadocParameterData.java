
package org.scijava.param;

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
import java.util.stream.Collectors;

import org.scijava.ops.simplify.SimplificationUtils;
import org.scijava.types.Types;

/**
 * Class able to scrape Op parameter metadata off the Op's Javadoc.
 * 
 * @author Gabriel Selzer
 */
public class JavadocParameterData {

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
		paramNames = new ArrayList<>();
		paramDescriptions = new ArrayList<>();
		FieldJavadoc doc = RuntimeJavadoc.getJavadoc(f);
		for (OtherJavadoc other : doc.getOther()) {
			switch (other.getName()) {
				case "param":
					String param = other.getComment().toString();
					paramNames.add(param.substring(0, param.indexOf(" ")));
					paramDescriptions.add(param.substring(param.indexOf(" ") + 1));
					break;
				case "return":
					if (returnDescription != null) throw new IllegalArgumentException(
						"Op cannot have multiple returns!");
					returnDescription = other.getComment().toString();
					break;
			}

		}
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
		List<ParamJavadoc> params = doc.getParams();
		paramNames = params.stream().map(param -> param.getName()).collect(
			Collectors.toList());
		paramDescriptions = params.stream().map(param -> param.getComment()
			.toString()).collect(Collectors.toList());
		returnDescription = doc.getReturns().toString();
	}

}
