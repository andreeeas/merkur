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

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.log4j.AmqpAppender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;

/**
 * Logback-Version des {@link AmqpAppender}, deren Implementierung auf dieser <a
 * href="http://logback.qos.ch/manual/migrationFromLog4j.html">Anleitung</a>
 * basiert.
 * 
 * @author andreas
 */
public class LogbackAmqpAppender extends AppenderBase<ILoggingEvent> {

	/**
	 * Key name for the application id (if there is one set via the appender
	 * config) in the message properties.
	 */
	public static final String APPLICATION_ID = "applicationId";

	/**
	 * Key name for the logger category name in the message properties
	 */
	public static final String CATEGORY_NAME = "categoryName";

	/**
	 * Key name for the logger level name in the message properties
	 */
	public static final String CATEGORY_LEVEL = "level";

	/**
	 * Name of the exchange to publish log events to.
	 */
	private String exchangeName = "logs";

	/**
	 * Type of the exchange to publish log events to.
	 */
	private String exchangeType = "topic";

	/**
	 * Log4J pattern format to use to generate a routing key.
	 */
	private String routingKeyPattern = "%c.%p";

	/**
	 * Log4J Layout to use to generate routing key.
	 */
	private Layout<ILoggingEvent> routingKeyLayout = new PatternLayout() {
		{
			setPattern(routingKeyPattern);
		}
	};

	/**
	 * Used to synchronize access to pattern layouts.
	 */
	private final Object layoutMutex = new Object();

	/**
	 * Whether or not we've tried to declare this exchange yet.
	 */
	private final AtomicBoolean exchangeDeclared = new AtomicBoolean(false);

	/**
	 * Configuration arbitrary application ID.
	 */
	private String applicationId = null;

	/**
	 * Where LoggingEvents are queued to send.
	 */
	private final LinkedBlockingQueue<Event> events = new LinkedBlockingQueue<Event>();

	/**
	 * The pool of senders.
	 */
	private ExecutorService senderPool = null;

	/**
	 * How many senders to use at once. Use more senders if you have lots of log
	 * output going through this appender.
	 */
	private int senderPoolSize = 2;

	/**
	 * How many times to retry sending a message if the broker is unavailable or
	 * there is some other error.
	 */
	private int maxSenderRetries = 30;

	/**
	 * Retries are delayed like: N ^ log(N), where N is the retry number.
	 */
	private final Timer retryTimer = new Timer("log-event-retry-delay", true);

	/**
	 * RabbitMQ ConnectionFactory.
	 */
	private AbstractConnectionFactory connectionFactory;

	/**
	 * RabbitMQ host to connect to.
	 */
	private String host = "localhost";

	/**
	 * RabbitMQ virtual host to connect to.
	 */
	private String virtualHost = "/";

	/**
	 * RabbitMQ port to connect to.
	 */
	private int port = 5672;

	/**
	 * RabbitMQ user to connect as.
	 */
	private String username = "guest";

	/**
	 * RabbitMQ password for this user.
	 */
	private String password = "guest";

	/**
	 * Default content-type of log messages.
	 */
	private String contentType = "text/plain";

	/**
	 * Default content-encoding of log messages.
	 */
	private String contentEncoding = null;

	/**
	 * Whether or not to try and declare the configured exchange when this
	 * appender starts.
	 */
	private boolean declareExchange = false;

	/**
	 * charset to use when converting String to byte[], default null (system
	 * default charset used). If the charset is unsupported on the current
	 * platform, we fall back to using the system charset.
	 */
	private String charset;

	private boolean durable = true;

	private boolean autoDelete = false;

	/**
	 * Used to determine whether {@link MessageProperties#setMessageId(String)}
	 * is set.
	 */
	private boolean generateId = false;

	private final AtomicBoolean initializing = new AtomicBoolean();

	public LogbackAmqpAppender() {
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}

	public String getRoutingKeyPattern() {
		return routingKeyPattern;
	}

	public void setRoutingKeyPattern(String routingKeyPattern) {
		this.routingKeyPattern = routingKeyPattern;
		final PatternLayout patternLayout = new PatternLayout();
		patternLayout.setPattern(routingKeyPattern);
		this.routingKeyLayout = patternLayout;
	}

