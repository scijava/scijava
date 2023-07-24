package net.imagej.ops2.copy;

import net.imglib2.Dimensions;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import org.scijava.function.Computers;
import org.scijava.ops.engine.util.Maps;
import org.scijava.ops.spi.OpDependency;

import java.lang.reflect.Array;

public class Copiers {

    /**
     * Copy {@link Type} to another {@link Type}
     *
     * @author Christian Dietz (University of Konstanz)
     * @param <T> the {@link Type} of the objects involved
     * @param input the input {@link Type}
     * @param output the {@link Type} that will be filled with the value of {@code input}
     * @implNote op names='copy, copy.type', type='org.scijava.function.Computers$Arity1'
     */
    public static <T extends Type<T>> void copyType(final T input, final T output) {
        output.set(input);
    }

    /**
     * Copies a {@link RandomAccessibleInterval} into another
     * {@link RandomAccessibleInterval}
     *
     * @param <T> the element type of each image
     * @param input the {@link RandomAccessibleInterval} whose data will be copied
     * @param copy  the {@link RandomAccessibleInterval} that will be filled with the contents of {@code input}
     * @author Christian Dietz (University of Konstanz)
     * @implNote op names='copy, copy.rai, copy.img', priority='10.0', type='org.scijava.function.Computers$Arity1'
     */
    public static <T> void copyRAI( //
                                    final @OpDependency(name = "copy") Computers.Arity1<T, T> copier, //
                                    final RandomAccessibleInterval<T> input, //
                                    final RandomAccessibleInterval<T> copy //
    ) {
        ensureEqualDimensions(input, copy);
        LoopBuilder.setImages(input, copy).forEachPixel(copier::compute);
    }

    /**
     * Copying {@link ImgLabeling} into another {@link ImgLabeling}
     *
     * @param <T> the element type of each image
     * @param raiCopier     an Op that can copy {@link RandomAccessibleInterval}s
     * @param mappingCopier an Op that can copy {@link LabelingMapping}s
     * @param input         the {@link ImgLabeling} to copy
     * @param output        the destination container of the copy operation
     * @author Christian Dietz (University of Konstanz)
     * @implNote op names='copy, copy.imgLabeling', type='org.scijava.function.Computers$Arity1'
     */
    public static <T extends IntegerType<T> & NativeType<T>, L> void copyImgLabeling( //
      final @OpDependency(name = "copy") Computers.Arity1<RandomAccessibleInterval<T>, RandomAccessibleInterval<T>> raiCopier,  //
      final @OpDependency(name = "copy") Computers.Arity1<LabelingMapping<L>, LabelingMapping<L>> mappingCopier, //
      final ImgLabeling<L, T> input, //
      final ImgLabeling<L, T> output //
    ) {
        ensureEqualDimensions(input, output);
        if (Util.getTypeFromInterval(input.getIndexImg()).getClass() != Util.getTypeFromInterval(output.getIndexImg())
                .getClass())
            throw new IllegalArgumentException("input and output index images must be of the same type!");
        raiCopier.compute(input.getIndexImg(), output.getIndexImg());
        mappingCopier.compute(input.getMapping(), output.getMapping());
    }

    /**
     * Copies a {@link LabelingMapping} into another {@link LabelingMapping}
     *
     * @author Christian Dietz (University of Konstanz)
     * @param <L> the type of the {@link LabelingMapping} elements
     * @param input the {@link LabelingMapping} to copy
     * @param output the destination container of the copy operation
     * @implNote op names='copy, copy.labelingMapping', priority='10000.', type='org.scijava.function.Computers$Arity1'
     */
    public static <L> void copyLabelingMapping(final LabelingMapping<L> input, final LabelingMapping<L> output) {
        output.setLabelSets(input.getLabelSets());
    }

    /**
     * Copying {@link ArrayImg} into another {@link ArrayImg}
     *
     * @author Christian Dietz (University of Konstanz)
     * @param <T> the type of the elements of each image
     * @param <A> the type of the backing data storage for each image
     * @param input the {@link ArrayImg} to copy
     * @param output the destination container of the copy operation
     * @implNote op names='copy, copy.img', priority='10000.', type='org.scijava.function.Computers$Arity1'
     */
    public static <T extends NativeType<T>, A extends ArrayDataAccess<A>> void copyArrayImage( //
           final ArrayImg<T, A> input,
           final ArrayImg<T, A> output
    ) {
        ensureEqualDimensions(input, output);

        final Object inArray = input.update(null).getCurrentStorageArray();
        final Object outArray = output.update(null).getCurrentStorageArray();
        System.arraycopy(inArray, 0, outArray, 0, Array.getLength(inArray));
    }


    /**
     * Copies an {@link IterableInterval} into another {@link IterableInterval}
     * TODO: Can we delete this in favor of some lifting operation?
     *
     * @author Christian Dietz (University of Konstanz)
     * @param <T> the element type of the {@link IterableInterval}s
     * @param copier an Op responsible for copying element types
     * @param input the {@link IterableInterval} to copy
     * @param output the destination container of the copy operation
     * @implNote op names='copy, copy.iterableInterval, copy.img', priority='1.0'
     */
    public static <T> void copyIterableInterval( //
        @OpDependency(name = "copy.type") final Computers.Arity1<T, T> copier, //
        final IterableInterval<T> input, //
        final IterableInterval<T> output
    ) {
        if (!input.iterationOrder().equals(output.iterationOrder()))
            throw new IllegalArgumentException("input and output must be of the same dimensions!");
        Computers.Arity1<Iterable<T>, Iterable<T>> mapped = Maps.ComputerMaps.Iterables.liftBoth(copier);
        mapped.compute(input, output);
    }

    private static void ensureEqualDimensions(Dimensions d1, Dimensions d2) {
        if (!Intervals.equalDimensions(d1, d2))
            throw new IllegalArgumentException("The Dimensions of the input and copy images must be the same!");
    }
}
