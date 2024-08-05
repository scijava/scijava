========================================
Parameter Conversion for Developers
========================================

In this example, we show how Ops developers can implement parameter conversion to enable seamless interoperability between Ops utilizing different data structures.between

Basics
======

A key :ref:`value <driving-values>` of SciJava Ops is flexibility, and flexibility is (in part) achieved through **parameter conversion**. At its core, parameter conversion allows *translation* of data stored in one data structure (e.g. an ImgLib2 ``RandomAccessibleInterval``) into a different data structure (e.g. an OpenCV ``Mat``) **on the fly**. This allows SciJava Ops to execute Ops backed by OpenCV code **on ImgLib2 data structures**.

.. figure:: https://media.scijava.org/scijava-ops/1.0.1/parameter-conversion-opencv.svg

At matching time, parameter conversion is invoked when an Op matches a user request in name and in Op type, but differing in individual parameter types. In these situations, it looks for ``engine.convert`` Ops that could potentially convert the user's provided inputs into the required Op inputs, and the same, in the other direction, for the output.

A toy example
=============

Suppose we have an Op that inherently operates on ``RandomAccessibleInterval<DoubleType>``\ s:

.. code-block:: java

	/**
	 * Conolves an image with a kernel, returning the output in a new object
	 *
	 * @param input the input data
	 * @param kernel the kernel
	 * @return the convolution of {@code input} and {@code kernel}
	 * @implNote op names="filter.convolve"
	 */
	public static RandomAccessibleInterval<DoubleType> convolveNaive(
			final RandomAccessibleInterval<DoubleType> input,
			final RandomAccessibleInterval<DoubleType> kernel
        ) {
            // convolve convolve convolve //
        }

This Op might work well, however if users have a small kernel that is *only* used for this Op, they may find it frustrating to represent that data as a ``RandomAccessibleInterval``. Fortunately, SciJava Ops allows us to write ``engine.convert`` Ops, so users can pass their data in a data structure more convenient for them.


.. code-block:: java

    Img<DoubleType> in = ...
    // 3x3 averaging kernel
    double m = 1/9;
    double[] data = new double[] { //
        m, m, m, //
        m, m, m, //
        m, m, m //
    };
    // Not ideal
    Img<DoubleType> kernel = ArrayImgs.doubles(data, new long[] {3, 3});

    var result = ops.op("filter.convolve").input(in, kernel).apply();

In this case, users might find it nicer to specify their kernel as a ``double[][]``, which is much easier for users to construct

.. code-block:: java

    Img<DoubleType> in = ...
    // 3x3 averaging kernel
    double m = 1/9;
    double[] kernel = new double[][] { //
        new double[] { m, m, m}, //
        new double[] { m, m, m}, //
        new double[] { m, m, m} //
    }

    // Ideal case - no need to wrap to Img
    var result = ops.op("filter.convolve").input(in, kernel).apply();

The only step for us as the developer is to tell SciJava Ops that it can convert ``double[][]``\ s to ``RandomAccessibleInterval<DoubleType>``\ s, which we do with ``engine.convert`` Ops.

A simple ``engine.convert`` Op
==============================

All ``engine.convert`` Ops are simple ``Function``\ s, that take as input the user argument to the Op, and return a *translation* of that data into the type expected by Ops. In our case, we want to convert *from* the user's ``double[][]`` into a ``RandomAccessibleInterval<DoubleType>``:

.. code-block:: java

    /**
     * @param input the input data
     * @return an output image whose values are equivalent to {@code input}s
     *         values but whose element types are {@link BitType}s.
     * @implNote op names='engine.convert', type=Function
     */
    public static RandomAccessibleInterval<DoubleType> arrayToDoubles(final double[][] input)
    {
        // Creates an empty image of doubles
        var img = ArrayImgs.doubles(input.length, input[0].length);
        var ra = img.randomAccess();
        // Deep copies the double[][] into the RAI
        for(int i = 0; i < input.length; i++) {
            for(int j = 0; j < input[0].length; j++) {
                ra.setPositionAndGet(i, j).set(input[i][j]);
            }
        }
        return img;
    }

This Op, discovered through the SciJava Ops Indexer, is **all** that is needed to make the execution pattern we want functional.


Adding efficiency
=================

While the above ``engine.convert`` Op is *functional*, it may not be *fast* as the data size increases. This is due to the **copy** inherent in its execution, as the ``ArrayImg`` contains new data structures.

In such cases, devising methods to *wrap* the user arguments, instead of *copying* it, will maximize performance and wow your users. In our case, we can refine our ``engine.convert`` Op to wrap user data, using the ``DoubleAccess`` interface of ImgLib2:

.. code-block:: java

	/**
	 * @param input the input data
	 * @return an output image whose values are equivalent to {@code input}s
	 *         values but whose element types are {@link BitType}s.
	 * @implNote op names='engine.convert', type=Function
	 */
	public static RandomAccessibleInterval<DoubleType> arrayToDoubles(final double[][] input)
	{
		// Wrap 2D array into DoubleAccess usable by ArrayImg
		var access = new DoubleAccess() {

			private final int rowSize = input[0].length;

			@Override
			public double getValue(int index) {
				var row = index / rowSize;
				var col = index % rowSize;
				return input[row][col];
			}

			@Override
			public void setValue(int index, double value) {
				var row = index / rowSize;
				var col = index % rowSize;
				input[row][col] = value;
			}
		};
		return ArrayImgs.doubles(access, input.length, input[0].length);
	}
