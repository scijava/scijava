package org.scijava.ops.image.filter.addNoise;

import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.ByteType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.scijava.ops.image.AbstractOpTest;

import java.util.Arrays;
import java.util.List;

public class NoiseAddersTest extends AbstractOpTest {

    @Test
    public void testAddUniformNoiseRegression() {
        var input = ArrayImgs.bytes(2, 2);
        ops.unary("image.fill").input(new ByteType((byte) 1)).output(input).compute();
        var output = ArrayImgs.bytes(2, 2);
        var rangeMin = new ByteType((byte) -1);
        var rangeMax = new ByteType((byte) 1);
        ops.ternary("filter.addUniformNoise").input(input, rangeMin, rangeMax).output(output).compute();
        var cursor = output.cursor();
        List<Byte> expected = Arrays.asList((byte) 0, (byte) 2, (byte) 1, (byte) 1);
        for(var e : expected) {
            Assertions.assertEquals(e, cursor.next().get());
        }
        Assertions.assertFalse(cursor.hasNext());
    }
}
