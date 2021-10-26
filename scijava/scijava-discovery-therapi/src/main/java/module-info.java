module org.scijava.discovery.therapi {

	exports org.scijava.discovery.therapi;
	opens org.scijava.discovery.therapi to therapi.runtime.javadoc;

	uses org.scijava.parse2.Parser;

	requires org.scijava.discovery;
	requires transitive org.scijava.parse2;
	requires therapi.runtime.javadoc;
}
