module org.scijava.ops.api {
	exports org.scijava.ops.api;
	exports org.scijava.ops.api.features;

	requires org.scijava;
	requires org.scijava.function;
	requires transitive org.scijava.struct;
	requires org.scijava.types;

	uses org.scijava.ops.api.OpInfoGenerator;
}
