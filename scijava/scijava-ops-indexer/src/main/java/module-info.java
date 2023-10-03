module org.scijava.javadoc.parser {
	exports org.scijava.ops.indexer;

	requires java.compiler;
	requires org.yaml.snakeyaml;

	provides javax.annotation.processing.Processor with  //
			org.scijava.ops.indexer.OpImplNoteParser;
}
