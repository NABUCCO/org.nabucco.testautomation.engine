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
package org.nabucco.testautomation.engine;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.settings.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.settings.facade.exception.engine.TestEngineException;

/**
 * TestEngine
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface TestEngine extends Remote {

	/**
     * Executes a TestConfiguration.
     * 
     * The execution will be done in a separate thread. So the executed method returns immediately.
     * 
     * @param configuration
     *            The TestConfiguration to be executed.
     * @return An information about the test status containing its job id
     * @throws TestEngineException thrown, if a problem occurs before or during the test run
     */
    public TestExecutionInfo executeTestConfiguration(TestConfiguration configuration, TestContext context) throws TestEngineException, RemoteException;
    
    /**
     * Accesses the status of an running test.
     * 
     * @param testStatus
     *            TestStatusInfo of the test runner job containing the job id
     * @return The actual status of the running job.
     * @throws TestEngineException throw, if an error occurs
     */
    public TestExecutionInfo getTestStatus(TestExecutionInfo testStatus) throws TestEngineException, RemoteException;
    
    /**
     * Loads the result of a finished test.
     * 
     * @param testResultInfo
     *            the information about the finished test.
     * 
     * @return The complete testresult.
     */
    public TestConfigurationResult getTestConfigurationResult(TestExecutionInfo testStatus) throws TestEngineException, RemoteException;

    /**
     * Interrupts the execution of the running test.
     * 
     * @param testStatus TestStatusInfo of the test runner job containing the job id
     * @return The actual status of the cancelled job.
     * @throws TestEngineException throw, if an error occurs
     * @throws RemoteException throw, if an error occurs
     */
    public TestExecutionInfo cancelTestConfiguration(TestExecutionInfo testStatus) throws TestEngineException, RemoteException;
    
    /**
     * Returns the {@link ClientInteraction} to the waiting TestExecution and resumes the paused Job. 
     * 
     * @param testStatus TestStatusInfo of the test runner job containing the job id
     * @param userInput the UserInput to set to the job
     * @throws TestEngineException throw, if an error occurs
     * @throws RemoteException throw, if an error occurs
     */
    public void setClientInteraction(TestExecutionInfo testStatus, ClientInteraction userInput) throws TestEngineException, RemoteException;
    
}
