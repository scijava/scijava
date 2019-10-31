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

	@Plugin(type = OpWrapper.class)
	public static class Function6OpWrapper<I1, I2, I3, I4, I5, I6, O> implements OpWrapper<Functions.Arity6<I1, I2, I3, I4, I5, I6, O>> {

		@Override
		public Functions.Arity6<I1, I2, I3, I4, I5, I6, O> wrap(final Functions.Arity6<I1, I2, I3, I4, I5, I6, O> op, final OpInfo opInfo) {
			class GenericTypedFunction6 implements Functions.Arity6<I1, I2, I3, I4, I5, I6, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6) { return op.apply(in1, in2, in3, in4, in5, in6); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function7OpWrapper<I1, I2, I3, I4, I5, I6, I7, O> implements OpWrapper<Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O>> {

		@Override
		public Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O> wrap(final Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O> op, final OpInfo opInfo) {
			class GenericTypedFunction7 implements Functions.Arity7<I1, I2, I3, I4, I5, I6, I7, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7) { return op.apply(in1, in2, in3, in4, in5, in6, in7); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function8OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, O> implements OpWrapper<Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>> {

		@Override
		public Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> wrap(final Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> op, final OpInfo opInfo) {
			class GenericTypedFunction8 implements Functions.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> implements OpWrapper<Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>> {

		@Override
		public Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> wrap(final Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> op, final OpInfo opInfo) {
			class GenericTypedFunction9 implements Functions.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> implements OpWrapper<Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>> {

		@Override
		public Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> wrap(final Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> op, final OpInfo opInfo) {
			class GenericTypedFunction10 implements Functions.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> implements OpWrapper<Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>> {

		@Override
		public Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> wrap(final Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> op, final OpInfo opInfo) {
			class GenericTypedFunction11 implements Functions.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> implements OpWrapper<Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>> {

		@Override
		public Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> wrap(final Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> op, final OpInfo opInfo) {
			class GenericTypedFunction12 implements Functions.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> implements OpWrapper<Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>> {

		@Override
		public Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> wrap(final Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> op, final OpInfo opInfo) {
			class GenericTypedFunction13 implements Functions.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function14OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> implements OpWrapper<Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>> {

		@Override
		public Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> wrap(final Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> op, final OpInfo opInfo) {
			class GenericTypedFunction14 implements Functions.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction14();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function15OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> implements OpWrapper<Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>> {

		@Override
		public Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> wrap(final Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> op, final OpInfo opInfo) {
			class GenericTypedFunction15 implements Functions.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14, I15 in15) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction15();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Function16OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> implements OpWrapper<Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>> {

		@Override
		public Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> wrap(final Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> op, final OpInfo opInfo) {
			class GenericTypedFunction16 implements Functions.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>, GenericTyped {
				@Override public O apply(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14, I15 in15, I16 in16) { return op.apply(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedFunction16();
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

	@Plugin(type = OpWrapper.class)
	public static class Computer6OpWrapper<I1, I2, I3, I4, I5, I6, O> implements OpWrapper<Computers.Arity6<I1, I2, I3, I4, I5, I6, O>> {

		@Override
		public Computers.Arity6<I1, I2, I3, I4, I5, I6, O> wrap(final Computers.Arity6<I1, I2, I3, I4, I5, I6, O> op, final OpInfo opInfo) {
			class GenericTypedComputer6 implements Computers.Arity6<I1, I2, I3, I4, I5, I6, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer7OpWrapper<I1, I2, I3, I4, I5, I6, I7, O> implements OpWrapper<Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O>> {

		@Override
		public Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O> wrap(final Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O> op, final OpInfo opInfo) {
			class GenericTypedComputer7 implements Computers.Arity7<I1, I2, I3, I4, I5, I6, I7, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer8OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, O> implements OpWrapper<Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>> {

		@Override
		public Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> wrap(final Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O> op, final OpInfo opInfo) {
			class GenericTypedComputer8 implements Computers.Arity8<I1, I2, I3, I4, I5, I6, I7, I8, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> implements OpWrapper<Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>> {

		@Override
		public Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> wrap(final Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O> op, final OpInfo opInfo) {
			class GenericTypedComputer9 implements Computers.Arity9<I1, I2, I3, I4, I5, I6, I7, I8, I9, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> implements OpWrapper<Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>> {

		@Override
		public Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> wrap(final Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O> op, final OpInfo opInfo) {
			class GenericTypedComputer10 implements Computers.Arity10<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> implements OpWrapper<Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>> {

		@Override
		public Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> wrap(final Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O> op, final OpInfo opInfo) {
			class GenericTypedComputer11 implements Computers.Arity11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> implements OpWrapper<Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>> {

		@Override
		public Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> wrap(final Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O> op, final OpInfo opInfo) {
			class GenericTypedComputer12 implements Computers.Arity12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> implements OpWrapper<Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>> {

		@Override
		public Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> wrap(final Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O> op, final OpInfo opInfo) {
			class GenericTypedComputer13 implements Computers.Arity13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer14OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> implements OpWrapper<Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>> {

		@Override
		public Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> wrap(final Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O> op, final OpInfo opInfo) {
			class GenericTypedComputer14 implements Computers.Arity14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer14();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer15OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> implements OpWrapper<Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>> {

		@Override
		public Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> wrap(final Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O> op, final OpInfo opInfo) {
			class GenericTypedComputer15 implements Computers.Arity15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14, I15 in15, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer15();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Computer16OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> implements OpWrapper<Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>> {

		@Override
		public Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> wrap(final Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O> op, final OpInfo opInfo) {
			class GenericTypedComputer16 implements Computers.Arity16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16, O>, GenericTyped {
				@Override public void compute(I1 in1, I2 in2, I3 in3, I4 in4, I5 in5, I6 in6, I7 in7, I8 in8, I9 in9, I10 in10, I11 in11, I12 in12, I13 in13, I14 in14, I15 in15, I16 in16, @Mutable O out) { op.compute(in1, in2, in3, in4, in5, in6, in7, in8, in9, in10, in11, in12, in13, in14, in15, in16, out); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedComputer16();
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

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_1OpWrapper<IO, I2, I3, I4, I5, I6> implements OpWrapper<Inplaces.Arity6_1<IO, I2, I3, I4, I5, I6>> {

		@Override
		public Inplaces.Arity6_1<IO, I2, I3, I4, I5, I6> wrap(final Inplaces.Arity6_1<IO, I2, I3, I4, I5, I6> op, final OpInfo opInfo) {
			class GenericTypedInplace6_1 implements Inplaces.Arity6_1<IO, I2, I3, I4, I5, I6>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_2OpWrapper<I1, IO, I3, I4, I5, I6> implements OpWrapper<Inplaces.Arity6_2<I1, IO, I3, I4, I5, I6>> {

		@Override
		public Inplaces.Arity6_2<I1, IO, I3, I4, I5, I6> wrap(final Inplaces.Arity6_2<I1, IO, I3, I4, I5, I6> op, final OpInfo opInfo) {
			class GenericTypedInplace6_2 implements Inplaces.Arity6_2<I1, IO, I3, I4, I5, I6>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_3OpWrapper<I1, I2, IO, I4, I5, I6> implements OpWrapper<Inplaces.Arity6_3<I1, I2, IO, I4, I5, I6>> {

		@Override
		public Inplaces.Arity6_3<I1, I2, IO, I4, I5, I6> wrap(final Inplaces.Arity6_3<I1, I2, IO, I4, I5, I6> op, final OpInfo opInfo) {
			class GenericTypedInplace6_3 implements Inplaces.Arity6_3<I1, I2, IO, I4, I5, I6>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_4OpWrapper<I1, I2, I3, IO, I5, I6> implements OpWrapper<Inplaces.Arity6_4<I1, I2, I3, IO, I5, I6>> {

		@Override
		public Inplaces.Arity6_4<I1, I2, I3, IO, I5, I6> wrap(final Inplaces.Arity6_4<I1, I2, I3, IO, I5, I6> op, final OpInfo opInfo) {
			class GenericTypedInplace6_4 implements Inplaces.Arity6_4<I1, I2, I3, IO, I5, I6>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_5OpWrapper<I1, I2, I3, I4, IO, I6> implements OpWrapper<Inplaces.Arity6_5<I1, I2, I3, I4, IO, I6>> {

		@Override
		public Inplaces.Arity6_5<I1, I2, I3, I4, IO, I6> wrap(final Inplaces.Arity6_5<I1, I2, I3, I4, IO, I6> op, final OpInfo opInfo) {
			class GenericTypedInplace6_5 implements Inplaces.Arity6_5<I1, I2, I3, I4, IO, I6>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace6_6OpWrapper<I1, I2, I3, I4, I5, IO> implements OpWrapper<Inplaces.Arity6_6<I1, I2, I3, I4, I5, IO>> {

		@Override
		public Inplaces.Arity6_6<I1, I2, I3, I4, I5, IO> wrap(final Inplaces.Arity6_6<I1, I2, I3, I4, I5, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace6_6 implements Inplaces.Arity6_6<I1, I2, I3, I4, I5, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace6_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_1OpWrapper<IO, I2, I3, I4, I5, I6, I7> implements OpWrapper<Inplaces.Arity7_1<IO, I2, I3, I4, I5, I6, I7>> {

		@Override
		public Inplaces.Arity7_1<IO, I2, I3, I4, I5, I6, I7> wrap(final Inplaces.Arity7_1<IO, I2, I3, I4, I5, I6, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_1 implements Inplaces.Arity7_1<IO, I2, I3, I4, I5, I6, I7>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_2OpWrapper<I1, IO, I3, I4, I5, I6, I7> implements OpWrapper<Inplaces.Arity7_2<I1, IO, I3, I4, I5, I6, I7>> {

		@Override
		public Inplaces.Arity7_2<I1, IO, I3, I4, I5, I6, I7> wrap(final Inplaces.Arity7_2<I1, IO, I3, I4, I5, I6, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_2 implements Inplaces.Arity7_2<I1, IO, I3, I4, I5, I6, I7>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_3OpWrapper<I1, I2, IO, I4, I5, I6, I7> implements OpWrapper<Inplaces.Arity7_3<I1, I2, IO, I4, I5, I6, I7>> {

		@Override
		public Inplaces.Arity7_3<I1, I2, IO, I4, I5, I6, I7> wrap(final Inplaces.Arity7_3<I1, I2, IO, I4, I5, I6, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_3 implements Inplaces.Arity7_3<I1, I2, IO, I4, I5, I6, I7>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_4OpWrapper<I1, I2, I3, IO, I5, I6, I7> implements OpWrapper<Inplaces.Arity7_4<I1, I2, I3, IO, I5, I6, I7>> {

		@Override
		public Inplaces.Arity7_4<I1, I2, I3, IO, I5, I6, I7> wrap(final Inplaces.Arity7_4<I1, I2, I3, IO, I5, I6, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_4 implements Inplaces.Arity7_4<I1, I2, I3, IO, I5, I6, I7>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_5OpWrapper<I1, I2, I3, I4, IO, I6, I7> implements OpWrapper<Inplaces.Arity7_5<I1, I2, I3, I4, IO, I6, I7>> {

		@Override
		public Inplaces.Arity7_5<I1, I2, I3, I4, IO, I6, I7> wrap(final Inplaces.Arity7_5<I1, I2, I3, I4, IO, I6, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_5 implements Inplaces.Arity7_5<I1, I2, I3, I4, IO, I6, I7>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_6OpWrapper<I1, I2, I3, I4, I5, IO, I7> implements OpWrapper<Inplaces.Arity7_6<I1, I2, I3, I4, I5, IO, I7>> {

		@Override
		public Inplaces.Arity7_6<I1, I2, I3, I4, I5, IO, I7> wrap(final Inplaces.Arity7_6<I1, I2, I3, I4, I5, IO, I7> op, final OpInfo opInfo) {
			class GenericTypedInplace7_6 implements Inplaces.Arity7_6<I1, I2, I3, I4, I5, IO, I7>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace7_7OpWrapper<I1, I2, I3, I4, I5, I6, IO> implements OpWrapper<Inplaces.Arity7_7<I1, I2, I3, I4, I5, I6, IO>> {

		@Override
		public Inplaces.Arity7_7<I1, I2, I3, I4, I5, I6, IO> wrap(final Inplaces.Arity7_7<I1, I2, I3, I4, I5, I6, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace7_7 implements Inplaces.Arity7_7<I1, I2, I3, I4, I5, I6, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace7_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8> implements OpWrapper<Inplaces.Arity8_1<IO, I2, I3, I4, I5, I6, I7, I8>> {

		@Override
		public Inplaces.Arity8_1<IO, I2, I3, I4, I5, I6, I7, I8> wrap(final Inplaces.Arity8_1<IO, I2, I3, I4, I5, I6, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_1 implements Inplaces.Arity8_1<IO, I2, I3, I4, I5, I6, I7, I8>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8> implements OpWrapper<Inplaces.Arity8_2<I1, IO, I3, I4, I5, I6, I7, I8>> {

		@Override
		public Inplaces.Arity8_2<I1, IO, I3, I4, I5, I6, I7, I8> wrap(final Inplaces.Arity8_2<I1, IO, I3, I4, I5, I6, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_2 implements Inplaces.Arity8_2<I1, IO, I3, I4, I5, I6, I7, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8> implements OpWrapper<Inplaces.Arity8_3<I1, I2, IO, I4, I5, I6, I7, I8>> {

		@Override
		public Inplaces.Arity8_3<I1, I2, IO, I4, I5, I6, I7, I8> wrap(final Inplaces.Arity8_3<I1, I2, IO, I4, I5, I6, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_3 implements Inplaces.Arity8_3<I1, I2, IO, I4, I5, I6, I7, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8> implements OpWrapper<Inplaces.Arity8_4<I1, I2, I3, IO, I5, I6, I7, I8>> {

		@Override
		public Inplaces.Arity8_4<I1, I2, I3, IO, I5, I6, I7, I8> wrap(final Inplaces.Arity8_4<I1, I2, I3, IO, I5, I6, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_4 implements Inplaces.Arity8_4<I1, I2, I3, IO, I5, I6, I7, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8> implements OpWrapper<Inplaces.Arity8_5<I1, I2, I3, I4, IO, I6, I7, I8>> {

		@Override
		public Inplaces.Arity8_5<I1, I2, I3, I4, IO, I6, I7, I8> wrap(final Inplaces.Arity8_5<I1, I2, I3, I4, IO, I6, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_5 implements Inplaces.Arity8_5<I1, I2, I3, I4, IO, I6, I7, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8> implements OpWrapper<Inplaces.Arity8_6<I1, I2, I3, I4, I5, IO, I7, I8>> {

		@Override
		public Inplaces.Arity8_6<I1, I2, I3, I4, I5, IO, I7, I8> wrap(final Inplaces.Arity8_6<I1, I2, I3, I4, I5, IO, I7, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_6 implements Inplaces.Arity8_6<I1, I2, I3, I4, I5, IO, I7, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8> implements OpWrapper<Inplaces.Arity8_7<I1, I2, I3, I4, I5, I6, IO, I8>> {

		@Override
		public Inplaces.Arity8_7<I1, I2, I3, I4, I5, I6, IO, I8> wrap(final Inplaces.Arity8_7<I1, I2, I3, I4, I5, I6, IO, I8> op, final OpInfo opInfo) {
			class GenericTypedInplace8_7 implements Inplaces.Arity8_7<I1, I2, I3, I4, I5, I6, IO, I8>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace8_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO> implements OpWrapper<Inplaces.Arity8_8<I1, I2, I3, I4, I5, I6, I7, IO>> {

		@Override
		public Inplaces.Arity8_8<I1, I2, I3, I4, I5, I6, I7, IO> wrap(final Inplaces.Arity8_8<I1, I2, I3, I4, I5, I6, I7, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace8_8 implements Inplaces.Arity8_8<I1, I2, I3, I4, I5, I6, I7, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace8_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_1<IO, I2, I3, I4, I5, I6, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_1<IO, I2, I3, I4, I5, I6, I7, I8, I9> wrap(final Inplaces.Arity9_1<IO, I2, I3, I4, I5, I6, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_1 implements Inplaces.Arity9_1<IO, I2, I3, I4, I5, I6, I7, I8, I9>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_2<I1, IO, I3, I4, I5, I6, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_2<I1, IO, I3, I4, I5, I6, I7, I8, I9> wrap(final Inplaces.Arity9_2<I1, IO, I3, I4, I5, I6, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_2 implements Inplaces.Arity9_2<I1, IO, I3, I4, I5, I6, I7, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_3<I1, I2, IO, I4, I5, I6, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_3<I1, I2, IO, I4, I5, I6, I7, I8, I9> wrap(final Inplaces.Arity9_3<I1, I2, IO, I4, I5, I6, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_3 implements Inplaces.Arity9_3<I1, I2, IO, I4, I5, I6, I7, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_4<I1, I2, I3, IO, I5, I6, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_4<I1, I2, I3, IO, I5, I6, I7, I8, I9> wrap(final Inplaces.Arity9_4<I1, I2, I3, IO, I5, I6, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_4 implements Inplaces.Arity9_4<I1, I2, I3, IO, I5, I6, I7, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_5<I1, I2, I3, I4, IO, I6, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_5<I1, I2, I3, I4, IO, I6, I7, I8, I9> wrap(final Inplaces.Arity9_5<I1, I2, I3, I4, IO, I6, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_5 implements Inplaces.Arity9_5<I1, I2, I3, I4, IO, I6, I7, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9> implements OpWrapper<Inplaces.Arity9_6<I1, I2, I3, I4, I5, IO, I7, I8, I9>> {

		@Override
		public Inplaces.Arity9_6<I1, I2, I3, I4, I5, IO, I7, I8, I9> wrap(final Inplaces.Arity9_6<I1, I2, I3, I4, I5, IO, I7, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_6 implements Inplaces.Arity9_6<I1, I2, I3, I4, I5, IO, I7, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9> implements OpWrapper<Inplaces.Arity9_7<I1, I2, I3, I4, I5, I6, IO, I8, I9>> {

		@Override
		public Inplaces.Arity9_7<I1, I2, I3, I4, I5, I6, IO, I8, I9> wrap(final Inplaces.Arity9_7<I1, I2, I3, I4, I5, I6, IO, I8, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_7 implements Inplaces.Arity9_7<I1, I2, I3, I4, I5, I6, IO, I8, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9> implements OpWrapper<Inplaces.Arity9_8<I1, I2, I3, I4, I5, I6, I7, IO, I9>> {

		@Override
		public Inplaces.Arity9_8<I1, I2, I3, I4, I5, I6, I7, IO, I9> wrap(final Inplaces.Arity9_8<I1, I2, I3, I4, I5, I6, I7, IO, I9> op, final OpInfo opInfo) {
			class GenericTypedInplace9_8 implements Inplaces.Arity9_8<I1, I2, I3, I4, I5, I6, I7, IO, I9>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace9_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO> implements OpWrapper<Inplaces.Arity9_9<I1, I2, I3, I4, I5, I6, I7, I8, IO>> {

		@Override
		public Inplaces.Arity9_9<I1, I2, I3, I4, I5, I6, I7, I8, IO> wrap(final Inplaces.Arity9_9<I1, I2, I3, I4, I5, I6, I7, I8, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace9_9 implements Inplaces.Arity9_9<I1, I2, I3, I4, I5, I6, I7, I8, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace9_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10> wrap(final Inplaces.Arity10_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_1 implements Inplaces.Arity10_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10> wrap(final Inplaces.Arity10_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_2 implements Inplaces.Arity10_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10> wrap(final Inplaces.Arity10_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_3 implements Inplaces.Arity10_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10> wrap(final Inplaces.Arity10_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_4 implements Inplaces.Arity10_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10> wrap(final Inplaces.Arity10_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_5 implements Inplaces.Arity10_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10> wrap(final Inplaces.Arity10_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_6 implements Inplaces.Arity10_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10> implements OpWrapper<Inplaces.Arity10_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10>> {

		@Override
		public Inplaces.Arity10_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10> wrap(final Inplaces.Arity10_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_7 implements Inplaces.Arity10_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10> implements OpWrapper<Inplaces.Arity10_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10>> {

		@Override
		public Inplaces.Arity10_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10> wrap(final Inplaces.Arity10_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_8 implements Inplaces.Arity10_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10> implements OpWrapper<Inplaces.Arity10_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10>> {

		@Override
		public Inplaces.Arity10_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10> wrap(final Inplaces.Arity10_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10> op, final OpInfo opInfo) {
			class GenericTypedInplace10_9 implements Inplaces.Arity10_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace10_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO> implements OpWrapper<Inplaces.Arity10_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO>> {

		@Override
		public Inplaces.Arity10_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO> wrap(final Inplaces.Arity10_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace10_10 implements Inplaces.Arity10_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace10_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_1 implements Inplaces.Arity11_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_2 implements Inplaces.Arity11_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_3 implements Inplaces.Arity11_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_4 implements Inplaces.Arity11_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_5 implements Inplaces.Arity11_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11> wrap(final Inplaces.Arity11_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_6 implements Inplaces.Arity11_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11> wrap(final Inplaces.Arity11_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_7 implements Inplaces.Arity11_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11> implements OpWrapper<Inplaces.Arity11_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11>> {

		@Override
		public Inplaces.Arity11_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11> wrap(final Inplaces.Arity11_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_8 implements Inplaces.Arity11_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11> implements OpWrapper<Inplaces.Arity11_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11>> {

		@Override
		public Inplaces.Arity11_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11> wrap(final Inplaces.Arity11_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_9 implements Inplaces.Arity11_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11> implements OpWrapper<Inplaces.Arity11_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11>> {

		@Override
		public Inplaces.Arity11_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11> wrap(final Inplaces.Arity11_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11> op, final OpInfo opInfo) {
			class GenericTypedInplace11_10 implements Inplaces.Arity11_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace11_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO> implements OpWrapper<Inplaces.Arity11_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO>> {

		@Override
		public Inplaces.Arity11_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO> wrap(final Inplaces.Arity11_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace11_11 implements Inplaces.Arity11_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace11_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_1 implements Inplaces.Arity12_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_2 implements Inplaces.Arity12_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_3 implements Inplaces.Arity12_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_4 implements Inplaces.Arity12_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_5 implements Inplaces.Arity12_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_6 implements Inplaces.Arity12_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12> wrap(final Inplaces.Arity12_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_7 implements Inplaces.Arity12_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12> wrap(final Inplaces.Arity12_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_8 implements Inplaces.Arity12_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12> implements OpWrapper<Inplaces.Arity12_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12>> {

		@Override
		public Inplaces.Arity12_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12> wrap(final Inplaces.Arity12_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_9 implements Inplaces.Arity12_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12> implements OpWrapper<Inplaces.Arity12_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12>> {

		@Override
		public Inplaces.Arity12_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12> wrap(final Inplaces.Arity12_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_10 implements Inplaces.Arity12_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12> implements OpWrapper<Inplaces.Arity12_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12>> {

		@Override
		public Inplaces.Arity12_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12> wrap(final Inplaces.Arity12_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12> op, final OpInfo opInfo) {
			class GenericTypedInplace12_11 implements Inplaces.Arity12_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType, I12 in12Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType, in12Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace12_12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO> implements OpWrapper<Inplaces.Arity12_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO>> {

		@Override
		public Inplaces.Arity12_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO> wrap(final Inplaces.Arity12_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace12_12 implements Inplaces.Arity12_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace12_12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_1 implements Inplaces.Arity13_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_2 implements Inplaces.Arity13_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_3 implements Inplaces.Arity13_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_4 implements Inplaces.Arity13_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_5 implements Inplaces.Arity13_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_6 implements Inplaces.Arity13_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_7 implements Inplaces.Arity13_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13> wrap(final Inplaces.Arity13_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_8 implements Inplaces.Arity13_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13> wrap(final Inplaces.Arity13_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_9 implements Inplaces.Arity13_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13> implements OpWrapper<Inplaces.Arity13_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13>> {

		@Override
		public Inplaces.Arity13_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13> wrap(final Inplaces.Arity13_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_10 implements Inplaces.Arity13_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13> implements OpWrapper<Inplaces.Arity13_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13>> {

		@Override
		public Inplaces.Arity13_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13> wrap(final Inplaces.Arity13_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_11 implements Inplaces.Arity13_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType, I12 in12Type, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType, in12Type, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13> implements OpWrapper<Inplaces.Arity13_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13>> {

		@Override
		public Inplaces.Arity13_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13> wrap(final Inplaces.Arity13_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13> op, final OpInfo opInfo) {
			class GenericTypedInplace13_12 implements Inplaces.Arity13_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, IO ioType, I13 in13Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, ioType, in13Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace13_13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO> implements OpWrapper<Inplaces.Arity13_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO>> {

		@Override
		public Inplaces.Arity13_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO> wrap(final Inplaces.Arity13_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace13_13 implements Inplaces.Arity13_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace13_13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_1 implements Inplaces.Arity14_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_2 implements Inplaces.Arity14_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_3 implements Inplaces.Arity14_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_4 implements Inplaces.Arity14_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_5 implements Inplaces.Arity14_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_6 implements Inplaces.Arity14_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_7 implements Inplaces.Arity14_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_8 implements Inplaces.Arity14_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14> wrap(final Inplaces.Arity14_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_9 implements Inplaces.Arity14_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14> wrap(final Inplaces.Arity14_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_10 implements Inplaces.Arity14_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14> implements OpWrapper<Inplaces.Arity14_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14>> {

		@Override
		public Inplaces.Arity14_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14> wrap(final Inplaces.Arity14_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_11 implements Inplaces.Arity14_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType, I12 in12Type, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType, in12Type, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14> implements OpWrapper<Inplaces.Arity14_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14>> {

		@Override
		public Inplaces.Arity14_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14> wrap(final Inplaces.Arity14_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_12 implements Inplaces.Arity14_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, IO ioType, I13 in13Type, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, ioType, in13Type, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14> implements OpWrapper<Inplaces.Arity14_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14>> {

		@Override
		public Inplaces.Arity14_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14> wrap(final Inplaces.Arity14_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14> op, final OpInfo opInfo) {
			class GenericTypedInplace14_13 implements Inplaces.Arity14_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, IO ioType, I14 in14Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, ioType, in14Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace14_14OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO> implements OpWrapper<Inplaces.Arity14_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO>> {

		@Override
		public Inplaces.Arity14_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO> wrap(final Inplaces.Arity14_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace14_14 implements Inplaces.Arity14_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace14_14();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_1 implements Inplaces.Arity15_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_2 implements Inplaces.Arity15_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_3 implements Inplaces.Arity15_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_4 implements Inplaces.Arity15_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_5 implements Inplaces.Arity15_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_6 implements Inplaces.Arity15_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_7 implements Inplaces.Arity15_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_8 implements Inplaces.Arity15_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_9 implements Inplaces.Arity15_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15> wrap(final Inplaces.Arity15_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_10 implements Inplaces.Arity15_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15> wrap(final Inplaces.Arity15_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_11 implements Inplaces.Arity15_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType, in12Type, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15> implements OpWrapper<Inplaces.Arity15_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15>> {

		@Override
		public Inplaces.Arity15_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15> wrap(final Inplaces.Arity15_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_12 implements Inplaces.Arity15_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, IO ioType, I13 in13Type, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, ioType, in13Type, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15> implements OpWrapper<Inplaces.Arity15_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15>> {

		@Override
		public Inplaces.Arity15_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15> wrap(final Inplaces.Arity15_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_13 implements Inplaces.Arity15_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, IO ioType, I14 in14Type, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, ioType, in14Type, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_14OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15> implements OpWrapper<Inplaces.Arity15_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15>> {

		@Override
		public Inplaces.Arity15_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15> wrap(final Inplaces.Arity15_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15> op, final OpInfo opInfo) {
			class GenericTypedInplace15_14 implements Inplaces.Arity15_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, IO ioType, I15 in15Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, ioType, in15Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_14();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace15_15OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO> implements OpWrapper<Inplaces.Arity15_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO>> {

		@Override
		public Inplaces.Arity15_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO> wrap(final Inplaces.Arity15_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace15_15 implements Inplaces.Arity15_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace15_15();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_1OpWrapper<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_1 implements Inplaces.Arity16_1<IO, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(IO ioType, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(ioType, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_1();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_2OpWrapper<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_2 implements Inplaces.Arity16_2<I1, IO, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, IO ioType, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, ioType, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_2();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_3OpWrapper<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_3 implements Inplaces.Arity16_3<I1, I2, IO, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, IO ioType, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, ioType, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_3();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_4OpWrapper<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_4 implements Inplaces.Arity16_4<I1, I2, I3, IO, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, IO ioType, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, ioType, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_4();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_5OpWrapper<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_5 implements Inplaces.Arity16_5<I1, I2, I3, I4, IO, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, IO ioType, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, ioType, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_5();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_6OpWrapper<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_6 implements Inplaces.Arity16_6<I1, I2, I3, I4, I5, IO, I7, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, IO ioType, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, ioType, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_6();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_7OpWrapper<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_7 implements Inplaces.Arity16_7<I1, I2, I3, I4, I5, I6, IO, I8, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, IO ioType, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, ioType, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_7();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_8OpWrapper<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_8 implements Inplaces.Arity16_8<I1, I2, I3, I4, I5, I6, I7, IO, I9, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, IO ioType, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, ioType, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_8();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_9OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_9 implements Inplaces.Arity16_9<I1, I2, I3, I4, I5, I6, I7, I8, IO, I10, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, IO ioType, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, ioType, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_9();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_10OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_10 implements Inplaces.Arity16_10<I1, I2, I3, I4, I5, I6, I7, I8, I9, IO, I11, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, IO ioType, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, ioType, in11Type, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_10();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_11OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15, I16> wrap(final Inplaces.Arity16_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_11 implements Inplaces.Arity16_11<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, IO, I12, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, IO ioType, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, ioType, in12Type, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_11();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_12OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15, I16> wrap(final Inplaces.Arity16_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_12 implements Inplaces.Arity16_12<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, IO, I13, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, IO ioType, I13 in13Type, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, ioType, in13Type, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_12();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_13OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15, I16> implements OpWrapper<Inplaces.Arity16_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15, I16>> {

		@Override
		public Inplaces.Arity16_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15, I16> wrap(final Inplaces.Arity16_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_13 implements Inplaces.Arity16_13<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, IO, I14, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, IO ioType, I14 in14Type, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, ioType, in14Type, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_13();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_14OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15, I16> implements OpWrapper<Inplaces.Arity16_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15, I16>> {

		@Override
		public Inplaces.Arity16_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15, I16> wrap(final Inplaces.Arity16_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_14 implements Inplaces.Arity16_14<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, IO, I15, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, IO ioType, I15 in15Type, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, ioType, in15Type, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_14();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_15OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO, I16> implements OpWrapper<Inplaces.Arity16_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO, I16>> {

		@Override
		public Inplaces.Arity16_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO, I16> wrap(final Inplaces.Arity16_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO, I16> op, final OpInfo opInfo) {
			class GenericTypedInplace16_15 implements Inplaces.Arity16_15<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, IO, I16>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, IO ioType, I16 in16Type) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, ioType, in16Type); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_15();
		}
	}

	@Plugin(type = OpWrapper.class)
	public static class Inplace16_16OpWrapper<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, IO> implements OpWrapper<Inplaces.Arity16_16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, IO>> {

		@Override
		public Inplaces.Arity16_16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, IO> wrap(final Inplaces.Arity16_16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, IO> op, final OpInfo opInfo) {
			class GenericTypedInplace16_16 implements Inplaces.Arity16_16<I1, I2, I3, I4, I5, I6, I7, I8, I9, I10, I11, I12, I13, I14, I15, IO>, GenericTyped {
				@Override public void mutate(I1 in1Type, I2 in2Type, I3 in3Type, I4 in4Type, I5 in5Type, I6 in6Type, I7 in7Type, I8 in8Type, I9 in9Type, I10 in10Type, I11 in11Type, I12 in12Type, I13 in13Type, I14 in14Type, I15 in15Type, IO ioType) { op.mutate(in1Type, in2Type, in3Type, in4Type, in5Type, in6Type, in7Type, in8Type, in9Type, in10Type, in11Type, in12Type, in13Type, in14Type, in15Type, ioType); }
				@Override public Type getType() { return opInfo.opType(); }
			}
			return new GenericTypedInplace16_16();
		}
	}



}
