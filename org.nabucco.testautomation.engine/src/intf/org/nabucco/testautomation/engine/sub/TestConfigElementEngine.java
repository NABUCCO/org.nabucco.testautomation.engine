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
package org.nabucco.testautomation.engine.sub;

import java.io.Serializable;

import org.nabucco.testautomation.engine.base.context.TestContext;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.facade.exception.engine.TestEngineException;
import org.nabucco.testautomation.result.facade.datatype.TestResult;

/**
 * TestConfigElementEngine
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface TestConfigElementEngine
        extends Serializable {

    /**
     * Executes the given TestConfigElement.
     * 
     * @param testConfigElement
     *            the TestConfigElement to execute
     * @param context
     *            the context of the test run
     * @param result
     *            the result of the parent TestConfifElement
     * @throws TestEngineException
     * 			  thrown, if an error occurs during the execution
     */
    public TestResult executeTestConfigElement(TestConfigElement testConfigElement, TestContext context, TestResult parentResult) throws TestEngineException;

}
