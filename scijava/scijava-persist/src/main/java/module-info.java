module org.scijava.persist {

	exports org.scijava.persist;

	opens org.scijava.persist to org.scijava;

	requires org.scijava;

	requires transitive com.google.gson;
}
