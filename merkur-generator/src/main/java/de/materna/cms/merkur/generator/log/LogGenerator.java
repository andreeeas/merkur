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

package de.materna.cms.merkur.generator.log;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

/**
 * Einfache Klasse zur Generierung von Log-Nachrichten.
 * 
 * Sie dient als rudimentäre Applikation zum Testen der Log-Anbindung an den
 * Message-Broker.
 * 
 * @author andreas
 */
public class LogGenerator implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private AtomicInteger count = new AtomicInteger();

	@Value("${id:LogGenerator}")
	private String id;

	@Value("${delay:1000}")
	private String delay;

	private int index;

	/**
	 * Setzt den Index des {@link LogGenerator}. Praktisch ist dies zur
	 * Unterscheidung mehrerer laufender Instanzen.
	 * 
	 * @param index
	 *            der Index des {@link LogGenerator}
	 * @throws IllegalArgumentException
	 *             wenn {@code index} < 0 ist
	 */
	public void setIndex(int index) {
		checkArgument(index > -1, "Der Index muss positiv sein!");
		this.index = index;
	}

	/**
	 * Ermittelt das Intervall in Millisekunden, in dem die Methode
	 * {@link #run()} aufgerufen werden soll. Quelle ist der Parameter --delay,
	 * der beim Programmaufruf übergeben werden kann. Wird dieser nicht
	 * angegeben, so wird ein Default von 1000ms gewählt.
	 * 
	 * @return das Intervall, in dem die Methode {@link #run()} aufgerufen
	 *         werden soll, niemals {@code null}
	 */
	public long taskDelay() {
		return Objects.firstNonNull(Ints.tryParse(delay), 1000).longValue();
	}

	/**
	 * Generiert Logs.
	 */
	public void run() {
		log.debug("[id: " + id + "{" + index + "}" + ",number: "
				+ count.incrementAndGet() + "]");
	}

	/**
	 * Prüft Invarianten des Objekts nach der Konstruktion.
	 */
	@PostConstruct
	public void checkObjectState() {
		checkState(taskDelay() > 0, "Das Aufrufintervall muss positiv sein!");
	}

}