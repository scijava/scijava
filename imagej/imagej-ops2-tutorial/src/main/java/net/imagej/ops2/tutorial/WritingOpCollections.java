package net.imagej.ops2.tutorial;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.ops.spi.OpMethod;

/**
 * {@link OpCollection}s, as the name suggests, define many Ops within one
 * class.
 * <p>
 * There are two different types of Ops that can be written inside {@link OpCollection}s:
 * <ol>
 *   <li>{@link OpField}s are Ops written as {@code public final} {@link Field}s.</li>
 *   <li>{@link OpMethod}s are Ops written as {@code public static} {@link Method}s.</li>
 * </ol>
 * Each {@link OpCollection} can contain an arbitrary number of either type of Op.
 */
public class WritingOpCollections implements OpCollection {

	/**
	 * {@link OpField}s are Ops written as {@link Field}s. They <b>must</b> be:
	 * <ul>
	 *   <li>public</li>
	 *   <li>final</li>
	 * </ul>
	 */
	@OpField(names="test.opField.power")
	public final BiFunction<Double, Double, Double> opFieldPower =
			(b, e) -> Math.pow(b, e);

	@OpMethod(names = "test.opMethod.power", type=BiFunction.class)
	public static Double opMethodPower(Double b, Double e) {
		return Math.pow(b, e);
	}

	public static void main(String... args){
		OpEnvironment ops = new DefaultOpEnvironment();

		Double result = ops.op("test.opField.power") //
				.input(2.0, 10.0) //
				.outType(Double.class) //
				.apply();

		System.out.println("2.0 to the power of 10.0 is " + result);

		result = ops.op("test.opMethod.power") //
				.input(2.0, 20.0) //
				.outType(Double.class) //
				.apply();

		System.out.println("2.0 to the power of 20.0 is " + result);
	}

}
