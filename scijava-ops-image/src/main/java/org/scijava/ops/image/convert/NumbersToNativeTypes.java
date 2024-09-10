/*-
 * #%L
 * Image processing operations for SciJava Ops.
 * %%
 * Copyright (C) 2014 - 2024 SciJava developers.
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

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.ops.spi.OpDependency;

import java.util.function.Function;

/**
 * Converters for converting between {@link NativeType}s and {@link Number}s
 *
 * @author Gabriel Selzer
 */
public class NumbersToNativeTypes<N extends Number, T extends RealType<T>> {

    // -- Numbers to RealTypes -- //

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
     * @implNote op names='engine.convert, convert.int8'
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
     * @output a {@link IntType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.int32'
     */
    public final Function<N, IntType> numberToIntType = //
            num -> new IntType(num.intValue());

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
     * @input num the {@link Number} to convert
     * @output a {@link DoubleType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.float64'
     */
    public final Function<N, DoubleType> numberToDoubleType = //
            num -> new DoubleType(num.doubleValue());

    // -- RealTypes to Numbers -- //

    /**
     * @input realType the {@link ByteType} to convert
     * @output the {@link Byte}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int8'
     */
    public final Function<ByteType, Byte> byteTypeToByte = ByteType::get;

    /**
     * @input realType the {@link ShortType} to convert
     * @output the {@link Short}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int16'
     */
    public final Function<ShortType, Short> shortTypeToShort = ShortType::get;

    /**
     * @input realType the {@link IntType} to convert
     * @output the {@link Integer}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int32'
     */
    public final Function<IntType, Integer> intTypeToInteger = IntType::get;

    /**
     * @input realType the {@link LongType} to convert
     * @output the {@link Long}, converted from {@code realType}
     * @implNote op names='engine.convert, convert.int64'
     */
    public final Function<LongType, Long> longTypeToLong = LongType::get;

    /**
     * @input num the {@link Number} to convert
     * @output a {@link DoubleType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.float32'
     */
    public final Function<FloatType, Float> floatTypeToFloat = FloatType::get;

    /**
     * @input num the {@link Number} to convert
     * @output a {@link DoubleType} containing the information in {@code num}
     * @implNote op names='engine.convert, convert.float64'
     */
    public final Function<DoubleType, Double> doubleTypeToDouble = DoubleType::get;
}
