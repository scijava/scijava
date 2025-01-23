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

import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.*;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

public class TestConvertRealTypeNumbers extends AbstractOpTest {

    public static final Class<?>[] REAL_TYPES = {
            ByteType.class,
            ShortType.class,
            IntType.class,
            LongType.class,
            FloatType.class,
            DoubleType.class
    };

    public static final Class<?>[] NUMBERS = {
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class
    };

    @Test
    public void testConversion() {
        for(Class<?> rt: REAL_TYPES){
            for (Class<?> n: NUMBERS) {
                // rt -> n
                Assertions.assertDoesNotThrow(() -> //
                    ops.op("engine.convert") //
                        .inType(rt) //
                        .outType(n) //
                        .function(), //
                        "Could not convert " + rt + " objects to " + n
                );
                // rt -> n
                Assertions.assertDoesNotThrow(() -> //
                        ops.op("engine.convert") //
                                .inType(n) //
                                .outType(rt) //
                                .function(), //
                        "Could not convert " + n + " objects to " + rt
                );
            }
        }

    }

    /**
     * Creates a five.
     *
     * @param input some unused input
     * @return five
     * @implNote op names="create.five"
     */
    public static <T extends RealType<T>> LongType createRealTypeFive(T input) {
        return new LongType(5L);
    }

    /**
     * Creates a five.
     *
     * @param input some unused input
     * @return five
     * @implNote op names="create.five"
     */
    public static <N extends Number> LongType createNumberFive(N input) {
        return new LongType(5L);
    }

    /**
     * Test that in practice the converters work
     */
    @Test
    public void testExecution() {
        Object[] numbers = { //
            (byte) 5, //
            (short) 5, //
            5, //
            5L, //
            5f, //
            5d //
        };
        for (var i: numbers) {
            ops.op("create.five").input(i).outType(LongType.class).apply();
        }

        Object[] realTypes = { //
                new ByteType((byte) 5), //
                new UnsignedByteType( 5), //
                new ShortType((short) 5), //
                new UnsignedShortType( 5), //
                new IntType(5), //
                new UnsignedIntType( 5), //
                new LongType(5), //
                new UnsignedLongType( 5), //
                new FloatType( 5f), //
                new DoubleType( 5d), //
        };
        for (var i: realTypes) {
            ops.op("create.five").input(i).outType(LongType.class).apply();
        }
    }

}
