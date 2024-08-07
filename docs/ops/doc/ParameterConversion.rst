========================================
Parameter Conversion for Developers
========================================

In this example, we explain parameter conversion to a developer audience. This page provides an overview of what parameter conversion involves, how it works, and how you can enable conversion for your own data types.

Basics
======

A :ref:`value <driving-values>` of SciJava Ops is flexibility, and flexibility is (in part) achieved through **parameter conversion**. At its core, parameter conversion allows *translation* of data stored in one data structure (e.g. an ImgLib2 ``RandomAccessibleInterval``) into a different data structure (e.g. an OpenCV ``Mat``) **on the fly**. This allows SciJava Ops to execute Ops backed by OpenCV code **on ImgLib2 data structures**.

.. figure:: https://media.scijava.org/scijava-ops/1.0.1/parameter-conversion-opencv.svg

At matching time, parameter conversion is invoked when an Op matches a user request in name and in Op type, but differing in individual parameter types. In these situations, it looks for ``engine.convert`` Ops that could potentially convert the user's provided inputs into the required Op inputs, and the same, in the other direction, for the output.

.. _original-op:

An example ``Function``
=======================

Suppose we have a ``Function`` Op that inherently operates on ``RandomAccessibleInterval<DoubleType>``\ s:

.. code-block:: java

	/**
	 * Convolves an image with a kernel, returning the output in a new object
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
    double[][] kernel = { //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9} //
    };
    // transform double[][] into a RandomAccessibleInterval
    Img<DoubleType> kernel = ArrayImgs.doubles(data, new long[] {3, 3});
    var cursor = kernel.cursor();
    while(cursor.hasNext())
        cursor.next().set(kernel[cursor.getIntPosition(0)][cursor.getIntPosition(1)]);

    var result = ops.op("filter.convolve") //
        .input(in, kernel) //
        .outType(new Nil<RandomAccessibleInterval<DoubleType>>() {}) //
        .apply();

In this case, users might find it nicer to specify their kernel as a ``double[][]``, which is easier for users to construct. The only step for us as the developer is to tell SciJava Ops that it can convert ``double[][]``\ s to ``RandomAccessibleInterval<DoubleType>``\ s, which we do with ``engine.convert`` Ops.

An ``engine.convert`` Op
==============================

All ``engine.convert`` Ops are simple ``Function``\ s, that take as input the user argument to the Op, and return a *translation* of that data into the type expected by Ops. In our case, we want to convert *from* the user's ``double[][]`` into a ``RandomAccessibleInterval<DoubleType>``:

.. code-block:: java

    /**
     * @param input the input data
     * @return a {@link RandomAccessibleInterval}, containing the data stored in {@code input}
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

This Op, discovered through the SciJava Ops Indexer, is enables the execution pattern we want.

.. code-block:: java

    Img<DoubleType> in = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9} //
    };

    // Ideal case - no need to wrap to Img
    var result = ops.op("filter.convolve") //
        .input(in, kernel) //
        .outType(new Nil<RandomAccessibleInterval<DoubleType>>() {}) //
        .apply();

At runtime, the Op matcher will invoke the following steps:

* The ``Img<DoubleType> input`` is left alone, as it is a ``RandomAccessibleInterval<DoubleType>``
* The ``double[][] kernel`` is converted to a ``RandomAccessibleInterval<DoubleType> kernel1`` using our ``engine.convert`` Op.
* The Op convolves ``input1`` with ``kernel1``, returning an ``Img<DoubleType>``
* The ``RandomAccessibleInterval<DoubleType> output1`` is converted to a ``double[][] output2``
* Nothing is returned


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

.. _function-output:

Converting ``Function`` outputs
===============================

Now, imagine that the user wished to execute the Op using **only** ``double[][]``\ s. In other words, they have a ``double[][] input``, a ``double[][] kernel``, and want back a ``double[][]`` containing the result:

.. code-block:: java

    double[][] in = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9} //
    };

    // Ideal case - no need to wrap to Img
    double[][] result = ops.op("filter.convolve") //
        .input(in, kernel) //
        .outType(double[][].class) //
        .apply();

Looking back at our :ref:`original Op<original-op>`, we would have to write an *additional* converter, to turn the output ``RandomAccessibleInterval<DoubleType>`` into a ``double[][]``:

.. code-block:: java

	/**
	 * @param input the input data
	 * @return a {@code double[][]}, containing the data stored in {@code input}
	 * @implNote op names='engine.convert', type=Function
	 */
	public static double[][] doublesToArray(final RandomAccessibleInterval<DoubleType> input)
	{
        // Create the array
		var width = input.dimension(0);
		var height = input.dimension(1);
		var result = new double[(int) width][(int) height];

		// Unfortunately, we have to deep copy here
		var ra = input.randomAccess();
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				result[i][j] = ra.setPositionAndGet(i, j).get();
			}
		}
		return result;
	}

When the user tries to invoke our ``filter.convolve`` ``Function`` Op on all ``double[][]``\ s, the following happens:

