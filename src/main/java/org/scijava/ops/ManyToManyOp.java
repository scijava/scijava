
package org.scijava.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ManyToManyOp {

	public static void main(String... args) {
		ArrayList<String> inList = new ArrayList<String>();
		inList.add("Hello");
		inList.add("World");
		inList.add("How");
		inList.add("Are");
		inList.add("you");
		ArrayList<String> outList = new ArrayList<>();

		final Iterator<String> iter = inList.iterator();
		final Supplier<String> in = () -> iter.next();
		goSupplier(in, outList::add);

		goIterator(inList.iterator(), outList::add);

		// Who's going to do the looping? ;-)
	}

	// For a list of strings, concatenate them in pairs.
	private static void goSupplier(Supplier<String> in, Consumer<String> out) {
		out.accept(in.get() + in.get());
	}

	// For a list of strings, concatenate them in pairs.
	private static void goIterator(Iterator<String> in, Consumer<String> out) {
		out.accept(in.next() + in.next());
	}
}
