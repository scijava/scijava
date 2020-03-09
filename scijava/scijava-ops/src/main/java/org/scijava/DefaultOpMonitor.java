package org.scijava;

import org.scijava.ops.OpMonitor;
import org.scijava.util.Logger;

/**
 * Default implementation of {@link OpMonitor}
 * 
 * @author Gabriel Selzer
 * @author Marcel Wiedenmann
 *
 */
public class DefaultOpMonitor implements OpMonitor {

	private volatile boolean canceled;

	private volatile double progress;

	private Logger logger;

	// private volatile OpMonitor parent;
	//
	// private List<OpMonitor> children;

	public DefaultOpMonitor() {
		this(new Logger());
	}

	public DefaultOpMonitor(Logger log) {
		logger = log;
		canceled = false;
		progress = 0d;
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public Logger logger() {
		return logger;
	}

	@Override
	public void setProgress(double progress) {
		this.progress = progress;
	}

	@Override
	public double getProgress() {
		return this.progress;
	}

}
