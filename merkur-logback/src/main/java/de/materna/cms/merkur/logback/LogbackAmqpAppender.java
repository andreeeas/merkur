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
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import de.materna.cms.merkur.logback.encoder.RoutingKeyPatternLayoutEncoder;

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
	 * Pattern format to use to generate a routing key.
	 */
	private String routingKeyPattern = "%c.%p";

	/**
	 * Encoder to use to generate routing key.
	 */
	private PatternLayoutEncoder routingKeyEncoder = encoderForRoutingKeyPattern();
	
	/**
	 * Pattern format to use for log messages.
	 */
	private String logMessagesPattern = "%d [%t] %-5p %c{1.} - %m%n";

	/**
	 * Encoder to use for log messages.
	 */
	private PatternLayoutEncoder logMessagesEncoder = logMessagesEncoder();

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
		this.routingKeyEncoder = encoderForRoutingKeyPattern();
	}

	public Layout<ILoggingEvent> getRoutingKeyLayout() {
		return routingKeyEncoder.getLayout();
	}
	
	public String getLogMessagesPattern() {
		return logMessagesPattern;
	}

	public void setLogMessagesPattern(String logMessagesPattern) {
		this.logMessagesPattern = logMessagesPattern;
		this.logMessagesEncoder = logMessagesEncoder();
	}

	public Layout<ILoggingEvent> getLogMessagesLayout() {
		return logMessagesEncoder.getLayout();
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
	 * asynchronen {@link EventSender}-Instanzen.
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

	/**
	 * Zentrale Methode zur Versendung der Log-Nachrichten an den
	 * Message-Broker.
	 * 
	 * <p>
	 * Beim initialen Aufruf wird zunächst eine Verbindung mit dem
	 * Message-Broker aufgebaut. Darüber hinaus wird der {@link Exchange}
	 * deklariert, sofern er noch nicht existiert.
	 * 
	 * <p>
	 * Ist die Verbindung hergestellt, so wird das empfangene
	 * {@link ILoggingEvent} zur asynchronen Weiterverarbeitung an den
	 * {@link #senderPool} geschickt.
	 * 
	 * @see EventSender
	 * @see CachingConnectionFactory
	 */
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

	/**
	 * Stoppt den Appender. Dies erfolgt in folgenden Schritten:
	 * 
	 * <ol>
	 * <li>Fährt den {@link #senderPool} herunter, der für das Verschicken von
	 * Log-Nachrichten zuständig ist.
	 * <li>Schließt die Verbindung zum Message-Broker.
	 * <li>Stoppt den {@link Timer}, der für die Wiederholungsversuche
	 * fehlgeschlagener Versendungen verwendet wird.
	 * </ol>
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
					sendEvent(rabbitTemplate, event);
				}
			} catch (Throwable t) {
				throw new RuntimeException(t.getMessage(), t);
			}
		}

		/**
		 * Versendet {@link Event}-Instanzen über ein {@link RabbitTemplate}.
		 * 
		 * @param rabbitTemplate
		 *            {@link RabbitTemplate} zur Versendung von
		 *            {@link ILoggingEvent}-Instanzen
		 * @param event
		 *            {@link Event} zur Kapselung des {@link ILoggingEvent} und
		 *            der zugrunde liegenden Properties
		 */
		private void sendEvent(final RabbitTemplate rabbitTemplate,
				final Event event) {
			final ILoggingEvent logEvent = event.getEvent();

			final String name = logEvent.getLoggerName();
			final Level level = logEvent.getLevel();

			final MessageProperties amqpProps = new MessageProperties();

			// muss vor den addXYZ-Methoden aufgerufen werden, da der MDC
			// ansonsten noch nicht korrekt zur Verfügung steht
			prepareMDC(event);

			// MessageProperties initialisieren
			addBasicHeaders(name, level, amqpProps);
			addTimestampToHeaders(logEvent, amqpProps);
			addMDCPropertiesToHeaders(event, amqpProps);
			addLocationHeaderFromCallerData(logEvent, amqpProps);

			// Nachricht zusammenbauen
			StringBuilder msgBody;
			String routingKey;
			final String formattedMessage = getLogMessagesLayout().doLayout(logEvent);
			synchronized (layoutMutex) {
				msgBody = new StringBuilder(formattedMessage);
				routingKey = getRoutingKeyLayout().doLayout(logEvent);
			}
			if (null != logEvent.getThrowableProxy()) {
				final IThrowableProxy tproxy = logEvent.getThrowableProxy();
				msgBody.append(String.format("%s%n", tproxy.getMessage()));
				for (final StackTraceElementProxy line : tproxy
						.getStackTraceElementProxyArray()) {
					msgBody.append(String.format("%s%n", line));
				}
			}

			// Nachricht versenden
			try {
				Message message = null;
				if (LogbackAmqpAppender.this.charset != null) {
					try {
						message = new Message(msgBody.toString().getBytes(
								LogbackAmqpAppender.this.charset), amqpProps);
					} catch (UnsupportedEncodingException e) {
						message = new Message(msgBody.toString().getBytes(),
								amqpProps);
					}
				}
				rabbitTemplate.send(exchangeName, routingKey, message);
			} catch (AmqpException e) {
				final int retries = event.incrementRetries();
				if (retries < maxSenderRetries) {
					// Versuch wiederholen
					retryTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							events.add(event);
						}
					}, (long) (Math.pow(retries, Math.log(retries)) * 1000));
				} else {
					final String errorMessage = "Could not send log message "
							+ formattedMessage + " after "
							+ maxSenderRetries + " retries";
					addError(errorMessage, e);
				}
			} finally {
				cleanMDC();
			}
		}

		private void cleanMDC() {
			if (null != applicationId) {
				MDC.remove(APPLICATION_ID);
			}
		}

		private void prepareMDC(final Event event) {
			if (null != applicationId) {
				MDC.put(APPLICATION_ID, applicationId);
			}

			@SuppressWarnings("unchecked")
			final Map<String, String> copyOfMDCPropertyMap = MDC
					.getCopyOfContextMap();
			if (null != copyOfMDCPropertyMap) {
				event.getProperties().putAll(copyOfMDCPropertyMap);
			}
		}

		private void addBasicHeaders(final String name, final Level level,
				final MessageProperties amqpProps) {
			amqpProps.setContentType(contentType);
			if (null != contentEncoding) {
				amqpProps.setContentEncoding(contentEncoding);
			}
			amqpProps.setHeader(CATEGORY_NAME, name);
			amqpProps.setHeader(CATEGORY_LEVEL, level.toString());
			if (generateId) {
				amqpProps.setMessageId(UUID.randomUUID().toString());
			}

			if (null != applicationId) {
				amqpProps.setAppId(applicationId);
			}
		}

		private void addTimestampToHeaders(final ILoggingEvent logEvent,
				final MessageProperties amqpProps) {
			final Calendar tstamp = Calendar.getInstance();
			tstamp.setTimeInMillis(logEvent.getTimeStamp());
			amqpProps.setTimestamp(tstamp.getTime());
		}

		private void addMDCPropertiesToHeaders(final Event event,
				final MessageProperties amqpProps) {
			final Map<String, String> props = event.getProperties();
			final Set<Entry<String, String>> entrySet = props.entrySet();
			for (final Entry<String, String> entry : entrySet) {
				amqpProps
						.setHeader(entry.getKey().toString(), entry.getValue());

			}
		}

		private void addLocationHeaderFromCallerData(
				final ILoggingEvent logEvent, final MessageProperties amqpProps) {
			final StackTraceElement[] callerData = logEvent.getCallerData();
			if (callerData.length > 0) {
				final StackTraceElement firstCallerData = callerData[0];
				if (!"?".equals(firstCallerData.getClassName())) {
					amqpProps.setHeader("location", String.format(
							"%s.%s()[%s]", firstCallerData.getClassName(),
							firstCallerData.getMethodName(),
							firstCallerData.getLineNumber()));
				}
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
	
	private PatternLayoutEncoder logMessagesEncoder() {
		final PatternLayoutEncoder patternLayoutEncoder = new PatternLayoutEncoder();
		patternLayoutEncoder.setPattern(logMessagesPattern);
		patternLayoutEncoder.setContext(getContext());
		patternLayoutEncoder.start();
		return patternLayoutEncoder;
	}

	private PatternLayoutEncoder encoderForRoutingKeyPattern() {
		final PatternLayoutEncoder patternLayoutEncoder = new RoutingKeyPatternLayoutEncoder();
		patternLayoutEncoder.setPattern(routingKeyPattern);
		patternLayoutEncoder.setContext(getContext());
		patternLayoutEncoder.start();
		return patternLayoutEncoder;
	}

}