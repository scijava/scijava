package org.scijava.ops.function;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.matcher.OpInfo;
import org.scijava.ops.types.GenericTyped;

public abstract class GenericTypedOp<OP> implements GenericTyped {

	protected final OP op;
	private final OpInfo opInfo;

	public GenericTypedOp(final OP op, final OpInfo opInfo) {
		this.op = op;
		this.opInfo = opInfo;
	}

	@Override
	public Type getType() {
		return opInfo.opType();
	}

	// FIXME: make all of these private; extract wrapper logic from OpService

	// -- producer --

	public static class P<T> extends GenericTypedOp<Producer<T>> implements Producer<T>{

		public P(Producer<T> op, OpInfo opInfo) {
				super(op, opInfo);
		}

		@Override
		public T create() {
			return op.create();
		}
	}

	// -- functions --

	public static class F1<T, U> extends GenericTypedOp<Function<T, U>> implements Function<T, U> {

		public F1(Function<T, U> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public U apply(T t) {
			return op.apply(t);
		}
	}

	public static class F2<T, U, V> extends GenericTypedOp<BiFunction<T, U, V>> implements BiFunction<T, U, V> {

		public F2(BiFunction<T, U, V> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public V apply(T t, U u) {
			return op.apply(t, u);
		}
	}

	public static class F3<T, U, V, W> extends GenericTypedOp<Functions.Arity3<T, U, V, W>> implements Functions.Arity3<T, U, V, W> {

		public F3(Functions.Arity3<T, U, V, W> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public W apply(T t, U u, V v) {
			return op.apply(t, u, v);
		}
	}

	// -- computers --

	public static final class C0<T> extends GenericTypedOp<Computers.Arity0<T>> implements Computers.Arity0<T> {

		public C0(Computers.Arity0<T> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void compute(T t) {
			op.compute(t);
		}
	}

	public static final class C1<T, U> extends GenericTypedOp<Computers.Arity1<T, U>> implements Computers.Arity1<T, U> {

		public C1(Computers.Arity1<T, U> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void compute(T t, U u) {
			op.compute(t, u);
		}
	}

	public static final class C2<T, U, V> extends GenericTypedOp<Computers.Arity2<T, U, V>> implements Computers.Arity2<T, U, V> {

		public C2(Computers.Arity2<T, U, V> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void compute(T t, U u, V v) {
			op.compute(t, u, v);
		}
	}

	public static final class C3<T, U, V, W> extends GenericTypedOp<Computers.Arity3<T, U, V, W>> implements Computers.Arity3<T, U, V, W> {

		public C3(Computers.Arity3<T, U, V, W> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void compute(T t, U u, V v, W w) {
			op.compute(t, u, v, w);
		}
	}

	public static final class IP1<IO> extends GenericTypedOp<Inplaces.Arity1<IO>> implements Inplaces.Arity1<IO> {

		public IP1(Inplaces.Arity1<IO> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(IO io) {
			op.mutate(io);
		}
	}

	public static final class IP2_1<IO, I2> extends GenericTypedOp<Inplaces.Arity2_1<IO, I2>> implements Inplaces.Arity2_1<IO, I2> {

		public IP2_1(Inplaces.Arity2_1<IO, I2> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(IO io, I2 in2) {
			op.mutate(io, in2);
		}
	}

	public static final class IP2_2<I1, IO> extends GenericTypedOp<Inplaces.Arity2_2<I1, IO>> implements Inplaces.Arity2_2<I1, IO> {

		public IP2_2(Inplaces.Arity2_2<I1, IO> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(I1 in1, IO io) {
			op.mutate(in1, io);
		}
	}

	public static final class IP3_1<IO, I2, I3> extends GenericTypedOp<Inplaces.Arity3_1<IO, I2, I3>> implements Inplaces.Arity3_1<IO, I2, I3> {

		public IP3_1(Inplaces.Arity3_1<IO, I2, I3> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(IO io, I2 in2, I3 in3) {
			op.mutate(io, in2, in3);
		}
	}

	public static final class IP3_2<I1, IO, I3> extends GenericTypedOp<Inplaces.Arity3_2<I1, IO, I3>> implements Inplaces.Arity3_2<I1, IO, I3> {

		public IP3_2(Inplaces.Arity3_2<I1, IO, I3> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(I1 in1, IO io, I3 in3) {
			op.mutate(in1, io, in3);
		}
	}

	public static final class IP3_3<I1, I2, IO> extends GenericTypedOp<Inplaces.Arity3_3<I1, I2, IO>> implements Inplaces.Arity3_2<I1, I2, IO> {

		public IP3_3(Inplaces.Arity3_3<I1, I2, IO> op, OpInfo opInfo) {
			super(op, opInfo);
		}

		@Override
		public void mutate(I1 in1, I2 in2, IO io) {
			op.mutate(in1, in2, io);
		}
	}
}