	public boolean isDeclareExchange() {
		return declareExchange;
	}

	public void setDeclareExchange(boolean declareExchange) {
		this.declareExchange = declareExchange;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public int getSenderPoolSize() {
		return senderPoolSize;
	}

	public void setSenderPoolSize(int senderPoolSize) {
		this.senderPoolSize = senderPoolSize;
	}

	public int getMaxSenderRetries() {
		return maxSenderRetries;
	}

	public void setMaxSenderRetries(int maxSenderRetries) {
		this.maxSenderRetries = maxSenderRetries;
	}

	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public boolean isAutoDelete() {
		return autoDelete;
	}

	public void setAutoDelete(boolean autoDelete) {
		this.autoDelete = autoDelete;
	}

	public boolean isGenerateId() {
		return generateId;
	}

	public void setGenerateId(boolean generateId) {
		this.generateId = generateId;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Startet einen {@link ExecutorService} mit {@link #getSenderPoolSize()}
	 * asynchronen {@link EventSender}.
	 * 
	 * @see Executors#newCachedThreadPool()
	 * @see EventSender
	 */
	protected void startSenders() {
		senderPool = Executors.newCachedThreadPool();
		for (int i = 0; i < senderPoolSize; i++) {
			senderPool.submit(new EventSender());
		}
	}

	/**
	 * Deklariert den {@link Exchange}, falls {@link #isDeclareExchange()}
	 * {@code true} ist. Der Typ des {@link Exchange} richtet sich nach der
	 * Property {@link #getExchangeType()}.
	 * 
	 * @see Exchange
	 * @see RabbitAdmin#declareExchange(Exchange)
	 */
	protected void maybeDeclareExchange() {
		RabbitAdmin admin = new RabbitAdmin(connectionFactory);
		if (declareExchange) {
			Exchange x;
			if ("topic".equals(exchangeType)) {
				x = new TopicExchange(exchangeName, durable, autoDelete);
			} else if ("direct".equals(exchangeType)) {
				x = new DirectExchange(exchangeName, durable, autoDelete);
			} else if ("fanout".equals(exchangeType)) {
				x = new FanoutExchange(exchangeName, durable, autoDelete);
			} else if ("headers".equals(exchangeType)) {
				x = new HeadersExchange(exchangeType, durable, autoDelete);
			} else {
				x = new TopicExchange(exchangeName, durable, autoDelete);
			}
			// admin.deleteExchange(exchangeName);
			admin.declareExchange(x);
		}
	}

	@Override
	protected void append(ILoggingEvent loggingevent) {
		if (null == senderPool && this.initializing.compareAndSet(false, true)) {
			try {
				connectionFactory = new CachingConnectionFactory();
				connectionFactory.setHost(host);
				connectionFactory.setPort(port);
				connectionFactory.setUsername(username);
				connectionFactory.setPassword(password);
				connectionFactory.setVirtualHost(virtualHost);
				maybeDeclareExchange();
				exchangeDeclared.set(true);

				startSenders();
			} finally {
				this.initializing.set(false);
			}
		}
		events.add(new Event(loggingevent, loggingevent.getMDCPropertyMap()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.core.AppenderBase#stop()
	 */
	@Override
	public void stop() {
		if (null != senderPool) {
			senderPool.shutdownNow();
			senderPool = null;
		}
		if (null != connectionFactory) {
			connectionFactory.destroy();
		}
		retryTimer.cancel();
		super.stop();
	}

	public boolean requiresLayout() {
		return true;
	}

	/**
	 * Hilfsklasse zur asynchronen Versendung von {@link ILoggingEvent}
	 * -Instanzen. Instanzen werden durch die Methode {@link #startSenders()}
	 * bei einem {@link ExecutorService} registriert.
	 */
	protected class EventSender implements Runnable {
		public void run() {
			try {
				RabbitTemplate rabbitTemplate = new RabbitTemplate(
						connectionFactory);
				while (true) {
					final Event event = events.take();
					final ILoggingEvent logEvent = event.getEvent();

					final String name = logEvent.getLoggerName();
					final Level level = logEvent.getLevel();

					final MessageProperties amqpProps = new MessageProperties();
					amqpProps.setContentType(contentType);
					if (null != contentEncoding) {
						amqpProps.setContentEncoding(contentEncoding);
					}
					amqpProps.setHeader(CATEGORY_NAME, name);
					amqpProps.setHeader(CATEGORY_LEVEL, level.toString());
					if (generateId) {
						amqpProps.setMessageId(UUID.randomUUID().toString());
					}

					// Set applicationId, if we're using one
					if (null != applicationId) {
						amqpProps.setAppId(applicationId);
						MDC.put(APPLICATION_ID, applicationId);
					}

					// Set timestamp
					final Calendar tstamp = Calendar.getInstance();
					tstamp.setTimeInMillis(logEvent.getTimeStamp());
					amqpProps.setTimestamp(tstamp.getTime());

					// Copy properties in from MDC
					final Map<String, String> props = event.getProperties();
					final Set<Entry<String, String>> entrySet = props
							.entrySet();
					for (final Entry<String, String> entry : entrySet) {
						amqpProps.setHeader(entry.getKey().toString(),
								entry.getValue());
					}

					final StackTraceElement[] callerData = logEvent
							.getCallerData();
					if (callerData.length > 0) {
						final StackTraceElement firstCallerData = callerData[0];
						if (!"?".equals(firstCallerData.getClassName())) {
							amqpProps.setHeader("location", String.format(
									"%s.%s()[%s]",
									firstCallerData.getClassName(),
									firstCallerData.getMethodName(),
									firstCallerData.getLineNumber()));
						}
					}

					StringBuilder msgBody;
					String routingKey;
					synchronized (layoutMutex) {
						msgBody = new StringBuilder(
								logEvent.getFormattedMessage());
						if (!routingKeyLayout.isStarted()) {
							routingKeyLayout.start();
						}

						routingKey = "LogbackAmqpAppenderTest.Test";
					}
					if (null != logEvent.getThrowableProxy()) {
						final IThrowableProxy tproxy = logEvent
								.getThrowableProxy();
						msgBody.append(String.format("%s%n",
								tproxy.getMessage()));
						for (final StackTraceElementProxy line : tproxy
								.getStackTraceElementProxyArray()) {
							msgBody.append(String.format("%s%n", line));
						}
					}

					// Send a message
					try {
						Message message = null;
						if (LogbackAmqpAppender.this.charset != null) {
							try {
								message = new Message(
										msgBody.toString()
												.getBytes(
														LogbackAmqpAppender.this.charset),
										amqpProps);
							} catch (UnsupportedEncodingException e) {/*
																	 * fall back
																	 * to
																	 * default
																	 */
							}
						}
						if (message == null) {
							message = new Message(
									msgBody.toString().getBytes(), amqpProps);
						}
						rabbitTemplate.send(exchangeName, routingKey, message);
					} catch (AmqpException e) {
						final int retries = event.incrementRetries();
						if (retries < maxSenderRetries) {
							// Schedule a retry based on the number of times
							// I've tried to re-send this
							retryTimer
									.schedule(
											new TimerTask() {
												@Override
												public void run() {
													events.add(event);
												}
											},
											(long) (Math.pow(retries,
													Math.log(retries)) * 1000));
						} else {
							final String errorMessage = "Could not send log message "
									+ logEvent.getFormattedMessage()
									+ " after " + maxSenderRetries + " retries";
							addError(errorMessage, e);
						}
					} finally {
						if (null != applicationId) {
							MDC.remove(APPLICATION_ID);
						}
					}
				}
			} catch (Throwable t) {
				throw new RuntimeException(t.getMessage(), t);
			}
		}
	}

	/**
	 * Hilfsklasse zur Kapselung eines {@link ILoggingEvent}, seiner MDC
	 * Properties und der Anzahl der Sendeversuche.
	 */
	protected class Event {
		private final ILoggingEvent event;
		private final Map<String, String> properties;
		private final AtomicInteger retries = new AtomicInteger(0);

		public Event(ILoggingEvent event, Map<String, String> properties) {
			this.event = event;
			this.properties = properties;
		}

		public ILoggingEvent getEvent() {
			return event;
		}

		public Map<String, String> getProperties() {
			return properties;
		}

		public int incrementRetries() {
			return retries.incrementAndGet();
		}
	}

}