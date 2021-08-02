
module org.scijava.ops.serviceloader {

	exports org.scijava.ops.serviceloader;

	requires org.scijava.ops.spi;
	requires org.scijava.ops.discovery;

	uses org.scijava.ops.spi.Op;
	uses org.scijava.ops.spi.OpCollection;

	provides org.scijava.ops.spi.Op with
			org.scijava.ops.serviceloader.ServiceBasedAdder;

	provides org.scijava.ops.spi.OpCollection with
			org.scijava.ops.serviceloader.ServiceBasedMultipliers;

}
