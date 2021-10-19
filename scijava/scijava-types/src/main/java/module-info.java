module org.scijava.types {

	exports org.scijava.types;
	exports org.scijava.types.inference;

	opens org.scijava.types.extractors to org.scijava;
	opens org.scijava.types to org.scijava;

	requires transitive com.google.common;
	requires org.scijava;
	requires transitive org.scijava.discovery;
	requires org.scijava.discovery.plugin;
	requires transitive org.scijava.log2;
}
