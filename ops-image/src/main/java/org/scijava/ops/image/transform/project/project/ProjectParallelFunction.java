package org.scijava.ops.image.transform.project.project;

import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Producer;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;

/**
 * Wraps {@link ProjectParallelComputer}, but creates a new output image in the process.
 * @param <T> the type of input image elements
 * @param <V> the type of output image elements
 * @author Gabriel Selzer
 * @implNote op name='transform.project', priority='99.'
 */
public class ProjectParallelFunction<T, V> implements
    Functions.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<? super RandomAccessibleInterval<T>, V>, Integer, RandomAccessibleInterval<V>>
{
    @OpDependency(name="transform.project")
    Computers.Arity3<RandomAccessibleInterval<T>, Computers.Arity1<? super RandomAccessibleInterval<T>, V>, Integer, RandomAccessibleInterval<V>> projector;

    @OpDependency(name="transform.translateView")
    BiFunction<RandomAccessibleInterval<V>, long[], RandomAccessibleInterval<V>> translator;

    @OpDependency(name="create.type")
    Producer<V> typeCreator;

    @OpDependency(name="create.img")
    BiFunction<FinalDimensions, V, RandomAccessibleInterval<V>> creator;


    /**
     * Projects {@code op} along 1-dimensional slices (along dimension {@code dim}) of {@code input}
     *
     * @param input the input {@code n}-dimensional dataset
     * @param op the Op to project over {@code dim}
     * @param dim the dimension along {@code input} to project
     * @return a {@code n-1}-dimensional dataset
     */
    @Override
    public RandomAccessibleInterval<V> apply(RandomAccessibleInterval<T> input, Computers.Arity1<? super RandomAccessibleInterval<T>, V> op, Integer dim) {
        var dims = new long[input.numDimensions() - 1];
        var min = new long[input.numDimensions() - 1];
        for(int i = 0; i < input.numDimensions() - 1; i++) {
            dims[i] = input.dimension(i >= dim ? i+1 : i);
            min[i] = input.min(i >= dim ? i+1 : i);
        }
        // Get an arbitrary instance of the output type
        var outImg = creator.apply(new FinalDimensions(dims), typeCreator.create());
        // translate by the minimum of the input img
        var translated = translator.apply(outImg, min);

        projector.compute(input, op, dim, translated);
        return translated;
    }
}
