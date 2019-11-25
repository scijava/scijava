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
	public void testWrapFunction6() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity6<Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction7() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity7<Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction8() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity8<Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction9() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity9<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction10() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity10<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction11() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity11<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction12() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity12<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction13() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity13<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction14() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity14<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction15() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity15<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}

	@Test
	public void testWrapFunction16() {
		Nil<Double> nilDouble = Nil.of(Double.class);
		Functions.Arity16<Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double, Double> op = Functions.match(ops, "test.addDoubles", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
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
	public void testWrapComputer6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity6<double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity7<double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer13() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer14() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer15() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapComputer16() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Computers.Arity16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Computers.match(ops, "test.addArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
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
	
	@Test
	public void testWrapInplace6_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_1<double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace6_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_2<double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace6_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_3<double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace6_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_4<double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace6_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_5<double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace6_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity6_6<double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_1<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_2<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_3<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_4<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_5<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_6<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace7_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity7_7<double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_1<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_2<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_3<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_4<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_5<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_6<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_7<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace8_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity8_8<double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_1<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_2<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_3<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_4<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_5<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_6<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_7<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_8<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace9_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity9_9<double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace10_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity10_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace11_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity11_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace12_12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity12_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match12(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match12(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace13_13() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity13_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match13(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match12(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_13() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match13(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace14_14() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity14_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match14(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match12(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_13() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match13(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_14() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match14(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace15_15() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity15_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match15(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_1() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_1<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match1(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_2() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_2<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match2(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_3() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_3<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match3(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_4() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_4<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match4(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_5() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_5<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match5(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_6() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_6<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match6(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_7() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_7<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match7(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_8() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_8<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match8(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_9() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_9<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match9(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_10() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_10<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match10(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_11() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_11<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match11(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_12() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_12<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match12(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_13() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_13<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match13(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_14() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_14<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match14(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_15() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_15<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match15(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
	@Test
	public void testWrapInplace16_16() {
		Nil<double[]> nilDouble = Nil.of(double[].class);
		Inplaces.Arity16_16<double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[], double[]> op = Inplaces.match16(ops, "test.mulArrays", nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble, nilDouble);
		assert(op instanceof GenericTyped);
	}
	
}
