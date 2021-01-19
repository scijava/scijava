
package org.scijava.ops.simplify;

import java.util.function.Function;

import org.scijava.ops.OpField;
import org.scijava.ops.core.OpCollection;
import org.scijava.plugin.Plugin;
import org.scijava.util.ObjectArray;

/**
 * A collection of Ops for simplifying and focusing primitive arrays
 *
 * @author Gabriel Selzer
 */
@Plugin(type = OpCollection.class)
public class PrimitiveArraySimplifiers {

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Byte[], ObjectArray<Number>> byteArrSimplifier =
		b -> new ObjectArray<>(b);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Byte[]> byteArrFocuser = o -> o
		.stream().map(b -> b == null ? null : b.byteValue()).toArray(Byte[]::new);

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Integer[], ObjectArray<Number>> intArrSimplifier =
		i -> new ObjectArray<>(i);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Integer[]> intArrFocuser = o -> o
		.stream().map(i -> i == null ? null : i.intValue()).toArray(Integer[]::new);

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Short[], ObjectArray<Number>> shortArrSimplifier =
		s -> new ObjectArray<>(s);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Short[]> shortArrFocuser = o -> o
		.stream().map(s -> s == null ? null : s.shortValue()).toArray(Short[]::new);

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Long[], ObjectArray<Number>> longArrSimplifier =
		l -> new ObjectArray<>(l);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Long[]> longArrFocuser = o -> o
		.stream().map(l -> l == null ? null : l.longValue()).toArray(Long[]::new);

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Float[], ObjectArray<Number>> floatArrSimplifier =
		f -> new ObjectArray<>(f);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Float[]> floatArrFocuser = o -> o
		.stream().map(f -> f == null ? null : f.floatValue()).toArray(Float[]::new);

	@Unsimplifiable
	@OpField(names = "simplify")
	public final Function<Double[], ObjectArray<Number>> doubleArrSimplifier =
		d -> new ObjectArray<>(d);

	@Unsimplifiable
	@OpField(names = "focus")
	public final Function<ObjectArray<Number>, Double[]> doubleArrFocuser = o -> o
		.stream().map(d -> d == null ? null : d.doubleValue()).toArray(Double[]::new);
}
