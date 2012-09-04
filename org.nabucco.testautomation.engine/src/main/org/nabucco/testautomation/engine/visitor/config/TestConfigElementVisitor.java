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
package org.nabucco.testautomation.engine.visitor.config;

import java.util.Date;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.framework.base.facade.datatype.visitor.VisitorException;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;
import org.nabucco.testautomation.engine.visitor.result.TestResultStatusVisitor;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.settings.facade.exception.engine.TestEngineException;

/**
 * TestConfigElementVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestConfigElementVisitor extends AbstractTestConfigElementVisitor<TestResult> {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            TestConfigElementVisitor.class);

    private final TestResultStatusVisitor resultVisitor = new TestResultStatusVisitor();

    /**
     * Construct a new instance using the given {@TestConfigElementEngine}.
     * 
     * @param testConfigElementEngine the TestConfigElementEngine for execution
     */
    protected TestConfigElementVisitor(TestConfigElementEngine testConfigElementEngine) {
        super(testConfigElementEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestConfiguration testConfiguration, TestContext context, TestResult argument) {
        throw new UnsupportedOperationException(
                "Should not visit a TestConfiguration from within a TestConfigElementVisitor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestConfigElement testConfigElement, TestContext context, TestResult argument) {

        logger.debug("Visting TestConfigElement '", testConfigElement.getName().getValue(), "'");
        context = this.checkCloneContext(testConfigElement, context);
        context.setCurrentTestConfigElement(testConfigElement);
        long startTime = System.currentTimeMillis();

        try {
            // Set Brand in TestResult
            argument.setBrandType(testConfigElement.getBrandType());

            // Execute TestConfigElement
            TestResult result = getTestConfigElementEngine().executeTestConfigElement(testConfigElement, context,
                    argument);

            // Do not execute children if skipped
            if (result.getStatus() != TestConfigElementStatusType.SKIPPED) {
                super.visit(testConfigElement, context, result);
            }

            long endTime = System.currentTimeMillis();
            result.setStartTime(new Date(startTime));
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);

            // Determine Status of TestResult
            TestResultStatusVisitor visitor = new TestResultStatusVisitor();
            visitor.visit(result);
        } catch (InterruptionException ex) {
            finish(argument, startTime);
            argument.setStatus(TestConfigElementStatusType.FAILED);
            argument.setErrorMessage("Execution of '" + testConfigElement.getName() + "' aborted");
            throw ex;
        } catch (TestEngineException ex) {
            finish(argument, startTime);
            argument.setErrorMessage(ex.getMessage());
            logger.error(ex.getMessage());
        } catch (VisitorException ex) {
            finish(argument, startTime);
            argument.setErrorMessage(ex.getMessage());
            logger.error(ex.getMessage());
        }
    }

    /**
     * 
     * @param testConfigElement
     * @param context
     * @param testConfigurationResult
     */
    public void visit(TestConfigElement testConfigElement, TestContext context,
            TestConfigurationResult testConfigurationResult) {

        logger.debug("Visting TestConfigElement '", testConfigElement.getName(), "'");
        context = this.checkCloneContext(testConfigElement, context);
        context.setCurrentTestConfigElement(testConfigElement);
        long startTime = System.currentTimeMillis();

        try {
            // Execute TestConfigElement
            TestResult result = getTestConfigElementEngine().executeTestConfigElement(testConfigElement, context, null);

            // Do not execute children if skipped
            if (result.getStatus() != TestConfigElementStatusType.SKIPPED) {
                TestResultHelper.addTestResult(result, testConfigurationResult);
                super.visit(testConfigElement, context, result);
            }

            long endTime = System.currentTimeMillis();
            result.setStartTime(new Date(startTime));
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);

            // Determine Status of TestResult
            resultVisitor.visit(result);
        } catch (InterruptionException ex) {
            finish(testConfigurationResult, startTime);
            throw ex;
        } catch (TestEngineException ex) {
            finish(testConfigurationResult, startTime);
            logger.error(ex.getMessage());
        } catch (VisitorException ex) {
            finish(testConfigurationResult, startTime);
            logger.error(ex.getMessage());
        }
    }
    
    /**
     * 
     * @param testConfigElement
     * @param context
     * @return
     */
    private TestContext checkCloneContext(TestConfigElement testConfigElement, TestContext context) {

        if (testConfigElement.getSchemaElement() != null
                && testConfigElement.getSchemaElement().getCloneContext() != null
                && testConfigElement.getSchemaElement().getCloneContext().getValue() != null
                && testConfigElement.getSchemaElement() != null
                && testConfigElement.getSchemaElement().getCloneContext() != null
                && testConfigElement.getSchemaElement().getCloneContext().getValue().booleanValue()) {
            return context.dublicate();
        }
        return context;
    }

    /**
     * 
     * @param argument
     * @param startTime
     */
    private void finish(TestResult argument, long startTime) {

        long endTime = System.currentTimeMillis();

        if (!argument.getTestResultList().isEmpty()) {
            TestResult result = argument.getTestResultList().last().getResult();
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);
        }
    }

    /**
     * 
     * @param argument
     * @param startTime
     */
    private void finish(TestConfigurationResult argument, long startTime) {

        long endTime = System.currentTimeMillis();

        if (!argument.getTestResultList().isEmpty()) {
            TestResult result = argument.getTestResultList().last().getResult();
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);
        }
    }

}
