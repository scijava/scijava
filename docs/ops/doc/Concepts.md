# Concepts

This page is designed to help *Ops users* understand the SciJava Ops concepts powering the framework.

## Ops
An **algorithm** is a mathematical routine that transforms, interrogate, or refines input values into output values. Algorithms are used throughout scientific computing, and the fundamental [purpose](Purpose.rst) of SciJava Ops is to facilitate their application.

We do that by providing a framework for consistently defining and invoking algorithm implementations as **Ops**. Ops have:
* a **name**, establishing its purpose. An Op's name describes *what* the Op does, but not *how* the Op does it.
  * An Op adding two numbers would be named `math.add`, as would an Op adding two images
  * An Op convolving an image with a kernel might be named `filter.convolve`, *regardless* of whether it performs that convolution in the physical or frequency domain.
* a number of **inputs** and **outputs**, defined by language-specific **types**.
  * A `math.add` Op might take two Java `Integer` inputs and return a Java `Integer` output
  * A `filter.convolve` Op might take two OpenCV `Mat` inputs, storing the convolved result into an OpenCV `Mat` output *buffer*.
* behavior adhering to one of the three **Op types** defined below.

## Computers

**Computer Ops** accept any number of immutable inputs and exactly one mutable **container** as parameters. When the algorithm they define is executed, *the result is stored within* the container parameter, overwriting its contents. The initial contents of the container do not affect computation.

For example, consider the following pseudocode, which populates a list of integers (`outList`) with the elementwise addition of an integer constant (`value`) to a provided list of integers (`inList`):
```text
math.add(inList: list[int], value: int, outList: list[int]):
  outList.clear()
  for i in 1..size(inList):
    outList[i] = inList[i] + value
```

## Functions

**Function Ops** accept any number of immutable inputs as parameters. When the algorithm they define is executed, *the result is returned* as a new output to the user.

For example, consider the following pseudocode, which creates a list of integers from the elementwise addition of an integer constant (`value`) to the provided list of integers (`inList`):
```text
math.add(inList: list[int], value: int) -> list[int]:
  outList = list()
  for i in 1..size(inList):
    outList[i] = inList[i] + value
  return outList
```

An advantage of Functions is their convenience. However, output creation can be expensive and unnecessary if the Op is called many times.

## Inplaces

**Inplace Ops** accept any number of input parameters, **exactly one** of which is mutable. When the algorithm they define is executed, *the mutable parameter is modified* as a result of computation.

For example, consider the following pseudocode, which modifies a provided list of integers (`inList`) by adding an integer constant (`value`) to each element:
```text
math.add(inList: list[int], value: int):
  for i in 1..size(inList):
    inList[i] = inList[i] + value
```

An advantage of Inplaces is both the convenience and savings (in time and memory) of not creating an output. On the other hand, the input data is lost through computation, and some algorithms require the input to remain unmodified (e.g. for neighborhood calculations).

## OpEnvironment

The **Op environment** collects all available Ops, and serves as a gateway for accessing their functionality. Obtaining an `OpEnvironment` is the first step for any Ops use. 

With an `OpEnvironment`, users can:
* Find Ops, using the `OpEnvironment.help()` API (see [Searching For Ops](SearchingForOps.md))
* Execute Ops, using the `OpEnvironment.op()` API (see [Calling Ops](CallingOps.md))

## OpBuilder

The `OpBuilder` constructs an Op request piece-by-piece using many function calls. The name Op Builder draws upon the [builder pattern](https://refactoring.guru/design-patterns/builder) it follows, and an Op Builder construction is started via the method `OpEnvironment.op()`.

The Op Builder syntax is extremely powerful, as learning to use it enables the execution of any Op in the Op environment, regardless of the language or library that it comes from. See [Calling Ops](CallingOps.md) for a tutorial on the Op Builder!
