package org.scijava.ops.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.function.Functions;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.ops.core.builder.OpBuilder;
import org.scijava.types.Nil;
import org.scijava.plugin.Plugin;

@Plugin(type = OpCollection.class)
public class OpsAsParametersTest extends AbstractTestEnvironment {

	@OpField(names = "test.parameter.computer")
	public final Function<Number, Double> func = (x) -> x.doubleValue();

	@OpField(names = "test.parameter.op")
	public final BiFunction<List<Number>, Function<Number, Double>, List<Double>> biFunc = (x, op) -> {
		List<Double> output = new ArrayList<>();
		for (Number n : x)
			output.add(op.apply(n));
		return output;
	};

	// TODO: find a better way to check that this call fails BECAUSE func cannot be
	// reified
	@Test(expected=IllegalArgumentException.class)
	public void TestOpWithNonReifiableFunction() {

		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		List<Double> output = ops.op("test.parameter.op").input(list, func).outType(new Nil<List<Double>>() {}).apply();
	}

	@Test
	public void TestOpWithOpFieldWithoutRun() {

		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		BiFunction<List<Number>, Function<Number, Double>, List<Double>> thing = FunctionUtils.match(ops.env(),
				"test.parameter.op", new Nil<List<Number>>() {
				}, new Nil<Function<Number, Double>>() {
				}, new Nil<List<Double>>() {
				});
		

		List<Double> output = thing.apply(list, func);
	}

	@Test
	public void TestOpWithOpClass() {

		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		Function<Number, Double> funcClass = FunctionUtils.match(ops.env(), "test.parameter.class", new Nil<Number>() {
		}, new Nil<Double>() {
		});

		@SuppressWarnings("unused")
		List<Double> output = ops.op("test.parameter.op").input(list, funcClass).outType(new Nil<List<Double>>() {}).apply();
	}

}

@Plugin(type = Op.class, name = "test.parameter.class")
class FuncClass implements Function<Number, Double> {

	@Override
	public Double apply(Number t) {
		return t.doubleValue() + 1;
	}

}
