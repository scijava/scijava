package org.scijava.ops.core.inplace;

import java.lang.reflect.Type;

import org.scijava.ops.core.GenericOp;
import org.scijava.ops.matcher.OpInfo;

/**
 * GenericInplaces are wrapped functions that know their generic typing at
 * runtime (because whoever wrapped the function knew it at the time of
 * wrapping).
 * 
 * TODO: can we make these classes protected/private? Is there a place to move
 * InplaceOps that makes sense that would allow protected/private?
 * 
 * @author Gabriel Selzer
 */
public class InplaceOps {

	public static final class InplaceOp<T> extends GenericOp implements Inplace<T> {

		private Inplace<T> c;
		private Type type;

		public InplaceOp(Inplace<T> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class BiInplaceFirstOp<T, U> extends GenericOp implements BiInplaceFirst<T, U> {

		private BiInplaceFirst<T, U> c;
		private Type type;

		public BiInplaceFirstOp(BiInplaceFirst<T, U> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class BiInplaceSecondOp<T, U> extends GenericOp implements BiInplaceSecond<T, U> {

		private BiInplaceSecond<T, U> c;
		private Type type;

		public BiInplaceSecondOp(BiInplaceSecond<T, U> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace3FirstOp<T, U, V> extends GenericOp implements Inplace3First<T, U, V> {

		private Inplace3First<T, U, V> c;
		private Type type;

		public Inplace3FirstOp(Inplace3First<T, U, V> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace3SecondOp<T, U, V> extends GenericOp implements Inplace3Second<T, U, V> {

		private Inplace3Second<T, U, V> c;
		private Type type;

		public Inplace3SecondOp(Inplace3Second<T, U, V> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace3ThirdOp<T, U, V> extends GenericOp implements Inplace3Third<T, U, V> {

		private Inplace3Third<T, U, V> c;
		private Type type;

		public Inplace3ThirdOp(Inplace3Third<T, U, V> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace4FirstOp<T, U, V, W> extends GenericOp implements Inplace4First<T, U, V, W> {

		private Inplace4First<T, U, V, W> c;
		private Type type;

		public Inplace4FirstOp(Inplace4First<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace4SecondOp<T, U, V, W> extends GenericOp implements Inplace4Second<T, U, V, W> {

		private Inplace4Second<T, U, V, W> c;
		private Type type;

		public Inplace4SecondOp(Inplace4Second<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace4ThirdOp<T, U, V, W> extends GenericOp implements Inplace4Third<T, U, V, W> {

		private Inplace4Third<T, U, V, W> c;
		private Type type;

		public Inplace4ThirdOp(Inplace4Third<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace4FourthOp<T, U, V, W> extends GenericOp implements Inplace4Fourth<T, U, V, W> {

		private Inplace4Fourth<T, U, V, W> c;
		private Type type;

		public Inplace4FourthOp(Inplace4Fourth<T, U, V, W> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace5FirstOp<T, U, V, W, X> extends GenericOp implements Inplace5First<T, U, V, W, X> {

		private Inplace5First<T, U, V, W, X> c;
		private Type type;

		public Inplace5FirstOp(Inplace5First<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace5SecondOp<T, U, V, W, X> extends GenericOp
			implements Inplace5Second<T, U, V, W, X> {

		private Inplace5Second<T, U, V, W, X> c;
		private Type type;

		public Inplace5SecondOp(Inplace5Second<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace5ThirdOp<T, U, V, W, X> extends GenericOp implements Inplace5Third<T, U, V, W, X> {

		private Inplace5Third<T, U, V, W, X> c;
		private Type type;

		public Inplace5ThirdOp(Inplace5Third<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace5FourthOp<T, U, V, W, X> extends GenericOp
			implements Inplace5Fourth<T, U, V, W, X> {

		private Inplace5Fourth<T, U, V, W, X> c;
		private Type type;

		public Inplace5FourthOp(Inplace5Fourth<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace5FifthOp<T, U, V, W, X> extends GenericOp implements Inplace5Fifth<T, U, V, W, X> {

		private Inplace5Fifth<T, U, V, W, X> c;
		private Type type;

		public Inplace5FifthOp(Inplace5Fifth<T, U, V, W, X> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6FirstOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6First<T, U, V, W, X, Y> {

		private Inplace6First<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6FirstOp(Inplace6First<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6SecondOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6Second<T, U, V, W, X, Y> {

		private Inplace6Second<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6SecondOp(Inplace6Second<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6ThirdOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6Third<T, U, V, W, X, Y> {

		private Inplace6Third<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6ThirdOp(Inplace6Third<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6FourthOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6Fourth<T, U, V, W, X, Y> {

		private Inplace6Fourth<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6FourthOp(Inplace6Fourth<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6FifthOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6Fifth<T, U, V, W, X, Y> {

		private Inplace6Fifth<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6FifthOp(Inplace6Fifth<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace6SixthOp<T, U, V, W, X, Y> extends GenericOp
			implements Inplace6Sixth<T, U, V, W, X, Y> {

		private Inplace6Sixth<T, U, V, W, X, Y> c;
		private Type type;

		public Inplace6SixthOp(Inplace6Sixth<T, U, V, W, X, Y> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

	public static final class Inplace7SecondOp<T, U, V, W, X, Y, Z> extends GenericOp
			implements Inplace7Second<T, U, V, W, X, Y, Z> {

		private Inplace7Second<T, U, V, W, X, Y, Z> c;
		private Type type;

		public Inplace7SecondOp(Inplace7Second<T, U, V, W, X, Y, Z> c, Type type, OpInfo opInfo) {
			super(opInfo);
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

}
