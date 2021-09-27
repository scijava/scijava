package org.scijava.ops.engine.progress;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Progress {

	private static final Map<Object, List<ProgressListener>> progressibleListeners = new HashMap<>();

	private static final ThreadLocal<ArrayDeque<ProgressibleObject>> progressibleStack = new InheritableThreadLocal<> () {
		@Override
		protected ArrayDeque<ProgressibleObject> childValue(ArrayDeque<ProgressibleObject> parentValue) {
			return parentValue.clone();
		}

			@Override
			protected ArrayDeque<ProgressibleObject> initialValue() {
				return new ArrayDeque<>();
			}
	};

	public static void addListener(Object progressible, ProgressListener l) {
		if(!progressibleListeners.containsKey(progressible)) {
			createListenerList(progressible);
		}
		addListenerToList(progressible, l);
	}

	private static void addListenerToList(Object progressible, ProgressListener l) {
		List<ProgressListener> list = progressibleListeners.get(progressible);
		synchronized (list) {
			list.add(l);
		}
	}

	private static synchronized void createListenerList(Object progressible) {
		if(progressibleListeners.containsKey(progressible)) return;
		progressibleListeners.put(progressible, new ArrayList<>());
	}

	public static void popExecution() {
		progressibleStack.get().pop();
	}

	public static void popAndCompleteExecution() {
		ProgressibleObject completed = progressibleStack.get().peek();
		completed.task().complete();
		pingListeners(completed);
		popExecution();
		if (progressibleStack.get().peek() != null) {
			pingListeners(progressibleStack.get().peek());
		}
	}

	public static void pushExecution(Object progressible) {
		Task t;
		if (progressibleStack.get().size() == 0) {
			t = new Task();
		}
		else {
			ProgressibleObject parent = progressibleStack.get().peek();
			t = parent.task().createSubtask();
		}
		progressibleStack.get().push(new ProgressibleObject(progressible, t));
	}

	private static void pingListeners(ProgressibleObject o) {
		List<ProgressListener> list = progressibleListeners.getOrDefault(o.object(), Collections.emptyList());
		synchronized (list) {
			list.forEach(l -> l.updateProgress(o.task()));
		}
	}

	private static Task currentTask() {
		ProgressibleObject o = progressibleStack.get().peek();
		return o.task();
	}

	public static void update() {
		update(1);
	}

	public static void update(long numElements) {
		currentTask().update(numElements);
		pingListeners(progressibleStack.get().peek());
	}

	public static void defineTotalProgress(int opStages) {
		currentTask().defineTotalProgress(opStages);
	}

	public static void defineTotalProgress(int opStages, int totalSubTasks) {
		currentTask().defineTotalProgress(opStages, totalSubTasks);
	}

	public static void registerSubtasks() {
		
	}

	public static void setStageMax(long max) {
		currentTask().setStageMax(max);
	}

	private Progress() {}

}

class ProgressibleObject {

	private final Object o;
	private final Task t;

	public ProgressibleObject(Object o, Task t) {
		this.o = o;
		this.t = t;
	}

	public Object object() {
		return o;
	}

	public Task task() {
		return t;
	}
	
}
