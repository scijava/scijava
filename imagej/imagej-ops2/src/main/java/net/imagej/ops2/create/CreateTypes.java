/*-
 * #%L
 * ImageJ2 software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2022 ImageJ2 developers.
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
package net.imagej.ops2.create;

import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.complex.ComplexDoubleType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.Unsigned128BitType;
import net.imglib2.type.numeric.integer.Unsigned12BitType;
import net.imglib2.type.numeric.integer.Unsigned2BitType;
import net.imglib2.type.numeric.integer.Unsigned4BitType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedLongType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import org.scijava.function.Producer;

public class CreateTypes {

	/**
	 * @output output
	 * @implNote op names='create, create.bit'
	 */
	public final Producer<BitType> bitTypeSource = () -> new BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint2'
	 */
	public final Producer<Unsigned2BitType> uint2TypeSource = () -> new Unsigned2BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint4'
	 */
	public final Producer<Unsigned4BitType> uint4TypeSource = () -> new Unsigned4BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.byte'
	 */
	public final Producer<ByteType> byteTypeSource = () -> new ByteType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint8'
	 */
	public final Producer<UnsignedByteType> uint8TypeSource = () -> new UnsignedByteType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint12'
	 */
	public final Producer<Unsigned12BitType> uint12TypeSource = () -> new Unsigned12BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.int16'
	 */
	public final Producer<ShortType> shortTypeSource = () -> new ShortType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint16'
	 */
	public final Producer<UnsignedShortType> uint16TypeSource = () -> new UnsignedShortType();

	/**
	 * @output output
	 * @implNote op names='create, create.int32'
	 */
	public final Producer<IntType> int32TypeSource = () -> new IntType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint32'
	 */
	public final Producer<UnsignedIntType> uint32TypeSource = () -> new UnsignedIntType();

	/**
	 * @output output
	 * @implNote op names='create, create.int64'
	 */
	public final Producer<LongType> int64TypeSource = () -> new LongType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint64'
	 */
	public final Producer<UnsignedLongType> uint64TypeSource = () -> new UnsignedLongType();

	/**
	 * @output output
	 * @implNote op names='create, create.uint128'
	 */
	public final Producer<Unsigned128BitType> uint128TypeSource = () -> new Unsigned128BitType();

	/**
	 * @output output
	 * @implNote op names='create, create.float32'
	 */
	public final Producer<FloatType> float32TypeSource = () -> new FloatType();

	/**
	 * @output output
	 * @implNote op names='create, create.cfloat32'
	 */
	public final Producer<ComplexFloatType> cfloat32TypeSource = () -> new ComplexFloatType();

	/**
	 * NB higher priority to match {@code Producer<RealType>} and {@code Producer<NativeType>}
	 *
	 * @output output
	 * @implNote op names='create, create.float64', priority='100.'
	 */
	public final Producer<DoubleType> float64TypeSource = () -> new DoubleType();

	/**
	 * @output output
	 * @implNote op names='create, create.cfloat64'
	 */
	public final Producer<ComplexDoubleType> cfloat64TypeSource = () -> new ComplexDoubleType();

}
