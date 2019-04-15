package org.scijava.ops.util;

import java.lang.reflect.Type;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.inplace.BiInplace1;
import org.scijava.ops.core.inplace.BiInplace2;
import org.scijava.ops.core.inplace.Inplace;
import org.scijava.ops.core.inplace.Inplace3First;
import org.scijava.ops.core.inplace.Inplace3Second;
import org.scijava.ops.core.inplace.Inplace3Third;
import org.scijava.ops.core.inplace.Inplace4First;
import org.scijava.ops.core.inplace.Inplace4Fourth;
import org.scijava.ops.core.inplace.Inplace4Second;
import org.scijava.ops.core.inplace.Inplace4Third;
import org.scijava.ops.core.inplace.Inplace5Fifth;
import org.scijava.ops.core.inplace.Inplace5First;
import org.scijava.ops.core.inplace.Inplace5Fourth;
import org.scijava.ops.core.inplace.Inplace5Second;
import org.scijava.ops.core.inplace.Inplace5Third;
import org.scijava.ops.core.inplace.Inplace6Fifth;
import org.scijava.ops.core.inplace.Inplace6First;
import org.scijava.ops.core.inplace.Inplace6Fourth;
import org.scijava.ops.core.inplace.Inplace6Second;
import org.scijava.ops.core.inplace.Inplace6Sixth;
import org.scijava.ops.core.inplace.Inplace6Third;
import org.scijava.ops.types.Nil;
import org.scijava.util.Types;

/**
 * Utility providing adaptation between {@link Op} types.
 */
public class Inplaces {

	private Inplaces() {
		// NB: Prevent instantiation of utility class.
	}

