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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.ExecutionServiceException;
import org.nabucco.testautomation.engine.exception.JobRejectionException;
import org.nabucco.testautomation.engine.execution.TestExecutionService;
import org.nabucco.testautomation.engine.execution.job.TestConfigurationExecutionJob;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.settings.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.settings.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.settings.facade.exception.engine.TestEngineException;

/**
 * TestEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class TestEngineImpl extends UnicastRemoteObject implements TestEngine {

    private static final long serialVersionUID = 1L;

    private final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(TestEngineImpl.class);

    private TestExecutionService service;

    /**
     * Constructs a new instance.
     */
    TestEngineImpl(TestExecutionService service) throws RemoteException {
        this.service = service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestExecutionInfo executeTestConfiguration(TestConfiguration testConfiguration, TestContext context)
            throws TestEngineException {
        TestExecutionInfo testExecutionInfo = null;

        try {
            TestConfigurationExecutionJob testJob = new TestConfigurationExecutionJob();
            testJob.setTestConfiguration(testConfiguration);
            testJob.setContext(context);

            long jobId = this.service.startExecution(testJob);
            logger.info("[JobId:"
                    + jobId + "] Started execution of TestConfiguration: " + testConfiguration.getName().getValue());
            testExecutionInfo = this.service.getTestExecutionInfo(jobId);
        } catch (JobRejectionException e) {
            testExecutionInfo = new TestExecutionInfo();
            testExecutionInfo.setTestStatus(ExecutionStatusType.REJECTED);
            logger.info("Rejected execution of TestConfiguration: " + testConfiguration.getName().getValue());
        } catch (ExecutionServiceException e) {
            throw e;
        } catch (Exception e) {
            String error = "Unexpected error while executing TestSheet '"
                    + testConfiguration.getName().getValue() + "': " + e.getMessage();
            logger.fatal(e, error);
            throw new TestEngineException(error);
        }
        return testExecutionInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestConfigurationResult getTestConfigurationResult(TestExecutionInfo testInfo) throws TestEngineException {
        return this.service.getTestConfigurationResult(testInfo.getJobId().getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestExecutionInfo getTestStatus(TestExecutionInfo testInfo) throws TestEngineException {
        return this.service.getTestExecutionInfo(testInfo.getJobId().getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestExecutionInfo cancelTestConfiguration(TestExecutionInfo testInfo) throws TestEngineException,
            RemoteException {
        long jobId = testInfo.getJobId().getValue();
        this.service.stopExecution(jobId);
        return this.service.getTestExecutionInfo(jobId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClientInteraction(TestExecutionInfo testInfo, ClientInteraction userInput)
            throws TestEngineException, RemoteException {
        long jobId = testInfo.getJobId().getValue();
        this.service.setClientInteraction(jobId, userInput);
    }

}
