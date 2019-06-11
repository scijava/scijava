package org.scijava.ops.util;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.scijava.ops.OpService;
import org.scijava.ops.core.Op;
import org.scijava.ops.core.inplace.BiInplaceFirst;
import org.scijava.ops.core.inplace.BiInplaceSecond;
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
import org.scijava.ops.core.inplace.Inplace7Second;
import org.scijava.ops.types.Nil;
import org.scijava.ops.util.Inplaces.InplaceInfo;
import org.scijava.util.Types;

public class Inplaces {

	/**
	 * All known inplace types and their arities and mutable positions. The
	 * entries are sorted by arity and mutable position.
	 */
	public static final Map<Class<?>, InplaceInfo> ALL_INPLACES;

	static {
		final Map<Class<?>, InplaceInfo> inplaces = new LinkedHashMap<>(22);
		inplaces.put(Inplace.class, new InplaceInfo(1, 0));
		inplaces.put(BiInplaceFirst.class, new InplaceInfo(2, 0));
		inplaces.put(BiInplaceSecond.class, new InplaceInfo(2, 1));
		inplaces.put(Inplace3First.class, new InplaceInfo(3, 0));
		inplaces.put(Inplace3Second.class, new InplaceInfo(3, 1));
		inplaces.put(Inplace3Third.class, new InplaceInfo(3, 2));
		inplaces.put(Inplace4First.class, new InplaceInfo(4, 0));
		inplaces.put(Inplace4Second.class, new InplaceInfo(4, 1));
		inplaces.put(Inplace4Third.class, new InplaceInfo(4, 2));
		inplaces.put(Inplace4Fourth.class, new InplaceInfo(4, 3));
		inplaces.put(Inplace5First.class, new InplaceInfo(5, 0));
		inplaces.put(Inplace5Second.class, new InplaceInfo(5, 1));
		inplaces.put(Inplace5Third.class, new InplaceInfo(5, 2));
		inplaces.put(Inplace5Fourth.class, new InplaceInfo(5, 3));
		inplaces.put(Inplace5Fifth.class, new InplaceInfo(5, 4));
		inplaces.put(Inplace6First.class, new InplaceInfo(6, 0));
		inplaces.put(Inplace6Second.class, new InplaceInfo(6, 1));
		inplaces.put(Inplace6Third.class, new InplaceInfo(6, 2));
		inplaces.put(Inplace6Fourth.class, new InplaceInfo(6, 3));
		inplaces.put(Inplace6Fifth.class, new InplaceInfo(6, 4));
		inplaces.put(Inplace6Sixth.class, new InplaceInfo(6, 5));
		inplaces.put(Inplace7Second.class, new InplaceInfo(7, 1));
		ALL_INPLACES = Collections.unmodifiableMap(inplaces);
	}

	private Inplaces() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * @return {@code true} if the given type is a {@link #ALL_INPLACES known}
	 *         inplace type, {@code false} otherwise. <br>
	 *         Note that only the type itself and not its type hierarchy is
	 *         considered.
	 * @throws NullPointerException If {@code type} is {@code null}.
	 */
	public static boolean isInplace(Type type) {
		return ALL_INPLACES.containsKey(Types.raw(type));
	}

	public static List<Class<?>> getInplacesOfArity(final int arity) {
		return Inplaces.ALL_INPLACES.entrySet().stream() //
			.filter(e -> e.getValue().arity() == arity) //
			.map(Entry<Class<?>, InplaceInfo>::getKey) //
			.collect(Collectors.toList());
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
				inputOutputType, //
				secondaryArgs);
	}

	public static <IO, I2> BiInplaceFirst<IO, I2> binary1(final OpService ops, final String opName,
			final Nil<IO> inputOutputType, final Nil<I2> input2Type, final Object... secondaryArgs) {

		Nil<BiInplaceFirst<IO, I2>> inplaceNil = new Nil<BiInplaceFirst<IO, I2>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiInplaceFirst.class,
						new Type[] { inputOutputType.getType(), input2Type.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { inputOutputType, input2Type }, //
				inputOutputType, //
				secondaryArgs);
	}

	public static <I1, IO> BiInplaceSecond<I1, IO> binary2(final OpService ops, final String opName,
			final Nil<I1> input1Type, final Nil<IO> inputOutputType, final Object... secondaryArgs) {

		Nil<BiInplaceSecond<I1, IO>> inplaceNil = new Nil<BiInplaceSecond<I1, IO>>() {
			@Override
			public Type getType() {
				return Types.parameterize(BiInplaceSecond.class,
						new Type[] { input1Type.getType(), inputOutputType.getType() });
			}
		};

		return ops.findOp( //
				opName, //
				inplaceNil, //
				new Nil[] { input1Type, inputOutputType }, //
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
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
				inputOutputType, //
				secondaryArgs);
	}

	public static class InplaceInfo {

		private final int arity;
		private final int mutablePosition;

		public InplaceInfo(final int arity, final int mutablePosition) {
			this.arity = arity;
			this.mutablePosition = mutablePosition;
		}

		public int arity() {
			return arity;
		}

		public int mutablePosition() {
			return mutablePosition;
		}
	}
}
