# Concepts

This page is designed to help *Ops users* understand the SciJava Ops concepts powering the framework.

## Ops
An **algorithm** is a mathematical routine that transforms, interrogate, or refines input values into output values, and are used throughout scientific computing.

SciJava Ops attempts to abstract algorithm implementations into a more abstract, language independent form, called an **Op**. Ops have:
* a **name**, defining its purpose. An Op's name describes *what* the Op does, but not *how* the Op does it. 
  * An Op adding two Java `Integer`s would be named `math.add`
  * An Op convolving an image with a kernel might be named `filter.convolve`, *regardless* of whether it performs that convolution in the physical or frequency domain.
* a number of **inputs** and **outputs**, defined by language-specific **types**.
  * A `math.add` Op might take two Java `Integer` inputs and return a Java `Integer` output
  * A `filter.convolve` Op might take two OpenCV `Mat` inputs, storing the convolved result into an OpenCV `Mat` output *buffer*.
* behavior adhering to a **functional interface** defined below.

## Computers

A **Computer** accepts `n` immutable inputs and a **container** as input parameters. Their singular method, `compute()`, executes an algorithm and *stores the result within* the container parameter, overwriting its contents.

For example, consider the following pseudocode, which replaces the contents of a list of integers `outList` with the elementwise addition of integer `val` to the list of integers `inList`:
```text
math.add(inList: list[int], val: int, outList: list[int]):
  outList.clear()
  for i in 1..size(inList):
    outList[i] = inList[i] + val
  
```

## Functions

A **Function** accepts `n` immutable inputs as parameters. Their singular method, `apply()`, executes an algorithm and *returns* a new output object to the user.

For example, consider the following pseudocode, which returns a list of integers with the elementwise addition of integer `val` to the list of integers `inList`:
```text
math.add(inList: list[int], val: int) -> list[int]:
  outList = list()
  for i in 1..size(inList):
    outList[i] = inList[i] + val
  return outList
```

Creating a new output object on every execution is a double-edged sword: on one hand, it can be convenient to delegate creation to the Op. On the other hand, output creation can be expensive and unnecessary if the Op is called many times.

## Inplaces

A **Inplace** accepts `n` input parameters, **only one** of which is mutable. Their singular method, `mutate()`, executes an algorithm and *overwrites* the single mutable parameter with the algorithm results.

For example, consider the following pseudocode, which mutates a list of integers `inList` by adding the integer `val` to each:
```text
math.add(inList: list[int], val: int):
  for i in 1..size(inList):
    inList[i] = inList[i] + val
```

A major advantage of Inplaces is the efficiency gained by the absence of an output buffer. On the other hand, the input data is lost through computation.

## OpEnvironment

The Op environment collects all available Ops, and provides access to their functionality. `OpEnvironment` objects are instantiated using the line shown below, and once created will contain all available Ops:
```java
OpEnvironment ops = OpEnvironment.build();
```

With an `OpEnvironment`, users can:
* Find Ops, using the `OpEnvironment.help()` API (see [Searching For Ops](SearchingForOps.md))
* Execute Ops, using the `OpEnvironment.op()` API (see [Calling Ops](CallingOps.md), and the `OpBuilder` section below.)

## OpBuilder

The `OpBuilder` constructs an Op request piece-by-piece using many function calls. The name Op Builder draws upon the [builder pattern](https://refactoring.guru/design-patterns/builder) it follows, and an Op Builder construction is started via the method `OpEnvironment.op()`.

The Op Builder syntax is extremely powerful, as learning to use it enables the execution of any Op in the Op environment, regardless of the language or library that it comes from. See [Calling Ops](CallingOps.md) for a tutorial on the Op Builder!
