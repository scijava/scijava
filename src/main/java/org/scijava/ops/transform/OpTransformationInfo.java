package org.scijava.ops.transform;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

import org.scijava.ops.OpService;
import org.scijava.ops.matcher.OpRef;

public class OpTransformationInfo {

	private LinkedList<OpRef> fromRefs = new LinkedList<>();
	private LinkedList<OpRef> toRefs = new LinkedList<>();
	private LinkedList<OpTransformer> transformationQueue = new LinkedList<>();

	public OpTransformationInfo(OpRef from, OpRef to, OpTransformer transformer) {
		fromRefs.addFirst(from);
		toRefs.addFirst(to);
		transformationQueue.addFirst(transformer);
	}

	public OpRef getFrom() {
		return fromRefs.getFirst();
	}

	public OpRef getTo() {
		return toRefs.getFirst();
	}
	
	public OpTransformer getTransformer() {
		return transformationQueue.getFirst();
	}

	public Object execute(Object obj, OpService opService) {
		Object candidate = obj;
		int i = 0;
		for (OpTransformer t : transformationQueue) {
			candidate = t.transform(opService, toRefs.get(i), candidate);
			i++;
		}
		return candidate;
	}
	
	public OpTransformationInfo chain(OpTransformationInfo transformation) {
		fromRefs.addFirst(transformation.getFrom());
		toRefs.addFirst(transformation.getTo());
		transformationQueue.addFirst(transformation.getTransformer());
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		Iterator<OpRef> fromIter = fromRefs.descendingIterator();
		Iterator<OpRef> toIter = toRefs.descendingIterator();
		Iterator<OpTransformer> transformIter = transformationQueue.descendingIterator();
		
		int i = 0;
		while(fromIter.hasNext()) {
			s.append(i + ")\n");
			s.append("\tFrom:\t");
			s.append(Arrays.deepToString(fromIter.next().getTypes()));
			s.append("\n\tTo:\t\t");
			s.append(Arrays.deepToString(toIter.next().getTypes()));
			s.append("\n\tWith:\t");
			s.append(transformIter.next().getClass().getName());
			s.append("\n\n");
		}
		return s.toString();
	}
}
