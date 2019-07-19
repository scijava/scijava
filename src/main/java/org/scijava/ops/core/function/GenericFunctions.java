package org.scijava.ops.core.function;

import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.scijava.ops.types.GenericTyped;

/**
 * GenericFunctions are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * @author Gabriel Selzer
 */
public class GenericFunctions {

	private static class GenericFunction<T, U> implements Function<T, U>, GenericTyped {

		private Function<T, U> c;
		private Type type;

		public GenericFunction(Function<T, U> c, Type type) {
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

	private static class GenericBiFunction<T, U, V> implements BiFunction<T, U, V>, GenericTyped {

		private BiFunction<T, U, V> c;
		private Type type;

		public GenericBiFunction(BiFunction<T, U, V> c, Type type) {
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

	private static class GenericFunction3<T, U, V, W> implements Function3<T, U, V, W>, GenericTyped {

		private Function3<T, U, V, W> c;
		private Type type;

		public GenericFunction3(Function3<T, U, V, W> c, Type type) {
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

	private static class GenericFunction4<T, U, V, W, X> implements Function4<T, U, V, W, X>, GenericTyped {

		private Function4<T, U, V, W, X> c;
		private Type type;

		public GenericFunction4(Function4<T, U, V, W, X> c, Type type) {
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

	private static class GenericFunction5<T, U, V, W, X, Y> implements Function5<T, U, V, W, X, Y>, GenericTyped {

		private Function5<T, U, V, W, X, Y> c;
		private Type type;

		public GenericFunction5(Function5<T, U, V, W, X, Y> c, Type type) {
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

	private static class GenericFunction6<T, U, V, W, X, Y, Z> implements Function6<T, U, V, W, X, Y, Z>, GenericTyped {

		private Function6<T, U, V, W, X, Y, Z> c;
		private Type type;

		public GenericFunction6(Function6<T, U, V, W, X, Y, Z> c, Type type) {
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

	private static class GenericFunction7<T, U, V, W, X, Y, Z, A>
			implements Function7<T, U, V, W, X, Y, Z, A>, GenericTyped {

		private Function7<T, U, V, W, X, Y, Z, A> c;
		private Type type;

		public GenericFunction7(Function7<T, U, V, W, X, Y, Z, A> c, Type type) {
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

	private static class GenericFunction8<T, U, V, W, X, Y, Z, A, B>
			implements Function8<T, U, V, W, X, Y, Z, A, B>, GenericTyped {

		private Function8<T, U, V, W, X, Y, Z, A, B> c;
		private Type type;

		public GenericFunction8(Function8<T, U, V, W, X, Y, Z, A, B> c, Type type) {
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

	private static class GenericFunction9<T, U, V, W, X, Y, Z, A, B, C>
			implements Function9<T, U, V, W, X, Y, Z, A, B, C>, GenericTyped {

		private Function9<T, U, V, W, X, Y, Z, A, B, C> c;
		private Type type;

		public GenericFunction9(Function9<T, U, V, W, X, Y, Z, A, B, C> c, Type type) {
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

	public static class Functions {

		public static <T, U> Function<T, U> generic(Function<T, U> function, Type type) {
			return new GenericFunction<>(function, type);
		}

		public static <T, U, V> BiFunction<T, U, V> generic(BiFunction<T, U, V> function, Type type) {
			return new GenericBiFunction<>(function, type);
		}

		public static <T, U, V, W> Function3<T, U, V, W> generic(Function3<T, U, V, W> function, Type type) {
			return new GenericFunction3<>(function, type);
		}

		public static <T, U, V, W, X> Function4<T, U, V, W, X> generic(Function4<T, U, V, W, X> function,
				Type type) {
			return new GenericFunction4<>(function, type);
		}

		public static <T, U, V, W, X, Y> Function5<T, U, V, W, X, Y> generic(
				Function5<T, U, V, W, X, Y> function, Type type) {
			return new GenericFunction5<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z> Function6<T, U, V, W, X, Y, Z> generic(
				Function6<T, U, V, W, X, Y, Z> function, Type type) {
			return new GenericFunction6<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z, A> Function7<T, U, V, W, X, Y, Z, A> generic(
				Function7<T, U, V, W, X, Y, Z, A> function, Type type) {
			return new GenericFunction7<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z, A, B> Function8<T, U, V, W, X, Y, Z, A, B> generic(
				Function8<T, U, V, W, X, Y, Z, A, B> function, Type type) {
			return new GenericFunction8<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z, A, B, C> Function9<T, U, V, W, X, Y, Z, A, B, C> generic(
				Function9<T, U, V, W, X, Y, Z, A, B, C> function, Type type) {
			return new GenericFunction9<>(function, type);
		}

	}

}
