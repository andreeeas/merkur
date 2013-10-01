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

package de.materna.cms.merkur.logback;

import java.util.concurrent.CountDownLatch;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

/**
 * {@link MessageListener}-Implementierung für die Integrations-Tests aus
 * {@link LogbackAmqpAppenderIntegrationTests}.
 * 
 * @author andreas
 */
public class TestListener implements MessageListener {

	private final CountDownLatch latch;

	private volatile Message message;

	public TestListener(int count) {
		latch = new CountDownLatch(count);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public Message getMessage() {
		return message;
	}

	public Object getId() {
		if (this.message == null || this.getMessageProperties() == null) {
			throw new IllegalStateException("No MessageProperties received");
		}
		return this.message.getMessageProperties().getMessageId();
	}

	public MessageProperties getMessageProperties() {
		if (this.message == null) {
			throw new IllegalStateException("No Message received");
		}
		return this.message.getMessageProperties();
	}

	public void onMessage(Message message) {
		System.out.println("MESSAGE: " + message);
		System.out.println("BODY: " + new String(message.getBody()));
		this.message = message;
		latch.countDown();
	}

}
