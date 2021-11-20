# SciJava Function: a collection of functional interfaces

This library carries a suite of [`FunctionalInterface`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/FunctionalInterface.html)s designed for general purpose use.

## `Function`s: `n` inputs, one output
`Function`s are designed to extend the capabilities of [`java.util.Function`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Function.html), and expose the implementable method `apply`:

```java
/**
 * Adds three numbers
 */
private Functions.Arity3<Integer, Integer, Integer, Integer> adder = 
	(in1, in2, in3) -> in1 + in2 + in3;

public static void main(String... args) {
	System.out.println("The sum of 5, 6, and 7 is " + adder.apply(5, 6, 7));
}
```

## `Computer`s: `n` inputs, one *preallocated* output
`Computer`s are designed to provide efficiency, writing their output into a preallocated data container. This improves performance in loops, if the output can be discarded after each loop; reusing a single container can avoid many `new` calls. `Computer`s expose the implementable method `compute`:

```java
/**
 * Splits {@link String} {@code s} using delimiter {@code c}, 
 * placing the results in {@link List} {@code outList}
 */
private Computers.Arity2<String, Character, List<String>> splitter = (s, c, outList) -> { 
	outList.clear();
	String[] strings = s.split(c);

	for (String str : strings) {
		outList.add(str);
	}
};

public static void main(String... args) {
	String[] exampleStrings = {"foo:bar:baz", "foo:baz", "bar:baz"};
	List<String> stringsContainingBar = new ArrayList<>();

	List<String> container = new ArrayList<>();
	for(String example : exampleStrings) {
		splitter.compute(example, ':', container);
		if(container.contains("bar")) {
			stringsContainingBar.add(example);
		}
	}
}
```

## `Inplace`s: `n-1` *pure* inputs, one I/O input
`Inplace`s improve on the efficiency of `Computer`s, when an input can be disposed of after the `Inplace`'s completion. `Inplace`s **overwrite** a specified input with the output its exposed method `mutate`, improving performance by removing the need for a `new` call. Where `Computer`s provide a preallocated output, `Inplace`s use their I/O object in the **computation** of the output.

`Inplace` are designated using two numbers (and are named `ArityX_Y`). The first number `X` indicates the number of inputs, and the second number `Y` indicates which parameter will be mutated. For example, `Arity3_2` takes three arguments, and indicates that the second input will be mutated.

```java
/**
 * Increments each number in the list
 */
private Inplaces.Arity2_2<Integer, List<Integer>> incrementer = (i, list) -> { 
	for (int index = 0; index < list.size(); index++) {
		list.set(index, list.get(index) + i);
	}
};

public static void main(String... args) {
	List<Integer> example = Arrays.asList(1, 1, 2, 3, 5, 8, 13, 21);
	incrementer.mutate(1, example);
}
```

## `Consumer`s: `n` inputs, no output
`Consumer`s are designed (read: expected) to operate via side effects, and build off of the foundation of [`Consumer`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/function/Consumer.html). `Consumer`s expose the implementable method `accept`.

```java
/**
 * Prints out the difference between a and b
 */
private Consumers.Arity2<Integer, Integer> printer = (a, b) -> {
	Integer diff = a - b;
	System.out.println("The difference between " + a + " and " + b + " is " + diff);
};

public static void main(String... args) {
	printer.accept(3, 2);
}