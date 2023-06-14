package org.scijava.ops.engine.matcher;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RawTypeMatchingTest  extends AbstractTestEnvironment implements
		OpCollection
{

	@BeforeAll
	public static void addNeededOps() {
		ops.register(new RawTypeMatchingTest());
	}

	@OpField(names = "test.match.raw")
	public final  Function<Number, List<Number>> func = (x) -> List.of(x);

	@Test
	public void rawTypeAdaptationTest() {
		Double d = 25.;
		var list = ops.op("test.match.raw").arity1().input(d).outType(List.class).apply();
		assertNotNull(list);
	}
}
