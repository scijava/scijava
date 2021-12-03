module org.scijava.ops.api {
	exports org.scijava.ops.api;
	exports org.scijava.ops.api.features;

	requires org.scijava.function;
	requires transitive org.scijava.struct;
	requires org.scijava.types;
	requires org.scijava.common3;

	uses org.scijava.ops.api.OpInfoGenerator;
}
