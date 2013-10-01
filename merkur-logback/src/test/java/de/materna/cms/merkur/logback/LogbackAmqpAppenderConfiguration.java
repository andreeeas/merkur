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

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * <p>
 * Konfiguration des Spring {@link AnnotationConfigApplicationContext} für die
 * Integrations-Tests des {@link LogbackAmqpAppender}.
 * 
 * <p>
 * Stellt die folgenden Koponenten bereit:
 * 
 * <ul>
 * <li>Eine {@link ConnectionFactory}, die die Verbindung zum Message-Broker
 * herstellt
 * <li>Ein {@link TopicExchange}, an den die Log-Nachrichten gesendet werden
 * <li>Eine {@link Queue}, die über ein {@link Binding} an den
 * {@link TopicExchange} gebunden wird
 * <li>Ein {@link RabbitAdmin} zur Deklaration der {@link Queue} und des
 * {@link Binding}
 * <li>Ein {@link SimpleMessageListenerContainer}, der in der {@link Queue}
 * eingehende Nachrichten empfängt und an einen {@link MessageListener}
 * weiterleitet, der die Nachricht seinerseits verarbeitet.
 * </ul>
 * 
 * @author andreas
 */
@Configuration
public class LogbackAmqpAppenderConfiguration {

	private static final String QUEUE = "logback.amqp.appender.test";
	private static final String EXCHANGE = "amq.topic";
	private static final String ROUTING_KEY = "LogbackAmqpAppenderTest.#";

	@Bean
	public CachingConnectionFactory connectionFactory() {
		return new CachingConnectionFactory();
	}

	@Bean
	public TopicExchange testExchange() {
		return new TopicExchange(EXCHANGE, true, false);
	}

	@Bean
	public Queue testQueue() {
		return new Queue(QUEUE);
	}

	@Bean
	public Binding testBinding() {
		return BindingBuilder.bind(testQueue()).to(testExchange())
				.with(ROUTING_KEY);
	}

	@Bean
	public RabbitAdmin rabbitAdmin() {
		return new RabbitAdmin(connectionFactory());
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public SimpleMessageListenerContainer listenerContainer() {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				connectionFactory());
		Queue q = testQueue();

		RabbitAdmin admin = rabbitAdmin();
		admin.declareQueue(q);
		admin.declareBinding(testBinding());

		container.setQueues(q);
		container.setAutoStartup(false);
		container.setAcknowledgeMode(AcknowledgeMode.AUTO);

		return container;
	}

	@Bean
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public TestListener testListener(int count) {
		return new TestListener(count);
	}

}
