/*
 * Copyright 2012 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nabucco.testautomation.engine.exception;

import org.nabucco.testautomation.engine.base.exception.NBCTestException;

/**
 * 
 * SynchronizationSemaphoreException
 *
 * @author Steffen Schmidt, PRODYNA AG
 */
public class SynchronizationSemaphoreException extends NBCTestException {

	private static final long serialVersionUID = 1L;

	/**
     * Constructs a new empty instance.
     */
	public SynchronizationSemaphoreException() {
		super();
	}

	/**
     * Constructs a new instance with a root cause and a given error message.
     * 
     * @param message the error message
     * @param cause the root cause
     */
	public SynchronizationSemaphoreException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
     * Constructs a new instance with a given error message.
     * 
     * @param message the error message
     */
	public SynchronizationSemaphoreException(String message) {
		super(message);
	}

	/**
     * Constructs a new instance with a root cause.
     * 
     * @param cause the root cause
     */
	public SynchronizationSemaphoreException(Throwable cause) {
		super(cause);
	}

}
