
package org.scijava.ops.engine.simplify;

import java.util.function.Function;

import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;

/**
 * A set of {@link Simplifier}s dealing with boxed primitive types.
 * 
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
public class PrimitiveSimplifiers implements OpCollection {

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Byte, Number> byteSimplifier = b -> b;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Byte> numberByteFocuser = n -> n.byteValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Integer, Number> integerSimplifier = i -> i;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Integer> numberIntegerFocuser = n -> n.intValue();
	
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Short, Number> shortSimplifier = s -> s;
	
	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Short> numberShortFocuser = n -> n.shortValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Long, Number> longSimplifier = l -> l;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Long> numberLongFocuser = n -> n.longValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Float, Number> floatSimplifier = f -> f;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Float> numberFloatFocuser = n -> n.floatValue();

	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "simplify")
	public final Function<Double, Number> doubleSimplifier = d -> d;

	// TODO: move to separate class
	@OpHints(hints = {Simplification.FORBIDDEN})
	@OpField(names = "focus")
	public final Function<Number, Double> numberDoubleFocuser = n -> n.doubleValue();

}
