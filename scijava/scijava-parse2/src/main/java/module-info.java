module org.scijava.parse2 {

	exports org.scijava.parse2;

	requires org.scijava;
	requires org.scijava.parsington;

	provides org.scijava.parse2.Parser with org.scijava.parse2.impl.DefaultParser;

}
