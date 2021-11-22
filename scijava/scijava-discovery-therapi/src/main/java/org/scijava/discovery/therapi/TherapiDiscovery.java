
package org.scijava.discovery.therapi;

import org.scijava.discovery.Discoverer;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A container for {@link Object}s returned by a {@link Discoverer}, grouping
 * differentiating metadata.
 * 
 * @author Gabriel Selzer
 * @param <T> the generic {@link Type} of this discovery.
 */
public class TherapiDiscovery {

	private final AnnotatedElement discovery;

	private final String tagType;

	private final Supplier<Map<String, ?>> optionGenerator;

	private Map<String, ?> tagOptions = null;

	public TherapiDiscovery(AnnotatedElement discovery, String tagType) {
		this(discovery, tagType, () -> Collections.emptyMap());
	}

	public TherapiDiscovery(AnnotatedElement discovery, String tagType, Map<String, ?> tagOptions) {
		this(discovery, tagType, () -> tagOptions);
		this.tagOptions = tagOptions;
	}

	public TherapiDiscovery(AnnotatedElement discovery, String tagType, Supplier<Map<String, ?>> optionGenerator) {
		this.discovery = discovery;
		this.tagType = tagType;
		this.optionGenerator = optionGenerator;
	}

	public AnnotatedElement discovery() {
		return discovery;
	}

	public String tagType() {
		return tagType;
	}

	public String option(String optionType) {
		if (tagOptions == null) populateOptions();
		try {
			return tagOptions.get(optionType).toString();
		}
		catch (NullPointerException e) {
			// tag doesn't contain option, return empty string
			return "";
		}
	}

	private synchronized void populateOptions() {
		if (tagOptions != null) return;
		try {
			tagOptions = optionGenerator.get();
		} catch(IllegalArgumentException e) {
			RuntimeException e1 = new IllegalArgumentException(
				"Cannot parse options for tag " + tagType + " from " + discovery + ":\n\t" + e.getMessage());
			e1.addSuppressed(e);
			throw e1;
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
