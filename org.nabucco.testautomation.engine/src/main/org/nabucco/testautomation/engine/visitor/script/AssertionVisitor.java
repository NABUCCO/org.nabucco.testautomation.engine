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
package org.nabucco.testautomation.engine.visitor.script;

import org.nabucco.common.scripting.ScriptContainer;
import org.nabucco.common.scripting.ScriptParameter;
import org.nabucco.common.scripting.ScriptType;
import org.nabucco.common.scripting.engine.ScriptingEngine;
import org.nabucco.common.scripting.runner.ScriptExecutionException;
import org.nabucco.common.scripting.runner.ScriptRunnerException;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.framework.support.scripting.facade.datatype.Script;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.PropertyException;
import org.nabucco.testautomation.engine.base.util.ContextHelper;
import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.scripting.FunctionScript;
import org.nabucco.testautomation.script.facade.datatype.scripting.ScriptingLogger;
import org.nabucco.testautomation.script.facade.datatype.scripting.exception.AssertionFailureException;

/**
 * AssertionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class AssertionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(AssertionVisitor.class);

    /**
     * Constructs a new AssertionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     * @param testScriptEngine
     */
    protected AssertionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Assertion assertion, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(assertion);

        // Check if Fail-Flag is set to true
        if (assertion.getFail() != null && assertion.getFail().getValue() != null && assertion.getFail().getValue()) {
            String message = null;

            if (assertion.getMessage() != null) {
                message = assertion.getMessage().getValue();
            } else {
                message = "Assertion '" + assertion.getName() + "' failed";
            }

            argument.setStatus(TestScriptStatusType.FAILED);
            argument.setErrorMessage(message);
            logger.info("Assertion '" + assertion.getName() + "' failed");
            throw new AssertionException(message);
        }

        // Execute Assertion-Script if set
        Script assertionScript = assertion.getAssertionScript();

        if (assertionScript != null) {
            ScriptingEngine engine = new ScriptingEngine(ScriptType.JAVA);

            if (assertionScript.getCode() == null || assertionScript.getCode().getValue() == null) {
                throw new TestScriptException("AssertionScript '" + assertionScript.getName() + "' is not compiled");
            }

            ScriptContainer container = new ScriptContainer(assertionScript.getName().getValue(), ScriptType.JAVA,
                    assertionScript.getCode().getValue());

            // Resolve PropertyReferences
            PropertyList assertionParameters = assertion.getPropertyList();

            if (assertionParameters != null) {
                assertionParameters = assertionParameters.cloneObject();
            }

            try {
                ContextHelper.resolvePropertyRefs(assertionParameters, getContext());
            } catch (PropertyException e) {
                throw new TestScriptException(e.getMessage());
            }

            // Create input parameters
            ScriptingLogger scriptingLogger = new ScriptingLogger(assertionScript.getName().getValue());
            ScriptParameter inputParameter = new ScriptParameter(FunctionScript.LOGGER,
                    ScriptingLogger.class.getName(), scriptingLogger);
            container.getInputParameter().add(inputParameter);
            inputParameter = new ScriptParameter(FunctionScript.PROPERTY_LIST, PropertyList.class.getName(),
                    assertionParameters);
            container.getInputParameter().add(inputParameter);

            try {
                logger.info("Executing AssertionScript '" + assertionScript.getName() + "'");
                engine.execute(container);
                logger.info("Result of assertion: success");
            } catch (AssertionFailureException e) {
                logger.info("Result of assertion: failure - " + e.getMessage());
                throw new AssertionException(e.getMessage());
            } catch (ScriptRunnerException e) {
                scriptingLogger.error(e.getCause());
                throw new TestScriptException("Technical error while executing Assertion '"
                        + assertion.getName() + "': " + e.getMessage());
            } catch (ScriptExecutionException e) {
                throw new TestScriptException("Error while executing Assertion '"
                        + assertion.getName() + "': " + e.getMessage());
            } finally {
                logToResult(scriptingLogger, argument);
            }
        }
        super.visit(assertion, argument);
    }

    private void logToResult(ScriptingLogger logger, TestScriptResult argument) {

        if (argument.getLogging() == null || argument.getLogging().getValue() == null) {
            argument.setLogging(logger.getInternalBuffer());
        } else {
            argument.setLogging(argument.getLogging().getValue() + logger.getInternalBuffer());
        }
    }

}
