
package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.scijava.ops.engine.matcher.simplify.SimplifiedOpInfo;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Struct;

public class OpRetypingMemberParser implements
	MemberParser<RetypingRequest, Member<?>>
{

	/**
	 * Generates a new {@link List} with retyped {@link Member}s. Using
	 * {@code originalStruct} as a template, this method retypes the inputs of
	 * {@code originalStruct} using {@code inputs}, and the output using
	 * {@code output}.
	 * <p>
	 * This method makes a couple of assumptions:
	 * <ol>
	 * <li>That {@code srcStruct} is valid</li>
	 * <li>That there are {@code inputs.length} input {@link Member}s in
	 * {@code srcStruct}</li>
	 * <li>That there is <b>one</b> output {@link Member} in {@code srcStruct}</li>
	 * </ol>
	 * We should consider adding the evalutation of these assumptions
	 * 
	 * @param source the {@link RetypingRequest} from which we create the new
	 *          {@link List} of {@link Member}s
	 * @return a new {@link Struct} reflecting the simplified arguments / focused
	 *         output of this {@link SimplifiedOpInfo}
	 */
	@Override
	public List<Member<?>> parse(RetypingRequest source, Type structType)
	{
		List<Member<?>> original = source.struct().members();
		List<FunctionalMethodType> newFmts = source.newFmts();
		List<Member<?>> ios = original.stream().filter(m -> m.isInput() || m.isOutput()).collect(
				Collectors.toList());
		if (ios.size() == newFmts.size())
			return strictConversion(original, newFmts);
		else
			return synthesizedConversion(newFmts, original);
	}

	private List<Member<?>> synthesizedConversion(List<FunctionalMethodType> newFmts, List<Member<?>> original) {
		// Create new members for all new I/O members
		List<Member<?>> newMembers = IntStream.range(0, newFmts.size()).boxed().map(foo -> mapToMember(foo, newFmts.get(foo))).collect(Collectors.toList());
		// Add any non-I/O members (e.g. dependencies)
		for (Member<?> m : original) {
			if (!m.isInput() && !m.isOutput()) {
				newMembers.add(m);
			}
		}
		return newMembers;
	}

	private Member<?> mapToMember(int i, FunctionalMethodType fmt) {
		return new Member<>() {

			@Override public String getKey() {
				ItemIO ioType = fmt.itemIO();
				if (ioType == ItemIO.INPUT)
					return "in" + i + 1;
				else if (ioType == ItemIO.CONTAINER)
					return "container";
				else if (ioType == ItemIO.MUTABLE)
					return "mutable";
				else if (ioType == ItemIO.OUTPUT)
					return "output";
				else
					return "";
			}

			@Override public Type getType() {
				return fmt.type();
			}

			@Override public ItemIO getIOType() {
				return fmt.itemIO();
			}
		};
	}

	private List<Member<?>> strictConversion(List<Member<?>> originalMembers, List<FunctionalMethodType> newFmts) {
		FunctionalMethodType outputFmt = newFmts.stream().filter(fmt -> fmt
				.itemIO() == ItemIO.OUTPUT || fmt.itemIO() == ItemIO.MUTABLE || fmt
				.itemIO() == ItemIO.CONTAINER).findFirst().get();
		List<Member<?>> newMembers = new ArrayList<>();
		int inputIndex = 0;
		for (Member<?> m : originalMembers) {
			if (m.isInput()) {
				m = ConvertedParameterMember.from(m, newFmts.get(inputIndex++));
			}
			else if (m.isOutput()) {
				m = ConvertedParameterMember.from(m, outputFmt);
			}
			newMembers.add(m);
		}
		return newMembers;

	}

	public List<Member<?>> parse(Struct s, List<FunctionalMethodType> newFmts, Type structType)
	{
		return parse(new RetypingRequest(s, newFmts), structType);
	}

}
