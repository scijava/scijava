package org.scijava.ops.util;

import org.scijava.ops.BiInplace1;
import org.scijava.ops.Inplace;
import org.scijava.ops.Op;
import org.scijava.ops.base.OpService;
import org.scijava.types.Nil;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Inplaces {

	private Inplaces() {
		// NB: Prevent instantiation of utility class.
	}

	public static <IO> Inplace<IO> unary(final OpService ops, final Class<? extends Op> opClass,
			final Nil<IO> inputOutputType, final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<Inplace<IO>>() {
				}, //
				new Nil[] { inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2> BiInplace1<IO, I2> binary1(final OpService ops, final Class<? extends Op> opClass,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Object... secondaryArgs) {
		return ops.findOp( //
				opClass, //
				new Nil<BiInplace1<IO, I2>>() {
				}, //
				new Nil[] { inputOutputType, input2Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

}
