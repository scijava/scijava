========================================
Parameter Conversion for Developers
========================================

In this example, we explain parameter conversion to a developer audience. This page provides an overview of what parameter conversion involves, how it works, and how you can enable conversion for your own data types.

Basics
======

A :ref:`value <driving-values>` of SciJava Ops is flexibility, and flexibility is (in part) achieved through **parameter conversion**. At its core, parameter conversion allows *translation* of data stored in one data structure (e.g. an ImgLib2 ``RandomAccessibleInterval``) into a different data structure (e.g. an OpenCV ``Mat``) **on the fly**. This allows SciJava Ops to execute Ops backed by OpenCV code **on ImgLib2 data structures**.

.. figure:: https://media.scijava.org/scijava-ops/1.1.0/parameter-conversion-opencv.svg

At matching time, parameter conversion is invoked when an Op matches a user request in name and in Op type, but differing in individual parameter types. In these situations, it looks for ``engine.convert`` Ops that could potentially convert the user's provided inputs into the required Op inputs, and the same, in the other direction, for the output.

.. _original-op:

An example ``Function``
=======================

Suppose we have a ``Function`` Op that inherently operates on ``RandomAccessibleInterval<DoubleType>``\ s:

.. code-block:: java

  /**
   * Convolves an image with a kernel, returning the output in a new object
   *
   * @param image the input image
   * @param kernel the kernel
   * @return the convolution of {@code image} and {@code kernel}
   * @implNote op names="filter.convolve"
   */
  public static RandomAccessibleInterval<DoubleType> convolveNaive(
      final RandomAccessibleInterval<DoubleType> image,
      final RandomAccessibleInterval<DoubleType> kernel
        ) {
            // convolve convolve convolve //
        }

Suppose a user wants to use this Op with a small, fixed kernel, which for ease is written as a ``double[][]``. Without additional aid, they'd have to manually convert their ``double[][]`` into a ``RandomAccessibleInterval<DoubleType>``, requiring knowledge of how to do that and baking extra boilerplate into their workflow:


.. code-block:: java

    Img<DoubleType> image = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d} //
    };
    // transform double[][] into a RandomAccessibleInterval
    Img<DoubleType> kernelImg = ArrayImgs.doubles(kernel, 3, 3);
    var cursor = kernelImg.cursor();
    while (cursor.hasNext())
        cursor.next().set(kernel[cursor.getIntPosition(0)][cursor.getIntPosition(1)]);

    var result = ops.op("filter.convolve") //
        .input(image, kernelImg) //
        .outType(new Nil<RandomAccessibleInterval<DoubleType>>() {}) //
        .apply();

Ideally, the user could just pass their ``double[][]`` to their Op matching call directly. Parameter conversion enables this, through the use of ``engine.convert`` Ops written by developers.

An ``engine.convert`` Op
==============================

All ``engine.convert`` Ops are ``Function``\ s that are given user arguments and return a *translation* of that data into the type expected by the Op. For our example ``Function``, we want to convert *from* the user's ``double[][]`` into a ``RandomAccessibleInterval<DoubleType>``:

.. code-block:: java

    /**
     * @param image the input image, as a matrix in 2D array form
     * @return an image ({@link RandomAccessibleInterval}) whose values are equivalent to {@code image}'s
     *         values but converted to {@link DoubleType}s.
     * @implNote op names='engine.convert', type=Function
     */
    public static RandomAccessibleInterval<DoubleType> arrayToRAI(final double[][] image)
    {
        // Creates an empty image of doubles
        var img = ArrayImgs.doubles(image.length, image[0].length);
        var ra = img.randomAccess();
        // Deep copies the double[][] into the RAI
        for(int i = 0; i < image.length; i++) {
            for(int j = 0; j < image[0].length; j++) {
                ra.setPositionAndGet(i, j).set(image[i][j]);
            }
        }
        return img;
    }

Using this ``engine.convert`` Op, SciJava Ops can match our ``filter.convolve`` Op to the user's data, **without explicit translation**.

.. code-block:: java

    Img<DoubleType> image = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d} //
    };

    // Ideal case - no need to wrap to Img
    var result = ops.op("filter.convolve") //
        .input(image, kernel) //
        .outType(new Nil<RandomAccessibleInterval<DoubleType>>() {}) //
        .apply();

At runtime, the Op matcher will invoke the following steps:

* The ``Img<DoubleType> image`` is left alone, as it is already of the type expected by the Op.
* The ``double[][] kernel`` is converted to a ``RandomAccessibleInterval<DoubleType> kernel1`` using our ``engine.convert`` Op.
* The Op convolves ``image`` with ``kernel1``, returning an ``Img<DoubleType> result``.


Adding efficiency
=================

While the above ``engine.convert`` Op is *functional*, it may not be *fast* as the data size increases. This is due to the **copy** inherent in its execution, as the ``ArrayImg`` contains new data structures.

In such cases, devising methods to instead *wrap* user arguments will maximize performance and wow your users. In our case, we can refine our ``engine.convert`` Op to wrap user data, using the ``DoubleAccess`` interface of ImgLib2:

.. code-block:: java

  /**
   * @param input the input data
   * @return an image ({@link RandomAccessibleInterval}) backed by the input {@code double[][]}
   * @implNote op names='engine.convert', type=Function
   */
  public static RandomAccessibleInterval<DoubleType> arrayToRAIWrap(final double[][] input)
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

    double[][] image = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d} //
    };

    double[][] result = ops.op("filter.convolve") //
        .input(image, kernel) //
        .outType(double[][].class) //
        .apply();

