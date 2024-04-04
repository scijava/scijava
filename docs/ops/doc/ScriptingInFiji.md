# Scripting in Fiji

Using SciJava Ops within scripts unlocks the most powerful aspects of Ops. The following page will explain how you can write a script in Fiji's Script Editor that utilizes Ops for image processing.

## Obtaining an OpEnvironment

To run Ops, scripts require an `OpEnvironment`. The easiest way to obtain an `OpEnvironment` with all available Ops is to declare an `OpEnvironment` as a script parameter:

```text
#@ OpEnvironment ops
```

## Obtaining inputs

Scripts using SciJava Ops obtain inputs like any other SciJava script, and the lines below will provide us with an `Img` input parameter and an `Img` output parameter, as well as a `ThreadService` which we will use later.

```text
#@ ThreadService ts
#@ Img imgInput
#@output Img out
```

For more information on SciJava scripting parameters, please visit [this page](https://imagej.net/scripting/parameters).

## Calling Ops

The OpBuilder syntax should be used to retrieve and execute Ops from the `OpEnvironment`. The following line executes a Gaussian Blur on an input image using a `filter.gauss` Op:
```text
out = ops.op("filter.gauss").arity2().input(imgInput, new Double(3.0)).apply()
```

## Putting it all together

The below script can be pasted into the Script Editor. **Ensure that the Script Editor is configured to run a Groovy script**.

```text
#@ OpEnvironment ops
#@ ThreadService ts
#@ Img imgInput
#@output Img out

// Call some Ops!
out = ops.op("filter.gauss").arity2().input(imgInput, new Double(3.0)).apply()
```
