package org.scijava.param;

import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.MethodJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import com.google.common.collect.Streams;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpMethod;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.create.CreateOpCollection;
import org.scijava.ops.simplify.SimplificationUtils;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;
import org.scijava.types.Types;

/**
 * Tests the ability of a Javadoc parser to scrape an Op's parameters out of its Javadoc
 * @author G
 *
 */
@Plugin(type = OpCollection.class)
public class JavadocParameterTest extends AbstractTestEnvironment {
	
	/**
	 * 
	 * @param in1 the first input
	 * @param in2 the second input
	 * @return in1 + in2
	 */
	@OpMethod(names = "test.javadoc", type = BiFunction.class)
	public static List<Long> OpMethodFoo(List<String> in1, List<String> in2) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) + Long.parseLong(s2);
		return Streams.zip(in1.stream(), in2.stream(), func).collect(Collectors.toList());
	}
	
	/**
	 * @param thing the input
	 * @return the output
	 */
	@OpField(names = "test.javadoc")
	public final Function<Double, Double> javadocFieldOp = (in) -> in + 1;
	
	@Test
	public void testJavadocMethod() throws NoSuchMethodException, SecurityException, NoSuchFieldException {
		Method fMethod = SimplificationUtils.findFMethod(JavadocOp.class);
		Type[] exactParameterTypes = Types.getExactParameterTypes(fMethod, JavadocOp.class);
		Class<?>[] rawParamTypes = Arrays.stream(exactParameterTypes).map(t -> Types.raw(t)).toArray(Class[]::new);
		Method opMethod = JavadocOp.class.getMethod(fMethod.getName(), rawParamTypes);
		MethodJavadoc javadoc = RuntimeJavadoc.getJavadoc(opMethod);
		System.out.println("Functional Method javadoc" + javadoc);
		
		ClassJavadoc classdoc = RuntimeJavadoc.getJavadoc(org.scijava.ops.OpInfo.class);
		System.out.println("Class javadoc" + classdoc);
		
		ClassJavadoc createDoc = RuntimeJavadoc.getJavadoc(CreateOpCollection.class);
		System.out.println("Create javadoc: " + createDoc);

		ClassJavadoc testDoc = RuntimeJavadoc.getJavadoc(JavadocOp.class);
		System.out.println("Class javado" + testDoc);
		ops.op("math.add").input(2, 3).apply();
		
		Field field = this.getClass().getField("javadocFieldOp");
		FieldJavadoc fieldDoc = RuntimeJavadoc.getJavadoc(field);
	}

}

/**
 * Test Op used to see if we can't scrape the javadoc.
 * @author Gabriel Selzer
 *
 */
@Plugin(type = Op.class) 
@Parameter(key = "input")
@Parameter(key = "output", itemIO = ItemIO.OUTPUT)
class JavadocOp implements Function<Double, Double> {

	/**
	 * @param t the input
	 * @return u the output
	 */
	@Override
	public Double apply(Double t) {
		return t + 1;
	}
	
}
