module org.scijava.parse2 {

	exports org.scijava.parse2;

	requires org.scijava.parsington;
	requires org.scijava.collections;

	provides org.scijava.parse2.Parser with org.scijava.parse2.impl.DefaultParser;

}
