package org.scijava.ops.api;

/**
 * Generic Ops utilities
 *
 * @author Gabriel Selzer
 */
public final class Ops {

	private Ops() {}

	/**
	 * Convenience function for determining whether {@code op} is a {@link RichOp}.
	 * @param op the Op
	 * @return true iff {@code op} is a {@link RichOp}
	 */
	public static boolean isRich(Object op) {
		return op instanceof RichOp;
	}

	/**
	 * Convenience function for getting the {@link RichOp} of {@code op}
	 * @param op the Op
	 * @return the {@link RichOp} wrapping {@code op}
	 * @param <T> the type of {@code op}
	 */
	@SuppressWarnings("unchecked")
	public static <T> RichOp<T> rich(T op) {
		return (RichOp<T>) op;
	}

	/**
	 * Convenience function for accessing {@link RichOp#recordExecutions(boolean)}
	 *
	 * @param op the Op
	 * @param record true iff {@code op} should record its executions
	 * @param <T> the type of the Op
	 */
	public static <T> void recordExecutions(T op, boolean record) {
		rich(op).recordExecutions(record);
	}

	/**
	 * Convenience function for accessing {@link RichOp#isRecordingExecutions()}
	 *
	 * @param op the Op
	 * @param <T> the type of the Op
	 * @return true iff Op is recording its executions
	 */
	public static <T> boolean isRecordingExecutions(T op) {
		return isRich(op) && rich(op).isRecordingExecutions();
	}

}
