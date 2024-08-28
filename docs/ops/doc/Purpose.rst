=======
Purpose
=======

The fundamental goal of SciJava Ops is to fit the "best" algorithm possible to each task. Historically, identifying and applying the "best" algorithm has been difficult for a variety of reasons:

* **Technology iterates quickly:** the "best" algorithm five years ago may be replaced by "better" algorithms within new libraries, in different programming languages, incompatible with established workflows.
* **Hardware dependence limits reuse:** as analysis increasingly migrates to GPU-based calculations, operating environments proliferate and algorithms become less portable.
* **Algorithm libraries are fragmented:** actually *finding* the "best" algorithm is no simple task, as diverse implementations may exist across any number of programming languages and documentation styles.

SciJava Ops takes strides to ease these burdens by separating the *what* ("I want to perform a gaussian blur on this image with this sigma value") from the *how* ("using scikit-image on zarr arrays"). By creating these abstractions, we move towards a single unified, standardized mechanism for applying algorithms. In such an environment, portable workflows can be created quickly and new technologies can be integrated seamlessly.

.. _driving-values:

Driving Values
==============

#. **Consistency**:  All Ops are called in the same way, regardless of the mechanisms used by the underlying framework. This means that you don't have to learn Python to call Ops written in Python, but it also means that you could pass the output from an Op written in Python to an Op written in Java, all with the same syntax!
#. **Reusability**: Ops extends Java's mantra of "write once, run anywhere" to image processing algorithms. Algorithms written for the SciJava Ops framework are usable as-is from any SciJava-compatible software project including Fiji, or from Python using PyImageJ.
#. **Flexibility**: Through adaptation and conversion pathways, Ops can be applied to all kinds of inputs, relaxing considerations for data structures. For example, binary numerical Ops are automatically looped and parallelized to operate on images. New data types extending core interfaces can be supported immediately, without rewriting existing algorithms.
#. **Safety**: An op may consist of any number of strongly typed inputs, and calls to access those ops can be as specific as desired. This allows analyst users to use ops without regard for data structure, while developers can rely on the type safety guarantees needed for optimization.
#. **Extensibility**: Ops provides a mechanism for incorporating existing algorithm implementations into the framework code-free. Existing ops can always be extended in new directions or specialized for particular inputs.
#. **Performance**: The Ops framework provides a means to override any general-but-slow op with a faster-but-more-specific alternative and the execution framework adds minimal overhead.


How does SciJava Ops compare with ImageJ Ops?
=============================================

As an evolution of the `ImageJ Ops`_ project, SciJava Ops adds many new features and makes many improvements. We first highlight how users can benefit by adopting SciJava Ops; we then provide benefits for developers who choose to write Ops to be exposed within the framework.

User Features
-------------

#. |Faster Matching|_: SciJava Ops is able to match Ops to user requests dramatically faster than ImageJ Ops, and caches matching requests to provide virtually no overhead on re-requests.
#. **Precise Matching**: SciJava Ops uses the :doc:`Op builder pattern <CallingOps>` to obtain precise knowledge about the user's desired Op inputs and outputs, leading to precise Op matches.

    * *ImageJ Ops's* ``OpService.run`` *mechanism is vulnerable to uncertain return values and frequent result casting*.
#. **Simple API**: In SciJava Ops, each Op is **either** a ``Function``, a ``Computer``, or an ``Inplace``, containing a **single method** that executes all functionality.

    * *In ImageJ Ops, an Op might implement many different interfaces and expose redundant API*.
#. **Automatic Progress Updates**: SciJava Ops automatically records Op executions within the SciJava Progress framework, providing a simple mechanism for monitoring script execution.

    * *In ImageJ Ops, Op execution is silent unless explicitly defined, and very few Ops do so.*

Developer Features
------------------
#. **Zero-code Op Declaration**: Executable code can be registered as an Op purely through YAML specifications, allowing the induction of entire external libraries without upstream edits.

    * *In ImageJ Ops, a separate class is required to wrap each algorithm from an external source*.
#. **Minimal Op Boilerplate**: Developers can choose to write Ops as Java classes, methods, or fields, depending on Op requirements.

    * *In ImageJ Ops, a distinct class, including annotations and interface inheritance, is necessary for each Op*.
#. **Op Adaptation**: SciJava Ops can automatically iterate pixelwise algorithms across entire images, and can automatically create output buffers if the user does not provide one, **without any additional code**.

    * *In ImageJ Ops, developers were required to program each additional way to call an Op to provide the same flexibility*.
#. **Full support for Java Generics**: Generic typing information baked into Java code enables SciJava Ops to consider each parameter's **generic** type in determining a match. This functionality allows developers to write separate Ops to specialize in increasingly narrow parameter types.

    * *In ImageJ Ops, developers were required to encapsulate functionality within delegation Ops*.


How do we integrate all of these different libraries and data structures?
=========================================================================

Core to SciJava Ops is the observation that algorithmic data structures generally fall into a one of a set of "forms", including:

* scalars
* sets
* tensors
* images
* ...

To enable the goals described above, SciJava Ops provides an interface for seamless conversion between implementations of different forms, which can be optimized for performance and/or efficiency. This allows us to write scripts calling algorithms in different languages and libraries.

To enable a uniform interface for algorithm execution, we extend the notion of "form"s to algorithms as well, as human intuition is good at determining whether an algorithm is a "gaussian blur", a "convolution", a "segmentation", and so on.

Within each algorithm "form", there are several different sub-algorithms - for example, for a gaussian blur, you might see algorithms requiring

* an image and a single sigma
* an image and a sigma per dimension
* an image and a shape over which to compute the gaussian blur
* ...

It is SciJava Ops' goal to collect all algorithms into such descriptions, and to provide a uniform execution pathway for each - hiding the complexities of each individual algorithm library.

Isn't SciJava Ops just another algorithm library?
-------------------------------------------------

While it may currently seem that way, SciJava Ops is designed to *wrap* existing algorithms, not to create new ones (although SciJava Ops makes that easy too!)

To facilitate the inclusion of existing algorithms, SciJava Ops exposes a rich API to integrate Ops from arbitrary sources, spanning libraries and programming languages.



.. _`ImageJ Ops`: https://imagej.net/libs/imagej-ops/

.. HACK: bold text link - see https://stackoverflow.com/a/63394243
.. _Faster Matching: Benchmarks.html
.. |Faster Matching| replace:: **Faster Matching**
