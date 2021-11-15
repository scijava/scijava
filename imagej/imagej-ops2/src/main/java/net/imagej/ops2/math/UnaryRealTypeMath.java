/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ops2.math;

import java.util.Random;

import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.function.Computers;

/**
 * Ops of the {@code math} namespace which operate on {@link RealType}s.
 *
 * @author Barry DeZonia
 * @author Jonathan Hale (University of Konstanz)
 * @author Curtis Rueden
 */
public final class UnaryRealTypeMath<I extends RealType<I>, O extends RealType<O>> {

	/**
	 * Sets the real component of an output real number to the absolute value of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.abs'
	 */
	public final Computers.Arity1<I, O> absoluteValue = (input, output) -> output.setReal(Math.abs(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the inverse cosine of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccos'
	 */
	public final Computers.Arity1<I, O> arccos = (input, output) -> output.setReal(Math.acos(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * cosine of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccosh'
	 */
	public final Computers.Arity1<I, O> arccosh = (input, output) -> {
		final double xt = input.getRealDouble();
		double delta = Math.sqrt(xt * xt - 1);
		if (xt <= -1)
			delta = -delta;
		output.setReal(Math.log(xt + delta));
	};

	/**
	 * Sets the real component of an output real number to the inverse cotangent of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccot'
	 */
	public final Computers.Arity1<I, O> arccot = (input, output) -> {
		double value = Math.atan(1.0 / input.getRealDouble());
		if (input.getRealDouble() < 0)
			value += Math.PI;
		output.setReal(value);
	};

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * cotangent of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccoth'
	 */
	public final Computers.Arity1<I, O> arccoth = (input, output) -> {
		final double xt = input.getRealDouble();
		output.setReal(0.5 * Math.log((xt + 1) / (xt - 1)));
	};

	/**
	 * Sets the real component of an output real number to the inverse cosecant of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccsc'
	 */
	public final Computers.Arity1<I, O> arccsc = (input, output) -> {
		final double xt = input.getRealDouble();
		if (xt > -1 && xt < 1)
			throw new IllegalArgumentException("arccsc(x) : x out of range");
		else if (xt == -1)
			output.setReal(-Math.PI / 2);
		else if (xt == 1)
			output.setReal(Math.PI / 2);
		else {
			DoubleType tmp = new DoubleType();
			tmp.setReal(1 / xt);
			DoubleType angle = new DoubleType();
			angle.setReal(Math.asin(tmp.getRealDouble()));
			output.setReal(angle.getRealDouble());
		}
	};

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * cosecant of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arccsch'
	 */
	public final Computers.Arity1<I, O> arccsch = (input, output) -> {
		final double xt = input.getRealDouble();
		final double delta = Math.sqrt(1 + 1 / (xt * xt));
		output.setReal(Math.log(1 / xt + delta));
	};

	/**
	 * Sets the real component of an output real number to the inverse secant of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arcsec'
	 */
	public final Computers.Arity1<I, O> arcsec = (input, output) -> {
		final double xt = input.getRealDouble();
		if (xt > -1 && xt < 1)
			throw new IllegalArgumentException("arcsec(x) : x out of range");
		else if (xt == -1)
			output.setReal(Math.PI);
		else if (xt == 1)
			output.setReal(0);
		else {
			DoubleType tmp = new DoubleType();
			tmp.setReal(Math.sqrt(xt * xt - 1) / xt);
			DoubleType angle = new DoubleType();
			angle.setReal(Math.asin(tmp.getRealDouble()));
			double value = angle.getRealDouble();
			if (xt < -1)
				value += Math.PI;
			output.setReal(value);
		}
	};

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * secant of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arcsech'
	 */
	public final Computers.Arity1<I, O> arcsech = (input, output) -> {
		final double xt = input.getRealDouble();
		final double numer = 1 + Math.sqrt(1 - xt * xt);
		output.setReal(Math.log(numer / xt));
	};

	/**
	 * Sets the real component of an output real number to the inverse sine of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arcsin'
	 */
	public final Computers.Arity1<I, O> arcsin = (input, output) -> output.setReal(Math.asin(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * sine of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arcsinh'
	 */
	public final Computers.Arity1<I, O> arcsinh = (input, output) -> {
		final double xt = input.getRealDouble();
		final double delta = Math.sqrt(xt * xt + 1);
		output.setReal(Math.log(xt + delta));
	};

	/**
	 * Sets the real component of an output real number to the inverse tangent of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arctan'
	 */
	public final Computers.Arity1<I, O> arctan = (input, output) -> output.setReal(Math.atan(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the inverse hyperbolic
	 * tangent of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.arctanh'
	 */
	public final Computers.Arity1<I, O> arctanh = (input, output) -> {
		final double xt = input.getRealDouble();
		output.setReal(0.5 * Math.log((1 + xt) / (1 - xt)));
	};

	/**
	 * Sets the real component of an output real number to the ceiling of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.ceil'
	 */
	public final Computers.Arity1<I, O> ceil = (input, output) -> output.setReal(Math.ceil(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the cosine of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.cos'
	 */
	public final Computers.Arity1<I, O> cos = (input, output) -> output.setReal(Math.cos(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the hyperbolic cosine of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.cosh'
	 */
	public final Computers.Arity1<I, O> cosh = (input, output) -> output.setReal(Math.cosh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the cotangent of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.cot'
	 */
	public final Computers.Arity1<I, O> cot = (input, output) -> output.setReal(1.0 / Math.tan(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the hyperbolic cotangent
	 * of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.coth'
	 */
	public final Computers.Arity1<I, O> coth = (input, output) -> output.setReal(1.0 / Math.tanh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the cosecant of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.csc'
	 */
	public final Computers.Arity1<I, O> csc = (input, output) -> output.setReal(1.0 / Math.sin(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the hyperbolic cosecant
	 * of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.csch'
	 */
	public final Computers.Arity1<I, O> csch = (input, output) -> output.setReal(1.0 / Math.sinh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the cube root of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.cubeRoot'
	 */
	public final Computers.Arity1<I, O> cubeRoot = (input, output) -> output.setReal(Math.cbrt(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the exponentiation of the
	 * real component of an input real number. (e raised to a power)
	 * @input input
	 * @output output
	 * @implNote op names='math.exp'
	 */
	public final Computers.Arity1<I, O> exp = (input, output) -> output.setReal(Math.exp(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to e^x - 1. x is the input
	 * argument to the operation.
	 * @input input
	 * @output output
	 * @implNote op names='math.expMinusOne'
	 */
	public final Computers.Arity1<I, O> expMinusOne = (input, output) -> output.setReal(Math.exp(input.getRealDouble()) - 1);

	/**
	 * Sets the real component of an output real number to the floor of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.floor'
	 */
	public final Computers.Arity1<I, O> floor = (input, output) -> output.setReal(Math.floor(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the gamma value of the
	 * real component of an input real number.
	 * @input input
	 * @input constant
	 * @output output
	 * @implNote op names='math.gamma'
	 */
	public final Computers.Arity2<I, Double, O> gamma = (input, constant, output) -> {
		final double inputVal = input.getRealDouble();
		if (inputVal <= 0)
			output.setReal(0);
		else {
			output.setReal(Math.exp(constant * Math.log(inputVal)));
		}
	};

	/**
	 * Sets the real component of an output real number to the inversion of the real
	 * component of an input real number about a range.
	 * @input input
	 * @input specifiedMin
	 * @input specifiedMax
	 * @output output
	 * @implNote op names='math.invert'
	 */
	public final Computers.Arity3<I, Double, Double, O> invert = (input, specifiedMin, specifiedMax, output) -> output
			.setReal(specifiedMax - (input.getRealDouble() - specifiedMin));

	/**
	 * Sets the real component of an output real number to the natural log of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.log'
	 */
	public final Computers.Arity1<I, O> log = (input, output) -> output.setReal(Math.log(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the 10-based log of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.log10'
	 */
	public final Computers.Arity1<I, O> log10 = (input, output) -> output.setReal(Math.log10(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the base 2 log of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.log2'
	 */
	public final Computers.Arity1<I, O> log2 = (input, output) -> output.setReal(Math.log(input.getRealDouble()) / Math.log(2));

	/**
	 * Sets the real component of an output real number to the natural logarithm of
	 * the sum of the argument and 1. This calculation is more accurate than
	 * explicitly calling log(1.0 + x).
	 * @input input
	 * @output output
	 * @implNote op names='math.logOnePlusX'
	 */
	public final Computers.Arity1<I, O> logOnePlusX = (input, output) -> output.setReal(Math.log1p(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the real component of an
	 * input real number unless it exceeds a maximum value. If it exceeds the
	 * maximum value then it sets the output real component to that maximum value.
	 * @input input
	 * @input constant
	 * @output output
	 * @implNote op names='math.max'
	 */
	public final Computers.Arity2<I, Double, O> max = (input, constant, output) -> {
		final double value = input.getRealDouble();
		if (value < constant)
			output.setReal(value);
		else
			output.setReal(constant);
	};

	/**
	 * Sets the real component of an output real number to the real component of an
	 * input real number unless it is less then a minimum value. If it is less than
	 * the minimum value then it sets the output real component to that minimum
	 * value.
	 * @input input
	 * @input constant
	 * @output output
	 * @implNote op names='math.min'
	 */
	public final Computers.Arity2<I, Double, O> min = (input, constant, output) -> {
		final double value = input.getRealDouble();
		if (value > constant)
			output.setReal(value);
		else
			output.setReal(constant);
	};

	/**
	 * Sets the real component of an output real number to the nearest integral
	 * value of the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.nearestInt'
	 */
	public final Computers.Arity1<I, O> nearestInt = (input, output) -> output.setReal(Math.rint(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the negation of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.negate'
	 */
	public final Computers.Arity1<I, O> negate = (input, output) -> output.setReal(-input.getRealDouble());

	/**
	 * Sets the real component of an output real number to the raising of the real
	 * component of an input real number to a constant value.
	 * @input input
	 * @input constant
	 * @output output
	 * @implNote op names='math.power'
	 */
	public final Computers.Arity2<I, Double, O> power = (input, constant, output) -> output
			.setReal(Math.pow(input.getRealDouble(), constant));
	
	/**
	 * Uses ImgLib2 Mul interface to take a type to an integer power.
	 * @input input
	 * @input constant
	 * @output output
	 * @implNote op names='math.power'
	 */
	public final Computers.Arity2<I, Integer, I> intPower = (input, constant,
		output) -> {
		output.setOne();
		for (int i = 0; i < constant; i++) {
			output.mul(input);
		}
	};

	/**
	 * Sets the real component of an output real number to a random value using a
	 * gaussian distribution. The input value is considered the standard deviation
	 * of the desired distribution and must be positive. The output value has mean
	 * value 0.
	 * @input input
	 * @input random
	 * @output output
	 * @implNote op names='math.randomGaussian'
	 */
	public final Computers.Arity2<I, Random, O> randomGaussian = (input, random, output) -> output
			.setReal(random.nextGaussian() * Math.abs(input.getRealDouble()));

	/**
	 * Calls the above method with a new Random object (for efficiency the user
	 * should make a Random object and call the above method if using more than
	 * once)
	 * @input input
	 * @output output
	 * @implNote op names='math.randomGaussian'
	 */
	public final Computers.Arity1<I, O> randomGaussianWithoutRandom = (input, output) -> randomGaussian.accept(input,
			new Random(), output);

	/**
	 * Calls the above method with a new Random object defined by a passed seed (for
	 * efficiency the user should make a Random object and call the above method if
	 * using more than once)
	 * @input input
	 * @input seed
	 * @output output
	 * @implNote op names='math.randomGaussian'
	 */
	public final Computers.Arity2<I, Long, O> randomGaussianWithSeed = (input, seed, output) -> randomGaussian.accept(input,
			new Random(seed), output);

	/**
	 * Sets the real component of an output real number to a random value between 0
	 * and (input real number).
	 * @input input
	 * @input random
	 * @output output
	 * @implNote op names='math.randomUniform'
	 */
	public final Computers.Arity2<I, Random, O> randomUniform = (input, random, output) -> output
			.setReal(random.nextDouble() * Math.abs(input.getRealDouble()));

	/**
	 * Calls the above method with a new Random object (for efficiency the user
	 * should make a Random object and call the above method if using more than
	 * once)
	 * @input input
	 * @output output
	 * @implNote op names='math.randomUniform'
	 */
	public final Computers.Arity1<I, O> randomUniformWithoutRandom = (input, output) -> randomUniform.accept(input,
			new Random(), output);

	/**
	 * Calls the above method with a new Random object defined by a passed seed (for
	 * efficiency the user should make a Random object and call the above method if
	 * using more than once)
	 * @input input
	 * @input seed
	 * @output output
	 * @implNote op names='math.randomUniform'
	 */
	public final Computers.Arity2<I, Long, O> randomUniformWithSeed = (input, seed, output) -> randomUniform.accept(input,
			new Random(seed), output);

	/**
	 * Sets the real component of an output real number to the reciprocal of the
	 * real component of an input real number.
	 * @input input
	 * @input divideByZeroValue
	 * @output output
	 * @implNote op names='math.reciprocal'
	 */
	public final Computers.Arity2<I, Double, O> reciprocal = (input, dbzVal, output) -> {
		final double inputVal = input.getRealDouble();
		if (inputVal == 0)
			output.setReal(dbzVal);
		else
			output.setReal(1.0 / inputVal);
	};

	/**
	 * Sets the real component of an output real number to the rounding of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.round'
	 */
	public final Computers.Arity1<I, O> round = (input, output) -> output.setReal((double) Math.round(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the secant of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sec'
	 */
	public final Computers.Arity1<I, O> sec = (input, output) -> output.setReal(1.0 / Math.cos(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the hyperbolic secant of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sech'
	 */
	public final Computers.Arity1<I, O> sech = (input, output) -> output.setReal(1.0 / Math.cosh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the signum of the real
	 * component of an input real number. It equals -1 if the input number is less
	 * than 0, it equals 1 if the input number is greater than 0, and it equals 0 if
	 * the input number equals 0.
	 * @input input
	 * @output output
	 * @implNote op names='math.signum'
	 */
	public final Computers.Arity1<I, O> signum = (input, output) -> output.setReal(Math.signum(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the sine of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sin'
	 */
	public final Computers.Arity1<I, O> sin = (input, output) -> output.setReal(Math.sin(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the sinc value of the
	 * real component of an input real number. The sinc function is defined as
	 * sin(x) / x.
	 * @input input
	 * @output output
	 * @implNote op names='math.sinc'
	 */
	public final Computers.Arity1<I, O> sinc = (input, output) -> {
		final double x = input.getRealDouble();
		double value;
		if (x == 0)
			value = 1;
		else
			value = Math.sin(x) / x;
		output.setReal(value);
	};

	/**
	 * Sets the real component of an output real number to the sinc (pi version) of
	 * the real component of an input real number. The pi version of sinc is defined
	 * as sin(x*pi) / (x*pi).
	 * @input input
	 * @output output
	 * @implNote op names='math.sincPi'
	 */
	public final Computers.Arity1<I, O> sincPi = (input, output) -> {
		final double x = input.getRealDouble();
		double value;
		if (x == 0)
			value = 1;
		else
			value = Math.sin(Math.PI * x) / (Math.PI * x);
		output.setReal(value);
	};

	/**
	 * Sets the real component of an output real number to the hyperbolic sine of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sinh'
	 */
	public final Computers.Arity1<I, O> sinh = (input, output) -> output.setReal(Math.sinh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the square of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sqr'
	 */
	public final Computers.Arity1<I, O> square = (input, output) -> output
			.setReal(input.getRealDouble() * input.getRealDouble());

	/**
	 * Sets the real component of an output real number to the square root of the
	 * real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.sqrt'
	 */
	public final Computers.Arity1<I, O> sqrt = (input, output) -> output.setReal(Math.sqrt(input.getRealDouble()));

	/**
	 * Sets an output real number to 0 if the input real number is less than 0.
	 * Otherwise sets the output real number to 1. This implements a step function
	 * similar to Mathematica's unitstep function. It is a Heaviside step function
	 * with h(0) = 1 rather than 0.5.
	 * @input input
	 * @output output
	 * @implNote op names='math.step'
	 */
	public final Computers.Arity1<I, O> step = (input, output) -> {
		if (input.getRealDouble() < 0)
			output.setZero();
		else
			output.setOne();
	};

	/**
	 * Sets the real component of an output real number to the tangent of the real
	 * component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.tan'
	 */
	public final Computers.Arity1<I, O> tan = (input, output) -> output.setReal(Math.tan(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the hyperbolic tangent of
	 * the real component of an input real number.
	 * @input input
	 * @output output
	 * @implNote op names='math.tanh'
	 */
	public final Computers.Arity1<I, O> tanh = (input, output) -> output.setReal(Math.tanh(input.getRealDouble()));

	/**
	 * Sets the real component of an output real number to the size of the ulp of an
	 * input real number. An ulp of a floating point value is the positive distance
	 * between an input floating-point value and the floating point value next
	 * larger in magnitude. Note that for non-NaN x, ulp(-x) == ulp(x).
	 * @input input
	 * @output output
	 * @implNote op names='math.ulp'
	 */
	public final Computers.Arity1<I, O> ulp = (input, output) -> output.setReal(Math.ulp(input.getRealDouble()));

}
