
package org.scijava.bigint;

import java.math.BigInteger;

/**
 * An enhanced {@link BigInteger} with support for {@link #POSITIVE_INFINITY},
 * {@link #NEGATIVE_INFINITY} and {@link #NaN}, in an analogous way to
 * {@link Double} and {@link Float}.
 * 
 * @author Curtis Rueden
 */
public class BigInt extends BigInteger {

	public static final BigInt POSITIVE_INFINITY = //
		new SpecialBigInt(Double.POSITIVE_INFINITY)
	{
		@Override
		public BigInteger nextProbablePrime() {
			throw new ArithmeticException("No prime larger than infinity");
		}

		@Override
		public BigInteger add(final BigInteger val) {
			return val == BigInt.NEGATIVE_INFINITY || val == BigInt.NaN
				? BigInt.NaN : BigInt.POSITIVE_INFINITY;
		}

		@Override
		public BigInteger subtract(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger multiply(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger divide(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger[] divideAndRemainder(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger remainder(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger pow(final int exponent) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger gcd(final BigInteger val) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger abs() {
			// FIXME
			return null;
		}

		@Override
		public BigInteger negate() {
			// FIXME
			return null;
		}

		@Override
		public int signum() {
			return 1;
		}

		@Override
		public BigInteger mod(BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger modPow(BigInteger exponent, BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger modInverse(BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger shiftLeft(int n) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger shiftRight(int n) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger and(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger or(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger xor(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger not() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger andNot(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int compareTo(BigInteger val) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public BigInteger min(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger max(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

	};

	public static final BigInteger NEGATIVE_INFINITY =
		new SpecialBigInt(Double.NEGATIVE_INFINITY)
	{

		@Override
		public BigInteger nextProbablePrime() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger add(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger subtract(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger multiply(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger divide(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger[] divideAndRemainder(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger remainder(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger pow(int exponent) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger gcd(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger abs() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger negate() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int signum() {
			return -1;
		}

		@Override
		public BigInteger mod(BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger modPow(BigInteger exponent, BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger modInverse(BigInteger m) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger shiftLeft(int n) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger shiftRight(int n) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger and(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger or(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger xor(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger not() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger andNot(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int compareTo(BigInteger val) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public BigInteger min(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger max(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}
		// TODO
	};

	public static final BigInteger NaN = new SpecialBigInt(Double.NaN) {

		@Override
		public BigInteger nextProbablePrime() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger add(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger subtract(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger multiply(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger divide(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger[] divideAndRemainder(BigInteger val) {
			return new BigInteger[] {this, this};
		}

		@Override
		public BigInteger remainder(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger pow(int exponent) {
			return this;
		}

		@Override
		public BigInteger gcd(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger abs() {
			return this;
		}

		@Override
		public BigInteger negate() {
			return this;
		}

		@Override
		public int signum() {
			return 0;
		}

		@Override
		public BigInteger mod(BigInteger m) {
			return this;
		}

		@Override
		public BigInteger modPow(BigInteger exponent, BigInteger m) {
			return this;
		}

		@Override
		public BigInteger modInverse(BigInteger m) {
			return this;
		}

		@Override
		public BigInteger shiftLeft(int n) {
			return this;
		}

		@Override
		public BigInteger shiftRight(int n) {
			return this;
		}

		@Override
		public BigInteger and(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger or(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger xor(BigInteger val) {
			return this;
		}

		@Override
		public BigInteger not() {
			return this;
		}

		@Override
		public BigInteger andNot(BigInteger val) {
			return this;
		}

		@Override
		public int compareTo(BigInteger val) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public BigInteger min(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BigInteger max(BigInteger val) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	// TODO: BigInt.ZERO, BigInt.ONE, etc.

	public BigInt(byte[] val, int off, int len) {
		super(val, off, len);
	}

	public BigInt(byte[] val) {
		super(val);
	}

	public BigInt(int signum, byte[] magnitude, int off, int len) {
		super(signum, magnitude, off, len);
	}

	public BigInt(int signum, byte[] magnitude) {
		super(signum, magnitude);
	}

	public BigInt(String val, int radix) {
		super(val, radix);
	}

	// TODO - Override all the BigInteger methods to handle infinity and NaN.

	// -- Helper classes --

	/** A {@link BigInt} whose behavior is backed by a special {@link Double}. */
	private static abstract class SpecialBigInt extends BigInt {

		private final Double v;

		protected SpecialBigInt(final double v) {
			super(new byte[1]);
			this.v = v;
		}

		@Override
		public abstract BigInteger nextProbablePrime();

		@Override
		public abstract BigInteger add(final BigInteger val);

		@Override
		public abstract BigInteger subtract(final BigInteger val);

		@Override
		public abstract BigInteger multiply(final BigInteger val);

		@Override
		public abstract BigInteger divide(final BigInteger val);

		@Override
		public abstract BigInteger[] divideAndRemainder(final BigInteger val);

		@Override
		public abstract BigInteger remainder(final BigInteger val);

		@Override
		public abstract BigInteger pow(final int exponent);

		@Override
		public abstract BigInteger gcd(final BigInteger val);

		@Override
		public abstract BigInteger abs();

		@Override
		public abstract BigInteger negate();

		@Override
		public abstract int signum();

		@Override
		public abstract BigInteger mod(final BigInteger m);

		@Override
		public abstract BigInteger modPow(final BigInteger exponent, final BigInteger m);

		@Override
		public abstract BigInteger modInverse(final BigInteger m);

		@Override
		public abstract BigInteger shiftLeft(final int n);

		@Override
		public abstract BigInteger shiftRight(final int n);

		@Override
		public abstract BigInteger and(final BigInteger val);

		@Override
		public abstract BigInteger or(final BigInteger val);

		@Override
		public abstract BigInteger xor(final BigInteger val);

		@Override
		public abstract BigInteger not();

		@Override
		public abstract BigInteger andNot(final BigInteger val);

		@Override
		public boolean testBit(final int n) {
			// FIXME
			return false;
		}

		@Override
		public BigInteger setBit(final int n) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger clearBit(final int n) {
			// FIXME
			return null;
		}

		@Override
		public BigInteger flipBit(final int n) {
			// FIXME
			return null;
		}

		@Override
		public int getLowestSetBit() {
			// FIXME
			return 0;
		}

		@Override
		public int bitLength() {
			// FIXME
			return 0;
		}

		@Override
		public int bitCount() {
			// FIXME
			return 0;
		}

		@Override
		public boolean isProbablePrime(final int certainty) {
			return false;
		}

		@Override
		public abstract int compareTo(final BigInteger val);

		@Override
		public boolean equals(final Object x) {
			return x == this;
		}

		@Override
		public abstract BigInteger min(final BigInteger val);

		@Override
		public abstract BigInteger max(final BigInteger val);

		@Override
		public int hashCode() {
			return v.hashCode();
		}

		@Override
		public String toString(final int radix) {
			return toString();
		}

		@Override
		public String toString() {
			return v.toString();
		}

		@Override
		public byte[] toByteArray() {
			return new byte[0];
		}

		@Override
		public int intValue() {
			return v.intValue();
		}

		@Override
		public long longValue() {
			return v.longValue();
		}

		@Override
		public float floatValue() {
			return v.floatValue();
		}

		@Override
		public double doubleValue() {
			return v.doubleValue();
		}

		@Override
		public long longValueExact() {
			throw new ArithmeticException("BigInteger out of long range");
		}

		@Override
		public int intValueExact() {
			throw new ArithmeticException("BigInteger out of int range");
		}

		@Override
		public short shortValueExact() {
			throw new ArithmeticException("BigInteger out of short range");
		}

		@Override
		public byte byteValueExact() {
			throw new ArithmeticException("BigInteger out of byte range");
		}
	}
}
