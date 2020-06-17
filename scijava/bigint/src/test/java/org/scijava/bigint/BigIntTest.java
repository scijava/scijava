package org.scijava.bigint;

import static org.junit.Assert.assertSame;

import java.math.BigInteger;

import org.junit.Test;

/**
 * Tests {@link BigInt}.
 * 
 * @author Curtis Rueden
 */
public class BigIntTest {

	@Test(expected = ArithmeticException.class)
	public void testNextProbablePrimePInf() {
		BigInt.POSITIVE_INFINITY.nextProbablePrime();
	}

	@Test(expected = ArithmeticException.class)
	public void testNextProbablePrimeNInf() {
		BigInt.NEGATIVE_INFINITY.nextProbablePrime();
	}

	@Test(expected = ArithmeticException.class)
	public void testNextProbablePrimeNaN() {
		BigInt.NaN.nextProbablePrime();
	}

	@Test
	public void testAddPInf() {
		assertSame(BigInt.POSITIVE_INFINITY, BigInt.POSITIVE_INFINITY.add(BigInteger.ZERO));
		assertSame(BigInt.POSITIVE_INFINITY, BigInt.POSITIVE_INFINITY.add(BigInteger.ONE));
	}

	@Test
	public void testAddNInf() {
	}

	@Test
	public void testAddNaN() {
	}

	@Test
	public void testSubtractPInf() {
	}

	@Test
	public void testSubtracNPInf() {
	}

	@Test
	public void testSubtracNaNInf() {
	}

	@Test
	public void testMultiplyPInf() {
	}

	@Test
	public void testMultiplyNInf() {
	}

	@Test
	public void testMultiplyNaN() {
	}

	@Test
	public void testDividePInf() {
	}

	@Test
	public void testDivideNInf() {
	}

	@Test
	public void testDivideNaN() {
	}

	@Test
	public void testDivideAndRemainderPInf() {
	}

	@Test
	public void testDivideAndRemainderNInf() {
	}

	@Test
	public void testDivideAndRemainderNaN() {
	}

	@Test
	public void testRemainderPInf() {
	}

	@Test
	public void testRemainderNInf() {
	}

	@Test
	public void testRemainderNaN() {
	}

	@Test
	public void testPowPInf() {
	}

	@Test
	public void testPowNInf() {
	}

	@Test
	public void testPowNaN() {
	}

	@Test
	public void testGcdPInf() {
	}

	@Test
	public void testGcdNInf() {
	}

	@Test
	public void testGcdNaN() {
	}

	@Test
	public void testAbsPInf() {
	}

	@Test
	public void testAbsNInf() {
	}

	@Test
	public void testAbsNaN() {
	}

	@Test
	public void testNegatePInf() {
	}

	@Test
	public void testNegateNInf() {
	}

	@Test
	public void testNegateNaN() {
	}

	@Test
	public void testSignumPInf() {
	}

	@Test
	public void testSignumNInf() {
	}

	@Test
	public void testSignumNaN() {
	}

	@Test
	public void testModPInf() {
	}

	@Test
	public void testModNInf() {
	}

	@Test
	public void testModNaN() {
	}

	@Test
	public void testModPowPInf() {
	}

	@Test
	public void testModPowNInf() {
	}

	@Test
	public void testModPowNaN() {
	}

	@Test
	public void testModInversePInf() {
	}

	@Test
	public void testModInverseNInf() {
	}

	@Test
	public void testModInverseNaN() {
	}

	@Test
	public void testShiftLeftPInf() {
	}

	@Test
	public void testShiftLeftNInf() {
	}

	@Test
	public void testShiftLeftNaN() {
	}

	@Test
	public void testShiftRightPInf() {
	}

	@Test
	public void testShiftRightNInf() {
	}

	@Test
	public void testShiftRightNaN() {
	}

	@Test
	public void testAndPInf() {
	}

	@Test
	public void testAndNInf() {
	}

	@Test
	public void testAndNaN() {
	}

	@Test
	public void testOrPInf() {
	}

	@Test
	public void testOrNInf() {
	}

	@Test
	public void testOrNaN() {
	}

	@Test
	public void testXorPInf() {
	}

	@Test
	public void testXorNInf() {
	}

	@Test
	public void testXorNaN() {
	}

	@Test
	public void testNotPInf() {
	}

	@Test
	public void testNotNInf() {
	}

