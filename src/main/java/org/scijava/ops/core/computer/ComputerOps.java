package org.scijava.ops.core.computer;

import java.lang.reflect.Type;

import org.scijava.ops.core.GenericOp;
import org.scijava.ops.matcher.OpInfo;

/**
 * GenericComputers are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * TODO: can we make these classes protected/private? Is there a place to move
 * ComputerOps that makes sense that would allow protected/private?
 * 
 * @author Gabriel Selzer
 */
public class ComputerOps {

	public static final class ComputerOp<T, U> extends GenericOp implements Computer<T, U> {

		private Computer<T, U> c;
		private Type type;

		public ComputerOp(Computer<T, U> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class BiComputerOp<T, U, V> extends GenericOp implements BiComputer<T, U, V> {

		private BiComputer<T, U, V> c;
		private Type type;

		public BiComputerOp(BiComputer<T, U, V> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer3Op<T, U, V, W> extends GenericOp implements Computer3<T, U, V, W> {

		private Computer3<T, U, V, W> c;
		private Type type;

		public Computer3Op(Computer3<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer4Op<T, U, V, W, X> extends GenericOp implements Computer4<T, U, V, W, X> {

		private Computer4<T, U, V, W, X> c;
		private Type type;

		public Computer4Op(Computer4<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer5Op<T, U, V, W, X, Y> extends GenericOp implements Computer5<T, U, V, W, X, Y> {

		private Computer5<T, U, V, W, X, Y> c;
		private Type type;

		public Computer5Op(Computer5<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer6Op<T, U, V, W, X, Y, Z> extends GenericOp implements Computer6<T, U, V, W, X, Y, Z> {

		private Computer6<T, U, V, W, X, Y, Z> c;
		private Type type;

		public Computer6Op(Computer6<T, U, V, W, X, Y, Z> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer7Op<T, U, V, W, X, Y, Z, A> extends GenericOp
			implements Computer7<T, U, V, W, X, Y, Z, A> {

		private Computer7<T, U, V, W, X, Y, Z, A> c;
		private Type type;

		public Computer7Op(Computer7<T, U, V, W, X, Y, Z, A> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer8Op<T, U, V, W, X, Y, Z, A, B> extends GenericOp
			implements Computer8<T, U, V, W, X, Y, Z, A, B> {

		private Computer8<T, U, V, W, X, Y, Z, A, B> c;
		private Type type;

		public Computer8Op(Computer8<T, U, V, W, X, Y, Z, A, B> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Computer9Op<T, U, V, W, X, Y, Z, A, B, D> extends GenericOp
			implements Computer9<T, U, V, W, X, Y, Z, A, B, D> {

		private Computer9<T, U, V, W, X, Y, Z, A, B, D> c;
		private Type type;

		public Computer9Op(Computer9<T, U, V, W, X, Y, Z, A, B, D> c, Type type, OpInfo opInfo) {
			super(opInfo);
			this.c = c;
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void compute(T t, U u, V v, W w, X x, Y y, Z z, A a, B b, D d) {
			c.compute(t, u, v, w, x, y, z, a, b, d);
		}
	}

}
