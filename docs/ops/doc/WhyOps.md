# Why Use SciJava Ops?

The fundamental goal of SciJava Ops is to fit the "best" algorithm possible to each task. Historically, identifying and applying the "best" algorithm has been difficult for a variety of reasons:
* **Technology iterates quickly:** the "best" algorithm five years ago may be replaced by "better" algorithms within new libraries, in different programming languages, incompatible with established workflows.
* **Hardware dependence limits reuse:** as analysis increasingly migrates to GPU-based calculations, operating environments proliferate and algorithms become less portable.
* **Algorithm libraries are fragmented:** actually *finding* the "best" algorithm is no simple task, as diverse implementations may exist across any number of programming languages and documentation styles. 

SciJava Ops takes strides to ease these burdens by separating the *what* ("I want to perform a gaussian blur on this image with this sigma value) from the *how* (using scikit-image on zarr arrays). By creating these abstractions, we move towards a single unified, standardized mechanism for applying algorithms. In such an environment, portable workflows can be created quickly and new technologies can be integrated seamlessly.

## What are the driving values of SciJava Ops?

1. **Consistency**:  All Ops are called in the same way, regardless of the mechanisms used by the underlying framework. This means that you don't have to learn Python to call Ops written in Python, but it also means that you could pass the output from an Op written in Python to an Op written in Java, all with the same syntax!
2. **Reusability**: Ops extends Java's mantra of "write once, run anywhere" to image processing algorithms. Algorithms written for the SciJava Ops framework are usable as-is from any SciJava-compatible software project including Fiji, or from Python using PyImageJ. 
3. **Reproducibility**: Ops are deterministic: calling the same op twice with the same arguments yields the same result, always. Ops are also versioned, meaning that if you use the same Op environment with the same library versions, you will always have reproducible workflows.
4. **Flexibility**: Through adaptation and simplification pathways, Ops can be applied to all kinds of inputs, relaxing considerations for data structures. For example, binary numerical Ops are automatically looped and parallelized to operate on images. New data types extending core interfaces can be supported immediately, without rewriting existing algorithms.
5. **Safety**: An op may consist of any number of strongly typed inputs, and calls to access those ops can be as specific as desired. This allows analyst users to use ops without regard for data structure, while developers can rely on the type safety guarantees needed for optimization.
6. **Extensibility**: Ops provides a mechanism for incorporating existing algorithm implementations into the framework code-free. Existing ops can always be extended in new directions or specialized for particular inputs.
7. **Performance**: The Ops framework provides a means to override any general-but-slow op with a faster-but-more-specific alternative and the execution framework adds minimal overhead.

## How do we integrate all of these different libraries and data structures?

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

## Isn't SciJava Ops just another algorithm library?

While it may currently seem that way, SciJava Ops is designed to *wrap* existing algorithms, not to create new ones (although SciJava Ops makes that easy too!)

To facilitate the inclusion of existing algorithms, SciJava Ops exposes a rich API to integrate Ops from arbitrary sources, spanning libraries and programming languages.

