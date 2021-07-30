
module org.scijava.ops.serviceloader {

	exports org.scijava.ops.serviceloader;

	requires org.scijava.ops.spi;
	requires org.scijava.ops.discovery;

	uses org.scijava.ops.spi.Op;
	provides org.scijava.ops.spi.Op with org.scijava.ops.serviceloader.ServiceBasedAdder;

}
