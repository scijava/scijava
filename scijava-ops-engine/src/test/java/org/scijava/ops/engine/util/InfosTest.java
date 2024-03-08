
package org.scijava.ops.engine.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.describe.PrimitiveDescriptors;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

import java.util.function.BiFunction;

/**
 * Test class for {@link Infos} static methods.
 *
 * @author Gabriel Selzer
 */
public class InfosTest extends AbstractTestEnvironment implements OpCollection {

	@BeforeAll
	public static void addNeededOps() {
		ops.register( //
			new InfosTest(), //
			new PrimitiveDescriptors<>() //
		);
	}

	@OpMethod(names = "test.nullableMethod", type = BiFunction.class)
	public static Integer nullableMethod(Integer i1, @Nullable Integer i2) {
		if (i2 == null) i2 = 0;
		return i1 + i2;
	}

	@Test
	public void testDescriptionOfNullableParameter() {
		var actual = ops.help("test.nullableMethod");
		var expected =
			"test.nullableMethod:\n\t- (number, number = null) -> number";
		Assertions.assertEquals(expected, actual);
	}
}
