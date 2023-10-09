// This class was adapted from the
// com.github.therapi.runtimejavadoc.scribe.JavadocAnnotationProcessor class
// of Therapi Runtime Javadoc 0.13.0, which is distributed
// under the Apache 2 license.

package org.scijava.ops.indexer;

import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.lang.model.element.ElementKind.*;

/**
 * {@link javax.annotation.processing.Processor} used to find code blocks
 * annotated as Ops, using the implNote syntax.
 * 
 * @author Gabriel Selzer
 */
public class OpImplNoteParser extends AbstractProcessor {

	public static final String OP_VERSION = "op.version";
	private static final String PARSE_OPS = "parse.ops";

	private final Yaml yaml = new Yaml();

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
		RoundEnvironment roundEnvironment)
	{
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
			"Processing Ops written using the implNote syntax...");
		final Map<String, String> options = processingEnv.getOptions();
		if ("true".equals(options.get(PARSE_OPS))) {
			final List<OpImplData> data = new ArrayList<>();

			// Make sure each element only gets processed once.
			final Set<Element> alreadyProcessed = new HashSet<>();

			// If retaining Javadoc for all packages, the @RetainJavadoc annotation is
			// redundant. Otherwise, make sure annotated classes have their Javadoc
			// retained regardless of package.
			for (TypeElement annotation : annotations) {
				for (Element e : roundEnvironment.getElementsAnnotatedWith(
					annotation))
				{
					generateJavadoc(e, data, alreadyProcessed);
				}
			}

			for (Element e : roundEnvironment.getRootElements()) {
				generateJavadoc(e, data, alreadyProcessed);
			}

			if (!roundEnvironment.getRootElements().isEmpty() && !data.isEmpty()) {
				try {
					outputYamlDoc(data);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}
		// This annotation processor only looks at Javadoc - it doesn't look at, and
		// thus doesn't claim any annotations
		return false;
	}

	// TODO: Consider adding record
	private static final EnumSet<ElementKind> elementKindsToInspect = EnumSet.of(
		ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM);

	private void generateJavadoc(
			Element element,
			List<OpImplData> data,
			Set<Element> alreadyProcessed
	) {
		// Ignore elements that have been parsed already
		if (!alreadyProcessed.add(element)) {
			return;
		}
		// Ignore elements that we don't care to parse
		if (!elementKindsToInspect.contains(element.getKind())) {
			return;
		}

		// Start by checking the element itself
		TypeElement classElement = (TypeElement) element;
		Optional<OpImplData> clsData = elementToImplData(classElement);
		clsData.ifPresent(data::add);
		
		// Then check contained elements
		for (Element e : classElement.getEnclosedElements()) {
			elementToImplData(e).ifPresent(data::add);
		}

	}

	private void outputYamlDoc(List<OpImplData> collectedData) throws IOException {
		var data = collectedData.stream().map(OpImplData::dumpData).collect(
				Collectors.toList());
		String doc = yaml.dump(data);
		FileObject resource = processingEnv.getFiler().createResource( //
			StandardLocation.CLASS_OUTPUT, //
			"", //
			"op.yaml" //
		);
		try (OutputStream os = resource.openOutputStream()) {
			os.write(doc.getBytes(UTF_8));
		}
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("*");
	}

	@Override
	public Set<String> getSupportedOptions() {
		Set<String> supportedOptions = new HashSet<>();
		supportedOptions.add(PARSE_OPS);
		supportedOptions.add(OP_VERSION);
		return supportedOptions;
	}

	private Optional<OpImplData> elementToImplData (final Element element) {
		String javadoc = processingEnv.getElementUtils().getDocComment(element);
		if (javadoc != null && javadoc.contains("implNote op")) {
			try {
				if (element.getKind() == CLASS) {
					TypeElement typeElement = (TypeElement) element;
					var fMethod = ProcessingUtils.findFunctionalMethod(processingEnv, typeElement);
					var fMethodDoc = processingEnv.getElementUtils().getDocComment(
						fMethod);
					return Optional.of(new OpClassImplData(typeElement, fMethod, javadoc,
						fMethodDoc, processingEnv));
				}
				else if (element.getKind() == METHOD) {
					return Optional.of(new OpMethodImplData((ExecutableElement) element,
						javadoc, processingEnv));
				}
				else if (element.getKind() == FIELD) {
					return Optional.of(new OpFieldImplData(element, javadoc,
						processingEnv));
				}
			}
			catch (Exception e) {
				ProcessingUtils.printProcessingException(element, e, processingEnv);
			}
		}
		return Optional.empty();
	}

}
