package org.scijava.ops.util;

import java.lang.reflect.Type;

import org.scijava.ops.OpService;
import org.scijava.ops.core.BiInplace1;
import org.scijava.ops.core.Inplace;
import org.scijava.ops.core.Op;
import org.scijava.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Inplaces {

	private Inplaces() {
		// NB: Prevent instantiation of utility class.
	}

	public static <IO> Inplace<IO> unary(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Object... secondaryArgs) {

		Nil<Inplace<IO>> inplaceNil = new Nil<Inplace<IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace.class, new Type[] { inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2> BiInplace1<IO, I2> binary1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Object... secondaryArgs) {

		Nil<BiInplace1<IO, I2>> inplaceNil = new Nil<BiInplace1<IO, I2>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiInplace1.class,
						new Type[] { inputOutputType.getType(), input2Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

}
