
module org.scijava.ops.serviceloader {

	exports org.scijava.ops.serviceloader;

	requires org.scijava.ops.spi;
	requires org.scijava.discovery;

	uses org.scijava.ops.spi.Op;
	uses org.scijava.ops.spi.OpCollection;
}
