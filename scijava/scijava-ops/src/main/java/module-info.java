module org.scijava.ops {
	
	//TODO: rearrange packages to export only needed classes
	exports org.scijava.ops; //contains OpDependency interface
	exports org.scijava.ops.function; // contains functional inferfaces
	exports org.scijava.ops.core; // contains OpCollection, Op interfaces
	exports org.scijava.ops.core.builder; // contains OpBuilder classes
	exports org.scijava.ops.matcher;
	exports org.scijava.ops.simplify;
	exports org.scijava.ops.conversionLoss;
	// TODO: move OpWrapper to its own package (org.scijava.ops.wrap??)
	exports org.scijava.ops.util; // contains OpWrapper interface
	exports org.scijava.struct;
	exports org.scijava.param;

	// -- Open plugins to scijava-common
	opens org.scijava.ops to org.scijava;
	opens org.scijava.ops.impl to org.scijava;

  // FIXME: This is a file name and is thus unstable
  requires geantyref;
  
  requires java.desktop;

	requires org.scijava;
	requires org.scijava.types;
	requires javassist;
}
