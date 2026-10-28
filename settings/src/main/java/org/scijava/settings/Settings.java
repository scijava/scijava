/*
 * #%L
 * Settings a user can read and edit, in one TOML file.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.settings;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.scijava.convert3.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings a user can read and edit, kept in one TOML file.
 * <p>
 * It is an object, not a singleton and not a service: settings belong to an
 * application, and two applications sharing a JVM want two of them. Ask a
 * container for one, or make one:
 * </p>
 *
 * <pre>
 * Settings settings = Settings.forApp("fiji");   // ~/.config/fiji/settings.toml
 * Settings settings = Settings.of(somePath);     // exactly there
 * Settings settings = Settings.inMemory();       // for a test
 * </pre>
 * <p>
 * <strong>One path on every platform.</strong> {@code ~/.config/<app>} on
 * Linux, macOS and Windows alike, honoring {@code XDG_CONFIG_HOME} where it is
 * set. This is deliberate and it is not what the platforms would each prefer:
 * the point of leaving {@code java.util.prefs} behind was that settings should
 * be a file a person can open, copy between machines, put in a repository and
 * name in a forum post - and one answer to "where is it?" is worth more to
 * that than matching each platform's convention. Appose made the same choice,
 * and an override is a system property away for anyone who disagrees.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Settings {

	private static final Logger log = LoggerFactory.getLogger(Settings.class);

	/** System property naming the settings file outright. */
	public static final String FILE_PROPERTY = "scijava.settings.file";

	/** System property naming the directory the settings file sits in. */
	public static final String DIR_PROPERTY = "scijava.config.dir";

	/** System property naming the application, as Jaunch sets it. */
	public static final String APP_PROPERTY = "scijava.app.name";

	private final Path file;
	private final Toml toml;

	private Settings(final Path file, final Toml toml) {
		this.file = file;
		this.toml = toml;
	}

	/** Reads the settings in the given file, which need not exist yet. */
	public static Settings of(final Path file) {
		return new Settings(file, read(file));
	}

	/**
	 * Reads the settings of the named application.
	 *
	 * @param appName the application's name, as it should appear in the path
	 * @return its settings
	 */
	public static Settings forApp(final String appName) {
		return of(fileFor(appName));
	}

	/**
	 * Reads the settings of the application this JVM is running, as named by
	 * the {@code scijava.app.name} property.
	 */
	public static Settings forApp() {
		return forApp(System.getProperty(APP_PROPERTY, "scijava"));
	}

	/** Creates settings with nowhere to save to, for tests and scratch use. */
	public static Settings inMemory() {
		return new Settings(null, Toml.empty());
	}

	/** Works out where the named application's settings live. */
	public static Path fileFor(final String appName) {
		final String explicitFile = System.getProperty(FILE_PROPERTY);
		if (explicitFile != null && !explicitFile.isEmpty()) {
			return Paths.get(explicitFile);
		}
		final String explicitDir = System.getProperty(DIR_PROPERTY);
		if (explicitDir != null && !explicitDir.isEmpty()) {
			return Paths.get(explicitDir).resolve("settings.toml");
		}
		final String xdg = System.getenv("XDG_CONFIG_HOME");
		final Path config = xdg != null && !xdg.isEmpty() ? Paths.get(xdg) //
			: Paths.get(System.getProperty("user.home"), ".config");
		return config.resolve(appName).resolve("settings.toml");
	}

	/** Gets the file these settings live in, if they live anywhere. */
	public Optional<Path> file() {
		return Optional.ofNullable(file);
	}

	/** Gets the TOML itself, for whatever this class does not do. */
	public Toml toml() {
		return toml;
	}

	/** Gets a value as it is stored. */
	public Object get(final String table, final String key) {
		return toml.get(table, key);
	}

	/**
	 * Gets a value as the type the caller wants.
	 * <p>
	 * The conversion is {@code scijava-convert3}'s, the same one a script
	 * header, a command line and a text field use - so a {@code File} comes back
	 * a {@code File}, an enum comes back an enum, and a list of paths comes back
	 * a {@code List<File>} if that is what was asked for.
	 * </p>
	 *
	 * @param <T> the type wanted
	 * @param table the table, which for a command is its identifier
	 * @param key the key, which for a parameter is its name
	 * @param type the type wanted
	 * @return the value, or empty if there is none or it cannot be that type
	 */
	public <T> Optional<T> get(final String table, final String key,
		final Class<T> type)
	{
		@SuppressWarnings("unchecked")
		final Optional<T> value = (Optional<T>) get(table, key, (Type) type);
		return value;
	}

	/** Gets a value as the given type, generics included. */
	public Optional<Object> get(final String table, final String key,
		final Type type)
	{
		final Object stored = toml.get(table, key);
		if (stored == null) return Optional.empty();
		final Optional<Object> converted = Converters.get().tryConvert(stored,
			type);
		if (converted.isEmpty()) {
			log.debug("Cannot read {}.{} as {}: it is stored as {}", table, key, type,
				stored.getClass().getSimpleName());
		}
		return converted;
	}

	/**
	 * Stores a value, in whichever TOML type suits it.
	 * <p>
	 * TOML has a small fixed set of types, so the mapping is a table: text and
	 * characters and enums and files become strings, whole numbers become
	 * integers, fractional ones floats, the date-and-time types map across
	 * directly, and a collection or array becomes an array of whatever its
	 * elements map to. Anything else is asked to describe itself as a string,
	 * which round-trips for every type that has a converter back - and is
	 * skipped, with a word in the log, for one that does not.
	 * </p>
	 *
	 * @param table the table, which for a command is its identifier
	 * @param key the key, which for a parameter is its name
	 * @param value what to store
	 * @return whether it could be stored
	 */
	public boolean set(final String table, final String key,
		final Object value)
	{
		if (value == null) {
			toml.remove(table, key);
			return true;
		}
		final Object stored = toTomlValue(value);
		if (stored == null) {
			log.debug("Not storing {}.{}: nothing in TOML holds a {}", table, key,
				value.getClass().getName());
			return false;
		}
		toml.set(table, key, stored);
		return true;
	}

	/** Forgets one value. */
	public void remove(final String table, final String key) {
		toml.remove(table, key);
	}

	/** Forgets everything one command remembered. */
	public void removeTable(final String table) {
		toml.removeTable(table);
	}

	/**
	 * Writes the settings out.
	 * <p>
	 * NB: through a temporary file and a move, so that a crash midway leaves the
	 * old settings rather than half of the new ones.
	 * </p>
	 */
	public void save() {
		if (file == null) return; // NB: nowhere to save to, which is allowed
		try {
			final Path directory = file.toAbsolutePath().getParent();
			if (directory != null) Files.createDirectories(directory);
			final Path temporary = file.resolveSibling(file.getFileName() + ".new");
			Files.write(temporary, toml.write().getBytes(StandardCharsets.UTF_8));
			try {
				Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING);
			}
			catch (final java.nio.file.AtomicMoveNotSupportedException exc) {
				Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (final IOException exc) {
			throw new UncheckedIOException("Cannot save settings: " + file, exc);
		}
	}

	@Override
	public String toString() {
		return file == null ? "settings (in memory)" : "settings (" + file + ")";
	}

	// -- Helper methods --

	private static Toml read(final Path file) {
		if (file == null || !Files.isReadable(file)) return Toml.empty();
		try {
			return Toml.parse(new String(Files.readAllBytes(file),
				StandardCharsets.UTF_8));
		}
		catch (final IOException exc) {
			throw new UncheckedIOException("Cannot read settings: " + file, exc);
		}
	}

	/**
	 * Maps a value onto one of TOML's types.
	 *
	 * @return the TOML value, or null if nothing there can hold it
	 */
	private static Object toTomlValue(final Object value) {
		// the types TOML has
		if (value instanceof String || value instanceof Boolean || //
			value instanceof LocalDate || value instanceof LocalTime || //
			value instanceof LocalDateTime || value instanceof OffsetDateTime)
		{
			return value;
		}
		// whole numbers, and fractional ones
		if (value instanceof Byte || value instanceof Short || //
			value instanceof Integer || value instanceof Long)
		{
			return ((Number) value).longValue();
		}
		if (value instanceof Float || value instanceof Double) {
			return ((Number) value).doubleValue();
		}
		// NB: as strings, because TOML's integers are 64-bit and its floats are
		// doubles -- storing these as numbers would quietly lose what the type
		// was chosen for.
		if (value instanceof BigInteger || value instanceof BigDecimal) {
			return value.toString();
		}
		if (value instanceof Character || value instanceof Enum) {
			return value.toString();
		}
		// NB: an old-style date is the one type that needs a decision: it is an
		// instant, so it keeps its offset rather than pretending to be local.
		if (value instanceof Date) {
			return OffsetDateTime.ofInstant(((Date) value).toInstant(), ZoneId
				.systemDefault());
		}
		// containers become arrays, element by element
		if (value instanceof Collection) {
			return toTomlArray(((Collection<?>) value).toArray());
		}
		if (value.getClass().isArray()) {
			final int length = Array.getLength(value);
			final Object[] elements = new Object[length];
			for (int i = 0; i < length; i++)
				elements[i] = Array.get(value, i);
			return toTomlArray(elements);
		}
		// NB: last resort, and the reason a File or a URI needs no special case:
		// anything that can describe itself as a string does, and comes back
		// through the converter that reads that string.
		return Converters.get().tryConvert(value, String.class).orElse(null);
	}

	private static Object toTomlArray(final Object[] elements) {
		final List<Object> list = new ArrayList<>(elements.length);
		for (final Object element : elements) {
			final Object stored = toTomlValue(element);
			if (stored == null) return null; // NB: all of it, or none of it
			list.add(stored);
		}
		return list;
	}
}
