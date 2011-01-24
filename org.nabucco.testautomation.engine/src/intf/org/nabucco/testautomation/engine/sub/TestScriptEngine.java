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
import java.util.List;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.TestScriptException;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * 
 * TestScriptEngine
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public interface TestScriptEngine
        extends Serializable {

    /**
     * Iterates through the given list of TestScripts and executes them in that order.
     * 
     * @param testScripts
     *            the list of TestScripts to be executed
     * @param context
     *            the context of the test run
     * @param testResult
     *            the result of the parent TestStep
     * @param parentElement
     *            the parent element of the scripts
     */
    public void executeTestScriptList(List<TestScriptContainer> testScripts, TestContext context, TestResult testResult,
            TestConfigElement parentElement);

    /**
     * Executes the given TestScript.
     * 
     * @param testScript
     *            the TestScript to execute
     * @param context
     *            the context of the test run
     * @param testResult
     *            the result of the parent TestStep
     * @throws TestScriptException
     *             thrown, if an error occurs during the execution of the TestScript
     */
    public void executeTestScript(TestScript testScript, TestContext context, TestResult testResult)
        throws TestScriptException;

    /**
     * Executes the given Action.
     * 
     * @param action
     *            the Action to execute
     * @param context
     *            the context of the test run
     * @return the response of the Action
     */
    public ActionResponse executeAction(Action action, TestContext context);

}
