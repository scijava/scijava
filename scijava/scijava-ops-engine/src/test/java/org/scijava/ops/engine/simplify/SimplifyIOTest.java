package org.scijava.ops.engine.simplify;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.scijava.function.Computers;
import org.scijava.function.Inplaces;
import org.scijava.ops.engine.AbstractTestEnvironment;
import org.scijava.ops.engine.copy.CopyOpCollection;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

public class SimplifyIOTest extends AbstractTestEnvironment implements OpCollection{

	@BeforeAll
	public static void AddNeededOps() {
		ops.register(new SimplifyIOTest());
		ops.register(new PrimitiveSimplifiers());
		ops.register(new PrimitiveArraySimplifiers());
		ops.register(new CopyOpCollection());
	}

	@OpField(names = "test.math.square")
	public final Function<Double, Double> squareOp = in -> in * in;

	@Test
	public void testFunctionOutputSimplification() {
		Integer in = 4;
		Integer square = ops.op("test.math.square").arity1().input(in).outType(Integer.class).apply();
		
		assertEquals(square, 16, 0.);
	}
	
	@OpField(names = "test.math.square")
	public final Computers.Arity1<Double[], Double[]> squareArray = (in, out) -> {
		for(int i = 0; i < in.length && i < out.length; i++) {
			out[i] = squareOp.apply(in[i]);
		}
	};

	@OpField(names = "test.math.add")
	public final Inplaces.Arity2_1<Double[], Double[]> addArray1 = (io, in1) -> {
		for (int i = 0; i < io.length && i < in1.length; i++) {
			io[i] += in1[i];
		}
	};

	@OpField(names = "test.math.add")
	public final Inplaces.Arity2_2<Double[], Double[]> addArray2 = (in0, io) -> {
		for (int i = 0; i < io.length && i < in0.length; i++) {
			io[i] += in0[i];
		}
	};
	
	@Test
	public void basicComputerTest() {
		Integer[] in = new Integer[] {1, 2, 3};
		Integer[] out = new Integer[] {4, 5, 6}; 
		
		ops.op("test.math.square").arity1().input(in).output(out).compute();
		assertArrayEquals(out, new Integer[] {1, 4, 9});
	}

	@Test
	public void basicInplace2_1Test() {
		Integer[] io = new Integer[] {1, 2, 3};
		Integer[] in1 = new Integer[] {4, 5, 6}; 
		Integer[] expected = new Integer[] {5, 7, 9};
		
		ops.op("test.math.add").arity2().input(io, in1).mutate1();
		assertArrayEquals(io, expected);
	}

	@Test
	public void basicInplace2_2Test() {
		Integer[] in0 = new Integer[] {4, 5, 6}; 
		Integer[] io = new Integer[] {1, 2, 3};
		Integer[] expected = new Integer[] {5, 7, 9};
		
		ops.op("test.math.add").arity2().input(in0, io).mutate2();
		assertArrayEquals(io, expected);
	}

}

