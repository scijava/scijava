module org.scijava.discovery.test {

	opens org.scijava.discovery.test to org.scijava.discovery;

	requires org.scijava.discovery;
	requires org.scijava.ops.spi;

	uses org.scijava.ops.spi.Op;
	uses org.scijava.ops.spi.OpCollection;

	provides org.scijava.ops.spi.Op with
			org.scijava.discovery.test.ServiceBasedAdder;

	provides org.scijava.ops.spi.OpCollection with
			org.scijava.discovery.test.ServiceBasedMultipliers;

}
