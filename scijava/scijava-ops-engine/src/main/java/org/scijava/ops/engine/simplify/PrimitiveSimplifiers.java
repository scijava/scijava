
package org.scijava.ops.engine.simplify;

import java.util.function.Function;

import org.scijava.ops.api.OpHints;
import org.scijava.ops.api.features.BaseOpHints.Simplification;
import org.scijava.ops.spi.OpCollection;
import org.scijava.ops.spi.OpField;
import org.scijava.plugin.Plugin;

/**
 * A set of {@link Simplifier}s dealing with boxed primitive types.
 * 
 * @author Gabriel Selzer
 * @author Curtis Rueden
 */
@Plugin(type = OpCollection.class)
public class PrimitiveSimplifiers {

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
//	@Plugin(type = Simplifier.class)
//	public static class ByteSimplifier implements Simplifier<Number, Byte> {
//
//		@Override
//		public Number simplify(Byte p) {
//			return p;
//		}
//
//		@Override
//		public Byte focus(Number g) {
//			return g.byteValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Byte Simplifier";
//		}
//
//	}
//
//	@Plugin(type = Simplifier.class)
//	public static class ShortSimplifier implements Simplifier<Number, Short> {
//
//		@Override
//		public Number simplify(Short p) {
//			return p;
//		}
//
//		@Override
//		public Short focus(Number g) {
//			return g.shortValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Short Simplifier";
//		}
//	}
//
//	@Plugin(type = Simplifier.class)
//	public static class IntegerSimplifier implements Simplifier<Number, Integer> {
//
//		@Override
//		public Number simplify(Integer p) {
//			return p;
//		}
//
//		@Override
//		public Integer focus(Number g) {
//			return g.intValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Integer Simplifier";
//		}
//	}
//
//	@Plugin(type = Simplifier.class)
//	public static class LongSimplifier implements Simplifier<Number, Long> {
//
//		@Override
//		public Number simplify(Long p) {
//			return p;
//		}
//
//		@Override
//		public Long focus(Number g) {
//			return g.longValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Long Simplifier";
//		}
//	}
//
//	@Plugin(type = Simplifier.class)
//	public static class FloatSimplifier implements Simplifier<Number, Float> {
//
//		@Override
//		public Number simplify(Float p) {
//			return p;
//		}
//
//		@Override
//		public Float focus(Number g) {
//			return g.floatValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Float Simplifier";
//		}
//	}
//
//	@Plugin(type = Simplifier.class)
//	public static class DoubleSimplifier implements Simplifier<Number, Double> {
//
//		@Override
//		public Number simplify(Double p) {
//			return p;
//		}
//
//		@Override
//		public Double focus(Number g) {
//			return g.doubleValue();
//		}
//
//		@Override
//		public String toString() {
//			return "Double Simplifier";
//		}
//	}

}
