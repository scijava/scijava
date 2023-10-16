/*-
 * #%L
 * SciJava Operations Engine: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2023 SciJava developers.
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

package org.scijava.ops.engine.log;

import static org.scijava.log2.LogLevel.DEBUG;
import static org.scijava.log2.LogLevel.ERROR;
import static org.scijava.log2.LogLevel.INFO;
import static org.scijava.log2.LogLevel.TRACE;
import static org.scijava.log2.LogLevel.WARN;

import org.scijava.log2.LogLevel;
import org.scijava.log2.LogListener;
import org.scijava.log2.LogMessage;
import org.scijava.log2.LogSource;

/**
 * TODO: Dummy class. To be replaced or properly implemented later. See:
 * {@literal https://github.com/scijava/scijava-ops/issues/13}
 */
public class Logger {

	public Logger() {
	}

	public void debug(final Object msg) {
		log(DEBUG, msg);
	}

	public void debug(final Throwable t) {
		log(DEBUG, t);
	}

	public void debug(final Object msg, final Throwable t) {
		log(DEBUG, msg, t);
	}

	public void error(final Object msg) {
		log(ERROR, msg);
	}

	public void error(final Throwable t) {
		log(ERROR, t);
	}

	public void error(final Object msg, final Throwable t) {
		log(ERROR, msg, t);
	}

	public void info(final Object msg) {
		log(INFO, msg);
	}

	public void info(final Throwable t) {
		log(INFO, t);
	}

	public void info(final Object msg, final Throwable t) {
		log(INFO, msg, t);
	}

	public void trace(final Object msg) {
		log(TRACE, msg);
	}

	public void trace(final Throwable t) {
		log(TRACE, t);
	}

	public void trace(final Object msg, final Throwable t) {
		log(TRACE, msg, t);
	}

	public void warn(final Object msg) {
		log(WARN, msg);
	}

	public void warn(final Throwable t) {
		log(WARN, t);
	}

	public void warn(final Object msg, final Throwable t) {
		log(WARN, msg, t);
	}

	public boolean isDebug() {
		return isLevel(DEBUG);
	}

	public boolean isError() {
		return isLevel(ERROR);
	}

	public boolean isInfo() {
		return isLevel(INFO);
	}

	public boolean isTrace() {
		return isLevel(TRACE);
	}

	public boolean isWarn() {
		return isLevel(WARN);
	}

	public boolean isLevel(final int level) {
		return getLevel() >= level;
	}

	/**
	 * Logs a message.
	 *
	 * @param level The level at which the message will be logged. If the current
	 *          level (given by {@link #getLevel()} is below this one, no logging
	 *          is performed.
	 * @param msg The message to log.
	 */
	public void log(final int level, final Object msg) {
		log(level, msg, null);
	}

	/**
	 * Logs an exception.
	 *
	 * @param level The level at which the exception will be logged. If the
	 *          current level (given by {@link #getLevel()} is below this one, no
	 *          logging is performed.
	 * @param t The exception to log.
	 */
	public void log(final int level, final Throwable t) {
		log(level, null, t);
	}

	/**
	 * Logs a message with an exception.
	 *
	 * @param level The level at which the information will be logged. If the
	 *          current level (given by {@link #getLevel()} is below this one, no
	 *          logging is performed.
	 * @param msg The message to log.
	 * @param t The exception to log.
	 */
	public void log(final int level, final Object msg, final Throwable t) {
		if (isLevel(level)) alwaysLog(level, msg, t);
	}

	/**
	 * Logs a message with an exception. This message will always be logged even
	 * if its level is above the current level (given by {@link #getLevel()}).
	 *
	 * @param level The level at which the information will be logged.
	 * @param msg The message to log.
	 * @param t The exception to log.
	 */
	public void alwaysLog(final int level, final Object msg,
		final Throwable t)
	{
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Returns the name of this logger. */
	public String getName() {
		return getSource().name();
	}

	/** Returns the {@link LogSource} associated with this logger. */
	public LogSource getSource() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Returns the log level of this logger. see {@link LogLevel} */
	public int getLevel() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Creates a sub logger, that forwards the message it gets to this logger. The
	 * sub logger will have the same log level as this logger.
	 */
	public Logger subLogger(final String name) {
		return subLogger(name, getLevel());
	}

	/**
	 * Creates a sub logger, that forwards the message it gets to this logger.
	 *
	 * @param name The name of the sub logger.
	 * @param level The log level of the sub logger.
	 */
	public Logger subLogger(final String name, final int level) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Adds an item to the list of registered listeners. */
	public void addLogListener(final LogListener listener) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Removes an item from the list of registered listeners. */
	public void removeLogListener(final LogListener listener) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	/** Broadcasts the given log message to the registered listeners. */
	public void notifyListeners(final LogMessage message) {
		throw new UnsupportedOperationException("not yet implemented");
	}

}
