module org.scijava.ops.api {
	exports org.scijava.ops.api;

	requires org.scijava.function;
	requires transitive org.scijava.struct;
	requires org.scijava.common3;
	requires org.scijava.discovery;
	requires org.scijava.priority;
	requires org.scijava.types;

	uses org.scijava.discovery.Discoverer;
	uses org.scijava.ops.api.OpEnvironment;
	uses org.scijava.ops.api.OpHistory;
	uses org.scijava.ops.api.OpInfoGenerator;
}
