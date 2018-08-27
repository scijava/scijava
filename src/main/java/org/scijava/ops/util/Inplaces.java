package org.scijava.ops.util;

import java.lang.reflect.Type;

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
			final Class<IO> inputOutputType) {
		return ops.findOp( //
				new Nil<Inplace<IO>>() {
				}, //
				new Type[] { opClass }, //
				new Type[] { inputOutputType }, //
				inputOutputType);
	}

	public static <IO, I2> BiInplace1<IO, I2> binary1(final OpService ops, final Class<? extends Op> opClass,
			final Class<IO> inputOutputType, final Class<I2> input2Type) {
		return ops.findOp( //
				new Nil<BiInplace1<IO, I2>>() {
				}, //
				new Type[] { opClass }, //
				new Type[] { inputOutputType, input2Type}, //
				inputOutputType);
	}

}
