package org.scijava.ops.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;
import org.scijava.ops.AbstractTestEnvironment;
import org.scijava.ops.OpField;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.OpCollection;
import org.scijava.param.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.struct.ItemIO;
import org.scijava.types.Nil;

@Plugin(type = OpCollection.class)
public class OpsAsParametersTest extends AbstractTestEnvironment {

	@OpField(names = "test.parameter.computer")
	@Parameter(key = "input")
	@Parameter(key = "output", type = ItemIO.OUTPUT)
	public final Function<Number, Double> func = (x) -> x.doubleValue();

	@OpField(names = "test.parameter.op")
	@Parameter(key = "inputList")
	@Parameter(key = "op")
	@Parameter(key = "outputList", type = ItemIO.OUTPUT)
	public final BiFunction<List<Number>, Function<Number, Double>, List<Double>> biFunc = (x, op) -> {
		List<Double> output = new ArrayList<>();
		for (Number n : x)
			output.add(op.apply(n));
		return output;
	};

	@Test
	public void TestOpWithOpField() {

		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		List<Double> output = (List<Double>) ops().run("test.parameter.op", list, func);
	}

	@Test
	public void TestOpWithOpFieldWithoutRun() {

		List<Number> list = new ArrayList<>();
		list.add(40l);
		list.add(20.5);
		list.add(4.0d);

		BiFunction<List<Number>, Function<Number, Double>, List<Double>> thing = Functions.binary(ops(),
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

		Function<Number, Double> funcClass = Functions.unary(ops(), "test.parameter.class", new Nil<Number>() {
		}, new Nil<Double>() {
		});

		List<Double> output = (List<Double>) ops().run("test.parameter.op", list, funcClass);
	}

}

@Plugin(type = Op.class, name = "test.parameter.class")
@Parameter(key = "input")
@Parameter(key = "output", type = ItemIO.OUTPUT)
class FuncClass implements Function<Number, Double> {

	@Override
	public Double apply(Number t) {
		return t.doubleValue() + 1;
	}

}
