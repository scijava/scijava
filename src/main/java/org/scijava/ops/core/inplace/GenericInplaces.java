package org.scijava.ops.core.inplace;

import java.lang.reflect.Type;

import org.scijava.ops.types.GenericTyped;

/**
 * GenericFunctions are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * @author Gabriel Selzer
 */
public class GenericInplaces {

	public static class GenericInplace<T> implements Inplace<T>, GenericTyped {

		private Inplace<T> c;
		private Type type;

		public GenericInplace(Inplace<T> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t) {
			c.mutate(t);
		}
	}

	public static class GenericBiInplaceFirst<T, U> implements BiInplaceFirst<T, U>, GenericTyped {

		private BiInplaceFirst<T, U> c;
		private Type type;

		public GenericBiInplaceFirst(BiInplaceFirst<T, U> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u) {
			c.mutate(t, u);
		}
	}
	
	public static class GenericBiInplaceSecond<T, U> implements BiInplaceSecond<T, U>, GenericTyped {

		private BiInplaceSecond<T, U> c;
		private Type type;

		public GenericBiInplaceSecond(BiInplaceSecond<T, U> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u) {
			c.mutate(t, u);
		}
	}

	public static class GenericInplace3First<T, U, V> implements Inplace3First<T, U, V>, GenericTyped {

		private Inplace3First<T, U, V> c;
		private Type type;

		public GenericInplace3First(Inplace3First<T, U, V> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v) {
			c.mutate(t, u, v);
		}
	}

	public static class GenericInplace3Second<T, U, V> implements Inplace3Second<T, U, V>, GenericTyped {

		private Inplace3Second<T, U, V> c;
		private Type type;

		public GenericInplace3Second(Inplace3Second<T, U, V> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v) {
			c.mutate(t, u, v);
		}
	}

	public static class GenericInplace3Third<T, U, V> implements Inplace3Third<T, U, V>, GenericTyped {

		private Inplace3Third<T, U, V> c;
		private Type type;

		public GenericInplace3Third(Inplace3Third<T, U, V> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v) {
			c.mutate(t, u, v);
		}
	}

	public static class GenericInplace4First<T, U, V, W> implements Inplace4First<T, U, V, W>, GenericTyped {

		private Inplace4First<T, U, V, W> c;
		private Type type;

		public GenericInplace4First(Inplace4First<T, U, V, W> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v, W w) {
			c.mutate(t, u, v, w);
		}
	}
	
	public static class GenericInplace4Second<T, U, V, W> implements Inplace4Second<T, U, V, W>, GenericTyped {

		private Inplace4Second<T, U, V, W> c;
		private Type type;

		public GenericInplace4Second(Inplace4Second<T, U, V, W> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v, W w) {
			c.mutate(t, u, v, w);
		}
	}

	public static class GenericInplace5First<T, U, V, W, X> implements Inplace5First<T, U, V, W, X>, GenericTyped {

		private Inplace5First<T, U, V, W, X> c;
		private Type type;

		public GenericInplace5First(Inplace5First<T, U, V, W, X> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v, W w, X x) {
			c.mutate(t, u, v, w, x);
		}
	}

	public static class GenericInplace6First<T, U, V, W, X, Y> implements Inplace6First<T, U, V, W, X, Y>, GenericTyped {

		private Inplace6First<T, U, V, W, X, Y> c;
		private Type type;

		public GenericInplace6First(Inplace6First<T, U, V, W, X, Y> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v, W w, X x, Y y) {
			c.mutate(t, u, v, w, x, y);
		}
	}

	public static class GenericInplace7Second<T, U, V, W, X, Y, Z>
			implements Inplace7Second<T, U, V, W, X, Y, Z>, GenericTyped {

		private Inplace7Second<T, U, V, W, X, Y, Z> c;
		private Type type;

		public GenericInplace7Second(Inplace7Second<T, U, V, W, X, Y, Z> c, Type type) {
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void mutate(T t, U u, V v, W w, X x, Y y, Z z) {
			c.mutate(t, u, v, w, x, y, z);
		}
	}
	
	public static class Inplaces {

		public static <T> Inplace<T> generic(Inplace<T> function, Type type) {
			return new GenericInplace<>(function, type);
		}

		public static <T, U> BiInplaceFirst<T, U> generic(BiInplaceFirst<T, U> function, Type type) {
			return new GenericBiInplaceFirst<>(function, type);
		}

		public static <T, U, V> Inplace3First<T, U, V> generic(Inplace3First<T, U, V> function, Type type) {
			return new GenericInplace3First<>(function, type);
		}

		public static <T, U, V> Inplace3Second<T, U, V> generic(Inplace3Second<T, U, V> function, Type type) {
			return new GenericInplace3Second<>(function, type);
		}

		public static <T, U, V> Inplace3Third<T, U, V> generic(Inplace3Third<T, U, V> function, Type type) {
			return new GenericInplace3Third<>(function, type);
		}

		public static <T, U, V, W> Inplace4First<T, U, V, W> generic(Inplace4First<T, U, V, W> function,
				Type type) {
			return new GenericInplace4First<>(function, type);
		}

		public static <T, U, V, W> Inplace4Second<T, U, V, W> generic(Inplace4Second<T, U, V, W> function,
				Type type) {
			return new GenericInplace4Second<>(function, type);
		}

		public static <T, U, V, W, X> Inplace5First<T, U, V, W, X> generic(
				Inplace5First<T, U, V, W, X> function, Type type) {
			return new GenericInplace5First<>(function, type);
		}

		public static <T, U, V, W, X, Y> Inplace6First<T, U, V, W, X, Y> generic(
				Inplace6First<T, U, V, W, X, Y> function, Type type) {
			return new GenericInplace6First<>(function, type);
		}

		public static <T, U, V, W, X, Y, Z> Inplace7Second<T, U, V, W, X, Y, Z> generic(
				Inplace7Second<T, U, V, W, X, Y, Z> function, Type type) {
			return new GenericInplace7Second<>(function, type);
		}

	}

}
