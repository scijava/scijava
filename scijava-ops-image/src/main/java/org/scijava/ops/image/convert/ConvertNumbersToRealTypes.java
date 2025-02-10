/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2025 SciJava developers.
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
package org.scijava.ops.image.convert;

import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;

import java.util.function.Function;

/**
 * Converters for converting between {@link RealType}s and {@link Number}s
 *
 * @author Gabriel Selzer
 */
public class ConvertNumbersToRealTypes<N extends Number, T extends RealType<T>, I extends IntegerType<I>> {

    // -- Numbers to RealTypes -- //

    // NB Combo conversion uses these to convert to any RealType

    /**
     * @input num the {@link Number} to convert
     * @output a {@link ByteType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.int8'
     */
    public final Function<N, ByteType> numberToByteType = //
            num -> new ByteType(num.byteValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link UnsignedByteType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.uint8'
     */
    public final Function<N, UnsignedByteType> numberToUnsignedByteType = //
            num -> new UnsignedByteType(num.shortValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link ShortType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.int16'
     */
    public final Function<N, ShortType> numberToShortType = //
            num -> new ShortType(num.shortValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link UnsignedShortType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.uint16'
     */
    public final Function<N, UnsignedShortType> numberToUnsignedShortType = //
            num -> new UnsignedShortType(num.intValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link IntType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.int32'
     */
    public final Function<N, IntType> numberToIntType = //
            num -> new IntType(num.intValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link UnsignedIntType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.uint32'
     */
    public final Function<N, UnsignedIntType> numberToUnsignedIntType = //
            num -> new UnsignedIntType(num.longValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link LongType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.int64'
     */
    public final Function<N, LongType> numberToLongType = //
            num -> new LongType(num.longValue());

    /**
     * @input num the {@link Number} to convert
     * @output a {@link FloatType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.float32'
     */
    public final Function<N, FloatType> numberToFloatType = //
            num -> new FloatType(num.floatValue());

    /**
     * NB This converter wins against those above in requests for e.g.
     * {@code Function<N, NativeType<T>>}
     *
     * @input num the {@link Number} to convert
     * @output a {@link DoubleType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.float64', priority=100
     */
    public final Function<N, DoubleType> numberToDoubleType = //
            num -> new DoubleType(num.doubleValue());

    // -- RealTypes to Numbers -- //

    /**
     * @input integerType the {@link IntegerType} to convert
     * @output the {@link Byte}, converted from {@code integerType}
     * @implNote op names='engine.convert, convert.int8', priority="10"
     */
    public final Function<I, Byte> integerTypeToByte = i -> (byte) i.getIntegerLong();

    /**
     * NB potentially lossy, so lower priority
     *
     * @input realType the {@link RealType} to convert
     * @output the {@link Byte}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int8'
     */
    public final Function<T, Byte> realTypeToByte = i -> (byte) i.getRealDouble();

    /**
     * @input integerType the {@link IntegerType} to convert
     * @output the {@link Short}, converted from {@code integerType}
     * @implNote op names='engine.convert, convert.int16', priority="10"
     */
    public final Function<I, Short> integerTypeToShort = i -> (short) i.getInteger();

    /**
     * NB potentially lossy, so lower priority
     *
     * @input realType the {@link RealType} to convert
     * @output the {@link Short}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int16'
     */
    public final Function<T, Short> realTypeToShort = i -> (short) i.getRealDouble();

    /**
     * @input integerType the {@link IntegerType} to convert
     * @output the {@link Integer}, converted from {@code integerType}
     * @implNote op names='engine.convert, convert.int32', priority="10"
     */
    public final Function<I, Integer> integerTypeToInteger = IntegerType::getInteger;

    /**
     * NB potentially lossy, so lower priority
     *
     * @input realType the {@link RealType} to convert
     * @output the {@link Integer}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int32'
     */
    public final Function<T, Integer> realTypeToInteger = i -> (int) i.getRealDouble();

    /**
     * @input integerType the {@link IntegerType} to convert
     * @output the {@link Long}, converted from {@code integerType}
     * @implNote op names='engine.convert, convert.int64', priority="10"
     */
    public final Function<I, Long> integerTypeToLong = IntegerType::getIntegerLong;

    /**
     * NB potentially lossy, so lower priority
     *
     * @input realType the {@link RealType} to convert
     * @output the {@link Long}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int64'
     */
    public final Function<T, Long> realTypeToLong = i -> (long) i.getRealDouble();

    /**
     * @input realType the {@link RealType} to convert
     * @output the {@link Float}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.float32'
     */
    public final Function<T, Float> realTypeToFloat = RealType::getRealFloat;

    /**
     * @input realType the {@link RealType} to convert
     * @output the {@link Double}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.float64'
     */
    public final Function<T, Double> realTypeToDouble = RealType::getRealDouble;
}
