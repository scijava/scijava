module org.scijava.ops.python {

	requires jep;

	requires org.scijava.ops.api;
	requires org.scijava.ops.engine; // TODO: Remove
	requires org.scijava.common3;
	requires org.scijava.types;
	requires javassist;

	provides org.scijava.ops.api.features.YAMLOpInfoCreator with
			org.scijava.ops.python.PythonYAMLOpInfoCreator;

}
