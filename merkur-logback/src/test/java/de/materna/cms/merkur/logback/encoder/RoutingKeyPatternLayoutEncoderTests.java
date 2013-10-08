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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import encoder.RoutingKeyPatternLayoutEncoder;

/**
 * Unit-Tests für den {@link RoutingKeyPatternLayoutEncoder}.
 * 
 * @author andreas
 */
public class RoutingKeyPatternLayoutEncoderTests {

	private RoutingKeyPatternLayoutEncoder routingKeyPatternLayoutEncoder;

	@Before
	public void setUp() throws Exception {
		routingKeyPatternLayoutEncoder = new RoutingKeyPatternLayoutEncoder();
	}

	@Test
	public void testStart_AddsExceptionSuppressionIfNotInPatternAlready() {
		// given
		routingKeyPatternLayoutEncoder.setPattern("%c.%p");

		// when
		routingKeyPatternLayoutEncoder.start();

		// then
		assertThat(routingKeyPatternLayoutEncoder.getPattern(),
				is("%c.%p%nopex"));
	}

	@Test
	public void testStart_DoesNotAddExceptionSuppressionIfAlreadyInPattern() {
		// given
		routingKeyPatternLayoutEncoder.setPattern("%c.%p%nopex");

		// when
		routingKeyPatternLayoutEncoder.start();

		// then
		assertThat(routingKeyPatternLayoutEncoder.getPattern(),
				is("%c.%p%nopex"));
	}

}
