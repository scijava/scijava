
package org.scijava.param;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Streams;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.OpInfo;
import org.scijava.ops.OpMethod;
import org.scijava.ops.OpUtils;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;

/**
 * Tests the ability of a Javadoc parser to scrape an Op's parameters out of its
 * Javadoc
 * 
 * @author G
 */
@Plugin(type = OpCollection.class)
public class JavadocParameterTest extends AbstractTestEnvironment {

	/**
	 * @param foo the first input
	 * @param bar the second input
	 * @return foo + bar
	 */
	@OpMethod(names = "test.javadoc.method", type = BiFunction.class)
	public static List<Long> OpMethodFoo(List<String> foo, List<String> bar) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) +
			Long.parseLong(s2);
		return Streams.zip(foo.stream(), bar.stream(), func).collect(Collectors
			.toList());
	}

	@Test
	public void testJavadocMethod() {
		Iterator<OpInfo> infos = ops.env().infos("test.javadoc.method").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(inputNames, new String[] { "foo", "bar" });

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(inputDescriptions, new String[] { "the first input", "the second input" });

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("output", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("foo + bar", outputDescription);
	}

	/**
	 * @input in the input
	 * @output the output
	 */
	@OpField(names = "test.javadoc.field")
	public final Function<Double, Double> javadocFieldOp = (in) -> in + 1;

	@Test
	public void testJavadocField() {
		Iterator<OpInfo> infos = ops.env().infos("test.javadoc.field").iterator();

		if (!infos.hasNext()) {
			Assert.fail("No OpInfos with name \"test.javadoc.field\"");
		}
		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.field\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "in" }, inputNames);

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "the input" }, inputDescriptions);

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("output", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		assertEquals("the output", outputDescription);
	}

	@Test
	public void testJavadocClass() {
		Iterator<OpInfo> infos = ops.env().infos("test.javadoc.class").iterator();

		if (!infos.hasNext()) {
			Assert.fail("No OpInfos with name \"test.javadoc.class\"");
		}
		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.class\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "t" }, inputNames);

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "the input" }, inputDescriptions);

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("output", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the output", outputDescription);
	}

	@Test
	public void opStringRegressionTest() {
		Iterator<OpInfo> infos = ops.env().infos("test.javadoc.method").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}

		// test standard op string
		String expected =
			"public static java.util.List<java.lang.Long> org.scijava.param.JavadocParameterTest." +
				"OpMethodFoo(java.util.List<java.lang.String>,java.util.List<java.lang.String>)(\n" +
				"	 Inputs:\n" +
				"		java.util.List<java.lang.String> foo -> the first input\n" +
				"		java.util.List<java.lang.String> bar -> the second input\n" +
				"	 Outputs:\n" +
				"		java.util.List<java.lang.Long> output -> foo + bar\n" + ")\n";
		String actual = info.toString();
		Assert.assertEquals(expected, actual);

		// test special op string
		expected = "(java.util.List<java.lang.Long> output -> foo + bar) =\n" +
			"	public static java.util.List<java.lang.Long> org.scijava.param.JavadocParameterTest." +
			"OpMethodFoo(java.util.List<java.lang.String>,java.util.List<java.lang.String>)(\n" +
			"		java.util.List<java.lang.String> foo -> the first input,\n" +
			"==>		java.util.List<java.lang.String> bar -> the second input)";
		actual = OpUtils.opString(info, info.inputs().get(1));
		Assert.assertEquals(expected, actual);
	}

}

/**
 * Test Op used to see if we can't scrape the javadoc.
 * 
 * @author Gabriel Selzer
 */
@Plugin(type = Op.class, name = "test.javadoc.class")
class JavadocOp implements Function<Double, Double> {

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return t + 1;
	}

}
