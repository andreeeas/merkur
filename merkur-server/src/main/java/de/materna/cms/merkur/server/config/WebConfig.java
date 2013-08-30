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

package de.materna.cms.merkur.server.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.handler.AnnotationMethodMessageHandler;
import org.springframework.messaging.simp.handler.SimpleBrokerMessageHandler;
import org.springframework.messaging.simp.handler.SimpleUserQueueSuffixResolver;
import org.springframework.messaging.simp.handler.UserDestinationMessageHandler;
import org.springframework.messaging.simp.stomp.StompBrokerRelayMessageHandler;
import org.springframework.messaging.simp.stomp.StompWebSocketHandler;
import org.springframework.messaging.support.channel.ExecutorSubscribableChannel;
import org.springframework.messaging.support.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.support.converter.MessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.socket.sockjs.SockJsService;
import org.springframework.web.socket.sockjs.support.DefaultSockJsService;
import org.springframework.web.socket.sockjs.support.SockJsHttpRequestHandler;

/**
 * @author andreas
 * 
 *         Konfiguration des Spring {@link WebApplicationContext}.
 */
@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

	private final MessageConverter<?> messageConverter = new MappingJackson2MessageConverter();

	private final SimpleUserQueueSuffixResolver userQueueSuffixResolver = new SimpleUserQueueSuffixResolver();

	@Bean
	public SimpleUrlHandlerMapping handlerMapping() {

		SockJsService sockJsService = new DefaultSockJsService(taskScheduler());
		HttpRequestHandler requestHandler = new SockJsHttpRequestHandler(
				sockJsService, stompWebSocketHandler());

		SimpleUrlHandlerMapping hm = new SimpleUrlHandlerMapping();
		hm.setOrder(-1);
		hm.setUrlMap(Collections.singletonMap("/merkur-server/**",
				requestHandler));
		return hm;
	}

	// WebSocketHandler fuer STOMP Nachrichten

	@Bean
	public StompWebSocketHandler stompWebSocketHandler() {
		StompWebSocketHandler handler = new StompWebSocketHandler(
				dispatchChannel());
		handler.setUserQueueSuffixResolver(this.userQueueSuffixResolver);
		webSocketHandlerChannel().subscribe(handler);
		return handler;
	}

	// MessageHandler, der Nachrichten an solche Methoden delegiert, die sich in
	// einer @Controller annotierten Klasse befinden

	// TODO : Spaeter vermutlich entfernen, falls alle Nachrichten direkt an den Message-Broker gehen sollen
	@Bean
	public AnnotationMethodMessageHandler annotationMessageHandler() {

		AnnotationMethodMessageHandler handler = new AnnotationMethodMessageHandler(
				dispatchMessagingTemplate(), webSocketHandlerChannel());

		// TODO: Destination-Prefix muss vermutlich noch angepasst werden
		handler.setDestinationPrefixes(Arrays.asList("/app/"));
		handler.setMessageConverter(this.messageConverter);
		dispatchChannel().subscribe(handler);
		return handler;
	}

	// MessageHandler, der als Nachrichten intern verarbeitet und NICHT an einen
	// eigenstaendigen STOMP Message-Broker sendet.
	// Kann in DispatcherServletInitializer an- bzw. ausgestellt werden

	@Bean
	@Profile("simple-broker")
	public SimpleBrokerMessageHandler simpleBrokerMessageHandler() {
		SimpleBrokerMessageHandler handler = new SimpleBrokerMessageHandler(
				webSocketHandlerChannel());
		// TODO: Destination-Prefix muss vermutlich noch angepasst werden
		handler.setDestinationPrefixes(Arrays.asList("/topic/", "/queue/"));
		dispatchChannel().subscribe(handler);
		return handler;
	}

	// MessageHandler, der Nachrichten an einen eigenstaendigen STOMP
	// Message-Broker sendet.
	// Kann in DispatcherServletInitializer an- bzw. ausgestellt werden

	@Bean
	@Profile("stomp-broker-relay")
	public StompBrokerRelayMessageHandler stompBrokerRelayMessageHandler() {

		StompBrokerRelayMessageHandler handler = new StompBrokerRelayMessageHandler(
		// TODO: Destination-Prefix muss vermutlich noch angepasst werden
				webSocketHandlerChannel(), Arrays.asList("/topic/", "/queue/"));

		dispatchChannel().subscribe(handler);
		return handler;
	}

	// MessageHandler, der Nachrichten verarbeitet, die mit dem
	// Destination-Prefix "/user/{user}" beginnen.

	@Bean
	public UserDestinationMessageHandler userMessageHandler() {
		UserDestinationMessageHandler handler = new UserDestinationMessageHandler(
				dispatchMessagingTemplate(), this.userQueueSuffixResolver);
		dispatchChannel().subscribe(handler);
		return handler;
	}

	// MessagingTemplate (und MessageChannel), der zur Verteilung der
	// Nachrichten dient
	// Alle MessageHandler beans weiter oben melden sich hier an.

	@Bean
	public SimpMessageSendingOperations dispatchMessagingTemplate() {
		SimpMessagingTemplate template = new SimpMessagingTemplate(
				dispatchChannel());
		template.setMessageConverter(this.messageConverter);
		return template;
	}

	@Bean
	public SubscribableChannel dispatchChannel() {
		return new ExecutorSubscribableChannel(asyncExecutor());
	}

	// Channel zur Versendung von STOMP Nachrichten an verbundene WebSocket
	// sessions

	@Bean
	public SubscribableChannel webSocketHandlerChannel() {
		return new ExecutorSubscribableChannel(asyncExecutor());
	}

	// Executor zur Weitergabe von Nachrichten ueber MessageChannel

	@Bean
	public ThreadPoolTaskExecutor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setCorePoolSize(8);
		executor.setThreadNamePrefix("MessageChannel-");
		return executor;
	}

	// Task executor, der von SockJS genutzt wird (heartbeat frames, session timeouts)

	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setThreadNamePrefix("SockJS-");
		taskScheduler.setPoolSize(4);
		return taskScheduler;
	}

	// Allow serving HTML files through the default Servlet

	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

}