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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Integrations-Tests für den {@link LogbackAmqpAppender}.
 * 
 * @author andreas
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LogbackAmqpAppenderConfiguration.class, loader = AnnotationConfigContextLoader.class)
public class LogbackAmqpAppenderIntegrationTests {

	@Rule
	public BrokerRunning brokerIsRunning = BrokerRunning.isRunning();

	@Autowired
	private ApplicationContext applicationContext;

	private Logger log;

	private SimpleMessageListenerContainer listenerContainer;

	@Before
	public void setUp() throws Exception {
		log = (Logger) LoggerFactory.getLogger(getClass());
		listenerContainer = applicationContext
				.getBean(SimpleMessageListenerContainer.class);
	}

	@After
	public void tearDown() {
		listenerContainer.shutdown();
	}

	@Test
	public void testAppend_Initialize() {
		final AtomicInteger count = new AtomicInteger();
		final LoggingEvent event = sampleLoggingEvent();
		final LogbackAmqpAppender appender = new LogbackAmqpAppender() {

			@Override
			protected void maybeDeclareExchange() {
				super.maybeDeclareExchange();
				if (count.incrementAndGet() < 2) {
					// ensure we don't try to initialize again while
					// initializing
					append(event);
				}
			}

		};
		appender.append(event);
		assertEquals(1, count.get());
	}

	@Test
	public void testAppend_InitializeWithRetry() {
		final AtomicInteger count = new AtomicInteger();
		final LoggingEvent event = sampleLoggingEvent();
		final LogbackAmqpAppender appender = new LogbackAmqpAppender() {

			@Override
			protected void maybeDeclareExchange() {
				super.maybeDeclareExchange();
				if (count.incrementAndGet() < 2) {
					throw new RuntimeException("foo");
				}
			}

		};
		try {
			appender.append(event);
			fail("Expected exception");
		} catch (RuntimeException e) {
			assertEquals("foo", e.getMessage());
		}
		// ensure we initialize again if the first time failed
		appender.append(event);
		assertEquals(2, count.get());
	}

	@Test
	public void testAppend_ReceivesMessagesWithinTimeAndMessagesHaveMessageId()
			throws InterruptedException {
		final TestListener testListener = (TestListener) applicationContext
				.getBean("testListener", 4);
		listenerContainer.setMessageListener(testListener);
		listenerContainer.start();

		final Logger log = (Logger) LoggerFactory.getLogger(getClass());

		log.debug("This is a DEBUG message");
		log.info("This is an INFO message");
		log.warn("This is a WARN message");
		log.error("This is an ERROR message", new RuntimeException(
				"Test exception"));

		assertTrue(testListener.getLatch().await(5, TimeUnit.SECONDS));
		assertNotNull(testListener.getId());
	}

	@Test
	public void testAppend_ReceivesMessagesWithinTimeAndCanAccessMDCProperties()
			throws InterruptedException {
		final TestListener testListener = (TestListener) applicationContext
				.getBean("testListener", 4);
		listenerContainer.setMessageListener(testListener);
		listenerContainer.start();

		final String propertyName = "someproperty";
		final String propertyValue = "property.value";
		MDC.put(propertyName, propertyValue);
		log.debug("This is a DEBUG message with properties");
		log.info("This is an INFO message with properties");
		log.warn("This is a WARN message with properties");
		log.error("This is an ERROR message with properties",
				new RuntimeException("Test exception"));
		MDC.remove(propertyName);

		assertTrue(testListener.getLatch().await(5, TimeUnit.SECONDS));
		final MessageProperties messageProperties = testListener
				.getMessageProperties();
		assertNotNull(messageProperties);
		assertNotNull(messageProperties.getHeaders().get(propertyName));
		assertEquals(propertyValue,
				messageProperties.getHeaders().get(propertyName));
	}

	@Test
	public void testAppend_ReceivesMessagesWithSpecifiedCharset()
			throws InterruptedException {
		final Logger rootLogger = (Logger) LoggerFactory
				.getLogger(Logger.ROOT_LOGGER_NAME);
		final LogbackAmqpAppender appender = (LogbackAmqpAppender) rootLogger
				.getAppender("amqp");
		assertEquals("UTF-8", appender.getCharset());

		final TestListener testListener = (TestListener) applicationContext
				.getBean("testListener", 1);
		listenerContainer.setMessageListener(testListener);
		listenerContainer.start();

		final String foo = new String("\u0fff"); // UTF-8 -> 0xe0bfbf
		log.info(foo);
		assertTrue(testListener.getLatch().await(5, TimeUnit.SECONDS));
		final byte[] body = testListener.getMessage().getBody();
		assertEquals(0xe0, body[0] & 0xff);
		assertEquals(0xbf, body[1] & 0xff);
		assertEquals(0xbf, body[2] & 0xff);
	}

	private LoggingEvent sampleLoggingEvent() {
		final LoggingEvent event = new LoggingEvent();
		event.setLoggerName("foo");
		event.setLevel(Level.INFO);
		event.setMessage("bar");
		final Map<String, String> map = new HashMap<String, String>();
		event.setMDCPropertyMap(map);
		event.setLoggerContextRemoteView(new LoggerContextVO("foo", map, 0));
		return event;
	}

}