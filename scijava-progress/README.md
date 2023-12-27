# SciJava Progress: an interrupt-based mechanism for progress reporting

This component provides a mechanism for progress-recording operations to notify interested parties of updates to progress.

## The `Progress` class

The `Progress` class is designed to be the middle-man between an operation that wishes to report its progress and a listener that wishes to be notified of progress updates. **Both** of these parties will interface with each other through `Progress` class.

### Accessing `Progress` as an operation

Suppose we have a `Function` that does some heavy calculation on a `List`, and wants to notify callers when it finishes one element, before it moves to the next:

```java
public class Foo implements Function<List<Integer>, List<Integer>> {

	@Override
	public Integer apply(List<Integer> in) {
		List<Integer> out = new ArrayList<>();
		for(int i = 0; i < in.size(); i++) {
			out.set(i, doTheComputation(in.get(i)));
		}
	}

}

```

Firstly, we add the bookkeeping steps:
1. Notify `Progress` that this Object wants to record its progress by calling `Progress.register(Object progressible)`. This can either be called within our `Function` (by passing `this`), or before the `apply` method is called (by passing our `Foo` instance).
2. Define what total progress means using `Progress.defineTotalProgress(int numStages, int numSubTasks)`. We make the distinction between `numStages` and `numSubtasks`:

  * `numStages` lets `Progress` know how many stages of computation will be performed within the current task
  * `numSubTasks` lets `Progress` know how many progress-reporting tasks **will be called** by the current task. Since they will report their own progress to `Progress`, `Progress` will automatically update the progress of our current task as the subtasks progress.
3. For each stage, `Progress.setStageMax(long max)` must be called so that `Progress` knows when each stage is completed, and moves onto the next.
4. Once the computation is complete, notify `Progress` that the task has completed by calling `Progress.complete()`.

Once these bookkeeping stages are added, we can then call `Progress.update`. Our `Function`, updating progress, would then look like:

```java
public class Foo implements Function<List<Integer>, List<Integer>> {

	@Override
	public Integer apply(List<Integer> in) {
		Progress.register(this);
		Progress.defineTotalProgress(1, 0);
		Progress.setStageMax(in.size());
		
		// compute
		List<Integer> out = new ArrayList<>();
		for(int i = 0; i < in.size(); i++) {
			out.set(i, doTheComputation(in.get(i)));
			Progress.update();
		}

		Progress.complete();
	}

}

```

Ops can also set their `status` through the `Progress` framework, using the method `Progress.setStatus(String status)`. In these situations, out `Function` would not need to call `Progress.defineTotalProgress()` or `Progress.setStageMax()`. Progressible code is, of course, allowed to set both status and progress.

### Accessing `Progress` as a listener

Progress is accessed by listeners using the `Progress.addListener(Object progressible, ProgressListener l)` method. This method must be called **before** `progressible`'s code is executed, and all executions of `progressible` will then be sent to `l`.

`ProgressListener` is, at its core, a `FunctionalInterface`, allowing `ProgressListener`s to be defined as lambdas. The functional method, `acknowledgeUpdate(Task t)`, is then called when **any execution** of `l` calls **either** `Progress.update()` or `Progress.setStatus()`. Below is an example of how one might write a `ProgressListener` for `Foo`:

```java
Foo f = new Foo();

// register a listener that prints progress to console
Progress.addListener(f, (t) -> System.out.println(t.progress()));

// call Foo
f.apply(Arrays.asList(1, 2, 3, 4);
```
