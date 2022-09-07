
package org.scijava.struct;

import org.scijava.common3.validity.ValidityException;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Parses a set of {@link Member}s from a source {@link Object}
 * 
 * @author Gabriel Selzer
 * @param <S> The {@link Type} of {@link Object}s parsable by this parser
 * @param <M> The {@link Type} of {@link Member}s returned by this parser
 */
public interface MemberParser<S, M extends Member<?>> {

	/**
	 * parses {@code source} for a {@link List} of {@link Member}s, which is then
	 * returned
	 * 
	 * @param source the {@link Object} to parse
	 * @param structType TODO
	 * @return a {@link List} of {@link Member}s parsed from {@code source}
	 * @throws {@link ValidityException} when the source of the {@link Member}s is
	 *           improperly declared, formatted
	 */
	List<M> parse(S source, Type structType) throws ValidityException;

}
