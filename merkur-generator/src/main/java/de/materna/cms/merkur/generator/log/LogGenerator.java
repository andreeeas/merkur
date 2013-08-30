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

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @author andreas
 * 
 *         Einfache Klasse zur Generierung von Log-Nachrichten.
 * 
 *         Sie dient als rudimentäre Applikation zum Testen der Log4J-Anbindung
 *         an den Message-Broker(RabbitMQ).
 */
public final class LogGenerator {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private UUID uuid = UUID.randomUUID();
	private AtomicInteger count = new AtomicInteger();

	// TODO: UUID und delay über System-Properties (-D) angeben(Stichwort:
	// fixedDelayString und @Value)
	@Scheduled(fixedDelay = 1000)
	public void generateLogs() {
		log.debug("[uuid: " + uuid + ",number: " + count.incrementAndGet()
				+ "]");
	}

}
