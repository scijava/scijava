module org.scijava.ops.serviceloader.test {

	opens org.scijava.ops.serviceloader.test to org.scijava.ops.serviceloader;

	requires org.scijava.discovery;
	requires org.scijava.ops.spi;
	requires org.scijava.ops.serviceloader;

	provides org.scijava.ops.spi.Op with
			org.scijava.ops.serviceloader.test.ServiceBasedAdder;

	provides org.scijava.ops.spi.OpCollection with
			org.scijava.ops.serviceloader.test.ServiceBasedMultipliers;

}
