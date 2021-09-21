package org.scijava.ops.engine.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.scijava.ops.api.ProgressReporter;


public class MultiStageProgressReporter implements ProgressReporter {

	static {
		Function<String, String> f = new Function<>() {

			@Override
			public String apply(String t) throws RuntimeException{
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	private List<ProgressReporter> reporters;
	private int currentStage = 0;

	public MultiStageProgressReporter(ProgressReporter... reporters) {
		this.reporters = Arrays.asList(reporters);
	}

	public MultiStageProgressReporter(Supplier<ProgressReporter> reporterFactory, int numStages) {
		reporters = new ArrayList<>(numStages);
		for(int i = 0; i < numStages; i++) {
			reporters.add(reporterFactory.get());
		}
	}

	@Override
	public void reportElements(long completedElements) {
		synchronized (this) {
			reporters.get(currentStage).reportElements(completedElements);
			if(reporters.get(currentStage).isCompleted()) currentStage++;
		}
		if (currentStage > reporters.size()) throw new IllegalArgumentException("More elements were completed than were declared to exist!");
	}

	@Override
	public void reportCompletion() {
		reporters.forEach(r -> r.reportCompletion());
	}

	@Override
	public double getProgress() {
		double sum = reporters.stream().mapToDouble(p -> p.getProgress()).sum();
		return sum / reporters.size();
	}

	@Override
	public boolean isCompleted() {
		return reporters.parallelStream().allMatch(p -> p.isCompleted());
	}

}
