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
package org.nabucco.testautomation.engine.sub;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nabucco.framework.base.facade.datatype.Duration;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.config.facade.datatype.comparator.TestScriptSorter;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.exception.PropertyException;
import org.nabucco.testautomation.engine.base.util.ContextHelper;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.engine.exception.SynchronizationException;
import org.nabucco.testautomation.engine.exception.TestExecutionAssertionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.visitor.result.TestResultStatusVisitor;
import org.nabucco.testautomation.engine.visitor.script.TestScriptVisitor;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.ActionStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * TestScriptEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class TestScriptEngineImpl implements TestScriptEngine {

    private static final long serialVersionUID = 1L;

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            TestScriptEngineImpl.class);

    private final Lock lock = new ReentrantLock();

    private final Condition delay = lock.newCondition();

    /**
     * Constructs a new TestScriptEngine instance using the given {@link ProxyEnginePoolEntry} for
     * SubEngine-calls.
     */
    public TestScriptEngineImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTestScript(TestScript testScript, TestContext context, TestResult testResult)
            throws TestScriptException {
        context.setCurrentTestScript(testScript);

        TestScriptResult testScriptResult = TestResultHelper.createTestScriptResult();
        testScriptResult.setTestScriptName(testScript.getName());
        testScriptResult.setTestScriptKey(testScript.getIdentificationKey());
        testScriptResult.setElementId(testScript.getId());
        testResult.getTestScriptResultList().add(testScriptResult);

        // Add PropertyList of TestScript to Context
        PropertyList testScriptProperties = testScript.getPropertyList();

        if (testScriptProperties != null) {
            context.put(testScriptProperties.cloneObject());
        }

        long startTime = 0;
        long endTime = 0;

        try {
            TestScriptVisitor visitor = new TestScriptVisitor(context, this);
            startTime = System.currentTimeMillis();
            testScriptResult.setStartTime(new Date(startTime));
            visitor.visit(testScript, testScriptResult);
            endTime = System.currentTimeMillis();
            testScriptResult.setElementStatus(TestScriptElementStatusType.EXECUTED);
            testScriptResult.setEndTime(new Date(endTime));
            testScriptResult.setDuration(endTime - startTime);
        } catch (TestScriptException ex) {
            endTime = System.currentTimeMillis();
            testScriptResult.setEndTime(new Date(endTime));
            testScriptResult.setDuration(endTime - startTime);
            testScriptResult.setErrorMessage(ex.getMessage());
            testScriptResult.setStatus(TestScriptStatusType.FAILED);
            throw ex;
        } catch (InterruptionException ex) {
            endTime = System.currentTimeMillis();
            testScriptResult.setEndTime(new Date(endTime));
            testScriptResult.setDuration(endTime - startTime);
            testScriptResult.setErrorMessage("Execution of TestScript aborted");
            testScriptResult.setStatus(TestScriptStatusType.ABORTED);
            throw ex;
        } catch (RuntimeException ex) {
            endTime = System.currentTimeMillis();
            testScriptResult.setEndTime(new Date(endTime));
            testScriptResult.setDuration(endTime - startTime);
            testScriptResult.setErrorMessage("Fatal error: " + ex.toString());
            testScriptResult.setStatus(TestScriptStatusType.FAILED);
            throw ex;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTestScriptList(List<TestScriptContainer> testScripts, TestContext context,
            TestResult testResult, TestConfigElement parentElement) {
        Collections.sort(testScripts, new TestScriptSorter());

        // loop over all TestScripts of one TestStep
        scriptLoop: for (TestScriptContainer testScriptContainer : testScripts) {
            TestScript testScript = testScriptContainer.getTestScript();

            try {
                executeTestScript(testScript, context, testResult);
                TestResultStatusVisitor visitor = new TestResultStatusVisitor();
                visitor.visit(testResult);

                if (testResult.getStatus() == TestConfigElementStatusType.FAILED) {
                    logger.info("TestScript '", testScript.getIdentificationKey().getValue(), "' failed");
                    break scriptLoop;
                }
            } catch (TestExecutionAssertionException ex) {
                // break assertion failed
                testResult.setErrorMessage(ex.getMessage());
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.info("TestExecutionAssertion: TestResult set to FAILED", " - ", ex.getMessage());
                break scriptLoop;
            } catch (AssertionException ex) {
                // Assertion failed
                testResult.setErrorMessage(ex.getMessage());
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.info("Assertion: TestResult set to FAILED", " - ", ex.getMessage());
                break scriptLoop;
            } catch (SynchronizationException ex) {
                // Lock could not be acquired
                testResult.setErrorMessage(ex.getMessage());
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.error(ex.getMessage());
                break scriptLoop;
            } catch (TestScriptException ex) {
                // Error in TestScript
                String error = "Execution of TestScript '"
                        + testScript.getIdentificationKey().getValue() + "' failed. Cause: " + ex.getMessage();
                testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.error(error);
                break scriptLoop;
            } catch (InterruptionException ex) {
                // TestScript interrupted
                String error = "Execution of TestScript '" + testScript.getIdentificationKey().getValue() + "' aborted";
                testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.error(error);
                throw ex;
            } catch (Exception ex) {
                // Unexpected error
                String error = "Execution of TestScript '"
                        + testScript.getIdentificationKey().getValue() + "' failed. Cause: " + ex.toString();
                testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.fatal(ex, error);
                break scriptLoop;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionResponse executeAction(Action action, TestContext context) {

        ActionResponse response = null;
        boolean trace = action.getTrace() != null
                && action.getTrace().getValue() != null && action.getTrace().getValue().booleanValue();
        context.setTracingEnabled(trace);
        PropertyList actionProperties = action.getPropertyList();
        
        if (actionProperties != null) {
            actionProperties = actionProperties.cloneObject();
        }

        // Invoke SubEngine
        try {
            ContextHelper.resolvePropertyRefs(actionProperties, context);
            SubEngineInvoker subEngineInvoker = new SubEngineInvoker(action.getMetadata(), context, actionProperties,
                    action.getActionCode());
            subEngineInvoker.invoke();
            response = subEngineInvoker.getResponse();
            response.setElementId(action.getId());

            // Delay of Action
            delayAction(action);

        } catch (InterruptionException ex) {
            throw ex;
        } catch (PropertyException ex) {
            response = TestResultHelper.createActionResponse();
            response.setActionStatus(ActionStatusType.FAILED);
            response.setErrorMessage("PropertyException before invoking SubEngine: " + ex.getMessage());
            logger.error(ex);
        } catch (NBCTestConfigurationException ex) {
            response = TestResultHelper.createActionResponse();
            response.setActionStatus(ActionStatusType.FAILED);
            response.setErrorMessage("ConfigurationError while invoking SubEngine: " + ex.getMessage());
            logger.error(ex);
        } catch (Exception ex) {
            response = TestResultHelper.createActionResponse();
            response.setActionStatus(ActionStatusType.FAILED);
            response.setErrorMessage("Unexpected error while invoking SubEngine: " + ex.toString());
            logger.fatal(ex);
        }
        return response;
    }

    /**
     * Blocks the execution if a delay is configured in the given {@link Action}.
     * 
     * @param action
     *            the Action to be check for a delay
     */
    private void delayAction(Action action) {

        Duration actionDelay = action.getDelay();

        if (actionDelay != null && actionDelay.getValue() != null) {
            try {
                lock.lock();
                this.delay.await(actionDelay.getValue(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new InterruptionException("Delay was unexpectedly interrupted");
            } finally {
                lock.unlock();
            }
        }
    }

}
