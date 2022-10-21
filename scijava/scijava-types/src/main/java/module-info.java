module org.scijava.types {

	exports org.scijava.types;
	exports org.scijava.types.inference;

	opens org.scijava.types.extractors to org.scijava;
	opens org.scijava.types to org.scijava;

	requires transitive com.google.common;
	requires transitive org.scijava.common3;
	requires transitive org.scijava.discovery;
	requires transitive org.scijava.log2;
	requires org.scijava.priority;

	uses org.scijava.types.TypeExtractor;

	provides org.scijava.types.TypeExtractor with
			org.scijava.types.extractors.MapTypeExtractor,
			org.scijava.types.extractors.IterableTypeExtractor;

}
