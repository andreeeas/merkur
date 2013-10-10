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

package de.materna.cms.merkur.server.web;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.ReplyToUser;
import org.springframework.stereotype.Controller;

/**
 * @author andreas
 * 
 */
@Controller
public class ApplicationController {

	@MessageExceptionHandler
	@ReplyToUser(value = "/queue/errors")
	public String handleException(Throwable exception) {
		return exception.getMessage();
	}

}