	public static <IO> Inplace<IO> unary(final OpService ops, final String opName, final Nil<IO> inputOutputType,
			final Object... secondaryArgs) {

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

	public static <I1, IO> BiInplace2<I1, IO> binary2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Object... secondaryArgs) {

		Nil<BiInplace2<I1, IO>> inplaceNil = new Nil<BiInplace2<I1, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiInplace2.class,
						new Type[] { input1Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input1Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2, I3> Inplace3First<IO, I2, I3> ternary1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Object... secondaryArgs) {

		Nil<Inplace3First<IO, I2, I3>> inplaceNil = new Nil<Inplace3First<IO, I2, I3>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace3First.class,
						new Type[] { inputOutputType.getType(), input2Type.getType(), input3Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type, input3Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, IO, I3> Inplace3Second<I1, IO, I3> ternary2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Nil<I3> input3Type,
			final Object... secondaryArgs) {

		Nil<Inplace3Second<I1, IO, I3>> inplaceNil = new Nil<Inplace3Second<I1, IO, I3>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace3Second.class,
						new Type[] { input1Type.getType(), inputOutputType.getType(), input3Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType, input3Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, IO> Inplace3Third<I1, I2, IO> ternary3(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<IO> inputOutputType,
			final Object... secondaryArgs) {

		Nil<Inplace3Third<I1, I2, IO>> inplaceNil = new Nil<Inplace3Third<I1, I2, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace3Third.class,
						new Type[] { input1Type.getType(), input2Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2, I3, I4> Inplace4First<IO, I2, I3, I4> quaternary1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<I4> input4Type,
			final Object... secondaryArgs) {

		Nil<Inplace4First<IO, I2, I3, I4>> inplaceNil = new Nil<Inplace4First<IO, I2, I3, I4>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace4First.class, new Type[] { inputOutputType.getType(),
						input2Type.getType(), input3Type.getType(), input4Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type, input3Type, input4Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, IO, I3, I4> Inplace4Second<I1, IO, I3, I4> quaternary2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Nil<I3> input3Type, final Nil<I4> input4Type,
			final Object... secondaryArgs) {

		Nil<Inplace4Second<I1, IO, I3, I4>> inplaceNil = new Nil<Inplace4Second<I1, IO, I3, I4>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace4Second.class, new Type[] { input1Type.getType(),
						inputOutputType.getType(), input3Type.getType(), input4Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType, input3Type, input4Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, IO, I4> Inplace4Third<I1, I2, IO, I4> quaternary3(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<IO> inputOutputType, final Nil<I4> input4Type,
			final Object... secondaryArgs) {

		Nil<Inplace4Third<I1, I2, IO, I4>> inplaceNil = new Nil<Inplace4Third<I1, I2, IO, I4>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace4Third.class, new Type[] { input1Type.getType(), input2Type.getType(),
						inputOutputType.getType(), input4Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, inputOutputType, input4Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, IO> Inplace4Fourth<I1, I2, I3, IO> quaternary4(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type, final Nil<IO> inputOutputType,
			final Object... secondaryArgs) {

		Nil<Inplace4Fourth<I1, I2, I3, IO>> inplaceNil = new Nil<Inplace4Fourth<I1, I2, I3, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace4Fourth.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2, I3, I4, I5> Inplace5First<IO, I2, I3, I4, I5> quinary1(final OpService ops,
			final String opName, final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Object... secondaryArgs) {

		Nil<Inplace5First<IO, I2, I3, I4, I5>> inplaceNil = new Nil<Inplace5First<IO, I2, I3, I4, I5>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace5First.class, new Type[] { inputOutputType.getType(),
						input2Type.getType(), input3Type.getType(), input4Type.getType(), input5Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type, input3Type, input4Type, input5Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, IO, I3, I4, I5> Inplace5Second<I1, IO, I3, I4, I5> quinary2(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Object... secondaryArgs) {

		Nil<Inplace5Second<I1, IO, I3, I4, I5>> inplaceNil = new Nil<Inplace5Second<I1, IO, I3, I4, I5>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace5Second.class, new Type[] { input1Type.getType(),
						inputOutputType.getType(), input3Type.getType(), input4Type.getType(), input5Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType, input3Type, input4Type, input5Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, IO, I4, I5> Inplace5Third<I1, I2, IO, I4, I5> quinary3(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<IO> inputOutputType,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Object... secondaryArgs) {

		Nil<Inplace5Third<I1, I2, IO, I4, I5>> inplaceNil = new Nil<Inplace5Third<I1, I2, IO, I4, I5>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace5Third.class, new Type[] { input1Type.getType(), input2Type.getType(),
						inputOutputType.getType(), input4Type.getType(), input5Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, inputOutputType, input4Type, input5Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, IO, I5> Inplace5Fourth<I1, I2, I3, IO, I5> quinary4(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<IO> inputOutputType, final Nil<I5> input5Type, final Object... secondaryArgs) {

		Nil<Inplace5Fourth<I1, I2, I3, IO, I5>> inplaceNil = new Nil<Inplace5Fourth<I1, I2, I3, IO, I5>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace5Fourth.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), inputOutputType.getType(), input5Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, inputOutputType, input5Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <I1, I2, I3, I4, IO> Inplace5Fifth<I1, I2, I3, I4, IO> quinary5(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<IO> inputOutputType, final Object... secondaryArgs) {

		Nil<Inplace5Fifth<I1, I2, I3, I4, IO>> inplaceNil = new Nil<Inplace5Fifth<I1, I2, I3, I4, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace5Fifth.class, new Type[] { input1Type.getType(), input2Type.getType(),
						input3Type.getType(), input4Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}

	public static <IO, I2, I3, I4, I5, I6> Inplace6First<IO, I2, I3, I4, I5, I6> senary1(final OpService ops,
			final String opName, final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<I6> input6Type,
			final Object... secondaryArgs) {

		Nil<Inplace6First<IO, I2, I3, I4, I5, I6>> inplaceNil = new Nil<Inplace6First<IO, I2, I3, I4, I5, I6>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6First.class, new Type[] { inputOutputType.getType(),
						input2Type.getType(), input3Type.getType(), input4Type.getType(), input5Type.getType(), input6Type.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type, input3Type, input4Type, input5Type, input6Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
	
	public static <I1, IO, I3, I4, I5, I6> Inplace6Second<I1, IO, I3, I4, I5, I6> senary2(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<I6> input6Type,
			final Object... secondaryArgs) {

		Nil<Inplace6Second<I1, IO, I3, I4, I5, I6>> inplaceNil = new Nil<Inplace6Second<I1, IO, I3, I4, I5, I6>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6Second.class, new Type[] { input1Type.getType(),
						inputOutputType.getType(), input3Type.getType(), input4Type.getType(), input5Type.getType(), input6Type.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType, input3Type, input4Type, input5Type, input6Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
	
	public static <I1, I2, IO, I4, I5, I6> Inplace6Third<I1, I2, IO, I4, I5, I6> senary3(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<IO> inputOutputType,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<I6> input6Type,
			final Object... secondaryArgs) {

		Nil<Inplace6Third<I1, I2, IO, I4, I5, I6>> inplaceNil = new Nil<Inplace6Third<I1, I2, IO, I4, I5, I6>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6Third.class, new Type[] { input1Type.getType(),
						input2Type.getType(), inputOutputType.getType(), input4Type.getType(), input5Type.getType(), input6Type.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, inputOutputType, input4Type, input5Type, input6Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
	
	public static <I1, I2, I3, IO, I5, I6> Inplace6Fourth<I1, I2, I3, IO, I5, I6> senary4(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<IO> inputOutputType, final Nil<I5> input5Type, final Nil<I6> input6Type,
			final Object... secondaryArgs) {

		Nil<Inplace6Fourth<I1, I2, I3, IO, I5, I6>> inplaceNil = new Nil<Inplace6Fourth<I1, I2, I3, IO, I5, I6>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6Fourth.class, new Type[] { input1Type.getType(),
						input2Type.getType(), input3Type.getType(), inputOutputType.getType(), input5Type.getType(), input6Type.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, inputOutputType, input5Type, input6Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
	
	public static <I1, I2, I3, I4, IO, I6> Inplace6Fifth<I1, I2, I3, I4, IO, I6> senary5(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<IO> inputOutputType, final Nil<I6> input6Type,
			final Object... secondaryArgs) {

		Nil<Inplace6Fifth<I1, I2, I3, I4, IO, I6>> inplaceNil = new Nil<Inplace6Fifth<I1, I2, I3, I4, IO, I6>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6Fifth.class, new Type[] { input1Type.getType(),
						input2Type.getType(), input3Type.getType(), input4Type.getType(), inputOutputType.getType(), input6Type.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, inputOutputType, input6Type }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
	
	public static <I1, I2, I3, I4, I5, IO> Inplace6Sixth<I1, I2, I3, I4, I5, IO> senary6(final OpService ops,
			final String opName, final Nil<I1> input1Type, final Nil<I2> input2Type, final Nil<I3> input3Type,
			final Nil<I4> input4Type, final Nil<I5> input5Type, final Nil<IO> inputOutputType,
			final Object... secondaryArgs) {

		Nil<Inplace6Sixth<I1, I2, I3, I4, I5, IO>> inplaceNil = new Nil<Inplace6Sixth<I1, I2, I3, I4, I5, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(Inplace6Sixth.class, new Type[] { input1Type.getType(),
						input2Type.getType(), input3Type.getType(), input4Type.getType(), input5Type.getType(), inputOutputType.getType()});
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, input2Type, input3Type, input4Type, input5Type, inputOutputType }, //
				new Nil[] { inputOutputType }, //
				secondaryArgs);
	}
}