#. Each ``double[][]`` is converted into a ``RandomAccessibleInterval<DoubleType>`` using our ``engine.convert(in: double[][]) -> RandomAccessibleInterval<DoubleType>`` Op.
#. The ``filter.convolve`` Op is invoked on the ``RandomAccessibleInterval<DoubleType>``\ s, returning a ``RandomAccessibleInterval<DoubleType>`` as an output.
#. The output ``RandomAccessibleInterval<DoubleType>`` is converted into a ``double[][]`` using our ``engine.convert(in: RandomAccessibleInterval<DoubleType>) -> double[][]`` Op.
#. The **converted** ``double[][]`` output is returned to the user.

Converting ``Computer`` and ``Inplace`` outputs
===============================================

Consider our ``filter.convolve`` Op example, instead written as a ``Computer``.

.. code-block:: java

	/**
	 * Convolves an image with a kernel, placing the result in the output buffer
	 *
	 * @param input the input data
	 * @param kernel the kernel
	 * @param output the result buffer
	 * @implNote op names="filter.convolve"
	 */
	public static void convolveNaive(
			final RandomAccessibleInterval<DoubleType> input,
			final RandomAccessibleInterval<DoubleType> kernel,
			final RandomAccessibleInterval<DoubleType> output
        ) {
            // convolve convolve convolve //
        }

If we want to call this Op using *only* ``double[][]``\ s, as so, we will certainly need the ``engine.convert(in: double[][]) -> RandomAccessibleInterval<DoubleType>`` Op and the ``engine.convert(in: RandomAccessibleInterval<DoubleType>) -> double[][]`` Op we wrote above:

.. code-block:: java

    double[][] in = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9}, //
        { 1/9, 1/9, 1/9} //
    };
    double[][] result = new double[in.length][in[0].length];

    ops.op("filter.convolve").input(in, kernel).output(result).apply();

However, if we follow the same procedure with :ref:`Functions <function-output>`, the user will not see the output in their ``result`` object. This is because many ``engine.convert`` Ops (including the one we wrote :ref:`above <function-output>` for our ``RandomAccessibleInterval``, and the original converter we wrote for the input ``double[][]``\ s), make *deep copies* instead of wrapping the user arguments. Indeed, SciJava Ops cannot guarantee that ``engine.convert`` Ops wrap user arguments, so without an additional step parameter conversion would not work for output buffers.

SciJava Ops remedies the situation by, after invoking the original Op and converting the output, calling an ``engine.copy`` Op to store the converted output *back into the user's object*. Thus, **if you want to enable parameter conversion on** ``Computer``\ **s or** ``Inplace``\ **s, you'll need** ``engine.copy`` **Ops too**.

Below is an ``engine.copy`` Op that would store the converted Op's output ``double[][]`` back into the user's Object:

.. code-block:: java

	/**
	 * Convolves an image with a kernel, placing the result in the output buffer
	 *
	 * @param opOutput the {@code double[][]} converted from the Op output
	 * @param userBuffer the original {@code double[][]} provided by the user
	 * @implNote op names="engine.copy" type=Computer
	 */
	public static void copyDoubleMatrix(
			final double[][] opOutput,
			final double[][] userBuffer
	) {
		for(int i = 0; i < opOutput.length; i++) {
			System.arraycopy(opOutput[i], 0, userBuffer[i], 0, opOutput[i].length);
		}
	}

When the user tries to invoke our ``filter.convolve`` ``Computer`` Op on all ``double[][]``\ s, the following happens:

#. Each ``double[][]`` is converted into a ``RandomAccessibleInterval<DoubleType>`` using our ``engine.convert(in: double[][]) -> RandomAccessibleInterval<DoubleType>`` Op.
#. The ``filter.convolve`` Op is invoked on the ``RandomAccessibleInterval<DoubleType>``\ s, returning a ``RandomAccessibleInterval<DoubleType>`` as an output.
#. The output ``RandomAccessibleInterval<DoubleType>`` is converted into a ``double[][]`` using our ``engine.convert(in: RandomAccessibleInterval<DoubleType>) -> double[][]`` Op.
#. The **converted** output ``double[][]`` is *copied* back into the user's ``double[][]`` buffer.

Summary
=======

All in all, you can enable parameter conversion from type ``A`` to type ``B`` by providing the following Ops:

* An ``engine.convert(input: A) -> B`` for input conversion
* An ``engine.convert(input: B) -> A`` for output conversion
* An ``engine.copy(converted_output: B, user_buffer: B)`` for ``Computer``\ s and ``Inplace``\ s, to move the converted output into the user's buffer object.

Note that, in the process of creating your ``engine.convert`` ``Function`` Ops, you'll likely want to write some ``engine.create`` Ops that could produce objects of type ``B``. In addition to making your ``engine.convert`` Ops more granular by using them as Op dependencies, but they'll additionally help enable features like Op adaptation.

Beyond this, it would also be helpful to ensure that an ``engine.copy(converted_output: A, user_buffer: A)`` Op exists, such that users can also call *your* ``Computer`` and ``Inplace`` Ops using objects of type ``A``.