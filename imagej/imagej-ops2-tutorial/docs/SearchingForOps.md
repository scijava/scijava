# Searching for Ops in the Environment

As the `OpEnvironment` is fully extensible, different `OpEnvironment`s might contain different Ops, and it is important to be able to query an `OpEnvironment` about the available Ops.

Users can query the `OpEnvironment` for Ops matching a given name using the method `OpEnvironment.descriptions(String)`. The following SciJava script queries an `OpEnvironment` for all Ops matching the name `filter.gauss`:

```groovy
import org.scijava.ops.engine.DefaultOpEnvironment
ops = new DefaultOpEnvironment()

ops.descriptions("filter.gauss")
```
