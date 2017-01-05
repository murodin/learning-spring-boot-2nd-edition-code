/*
 * Copyright 2016 the original author or authors.
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
package com.greglturnquist.learningspringboot.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * @author Greg Turnquist
 */
// tag::secured-1[]
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends
	AbstractSecurityWebSocketMessageBrokerConfigurer {
	// end::secured-1[]

	private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

	@Value("https://${vcap.application.uris[0]}")
	String origin;

	// tag::cors[]
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		log.warn("!!! Configuring allow origin of " + origin);
		registry.addEndpoint("/learning-spring-boot").setAllowedOrigins(origin).withSockJS();
	}
	// end::cors[]

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableSimpleBroker("/topic", "/queue");
	}

	// tag::secured-2[]
	@Override
	protected void configureInbound(
		MessageSecurityMetadataSourceRegistry messages) {

		messages
			.nullDestMatcher().authenticated()
			.simpDestMatchers("/app/**").hasRole("USER")
			.simpSubscribeDestMatchers(
				"/user/**", "/topic/**").hasRole("USER")
			.anyMessage().denyAll();
	}
	// end::secured-2[]

}
