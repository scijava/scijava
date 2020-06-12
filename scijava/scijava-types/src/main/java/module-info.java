module org.scijava.types {

	exports org.scijava.types;

	opens org.scijava.types.extractors to org.scijava;
	opens org.scijava.types to org.scijava;

	requires transitive com.google.common;
	requires org.scijava;
}
