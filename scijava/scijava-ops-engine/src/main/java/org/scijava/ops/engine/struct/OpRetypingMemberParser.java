
package org.scijava.ops.engine.struct;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.scijava.ops.engine.simplify.SimplifiedOpInfo;
import org.scijava.struct.FunctionalMethodType;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;
import org.scijava.struct.MemberParser;
import org.scijava.struct.Struct;
import org.scijava.struct.ValidityException;

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
	 * <li>That {@code srcStruct} is valid
	 * <li>That there are {@code inputs.length} input {@link Member}s in
	 * {@code srcStruct}
	 * <li>That there is <b>one</b> output {@link Member} in {@code srcStruct}
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
		throws ValidityException
	{
		List<FunctionalMethodType> newFmts = source.newFmts();
		FunctionalMethodType outputFmt = newFmts.stream().filter(fmt -> fmt
			.itemIO() == ItemIO.OUTPUT || fmt.itemIO() == ItemIO.MUTABLE || fmt
				.itemIO() == ItemIO.CONTAINER).findFirst().get();
		List<Member<?>> newMembers = new ArrayList<>();
		int inputIndex = 0;
		for (Member<?> m : source.struct().members()) {
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
		throws ValidityException
	{
		return parse(new RetypingRequest(s, newFmts), structType);
	}

}
