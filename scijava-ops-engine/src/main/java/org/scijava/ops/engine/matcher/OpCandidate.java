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

package org.scijava.ops.engine.matcher;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.api.OpRequest;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.common3.Types;

/**
 * Container class for a possible operation match between an {@link OpRequest}
 * and an {@link OpInfo}.
 *
 * @author Curtis Rueden
 * @author Gabriel Selzer
 */
public class OpCandidate {

	public static enum StatusCode {
			MATCH, //
			OUTPUT_TYPES_DO_NOT_MATCH, //
			TOO_MANY_ARGS, //
			TOO_FEW_ARGS, //
			ARG_TYPES_DO_NOT_MATCH, //
			REQUIRED_ARG_IS_NULL, //
			CANNOT_CONVERT, //
			DOES_NOT_CONFORM, OTHER //
	}

	private final OpEnvironment env;
	private final OpRequest request;
	private final OpInfo info;

	private final Map<TypeVariable<?>, Type> typeVarAssigns;
	private final Type reifiedType;

	private StatusCode code;
	private String message;
	private Member<?> statusItem;

	/**
	 * (Null-)Padded arguments of the op if the op has not required parameters. If
	 * the op does not, this will be the same as {@link #request}.getArgs().
	 */
	private final Type[] paddedArgs;

	public OpCandidate(final OpEnvironment env, final OpRequest request,
		final OpInfo info, final Map<TypeVariable<?>, Type> typeVarAssigns)
	{
		this.env = env;
		this.request = request;
		this.info = info;
		this.typeVarAssigns = typeVarAssigns;

		this.paddedArgs = padTypes(this, getRequest().argTypes());
		this.reifiedType = getReifiedType(request, info, typeVarAssigns);
	}

	public OpCandidate(final OpEnvironment env, final OpRequest request,
		final OpInfo info)
	{
		this(env, request, info, typeVarAssignsFromRequestAndInfo(request, info));
	}

	public static Type getReifiedType(OpRequest request, OpInfo info,
		Map<TypeVariable<?>, Type> typeVarAssigns)
	{
        var exactSuperType = Types.superTypeOf(info.opType(), Types.raw(
			request.type()));
		return Types.unroll(exactSuperType, typeVarAssigns);
	}

	/** Gets the op execution environment of the desired match. */
	public OpEnvironment env() {
		return env;
	}

	/** Gets the op request describing the desired match. */
	public OpRequest getRequest() {
		return request;
	}

	/** Gets the {@link OpInfo} metadata describing the op to match against. */
	public OpInfo opInfo() {
		return info;
	}

	/** Gets the reified {@link Type} of the Op described by this candidate. */
	public Type getType() {
		return reifiedType;
	}

	/** Gets the priority of this result */
	public double priority() {
		return info.priority();
	}

	/**
	 * Gets the mapping between {@link TypeVariable}s and {@link Type}s that makes
	 * the {@link OpCandidate} pair legal.
	 */
	public Map<TypeVariable<?>, Type> typeVarAssigns() {
		return typeVarAssigns;
	}

	public Type[] paddedArgs() {
		return paddedArgs;
	}

	/**
	 * Gets the {@link Struct} metadata describing the op to match against.
	 *
	 * @see OpInfo#struct()
	 */
	public Struct struct() {
		return info.struct();
	}

	/** Sets the status of the matching attempt. */
	public void setStatus(final StatusCode code) {
		setStatus(code, null, null);
	}

	/** Sets the status of the matching attempt. */
	public void setStatus(final StatusCode code, final String message) {
		setStatus(code, message, null);
	}

	/** Sets the status of the matching. */
	public void setStatus(final StatusCode code, final String message,
		final Member<?> item)
	{
		this.code = code;
		this.message = message;
		this.statusItem = item;
	}

	/** Gets the matching status code. */
	public StatusCode getStatusCode() {
		return code;
	}

	/**
	 * Gets the status item related to the matching status, if any. Typically, if
	 * set, this is the parameter for which matching failed.
	 */
	public Member<?> getStatusItem() {
		return statusItem;
	}

