/*
 * Copyright 2012 andreas.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.materna.cms.merkur.logback.encoder;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.pattern.EnsureExceptionHandling;
import ch.qos.logback.core.pattern.PostCompileProcessor;
import de.materna.cms.merkur.logback.LogbackAmqpAppender;

/**
 * Ein {@link PatternLayoutEncoder}, der zur Generierung von Routing-Keys
 * genutzt werden kann, da er die Ausgabe von Exceptions unterdrückt.
 * 
 * @author andreas
 */
public class RoutingKeyPatternLayoutEncoder extends PatternLayoutEncoder {

	/**
	 * Konvertierungswort zur Unterdrückung von Exceptions in der Logausgabe
	 */
	private static final String CONVERSION_WORD_SUPPRESS_EXCEPTIONS = "%nopex";

	/**
	 * Fügt dem Pattern das Konvertierungswort "nopex" hinzu, falls es nicht
	 * schon enthalten ist.
	 * 
	 * <p>
	 * Wird eine Lognachricht zusammen mit einer Exception gelogt, so sorgt der
	 * Logback-interne Mechanismus über den {@link PostCompileProcessor}
	 * {@link EnsureExceptionHandling} dafür, dass der Stacktrace der Exception
	 * in jedem Fall mit durch das Pattern gerendert wird.
	 * 
	 * <p>
	 * Da dies für das Rendern des Routing-Keys des {@link LogbackAmqpAppender}
	 * nicht gewünscht ist, muss das o.g. Konvertierungswort ggf. ergänzt
	 * werden.
	 * 
	 * @see ch.qos.logback.classic.encoder.PatternLayoutEncoder#start()
	 */
	@Override
	public void start() {
		suppressExceptionsInPatternIfRequired();
		super.start();
	}

	private void suppressExceptionsInPatternIfRequired() {
		if (!getPattern().contains(CONVERSION_WORD_SUPPRESS_EXCEPTIONS)) {
			setPattern(getPattern() + CONVERSION_WORD_SUPPRESS_EXCEPTIONS);
		}
	}

}