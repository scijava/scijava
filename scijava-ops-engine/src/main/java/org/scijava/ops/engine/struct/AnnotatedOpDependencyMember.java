/*
 * #%L
 * Java implementation of the SciJava Ops matching engine.
 * %%
 * Copyright (C) 2016 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ops.engine.struct;

import org.scijava.ops.api.Hints;
import org.scijava.ops.engine.OpDependencyMember;
import org.scijava.ops.spi.OpDependency;
import org.scijava.struct.Member;
import org.scijava.struct.Structs;

import java.lang.reflect.Type;
import java.util.function.Supplier;

/**
 * @author Marcel Wiedenmann
 */
public abstract class AnnotatedOpDependencyMember<T> implements
	OpDependencyMember<T>
{

	private final Supplier<String> keyGenerator;
	private String key;
	private boolean keyGenerated;

	private final Supplier<String> descriptionGenerator;
	private String description;
	private boolean descriptionGenerated;

	private final Type type;
	private final OpDependency annotation;
	private final Hints hints;

	/**
	 * This constructor is ideal for situations where the key and description are
	 * readily available
	 *
	 * @param key the key
	 * @param description the description
	 * @param type the {@link Type} of this {@link Member}
	 * @param annotation the {@link OpDependency} annotation
	 */
	public AnnotatedOpDependencyMember(String key, String description, Type type,
		final OpDependency annotation)
	{
		this(() -> key, () -> description, type, annotation);
		this.key = key;
		this.keyGenerated = true;
		this.description = description;
		this.descriptionGenerated = true;
	}

	/**
	 * This constructor is ideal for situations where obtaining the key or
	 * description are computationally expensive.
	 *
	 * @param keyGenerator the {@link Supplier} able to generate the key
	 * @param descriptionGenerator the {@link Supplier} able to generate the
	 *          description
	 * @param type the {@link Type} of this {@link Member}
	 * @param annotation the {@link OpDependency} annotation
	 */
	public AnnotatedOpDependencyMember(Supplier<String> keyGenerator,
		Supplier<String> descriptionGenerator, Type type,
		final OpDependency annotation)
	{
		this.keyGenerator = keyGenerator;
		this.keyGenerated = false;
		this.descriptionGenerator = descriptionGenerator;
		this.descriptionGenerated = false;
		this.type = type;
		this.annotation = annotation;
		this.hints = new Hints(annotation.hints());
	}

	public OpDependency getAnnotation() {
		return annotation;
	}

	// -- OpDependencyMember methods --

	@Override
	public String getDependencyName() {
		return annotation.name();
	}

	@Override
	public Hints hints() {
		return hints;
	}

	// -- Member methods --

	@Override
	public String key() {
		if (!keyGenerated) generateKey();
		return key;
	}

	private synchronized void generateKey() {
		if (keyGenerated) return;
		key = keyGenerator.get();
		keyGenerated = true;
	}

	@Override
	public String description() {
		if (!descriptionGenerated) generateDescription();
		return description;
	}

	private synchronized void generateDescription() {
		if (descriptionGenerated) return;
		description = descriptionGenerator.get();
		descriptionGenerated = true;
	}

	@Override
	public Type type() {
		return type;
	}

	@Override
	public String toString() {
		return Structs.toString(this);
	}
}
