module org.scijava.discovery.plugin {

	exports org.scijava.discovery.plugin;
	opens org.scijava.discovery.plugin to org.scijava;

	requires org.scijava.discovery;
	requires transitive org.scijava;
}
