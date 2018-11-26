/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2014 - 2018 ImageJ developers.
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

package org.scijava.ops.matcher;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.scijava.ops.OpEnvironment;
import org.scijava.ops.OpUtils;
import org.scijava.ops.util.Inject;
import org.scijava.param.ValidityProblem;
import org.scijava.struct.Member;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;

/**
 * Container class for a possible operation match between an {@link OpRef} and
 * an {@link OpInfo}, as computed by the {@link OpTypeMatchingService}.
 *
 * @author Curtis Rueden
 * @see OpTypeMatchingService
 */
public class OpCandidate {

	public static enum StatusCode {
		MATCH, //
		INVALID_STRUCT, //
		TOO_FEW_OUTPUTS, //
		TOO_MANY_OUTPUTS,
		OUTPUT_TYPES_DO_NOT_MATCH, //
		TOO_MANY_ARGS, //
		TOO_FEW_ARGS, //
		ARG_TYPES_DO_NOT_MATCH, //
		REQUIRED_ARG_IS_NULL, //
		CANNOT_CONVERT, //
		DOES_NOT_CONFORM, OTHER //
	}

	private final OpEnvironment ops;
	private final OpRef ref;
	private final OpInfo info;

	private final Map<TypeVariable<?>, Type> typeVarAssigns;

	private StatusCode code;
	private String message;
	private Member<?> statusItem;

	/** (Null-)Padded arguments of the op if the op has not required parameters.
	 * If the op does not, this will be the same as {@link #ref}.getArgs(). */
	private final Type[] paddedArgs;

	public OpCandidate(final OpEnvironment ops, final OpRef ref, final OpInfo info, final Map<TypeVariable<?>, Type> typeVarAssigns) {
		this.ops = ops;
		this.ref = ref;
		this.info = info;
		this.typeVarAssigns = typeVarAssigns;

		this.paddedArgs = OpUtils.padTypes(this, getRef().getArgs());
	}

	/** Gets the op execution environment of the desired match. */
	public OpEnvironment ops() {
		return ops;
	}

	/** Gets the op reference describing the desired match. */
	public OpRef getRef() {
		return ref;
	}

	/** Gets the {@link OpInfo} metadata describing the op to match against. */
	public OpInfo opInfo() {
		return info;
	}

	/** Gets the mapping between {@link TypeVariable}s and {@link Type}s that makes the {@link OpCandidate} pair legal. */
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
	public void setStatus(final StatusCode code, final String message, final Member<?> item) {
		this.code = code;
		this.message = message;
		this.statusItem = item;
	}

	/** Gets the matching status code. */
	public StatusCode getStatusCode() {
		return code;
	}

	/**
	 * Gets the status item related to the matching status, if any. Typically,
	 * if set, this is the parameter for which matching failed.
	 */
	public Member<?> getStatusItem() {
		return statusItem;
	}

	/** Gets a descriptive status message in human readable form. */
	public String getStatus() {
		final StatusCode statusCode = getStatusCode();
		if (statusCode == null)
			return null;

		final StringBuilder sb = new StringBuilder();
		switch (statusCode) {
		case MATCH:
			sb.append("MATCH");
			break;
		case INVALID_STRUCT:
			sb.append("Invalid struct:");
			for (ValidityProblem vp : opInfo().getValidityException().problems()) {
				sb.append("\n\t");
				sb.append(vp.getMessage());
			}
			break;
		case TOO_FEW_OUTPUTS:
			sb.append("Too few outputs");
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
		final String msg = message;
		if (msg != null)
			sb.append(": " + msg);

		return sb.toString();
	}

	@Override
	public String toString() {
		return info.toString();
	}

	public StructInstance<?> createOpInstance(Object... secondaryArgs) throws OpMatchingException {
		if (!getStatusCode().equals(StatusCode.MATCH)) {
			throw new OpMatchingException(
					"Status of candidate to create op from indicates a problem: " + getStatus());
		}

		StructInstance<?> inst = opInfo().createOpInstance();
		inject(inst, secondaryArgs);
		return inst;
	}

	public Object createOp(Object... secondaryArgs) throws OpMatchingException {
		return createOpInstance(secondaryArgs).object();
	}

	private void inject(StructInstance<?> opInst, Object... secondaryArgs) {
		// Inject the secondary args if there are any
		if (Inject.Structs.isInjectable(opInst)) {
			// Get padded secondary args
			Object[] paddedArgs = OpUtils.padArgs(this, true, secondaryArgs);
			if (paddedArgs == null) {
				throw new IllegalArgumentException(opInfo().implementationName() + " | " + getStatus());
			}
			Inject.Structs.inputs(opInst, paddedArgs);
			// Secondary args are given, however there are no to inject
		} else if (secondaryArgs.length > 0) {
			ops().logger().warn("Specified Op has no secondary args, however secondary args are given. "
					+ "The specified args will not be injected.");
		}
	}
}
