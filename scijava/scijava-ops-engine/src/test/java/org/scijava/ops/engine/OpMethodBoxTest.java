package org.scijava.ops.engine;

import java.util.function.Function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpMethod;

public class OpMethodBoxTest extends AbstractTestEnvironment implements OpCollection {
	@BeforeAll
	public static void addNeededOps() {
		ops.register(new OpMethodBoxTest());
	}

	@OpMethod(names = "test.box", type = Function.class)
	public static int increment(final int i1) {
		return i1 + 1;
	}

	@Test
	public void testOpMethodBoxing(){
		Integer result = ops.op("test.box").arity1().input(1) //
			.outType(Integer.class).apply();
		Assertions.assertEquals(2, result);
	}


}