Looking back at our :ref:`original Op<original-op>`, we would have to write an *additional* converter to turn the output ``RandomAccessibleInterval<DoubleType>`` back into a ``double[][]``:

.. code-block:: java

  /**
   * @param input the input data
   * @return a {@code double[][]} representation of the input image ({@link RandomAccessibleInterval})
   * @implNote op names='engine.convert', type=Function
   */
  public static double[][] raiToArray(final RandomAccessibleInterval<DoubleType> input)
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

#. Each ``double[][]`` is converted into a ``RandomAccessibleInterval<DoubleType>`` using our ``arrayToRAIWrap`` ``engine.convert`` Op.
#. The ``filter.convolve`` Op is invoked on the ``RandomAccessibleInterval<DoubleType>``\ s, returning a ``RandomAccessibleInterval<DoubleType>`` as output.
#. This output ``RandomAccessibleInterval<DoubleType>`` is converted into a ``double[][]`` using our ``raiToArray`` ``engine.convert`` Op.
#. The **converted** ``double[][]`` output is returned to the user.

The result is offering to the user a ``filter.convolve(input: double[][], kernel: double[][]) -> double[][]`` Op, even though we never wrote one!

Converting ``Computer`` and ``Inplace`` outputs
===============================================

Finally, consider our ``filter.convolve`` Op example, instead written as a ``Computer``.

.. code-block:: java

  /**
   * Convolves an image with a kernel, placing the result in the output buffer
   *
   * @param image the input image
   * @param kernel the kernel
   * @param output the result buffer
   * @implNote op names="filter.convolve" type=Computer
   */
  public static void convolveNaive(
      final RandomAccessibleInterval<DoubleType> image,
      final RandomAccessibleInterval<DoubleType> kernel,
      final RandomAccessibleInterval<DoubleType> output
        ) {
            // convolve convolve convolve //
        }

Suppose that again the user wants to call this Op using *only* ``double[][]``\ s:

.. code-block:: java

    double[][] image = ...
    // 3x3 averaging kernel
    double[][] kernel = { //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d}, //
        { 1/9d, 1/9d, 1/9d} //
    };
    double[][] result = new double[image.length][image[0].length];

    ops.op("filter.convolve").input(image, kernel).output(result).apply();

We will certainly need the ``engine.convert(input: double[][]) -> RandomAccessibleInterval<DoubleType>`` Op and the ``engine.convert(input: RandomAccessibleInterval<DoubleType>) -> double[][]`` Op we wrote above, however if we follow the same procedure with :ref:`Functions <function-output>`, the ``result`` array they provided will be empty/unmodified. This is because our ``raiToArray` ``engine.convert`` Op we wrote above *creates a new ``double[][]``*. Writing ``engine.convert`` Ops as wrappers is ideal, but in cases like this may not be possible (i.e. we can't create a custom ``double[][]`` implementation).

Because SciJava Ops cannot guarantee that ``engine.convert`` Ops wrap user arguments, an additional step is required for parameter conversion with ``Computer`` Ops. This is done by calling an ``engine.copy`` Op to copy the converted output *back into the user's object*. **If you want to enable parameter conversion** on ``Computer``\ s or ``Inplace``\ s, **you must implement** an ``engine.copy`` identity Op for your data type in addition to any ``engine.convert`` Ops. Because there is no way to know how Ops will be implemented (and ``Computer``\s do make a large portion of current Ops) **this is highly recommended**.

Below is an ``engine.copy`` Op that would store the converted Op's output ``double[][]`` back into the user's Object:

.. code-block:: java

  /**
   * Copy one {@code double[][]} to another.
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

#. Each ``double[][]`` is converted into a ``RandomAccessibleInterval<DoubleType>`` using our ``arrayToRAIWrap`` ``engine.convert`` Op.
#. The ``filter.convolve`` Op is invoked on the ``RandomAccessibleInterval<DoubleType>``\ s, returning a ``RandomAccessibleInterval<DoubleType>`` as an output.
#. The output ``RandomAccessibleInterval<DoubleType>`` is converted into a ``double[][]`` using our ``raiToArray`` ``engine.convert`` Op.
#. The **converted** output ``double[][]`` is *copied* back into the user's ``double[][]`` buffer using our ``copyDoubleMatrix`` ``engine.copy`` Op.

Summary
=======

All in all, you can enable parameter conversion from type ``A`` to type ``B`` by providing the following Ops:

* An ``engine.convert(input: A) -> B`` for input conversion
* An ``engine.convert(input: B) -> A`` for output conversion
* An ``engine.copy(converted_output: B, user_buffer: B)`` for ``Computer``\ s and ``Inplace``\ s, to move the converted output into the user's buffer object.

Note that, in the process of creating your ``engine.convert`` ``Function`` Ops, you'll likely want to write some ``engine.create`` Ops that could produce objects of type ``B``. In addition to making your ``engine.convert`` Ops more granular by using them as Op dependencies, but they'll additionally help enable features like Op adaptation.

Beyond this, it would also be helpful to ensure that an ``engine.copy(converted_output: A, user_buffer: A)`` Op exists, such that users can also call *your* ``Computer`` and ``Inplace`` Ops using objects of type ``A``.
