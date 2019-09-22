package org.scijava.ops.core.computer;

import java.lang.reflect.Type;

import org.scijava.ops.types.GenericTyped;

/**
 * GenericComputers are wrapped computers that know their generic typing at
 * runtime (because whoever wrapped the computers knew it at the time of
 * wrapping).
 * 
 * @author Gabriel Selzer
 */
public class GenericComputers {
	
	public static class GenericComputer<T, U> implements Computer<T, U>, GenericTyped {

		private Computer<T, U> c;
		private Type type;

		public GenericComputer(Computer<T, U> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u) {
			c.compute(t, u);
		}
	}

	public static class GenericBiComputer<T, U, V> implements BiComputer<T, U, V>, GenericTyped {

		private BiComputer<T, U, V> c;
		private Type type;

		public GenericBiComputer(BiComputer<T, U, V> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v) {
			c.compute(t, u, v);
		}
	}
	
	public static class GenericComputer3<T, U, V, W> implements Computer3<T, U, V, W>, GenericTyped {

		private Computer3<T, U, V, W> c;
		private Type type;

		public GenericComputer3(Computer3<T, U, V, W> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w) {
			c.compute(t, u, v, w);
		}
	}
	
	public static class GenericComputer4<T, U, V, W, X> implements Computer4<T, U, V, W, X>, GenericTyped {

		private Computer4<T, U, V, W, X> c;
		private Type type;

		public GenericComputer4(Computer4<T, U, V, W, X> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x) {
			c.compute(t, u, v, w, x);
		}
	}
	
	public static class GenericComputer5<T, U, V, W, X, Y> implements Computer5<T, U, V, W, X, Y>, GenericTyped {

		private Computer5<T, U, V, W, X, Y> c;
		private Type type;

		public GenericComputer5(Computer5<T, U, V, W, X, Y> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x, Y y) {
			c.compute(t, u, v, w, x, y);
		}
	}
	
	public static class GenericComputer6<T, U, V, W, X, Y, Z> implements Computer6<T, U, V, W, X, Y, Z>, GenericTyped {

		private Computer6<T, U, V, W, X, Y, Z> c;
		private Type type;

		public GenericComputer6(Computer6<T, U, V, W, X, Y, Z> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x, Y y, Z z) {
			c.compute(t, u, v, w, x, y, z);
		}
	}
	
	public static class GenericComputer7<T, U, V, W, X, Y, Z, A> implements Computer7<T, U, V, W, X, Y, Z, A>, GenericTyped {

		private Computer7<T, U, V, W, X, Y, Z, A> c;
		private Type type;

		public GenericComputer7(Computer7<T, U, V, W, X, Y, Z, A> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x, Y y, Z z, A a) {
			c.compute(t, u, v, w, x, y, z, a);
		}
	}
	
	public static class GenericComputer8<T, U, V, W, X, Y, Z, A, B> implements Computer8<T, U, V, W, X, Y, Z, A, B>, GenericTyped {

		private Computer8<T, U, V, W, X, Y, Z, A, B> c;
		private Type type;

		public GenericComputer8(Computer8<T, U, V, W, X, Y, Z, A, B> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x, Y y, Z z, A a, B b) {
			c.compute(t, u, v, w, x, y, z, a, b);
		}
	}

	public static class Computers {

		public static <T, U> Computer<T, U> generic(Computer<T, U> function, Type type) {
			return new GenericComputer<>(function, type);
		}

		public static <T, U, V> BiComputer<T, U, V> generic(BiComputer<T, U, V> function, Type type) {
			return new GenericBiComputer<>(function, type);
		}

		public static <T, U, V, W> Computer3<T, U, V, W> generic(Computer3<T, U, V, W> function, Type type) {
			return new GenericComputer3<>(function, type);
		}

		public static <T, U, V, W, X> Computer4<T, U, V, W, X> generic(Computer4<T, U, V, W, X> function,
				Type type) {
			return new GenericComputer4<>(function, type);
		}

		public static <T, U, V, W, X, Y> Computer5<T, U, V, W, X, Y> generic(
				Computer5<T, U, V, W, X, Y> function, Type type) {
			return new GenericComputer5<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z> Computer6<T, U, V, W, X, Y, Z> generic(
				Computer6<T, U, V, W, X, Y, Z> function, Type type) {
			return new GenericComputer6<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z, A> Computer7<T, U, V, W, X, Y, Z, A> generic(
				Computer7<T, U, V, W, X, Y, Z, A> function, Type type) {
			return new GenericComputer7<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z, A, B> Computer8<T, U, V, W, X, Y, Z, A, B> generic(
				Computer8<T, U, V, W, X, Y, Z, A, B> function, Type type) {
			return new GenericComputer8<>(function, type);
		}

	}

}
