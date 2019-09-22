package org.scijava.ops.core.function;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.core.GenericOp;
import org.scijava.ops.matcher.OpInfo;

/**
 * GenericFunctions are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * TODO: can we make these classes protected/private? Is there a place to move
 * FunctionOps that makes sense that would allow protected/private?
 * 
 * @author Gabriel Selzer
 */
public class FunctionOps {

	public static final class FunctionOp<T, U> extends GenericOp implements Function<T, U> {

		private Function<T, U> c;
		private Type type;

		public FunctionOp(Function<T, U> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public U apply(T t) {
			return c.apply(t);
		}
	}

	public static final class BiFunctionOp<T, U, V> extends GenericOp implements BiFunction<T, U, V> {

		private BiFunction<T, U, V> c;
		private Type type;

		public BiFunctionOp(BiFunction<T, U, V> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public V apply(T t, U u) {
			return c.apply(t, u);
		}
	}

	public static final class Function3Op<T, U, V, W> extends GenericOp implements Function3<T, U, V, W> {

		private Function3<T, U, V, W> c;
		private Type type;

		public Function3Op(Function3<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public W apply(T t, U u, V v) {
			return c.apply(t, u, v);
		}
	}

	public static final class Function4Op<T, U, V, W, X> extends GenericOp implements Function4<T, U, V, W, X> {

		private Function4<T, U, V, W, X> c;
		private Type type;

		public Function4Op(Function4<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public X apply(T t, U u, V v, W w) {
			return c.apply(t, u, v, w);
		}
	}

	public static final class Function5Op<T, U, V, W, X, Y> extends GenericOp implements Function5<T, U, V, W, X, Y> {

		private Function5<T, U, V, W, X, Y> c;
		private Type type;

		public Function5Op(Function5<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Y apply(T t, U u, V v, W w, X x) {
			return c.apply(t, u, v, w, x);
		}
	}

	public static final class Function6Op<T, U, V, W, X, Y, Z> extends GenericOp implements Function6<T, U, V, W, X, Y, Z> {

		private Function6<T, U, V, W, X, Y, Z> c;
		private Type type;

		public Function6Op(Function6<T, U, V, W, X, Y, Z> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public Z apply(T t, U u, V v, W w, X x, Y y) {
			return c.apply(t, u, v, w, x, y);
		}
	}

	public static final class Function7Op<T, U, V, W, X, Y, Z, A>
			extends GenericOp implements Function7<T, U, V, W, X, Y, Z, A> {

		private Function7<T, U, V, W, X, Y, Z, A> c;
		private Type type;

		public Function7Op(Function7<T, U, V, W, X, Y, Z, A> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public A apply(T t, U u, V v, W w, X x, Y y, Z z) {
			return c.apply(t, u, v, w, x, y, z);
		}
	}

	public static final class Function8Op<T, U, V, W, X, Y, Z, A, B>
			extends GenericOp implements Function8<T, U, V, W, X, Y, Z, A, B> {

		private Function8<T, U, V, W, X, Y, Z, A, B> c;
		private Type type;

		public Function8Op(Function8<T, U, V, W, X, Y, Z, A, B> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public B apply(T t, U u, V v, W w, X x, Y y, Z z, A a) {
			return c.apply(t, u, v, w, x, y, z, a);
		}
	}

	public static final class Function9Op<T, U, V, W, X, Y, Z, A, B, C>
			extends GenericOp implements Function9<T, U, V, W, X, Y, Z, A, B, C> {

		private Function9<T, U, V, W, X, Y, Z, A, B, C> c;
		private Type type;

		public Function9Op(Function9<T, U, V, W, X, Y, Z, A, B, C> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public C apply(T t, U u, V v, W w, X x, Y y, Z z, A a, B b) {
			return c.apply(t, u, v, w, x, y, z, a, b);
		}
	}

}
