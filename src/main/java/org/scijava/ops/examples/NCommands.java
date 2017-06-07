package org.scijava.ops.examples;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.param.Parameter;
import org.scijava.util.DoubleArray;

// FlatMap is the Spark term for 1-to-N maps.
// E.g., FlatMap<Integer, String> = Map<Integer, Consumer<String>>
//
// We want to model 1-to-N functions (really, computers, because the Consumer
// instance must be passed in) this way.
//
// We want to see the Abstract*ary*Op base classes disappear.
// The run() method should disappear / become a layer on top of the lower level
// interfaces.

public class NCommands {
/*

	public static void main(final String... args) {
		System.out.println("Hello");
	}

	public static class Stats {
		@Parameter(type = ItemIO.OUTPUT) // eh
		private double mean;
		@Parameter(type = ItemIO.OUTPUT) // eh
		private double median;
	}

	public static class OneToNCommand implements Command {
		@Parameter
		private DoubleArray numbers;
		@Parameter
		private int neighborhood;

		@Parameter(type = ItemIO.BOTH)
		private RowOutput<Stats> stats;

		@Override
		public void run() {
			Stats s = new Stats();
			for (int i = 0; i < numbers.size(); i++) {
				s.mean = 1;
				s.median = 2;
				stats.accept(s);
			}
		}
	}

	public static class OneToNCommandWorse implements Command {
		@Parameter
		private DoubleArray numbers;
		@Parameter
		private int neighborhood;

		@Parameter(type = ItemIO.BOTH)
		private List<Stats> stats;

		@Override
		public void run() {
			Stats s = new Stats();
			for (int i = 0; i < numbers.size(); i++) {
				s.mean = 1;
				s.median = 2;
				stats.add(s);
			}
		}
	}



	public static class Data {
		@Parameter
		private double mean;
		@Parameter
		private double median;
	}

	public static class NToOneCommand implements Command {
		@Parameter
		private Iterable<Data> data;

		// Double does not annotate @Parameters
		// We said, in that case, we will return a single element: Double for getComponentTypes
		// So, the components of each row of numbers are: (Double)
		@Parameter(type = ItemIO.OUTPUT)
		private int sum;

		@Override
		public void run() {
			sum = 0;
			for (Data d : data) {
				sum += d.mean;
				sum += d.median;
			}
		}
	}


	public interface RowInput<T> extends Iterable<T> {}
	public interface OrderedRowInput<T> extends RowInput<T> {}
	public interface BoundedRowInput<T> extends Collection<T>, RowInput<T> {}
	public interface BoundedOrderedRowInput<T> extends List<T>, BoundedRowInput<T>, OrderedRowInput<T> {}

	public interface RowOutput<T> extends Consumer<T> {}

	public static class TableToTableMapCommand implements Command {
		@Parameter
		private BoundedChannel<Data> inTable;
		
		@Parameter(type = ItemIO.BOTH)
		private BoundedChannel<Data> outTable;

		@Override
		public void run() {
			inTable.forEach(e -> outTable.accept(e));
		}
	}

	// number of rows in -> # of rows out
	// single tables only for now


	// ---- High level construction of ops in comp-graph style ----
	// DOG:
	// Function<Element<I>, Element<I>> where I is an image type
	// Element<I> apply(Element<I> in) {
	//   Element<I> gauss1 = in.map(Gauss.class, sigma1) // produces an Element
	//   Element<I> gauss2 = in.map(Gauss.class, sigma2) // produces an Element
	//   Element<Pair<I, I>> result = gauss1.join(gauss2) // needs to be bounded to join
	//   Element<I> dog = result.apply(Subtract.class)
	// }
	//
	// How to solve the problem where this level of indirection makes the op
	// hard to consume downstream for additional comp-graph-style actions like map?
	//
	// E.g.:
	//
	// Channel<I> images;
	// Function<Element<I>, ElementI>> dogFunction = bestMatchingDog(...);
	// Channel<I> dogs = images.channelMap(dogFunction)
	//
	// Will work as long as map is declared as:
	//     channelMap(Function<Element<I>, Element<O>> func);

	// ---- partitioner ---- decomposition of some Blob into pieces (not necessarily also Blobs)
	// Element<List<T>> tiles = dog.partition(new BiConsumer<I, Consumer<T>> {
	//   
  //    void accept(I image, Consumer<T> tiles) {
	//       for (T tile : chopUp(image))
	//         tiles.accept(tile)
	//    }
	// });
	
	// ----- group ----- categorization of some channel<T> into channel<channel<T>>

	// ---- Can we write general code to operate as above, based on human-friendly one-line expressions? ----
	// From this expression: gauss(i, sigma2) - gauss(i, sigma1)
	// We have the postfix queue: gauss i sigma2 (2) <Fn> gauss i sigma1 (2) <Fn> subtract
	//
	// We can of course restrict the matcher to only give function gauss.
	// BUT: matching computer is useful here, as long as we take care.

	@Plugin(type = Aggregator.java)
	public static class TableToTableReduce<Data, Data> implements Aggregator<Data, Data> {
		@Parameter
		private double sigma;
		@Parameter(type = OUTPUT)
		private String message;
		@Override
		public void aggregate(Collection<Data> in, Consumer<Data> out) {
		}
	}
	// REDUCE
	data(a, b, c)

	f(table(data: a, b, c), sigma) -> (table(data: a', b', c'), message)

	// 

	public static class TableToTableAggregateCommand implements Command {
		@Parameter
		private RowInput<Data> inTable;
		
		@Parameter(type = ItemIO.BOTH)
		private Consumer<Data> outData;

		@Override
		public void run() {
			outData.accept(computeRow1(inTable));
			outData.accept(computeRow2(inTable));
		}
	}


	public static class OneToOneCommand implements Command {
		@Parameter
		private RowInput<Data> data;

		// Double does not annotate @Parameters
		// We said, in that case, we will return a single element: Double for getComponentTypes
		// So, the components of each row of numbers are: (Double)
		@Parameter(type = ItemIO.OUTPUT)
		private int sum;

		@Override
		public void run() {
			sum = 0;
			for (Data d : data) {
				sum += d.mean;
				sum += d.median;
			}
		}

	}
*/
}
