module org.scijava.ops.api {
	exports org.scijava.ops.api;

	requires org.scijava.common3;
	requires org.scijava.function;
	requires org.scijava.priority;
	requires transitive org.scijava.struct;
	requires org.scijava.types;

	uses org.scijava.discovery.Discoverer;
	uses org.scijava.ops.api.OpEnvironment;
	uses org.scijava.ops.api.OpHistory;
}