	@Test
	public void testNotNaN() {
	}

	@Test
	public void testAndNotPInf() {
	}

	@Test
	public void testAndNotNInf() {
	}

	@Test
	public void testAndNotNaN() {
	}

	@Test
	public void testTestBitPInf() {
	}

	@Test
	public void testTestBitNInf() {
	}

	@Test
	public void testTestBitNaN() {
	}

	@Test
	public void testSetBitPInf() {
	}

	@Test
	public void testSetBitNInf() {
	}

	@Test
	public void testSetBitNaN() {
	}

	@Test
	public void testClearBitPInf() {
	}

	@Test
	public void testClearBitNInf() {
	}

	@Test
	public void testClearBitNaN() {
	}

	@Test
	public void testFlipBitPInf() {
	}

	@Test
	public void testFlipBitNInf() {
	}

	@Test
	public void testFlipBitNaN() {
	}

	@Test
	public void testGetLowestSetBitPInf() {
	}

	@Test
	public void testGetLowestSetBitNInf() {
	}

	@Test
	public void testGetLowestSetBitNaN() {
	}

	@Test
	public void testBitLengthPInf() {
	}

	@Test
	public void testBitLengthNInf() {
	}

	@Test
	public void testBitLengthNaN() {
	}

	@Test
	public void testBitCountPInf() {
	}

	@Test
	public void testBitCountNInf() {
	}

	@Test
	public void testBitCountNaN() {
	}

	@Test
	public void testIsProbablePrimePInf() {
	}

	@Test
	public void testIsProbablePrimeNInf() {
	}

	@Test
	public void testIsProbablePrimeNaN() {
	}

	@Test
	public void testCompareToPInf() {
	}

	@Test
	public void testCompareToNInf() {
	}

	@Test
	public void testCompareToNaN() {
	}

	@Test
	public void testEqualsPInf() {
	}

	@Test
	public void testEqualsNInf() {
	}

	@Test
	public void testEqualsNaN() {
	}

	@Test
	public void testMinPInf() {
	}

	@Test
	public void testMinNInf() {
	}

	@Test
	public void testMinNaN() {
	}

	@Test
	public void testMaxPInf() {
	}

	@Test
	public void testMaxNInf() {
	}

	@Test
	public void testMaxNaN() {
	}

	@Test
	public void testHashCodePInf() {
	}

	@Test
	public void testHashCodeNInf() {
	}

	@Test
	public void testHashCodeNaN() {
	}

	@Test
	public void testToStringRadixPInf() {
	}

	@Test
	public void testToStringRadixNInf() {
	}

	@Test
	public void testToStringRadixNaN() {
	}

	@Test
	public void testToStringPInf() {
	}

	@Test
	public void testToStringNInf() {
	}

	@Test
	public void testToStringNaN() {
	}

	@Test
	public void testToByteArrayPInf() {
	}

	@Test
	public void testToByteArrayNInf() {
	}

	@Test
	public void testToByteArrayNaN() {
	}

	@Test
	public void testIntValuePInf() {
	}

	@Test
	public void testIntValueNInf() {
	}

	@Test
	public void testIntValueNaN() {
	}

	@Test
	public void testLongValuePInf() {
	}

	@Test
	public void testLongValueNInf() {
	}

	@Test
	public void testLongValueNaN() {
	}

	@Test
	public void testFloatValuePInf() {
	}

	@Test
	public void testFloatValueNInf() {
	}

	@Test
	public void testFloatValueNaN() {
	}

	@Test
	public void testDoubleValuePInf() {
	}

	@Test
	public void testDoubleValueNInf() {
	}

	@Test
	public void testDoubleValueNaN() {
	}

	@Test
	public void testLongValueExactPInf() {
	}

	@Test
	public void testLongValueExactNInf() {
	}

	@Test
	public void testLongValueExactNaN() {
	}

	@Test
	public void testIntValueExactPInf() {
	}

	@Test
	public void testIntValueExactNInf() {
	}

	@Test
	public void testIntValueExactNaN() {
	}

	@Test
	public void testShortValueExactPInf() {
	}

	@Test
	public void testShortValueExactNInf() {
	}

	@Test
	public void testShortValueExactNaN() {
	}

	@Test
	public void testByteValueExactPInf() {
	}
	@Test
	public void testByteValueExactNInf() {
	}
	@Test
	public void testByteValueExactNaN() {
	}
}
