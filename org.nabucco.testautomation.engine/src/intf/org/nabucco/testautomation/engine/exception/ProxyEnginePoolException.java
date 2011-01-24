/*
* Copyright 2010 PRODYNA AG
*
* Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.opensource.org/licenses/eclipse-1.0.php or
* http://www.nabucco-source.org/nabucco-license.html
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.nabucco.testautomation.engine.exception;

import org.nabucco.testautomation.engine.proxy.exception.SubEngineException;

/**
 * ProxyEnginePoolException
 * 
 * @author Frank Ratschinski, PRODYNA AG
 * 
 */
public class ProxyEnginePoolException extends SubEngineException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new empty instance.
     */
    public ProxyEnginePoolException() {
        super();
    }

    /**
     * Constructs a new instance with a root cause.
     * 
     * @param cause the root cause
     */
    public ProxyEnginePoolException(Exception cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with a root cause and a given error message.
     * 
     * @param message the error message
     * @param cause the root cause
     */
    public ProxyEnginePoolException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance with a given error message.
     * 
     * @param message the error message
     */
    public ProxyEnginePoolException(String message) {
        super(message);
    }
}
