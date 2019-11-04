package org.scijava.ops.function;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.types.GenericTyped;
import org.scijava.ops.util.OpWrapper;
import org.scijava.param.Mutable;
import org.scijava.plugin.Plugin;

public class OpWrappers {

	// -- producer --

	@Plugin(type = OpWrapper.class)
	public static class ProducerOpWrapper<T> implements OpWrapper<Producer<T>> {

		@Override
		public Producer<T> wrap(final Producer<T> op, final OpInfo opInfo) {
			class GenericTypedProducer implements Producer<T>, GenericTyped {
				@Override public T create() { return op.create(); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedProducer();
		}
	}

	// -- functions --

	@Plugin(type = OpWrapper.class)
	public static class Function1OpWrapper<I, O> implements OpWrapper<Function<I, O>> {

		@Override
		public Function<I, O> wrap(final Function<I, O> op, final OpInfo opInfo) {
			class GenericTypedFunction1 implements Function<I, O>, GenericTyped {
				@Override public O apply(I in) { return op.apply(in); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function2OpWrapper<I1, I2, O> implements OpWrapper<BiFunction<I1, I2, O>> {

		@Override
		public BiFunction<I1, I2, O> wrap(final BiFunction<I1, I2, O> op, final OpInfo opInfo) {
			class GenericTypedFunction2 implements BiFunction<I1, I2, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2) { return op.apply(in1, in2); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function3OpWrapper<I1, I2, I3, O> implements OpWrapper<Functions.Arity3<I1, I2, I3, O>> {

		@Override
		public Functions.Arity3<I1, I2, I3, O> wrap(final Functions.Arity3<I1, I2, I3, O> op, final OpInfo opInfo) {
			class GenericTypedFunction3 implements Functions.Arity3<I1, I2, I3, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3) { return op.apply(in1, in2, in3); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function4OpWrapper<I1, I2, I3, I4, O> implements OpWrapper<Functions.Arity4<I1, I2, I3, I4, O>> {

		@Override
		public Functions.Arity4<I1, I2, I3, I4, O> wrap(final Functions.Arity4<I1, I2, I3, I4, O> op, final OpInfo opInfo) {
			class GenericTypedFunction4 implements Functions.Arity4<I1, I2, I3, I4, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4) { return op.apply(in1, in2, in3, in4); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function5OpWrapper<I1, I2, I3, I4, I5, O> implements OpWrapper<Functions.Arity5<I1, I2, I3, I4, I5, O>> {

		@Override
		public Functions.Arity5<I1, I2, I3, I4, I5, O> wrap(final Functions.Arity5<I1, I2, I3, I4, I5, O> op, final OpInfo opInfo) {
			class GenericTypedFunction5 implements Functions.Arity5<I1, I2, I3, I4, I5, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5) { return op.apply(in1, in2, in3, in4, in5); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction5();
		}
	}

	// -- computers --
	
	@Plugin(type = OpWrapper.class)
	public static class Computer0OpWrapper<O> implements OpWrapper<Computers.Arity0<O>> {

		@Override
		public Computers.Arity0<O> wrap(final Computers.Arity0<O> op, final OpInfo opInfo) {
			class GenericTypedComputer0 implements Computers.Arity0<O>, GenericTyped {
				@Override public void compute(@Mutable O out) { op.compute(out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer0();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer1OpWrapper<I, O> implements OpWrapper<Computers.Arity1<I, O>> {

		@Override
		public Computers.Arity1<I, O> wrap(final Computers.Arity1<I, O> op, final OpInfo opInfo) {
			class GenericTypedComputer1 implements Computers.Arity1<I, O>, GenericTyped {
				@Override public void compute(I in, @Mutable O out) { op.compute(in, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer2OpWrapper<I1, I2, O> implements OpWrapper<Computers.Arity2<I1, I2, O>> {

		@Override
		public Computers.Arity2<I1, I2, O> wrap(final Computers.Arity2<I1, I2, O> op, final OpInfo opInfo) {
			class GenericTypedComputer2 implements Computers.Arity2<I1, I2, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, @Mutable O out) { op.compute(in1, in2, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer3OpWrapper<I1, I2, I3, O> implements OpWrapper<Computers.Arity3<I1, I2, I3, O>> {

		@Override
		public Computers.Arity3<I1, I2, I3, O> wrap(final Computers.Arity3<I1, I2, I3, O> op, final OpInfo opInfo) {
			class GenericTypedComputer3 implements Computers.Arity3<I1, I2, I3, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, @Mutable O out) { op.compute(in1, in2, in3, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer4OpWrapper<I1, I2, I3, I4, O> implements OpWrapper<Computers.Arity4<I1, I2, I3, I4, O>> {

		@Override
		public Computers.Arity4<I1, I2, I3, I4, O> wrap(final Computers.Arity4<I1, I2, I3, I4, O> op, final OpInfo opInfo) {
			class GenericTypedComputer4 implements Computers.Arity4<I1, I2, I3, I4, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, @Mutable O out) { op.compute(in1, in2, in3, in4, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer5OpWrapper<I1, I2, I3, I4, I5, O> implements OpWrapper<Computers.Arity5<I1, I2, I3, I4, I5, O>> {

		@Override
		public Computers.Arity5<I1, I2, I3, I4, I5, O> wrap(final Computers.Arity5<I1, I2, I3, I4, I5, O> op, final OpInfo opInfo) {
			class GenericTypedComputer5 implements Computers.Arity5<I1, I2, I3, I4, I5, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer5();
		}
	}

	// -- inplaces --
	
	@Plugin(type = OpWrapper.class)
	public static class Inplace1OpWrapper<IO> implements OpWrapper<Inplaces.Arity1<IO>> {

		@Override
		public Inplaces.Arity1<IO> wrap(final Inplaces.Arity1<IO> op, final OpInfo opInfo) {
			class GenericTypedInplace1 implements Inplaces.Arity1<IO>, GenericTyped {
				@Override public void mutate(IO ioType) { op.mutate(ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace2_1OpWrapper<IO, I2> implements OpWrapper<Inplaces.Arity2_1<IO, I2>> {

		@Override
		public Inplaces.Arity2_1<IO, I2> wrap(final Inplaces.Arity2_1<IO, I2> op, final OpInfo opInfo) {
			class GenericTypedInplace2_1 implements Inplaces.Arity2_1<IO, I2>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type) { op.mutate(ioType, in2Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace2_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace2_2OpWrapper<I1, IO> implements OpWrapper<Inplaces.Arity2_2<I1, IO>> {

		@Override
		public Inplaces.Arity2_2<I1, IO> wrap(final Inplaces.Arity2_2<I1, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace2_2 implements Inplaces.Arity2_2<I1, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType) { op.mutate(in1Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace2_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace3_1OpWrapper<IO, I2, I3> implements OpWrapper<Inplaces.Arity3_1<IO, I2, I3>> {

		@Override
		public Inplaces.Arity3_1<IO, I2, I3> wrap(final Inplaces.Arity3_1<IO, I2, I3> op, final OpInfo opInfo) {
			class GenericTypedInplace3_1 implements Inplaces.Arity3_1<IO, I2, I3>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type) { op.mutate(ioType, in2Type, in3Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace3_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace3_2OpWrapper<I1, IO, I3> implements OpWrapper<Inplaces.Arity3_2<I1, IO, I3>> {

		@Override
		public Inplaces.Arity3_2<I1, IO, I3> wrap(final Inplaces.Arity3_2<I1, IO, I3> op, final OpInfo opInfo) {
			class GenericTypedInplace3_2 implements Inplaces.Arity3_2<I1, IO, I3>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type) { op.mutate(in1Type, ioType, in3Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace3_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace3_3OpWrapper<I1, I2, IO> implements OpWrapper<Inplaces.Arity3_3<I1, I2, IO>> {

		@Override
		public Inplaces.Arity3_3<I1, I2, IO> wrap(final Inplaces.Arity3_3<I1, I2, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace3_3 implements Inplaces.Arity3_3<I1, I2, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType) { op.mutate(in1Type, in2Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace3_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace4_1OpWrapper<IO, I2, I3, I4> implements OpWrapper<Inplaces.Arity4_1<IO, I2, I3, I4>> {

		@Override
		public Inplaces.Arity4_1<IO, I2, I3, I4> wrap(final Inplaces.Arity4_1<IO, I2, I3, I4> op, final OpInfo opInfo) {
			class GenericTypedInplace4_1 implements Inplaces.Arity4_1<IO, I2, I3, I4>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type) { op.mutate(ioType, in2Type, in3Type, in4Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace4_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace4_2OpWrapper<I1, IO, I3, I4> implements OpWrapper<Inplaces.Arity4_2<I1, IO, I3, I4>> {

		@Override
		public Inplaces.Arity4_2<I1, IO, I3, I4> wrap(final Inplaces.Arity4_2<I1, IO, I3, I4> op, final OpInfo opInfo) {
			class GenericTypedInplace4_2 implements Inplaces.Arity4_2<I1, IO, I3, I4>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type) { op.mutate(in1Type, ioType, in3Type, in4Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace4_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace4_3OpWrapper<I1, I2, IO, I4> implements OpWrapper<Inplaces.Arity4_3<I1, I2, IO, I4>> {

		@Override
		public Inplaces.Arity4_3<I1, I2, IO, I4> wrap(final Inplaces.Arity4_3<I1, I2, IO, I4> op, final OpInfo opInfo) {
			class GenericTypedInplace4_3 implements Inplaces.Arity4_3<I1, I2, IO, I4>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type) { op.mutate(in1Type, in2Type, ioType, in4Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace4_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace4_4OpWrapper<I1, I2, I3, IO> implements OpWrapper<Inplaces.Arity4_4<I1, I2, I3, IO>> {

		@Override
		public Inplaces.Arity4_4<I1, I2, I3, IO> wrap(final Inplaces.Arity4_4<I1, I2, I3, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace4_4 implements Inplaces.Arity4_4<I1, I2, I3, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace4_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace5_1OpWrapper<IO, I2, I3, I4, I5> implements OpWrapper<Inplaces.Arity5_1<IO, I2, I3, I4, I5>> {

		@Override
		public Inplaces.Arity5_1<IO, I2, I3, I4, I5> wrap(final Inplaces.Arity5_1<IO, I2, I3, I4, I5> op, final OpInfo opInfo) {
			class GenericTypedInplace5_1 implements Inplaces.Arity5_1<IO, I2, I3, I4, I5>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace5_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace5_2OpWrapper<I1, IO, I3, I4, I5> implements OpWrapper<Inplaces.Arity5_2<I1, IO, I3, I4, I5>> {

		@Override
		public Inplaces.Arity5_2<I1, IO, I3, I4, I5> wrap(final Inplaces.Arity5_2<I1, IO, I3, I4, I5> op, final OpInfo opInfo) {
			class GenericTypedInplace5_2 implements Inplaces.Arity5_2<I1, IO, I3, I4, I5>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace5_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace5_3OpWrapper<I1, I2, IO, I4, I5> implements OpWrapper<Inplaces.Arity5_3<I1, I2, IO, I4, I5>> {

		@Override
		public Inplaces.Arity5_3<I1, I2, IO, I4, I5> wrap(final Inplaces.Arity5_3<I1, I2, IO, I4, I5> op, final OpInfo opInfo) {
			class GenericTypedInplace5_3 implements Inplaces.Arity5_3<I1, I2, IO, I4, I5>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace5_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace5_4OpWrapper<I1, I2, I3, IO, I5> implements OpWrapper<Inplaces.Arity5_4<I1, I2, I3, IO, I5>> {

		@Override
		public Inplaces.Arity5_4<I1, I2, I3, IO, I5> wrap(final Inplaces.Arity5_4<I1, I2, I3, IO, I5> op, final OpInfo opInfo) {
			class GenericTypedInplace5_4 implements Inplaces.Arity5_4<I1, I2, I3, IO, I5>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace5_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace5_5OpWrapper<I1, I2, I3, I4, IO> implements OpWrapper<Inplaces.Arity5_5<I1, I2, I3, I4, IO>> {

		@Override
		public Inplaces.Arity5_5<I1, I2, I3, I4, IO> wrap(final Inplaces.Arity5_5<I1, I2, I3, I4, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace5_5 implements Inplaces.Arity5_5<I1, I2, I3, I4, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace5_5();
		}
	}



}
