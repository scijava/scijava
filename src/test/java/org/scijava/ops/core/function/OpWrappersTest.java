package org.scijava.ops.core.function;

import java.util.function.Function;
import java.util.function.BiFunction;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.function.Computers;
import org.scijava.ops.function.Functions;
import org.scijava.ops.function.Inplaces;
import org.scijava.ops.function.Producer;
import org.scijava.ops.types.GenericTyped;
import org.scijava.ops.types.Nil;

/**
 * Tests whether the following Op types are wrapped by the Op matcher. We do
 * this by checking to make sure that they are a {@link GenericTyped}.
 * 
 * @author Gabriel
 *
 */
public class OpWrappersTest extends AbstractTestEnvironment{

	@Test
	public void testWrapProducer() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Producer<Double> op = Functions.match(ops, "test.addDoubles", nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction1() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Function<Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction2() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		BiFunction<Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction3() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity3<Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction4() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity4<Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction5() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity5<Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}


	@Test
	public void testWrapComputer1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity1<double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity2<double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity3<double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity4<double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity5<double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	

	@Test
	public void testWrapInplace1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity1<double[]> op = Inplaces.match(ops, "test.mulArrays", nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace2_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity2_1<double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace2_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity2_2<double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace3_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity3_1<double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace3_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity3_2<double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace3_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity3_3<double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace4_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity4_1<double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace4_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity4_2<double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace4_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity4_3<double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace4_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity4_4<double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace5_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity5_1<double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace5_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity5_2<double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace5_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity5_3<double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace5_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity5_4<double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace5_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity5_5<double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
}
