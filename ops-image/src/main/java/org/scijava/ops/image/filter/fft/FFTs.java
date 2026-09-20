package org.scijava.ops.image.filter.fft;

import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.fft2.FFTMethods;
import net.imglib2.img.Img;
import net.imglib2.outofbounds.OutOfBoundsFactory;
import net.imglib2.type.numeric.ComplexType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.ops.spi.Nullable;
import org.scijava.ops.spi.OpDependency;

import java.util.function.BiFunction;

/**
 * Ops performing fast fourier transforms.
 * <p>
 * Note that there are explicit {@link Functions} in this class. These are required
 * as adaptation of {@link Computers} into {@link Functions} assumes the computer
 * output is the same size as the computer input. For Fourier transforms, this is
 * not the case.
 * </p>
 *
 * @author Brian Northan
 * @author Gabriel Selzer
 */
public class FFTs {

    private FFTs() {
        // Prevent instantiation of static utility class
    }

    /**
     * Forward FFT computer that operates on an RAI and wraps FFTMethods. The input
     * and output size must conform to a supported FFT size. Use
     * {@link org.scijava.ops.image.filter.fftSize.ComputeFFTSize} to calculate the
     * supported FFT size.
     *
     * @param <T> The data type of elements in the position space
     * @param <C> The data type of elements in the fourier space
     * @param input the input data
     * @param output the output
     * @implNote op names='filter.fft', priority='0.', type=Computer
     */
    public static <T extends RealType<T>, C extends ComplexType<C>> void compute( //
      final RandomAccessibleInterval<T> input, //
      final RandomAccessibleInterval<C> output //
    ) {

        // perform a real to complex FFT in the first dimension
        FFTMethods.realToComplex(input, output, 0, false);

        // loop and perform complex to complex FFT in the remaining dimensions
        for (var d = 1; d < input.numDimensions(); d++)
            FFTMethods.complexToComplex(output, d, true, false);
    }


    /**
     * Function that uses FFTMethods to perform a forward FFT
     *
     * @param <T> The data type of elements in the position space
     * @param <C> The data type of elements in the fourier space
     * @param padOp pads the boundaries of the input dataset
     * @param createOp creates the output dataset
     * @param fftOp performs the fast fourier transform
     * @param input the input data
     * @param fftType the complex type of the output
     * @param borderSize the size of border to apply in each dimension
     * @param fast whether to perform a fast FFT; default true
     * @return the output
     * @implNote op names='filter.fft', priority='100.'
     */
    public static <T extends RealType<T>, C extends ComplexType<C>> RandomAccessibleInterval<C> transformComplexType( //
      @OpDependency(name = "filter.padInputFFTMethods") final Functions.Arity4< //
          RandomAccessibleInterval<T>, //
          Dimensions, //
          Boolean, //
          OutOfBoundsFactory<T, RandomAccessibleInterval<T>>, //
          RandomAccessibleInterval<T> //
      > padOp, //
      @OpDependency(name = "filter.createFFTOutput") final Functions.Arity3< //
          Dimensions, //
          C, //
          Boolean, //
          RandomAccessibleInterval<C> //
      > createOp, //
      @OpDependency(name = "filter.fft") final Computers.Arity1< //
          RandomAccessibleInterval<T>, //
          RandomAccessibleInterval<C> //
      > fftOp, //
      final RandomAccessibleInterval<T> input, //
      final C fftType, //
      @Nullable long[] borderSize, //
      @Nullable Boolean fast //
    ) {

        if (fast == null) {
            fast = true;
        }
        // calculate the padded size
        long[] paddedSize = new long[input.numDimensions()];

        for (int d = 0; d < input.numDimensions(); d++) {
            paddedSize[d] = input.dimension(d);

            if (borderSize != null) {
                paddedSize[d] += borderSize[d];
            }
        }

        Dimensions paddedDimensions = new FinalDimensions(paddedSize);

        // create the complex output
        RandomAccessibleInterval<C> output = createOp.apply(paddedDimensions,
                fftType, fast);

        // pad the input
        RandomAccessibleInterval<T> paddedInput = padOp.apply(input,
                paddedDimensions, fast, null);

        // compute and return fft
        fftOp.compute(paddedInput, output);

        return output;

    }

    /**
     * Function that uses FFTMethods to perform a forward FFT
     *
     * @param <T> The data type of elements in the position space
     * @param fftOp performs the fast fourier transform
     * @param input the input data
     * @param borderSize the size of border to apply in each dimension
     * @param fast whether to perform a fast FFT; default true
     * @return the output
     * @implNote op names='filter.fft', priority='100.'
     */
    public static <T extends RealType<T>> RandomAccessibleInterval<ComplexFloatType> transformComplexType( //
        @OpDependency(name = "filter.fft") final Functions.Arity4< //
                RandomAccessibleInterval<T>, //
                ComplexFloatType, //
                long[], //
                Boolean, //
                RandomAccessibleInterval<ComplexFloatType> //
        > fftOp, //
        final RandomAccessibleInterval<T> input, //
        @Nullable long[] borderSize, //
        @Nullable Boolean fast //
    ) {
        return fftOp.apply(input, new ComplexFloatType(), borderSize, fast);
    }

    /**
     * Function that creates an output for FFTMethods FFT
     *
     * @param <T> the type of image elements
     * @param creator creates the new image
     * @param paddedDimensions the size of the output image
     * @param outType the type of the output image
     * @param fast whether to perform a fast FFT; default true
     * @return the output
     * @implNote op names='filter.createFFTOutput'
     */
    public static <T> Img<T> createFFTMethodsOutput(
        @OpDependency(name="create.img") BiFunction<Dimensions, T, Img<T>> creator, //
        Dimensions paddedDimensions, //
        T outType, //
        @Nullable Boolean fast
    ) {
        if (fast == null) {
            fast = true;
        }

        var dims  = getFFTDimensionsRealToComplex(fast, paddedDimensions);

        return creator.apply(dims, outType);
    }

    /**
     * Calculates padding size and complex FFT size for real to complex FFT
     *
     * @param fast if true calculate size for fast FFT
     * @param inputDimensions original real dimensions
     * @param paddedDimensions padded real dimensions
     * @param fftDimensions complex FFT dimensions
     */
    public static void dimensionsRealToComplex( //
        final boolean fast, //
        final Dimensions inputDimensions, //
        final long[] paddedDimensions, //
        final long[] fftDimensions //
    ) {
        if (fast) {
            FFTMethods.dimensionsRealToComplexFast(inputDimensions, paddedDimensions,
                    fftDimensions);
        }
        else {
            FFTMethods.dimensionsRealToComplexSmall(inputDimensions, paddedDimensions,
                    fftDimensions);
        }
    }

    /**
     * Calculates complex FFT size for real to complex FFT
     *
     * @param fast if true calculate size for fast FFT
     * @param inputDimensions original real dimensions
     * @return complex FFT dimensions
     */
    public static Dimensions getFFTDimensionsRealToComplex(final boolean fast,
                                                           final Dimensions inputDimensions)
    {
        final var paddedSize = new long[inputDimensions.numDimensions()];
        final var fftSize = new long[inputDimensions.numDimensions()];

        dimensionsRealToComplex(fast, inputDimensions, paddedSize, fftSize);

        return new FinalDimensions(fftSize);

    }
}
