
package org.scijava.ops.engine;

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpUtils;
import org.scijava.ops.spi.Op;
import org.scijava.ops.spi.OpClass;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpDependency;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

/**
 * Tests the ability of a Javadoc parser to scrape an Op's parameters out of its
 * Javadoc
 * 
 * @author Gabriel Selzer
 */
public class JavadocParameterTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeClass
	public static void addNeededOps() {
		discoverer.register(JavadocParameterTest.class, "opcollection");
		discoverer.register(JavadocOp.class, "op");
		discoverer.register(JavadocComplexOp.class, "op");
	}

	/**
	 * Tests javadoc scraping with param (P) and return (R)
	 * 
	 * @param foo the first input
	 * @param bar the second input
	 * @return foo + bar
	 */
	@OpMethod(names = "test.javadoc.methodPR", type = BiFunction.class)
	public static List<Long> OpMethodPR(List<String> foo, List<String> bar) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) +
			Long.parseLong(s2);
		return Streams.zip(foo.stream(), bar.stream(), func).collect(Collectors
			.toList());
	}

	/**
	 * Tests javadoc scraping with input (I) and output (O)
	 * 
	 * @input foo the first input
	 * @input bar the second input
	 * @output foo + bar
	 */
	@OpMethod(names = "test.javadoc.methodIO", type = BiFunction.class)
	public static List<Long> OpMethodIO(List<String> foo, List<String> bar) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) +
			Long.parseLong(s2);
		return Streams.zip(foo.stream(), bar.stream(), func).collect(Collectors
			.toList());
	}

	/**
	 * Tests javadoc scraping with input (I) and return (R)
	 * 
	 * @input foo the first input
	 * @input bar the second input
	 * @return foo + bar
	 */
	@OpMethod(names = "test.javadoc.methodIR", type = BiFunction.class)
	public static List<Long> OpMethodIR(List<String> foo, List<String> bar) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) +
			Long.parseLong(s2);
		return Streams.zip(foo.stream(), bar.stream(), func).collect(Collectors
			.toList());
	}

	/**
	 * Tests javadoc scraping with input (I) ONLY
	 * 
	 * @input foo the first input
	 * @input bar the second input
	 */
	@OpMethod(names = "test.javadoc.methodI", type = BiFunction.class)
	public static List<Long> OpMethodI(List<String> foo, List<String> bar) {
		BiFunction<String, String, Long> func = (s1, s2) -> Long.parseLong(s1) +
			Long.parseLong(s2);
		return Streams.zip(foo.stream(), bar.stream(), func).collect(Collectors
			.toList());
	}

	@Test
	public void testJavadocMethodPR() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodPR").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}
		isSuitableScrapedOpMethodInfo(info);
	}

	@Test
	public void testJavadocMethodIO() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodIO").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}
		isSuitableGenericOpMethodInfo(info);
	}

	@Test
	public void testJavadocMethodIR() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodIR").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}
		isSuitableGenericOpMethodInfo(info);
	}
	
	@Test
	public void testJavadocMethodI() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodI").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}
		isSuitableGenericOpMethodInfo(info);
	}

	/**
	 * Tests javadoc scraping of mutable taglet
	 * 
	 * @param foo the i/o argument
	 */
	@OpMethod(names = "test.javadoc.methodInplaceI", type = Inplaces.Arity1.class)
	public static void OpMethodInplaceI(List<String> foo) {
		for (int i = 0; i < foo.size(); i++) {
			foo.set(i, foo.get(i) + " foo");
		}
	}

	/**
	 * Tests javadoc scraping of mutable taglet
	 * 
	 * @param inplace the Op being wrapped
	 * @param foo the i/o argument
	 */
	@OpMethod(names = "test.javadoc.methodDependency", type = Inplaces.Arity1.class)
	public static void OpMethodInplaceI(@OpDependency(
		name = "test.javadoc.methodInplaceI") Inplaces.Arity1<List<String>> inplace,
		List<String> foo)
	{
		inplace.mutate(foo);
	}

	@Test
	public void testJavadocMethodInplaceI() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodInplaceI").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(inputNames, new String[] { "foo" });

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m
			.getDescription()).toArray(String[]::new);
		Assert.assertArrayEquals(inputDescriptions, new String[] {
			"the i/o argument"});

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("foo", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the i/o argument", outputDescription);
	}
	
	@Test
	public void testJavadocMethodInplaceWithDepedency() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodDependency").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.methodDependency\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(inputNames, new String[] { "foo" });

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m
			.getDescription()).toArray(String[]::new);
		Assert.assertArrayEquals(inputDescriptions, new String[] {
			"the i/o argument"});

		// assert dependency descriptions
		String[] dependencyDescriptions = info.dependencies().stream().map(m -> m
			.getDescription()).toArray(String[]::new);
		Assert.assertArrayEquals(dependencyDescriptions, new String[] {
			"the Op being wrapped"});

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("foo", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the i/o argument", outputDescription);
	}

	/**
	 * Asserts that the {@link OpInfo} has as inputs:
	 * <ul>
	 * <li> foo - the first input
	 * <li> bar - the second input
	 * </ul>
	 * and as output:
	 * <ul>
	 * <li> output - foo + bar
	 * </ul>
	 */
	private void isSuitableScrapedOpMethodInfo(OpInfo info) {
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
	 * Asserts that the {@link OpInfo} has as inputs:
	 * <ul>
	 * <li> input1
	 * <li> input2
	 * </ul>
	 * and as output:
	 * <ul>
	 * <li> output
	 * </ul>
	 */
	private void isSuitableGenericOpMethodInfo(OpInfo info) {
		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "input1", "input2" }, inputNames);

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "", ""}, inputDescriptions);

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("output1", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("", outputDescription);
	}

	/**
	 * @input in the input
	 * @output the output
	 */
	@OpField(names = "test.javadoc.fieldF")
	public final Function<Double, Double> javadocFieldOp = (in) -> in + 1;

	/**
	 * @input inList the input
	 * @container outList the preallocated output
	 */
	@OpField(names = "test.javadoc.fieldC")
	public final Computers.Arity1<List<Double>, List<Double>> javadocFieldOpComputer = (in, out) -> {
		out.clear();
		for(Double d :in) {
			out.add(d + 1);
		}
	};

	@Test
	public void testJavadocFieldF() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.fieldF").iterator();

		if (!infos.hasNext()) {
			Assert.fail("No OpInfos with name \"test.javadoc.fieldF\"");
		}
		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.fieldF\"");
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
		Assert.assertEquals("output1", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the output", outputDescription);
	}
	
	@Test
	public void testJavadocFieldC() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.fieldC").iterator();

		if (!infos.hasNext()) {
			Assert.fail("No OpInfos with name \"test.javadoc.fieldC\"");
		}
		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.fieldC\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "inList", "outList" }, inputNames);

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "the input", "the preallocated output" }, inputDescriptions);

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("outList", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the preallocated output", outputDescription);
	}

	@Test
	public void testJavadocClass() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.dependentClass").iterator();

		if (!infos.hasNext()) {
			Assert.fail("No OpInfos with name \"test.javadoc.dependentClass\"");
		}
		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.dependentClass\"");
		}

		// assert input names
		String[] inputNames = info.inputs().stream().map(m -> m.getKey()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "t" }, inputNames);

		// assert input descriptions
		String[] inputDescriptions = info.inputs().stream().map(m -> m.getDescription()).toArray(
			String[]::new);
		Assert.assertArrayEquals(new String[] { "the input" }, inputDescriptions);

		// assert dependency description
		String[] dependencyDescriptions = info.dependencies().stream().map(m -> m
			.getDescription()).toArray(String[]::new);
		Assert.assertArrayEquals(new String[] {
			"Used to compute output for each element in the array" },
			dependencyDescriptions);

		// assert output name
		String outputName = info.output().getKey();
		Assert.assertEquals("output", outputName);

		// assert output description
		String outputDescription = info.output().getDescription();
		Assert.assertEquals("the output", outputDescription);
	}

	@Test
	public void opStringRegressionTest() {
		Iterator<OpInfo> infos = ops.infos("test.javadoc.methodPR").iterator();

		OpInfo info = infos.next();
		if (infos.hasNext()) {
			Assert.fail("Multiple OpInfos with name \"test.javadoc.method\"");
		}

		// test standard op string
		String expected =
			"org.scijava.ops.engine.JavadocParameterTest." +
				"OpMethodPR(java.util.List<java.lang.String>,java.util.List<java.lang.String>)(\n" +
				"	 Inputs:\n" +
				"		java.util.List<java.lang.String> foo -> the first input\n" +
				"		java.util.List<java.lang.String> bar -> the second input\n" +
				"	 Outputs:\n" +
				"		java.util.List<java.lang.Long> output -> foo + bar\n" + ")\n";
		String actual = info.toString();
		Assert.assertEquals(expected, actual);

		// test special op string
		expected =
			"org.scijava.ops.engine.JavadocParameterTest." +
				"OpMethodPR(java.util.List<java.lang.String>,java.util.List<java.lang.String>)(\n" +
				"	 Inputs:\n" +
				"		java.util.List<java.lang.String> foo -> the first input\n" +
				"==> 	java.util.List<java.lang.String> bar -> the second input\n" +
				"	 Outputs:\n" +
				"		java.util.List<java.lang.Long> output -> foo + bar\n" + ")\n";
		actual = OpUtils.opString(info, info.inputs().get(1));
		Assert.assertEquals(expected, actual);
	}

}

/**
 * Test Op used to see if we can't scrape the javadoc.
 * 
 * @author Gabriel Selzer
 */
@OpClass(names = "test.javadoc.class")
class JavadocOp implements Function<Double, Double>, Op {

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double apply(Double t) {
		return t + 1;
	}

}

/**
 * Test Op used to see if we can't scrape the javadoc.
 * 
 * @author Gabriel Selzer
 */
@OpClass(names = "test.javadoc.dependentClass")
class JavadocComplexOp implements Function<Double[], Double[]>, Op {

	/**
	 * Used to compute output for each element in the array
	 */
	@OpDependency(name = "test.javadoc.class")
	private Function<Double, Double> fooOp;

	/**
	 * @param t the input
	 * @return the output
	 */
	@Override
	public Double[] apply(Double[] t) {
		return Arrays.stream(t).map(fooOp).toArray(Double[]::new);
	}

}