	/** Gets a descriptive status message in human readable form. */
	public String getStatus() {
		final var statusCode = getStatusCode();
		if (statusCode == null) return null;

		final var sb = new StringBuilder();
		switch (statusCode) {
			case MATCH:
				sb.append("MATCH");
				break;
			case OUTPUT_TYPES_DO_NOT_MATCH:
				sb.append("Output types do not match");
				break;
			case TOO_MANY_ARGS:
				sb.append("Too many arguments");
				break;
			case TOO_FEW_ARGS:
				sb.append("Not enough arguments");
				break;
			case ARG_TYPES_DO_NOT_MATCH:
				sb.append("Argument types do not match");
				break;
			case REQUIRED_ARG_IS_NULL:
				sb.append("Missing required argument");
				break;
			case CANNOT_CONVERT:
				sb.append("Inconvertible type");
				break;
			case DOES_NOT_CONFORM:
				sb.append("Inputs do not conform to op rules");
				break;
			default:
				return message;
		}
		final var msg = message;
		if (msg != null) sb.append(": ").append(msg);

		return sb.toString();
	}

	@Override
	public String toString() {
		return info.toString();
	}

	public StructInstance<?> createOpInstance(List<?> dependencies) {
		if (getStatusCode().equals(StatusCode.MATCH)) {
			return opInfo().createOpInstance(dependencies);
		}

		throw new IllegalStateException(
			"Status of candidate to create op from indicates a problem: " +
				getStatus());
	}

	public Object createOp(List<?> dependencies) {
		return createOpInstance(dependencies).object();
	}

	// -- Helper methods -- //
	private static Map<TypeVariable<?>, Type> typeVarAssignsFromRequestAndInfo(
		final OpRequest request, final OpInfo info)
	{
		Map<TypeVariable<?>, Type> typeVarAssigns = new HashMap<>();
		if (!request.typesMatch(info.opType(), typeVarAssigns))
			throw new IllegalArgumentException("OpInfo " + info +
				" cannot satisfy the requirements contained within OpRequest " +
				request);
		return typeVarAssigns;
	}

	private Type[] padTypes(final OpCandidate candidate, Type[] types) {
		final var padded = padArgs(candidate, (Object[]) types);
		return Arrays.copyOf(padded, padded.length, Type[].class);
	}

	private Object[] padArgs(final OpCandidate candidate, Object... args) {
		List<Member<?>> members;
		String argName;
		members = candidate.opInfo().inputs();
		argName = "args";

		int inputCount = 0, requiredCount = 0;
		for (final var item : members) {
			inputCount++;
			if (!item.isRequired()) requiredCount++;
		}
		if (args.length == inputCount) {
			// correct number of arguments
			return args;
		}
		if (args.length > inputCount) {
			// too many arguments
			candidate.setStatus(StatusCode.TOO_MANY_ARGS, "\nNumber of " + argName +
				" given: " + args.length + "  >  " + "Number of " + argName +
				" of op: " + inputCount);
			return null;
		}
		if (args.length < requiredCount) {
			// too few arguments
			candidate.setStatus(StatusCode.TOO_FEW_ARGS, "\nNumber of " + argName +
				" given: " + args.length + "  <  " + "Number of required " + argName +
				" of op: " + requiredCount);
			return null;
		}

		// pad nullable parameters with null (from right to left)
		final var argsToPad = inputCount - args.length;
		final var nullableCount = inputCount - requiredCount;
		final var nullablesToFill = nullableCount - argsToPad;
		final var paddedArgs = new Object[inputCount];
		int argIndex = 0, paddedIndex = 0, nullableIndex = 0;
		for (final var item : members) {
			if (!item.isRequired() && nullableIndex++ >= nullablesToFill) {
				// skip this nullable parameter (pad with null)
				paddedIndex++;
				continue;
			}
			paddedArgs[paddedIndex++] = args[argIndex++];
		}
		return paddedArgs;
	}

}
