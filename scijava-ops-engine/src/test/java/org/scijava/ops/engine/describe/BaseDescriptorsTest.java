
package org.scijava.ops.engine.describe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Producer;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@code engine.describe} Ops in {@link BaseDescriptors}.
 *
 * @author Gabriel Selzer
 */
public class BaseDescriptorsTest extends AbstractTestEnvironment implements
	OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new BaseDescriptors<>());
		ops.register(new BaseDescriptorsTest());
	}

	@OpField(names = "test.byteDescriptor")
	public final Producer<Byte> byteProducer = //
		() -> (byte) 0;

	@OpField(names = "test.shortDescriptor")
	public final Producer<Short> shortProducer = //
		() -> (short) 0;

	@OpField(names = "test.intDescriptor")
	public final Producer<Integer> intProducer = //
		() -> 0;

	@OpField(names = "test.longDescriptor")
	public final Producer<Long> longProducer = //
		() -> 0L;

	@OpField(names = "test.floatDescriptor")
	public final Producer<Float> floatProducer = //
		() -> 0.0f;

	@OpField(names = "test.doubleDescriptor")
	public final Producer<Double> doubleProducer = //
		() -> 0.0;

	@Test
	public void testBoxedPrimitiveDescriptor() {
		String[] tests = { "byte", "short", "int", "long", "float", "double" };
		for (String t : tests) {
			var expected = "test." + t + "Descriptor:\n\t- () -> number";
			var actual = ops.help("test." + t + "Descriptor");
			Assertions.assertEquals(expected, actual);
		}
	}

	@OpField(names = "test.listDescriptor")
	public final Producer<List<Double>> listDoubleProducer = ArrayList::new;

	@Test
	public void testListDescriptor() {
		var expected = "test.listDescriptor:\n\t- () -> list<number>";
		var actual = ops.help("test.listDescriptor");
		Assertions.assertEquals(expected, actual);
	}

	@OpField(names = "test.arrayDescriptor")
	public final Producer<double[]> doubleArrayProducer = //
		() -> new double[0];

	@Test
	public void testArrayDescriptor() {
		var expected = "test.arrayDescriptor:\n\t- () -> number[]";
		var actual = ops.help("test.arrayDescriptor");
		Assertions.assertEquals(expected, actual);
	}

	@OpField(names = "test.arrayArrayDescriptor")
	public final Producer<double[][]> doubleArrayArrayProducer = //
		() -> new double[0][0];

	@Test
	public void testArrayArrayDescriptor() {
		var expected = "test.arrayArrayDescriptor:\n\t- () -> number[][]";
		var actual = ops.help("test.arrayArrayDescriptor");
		Assertions.assertEquals(expected, actual);
	}

	@OpField(names = "test.listArrayDescriptor")
	public final Producer<List<Double[]>> listDoubleArrayProducer = //
		ArrayList::new;

	@Test
	public void testListArrayDescriptor() {
		var expected = "test.listArrayDescriptor:\n\t- () -> list<number[]>";
		var actual = ops.help("test.listArrayDescriptor");
		Assertions.assertEquals(expected, actual);
	}

	public static class Foo {

	}

	@OpField(names = "test.fooDescriptor")
	public final Producer<Foo> fooProducer = Foo::new;

	/**
	 * Test behavior when no {@code engine.describe} Op exists for a type.
	 */
	@Test
	public void testFallbackDescriptions() {
		var expected =
			"test.fooDescriptor:\n\t- () -> Foo";
		var actual = ops.help("test.fooDescriptor");
		Assertions.assertEquals(expected, actual);
	}

}
