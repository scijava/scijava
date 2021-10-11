module org.scijava.discovery.therapi {

	exports org.scijava.discovery.therapi;
	opens org.scijava.discovery.therapi to therapi.runtime.javadoc;

	requires org.scijava.discovery;
	requires therapi.runtime.javadoc;

	provides org.scijava.discovery.Discoverer with org.scijava.discovery.therapi.TherapiDiscoverer;

}
