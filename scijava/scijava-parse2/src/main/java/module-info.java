module org.scijava.parse2 {

	exports org.scijava.parse2;

	opens org.scijava.parse2 to org.scijava;
	opens org.scijava.parse2.impl to org.scijava;

	requires org.scijava;
	requires org.scijava.parsington;

}
