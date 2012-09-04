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
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Function;
import org.nabucco.testautomation.script.facade.datatype.scripting.FunctionResult;
import org.nabucco.testautomation.script.facade.datatype.scripting.FunctionScript;
import org.nabucco.testautomation.script.facade.datatype.scripting.ScriptingLogger;

/**
 * FunctionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class FunctionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(FunctionVisitor.class);

    /**
     * Constructs a new FunctionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     * @param testScriptEngine
     */
    protected FunctionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Function function, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(function);

        // Execute Assertion-Script if set
        Script script = function.getScript();

        if (script == null) {
            throw new TestScriptException("No script defined for Function '" + function.getName() + "'");
        }

        ScriptingEngine engine = new ScriptingEngine(ScriptType.JAVA);
        
        if (script.getCode() == null || script.getCode().getValue() == null) {
            throw new TestScriptException("FunctionScript '" + script.getName() + "' is not compiled");
        }
        
        ScriptContainer container = new ScriptContainer(script.getName().getValue(), ScriptType.JAVA, script.getCode()
                .getValue());

        // Resolve PropertyReferences
        PropertyList functionParameters = function.getPropertyList();
        
        if (functionParameters != null) {
            functionParameters = functionParameters.cloneObject();
        
            try {
                ContextHelper.resolvePropertyRefs(functionParameters, getContext());
            } catch (PropertyException e) {
                throw new TestScriptException(e.getMessage());
            }
        }
        
        // Create input parameters
        ScriptingLogger scriptingLogger = new ScriptingLogger(script.getName().getValue());
        ScriptParameter inputParameter = new ScriptParameter(FunctionScript.LOGGER, ScriptingLogger.class.getName(),
                scriptingLogger);
        container.getInputParameter().add(inputParameter);
        
        if (functionParameters != null) {
            inputParameter = new ScriptParameter(FunctionScript.PROPERTY_LIST, PropertyList.class.getName(), functionParameters);
            container.getInputParameter().add(inputParameter);
        }

        // Create output parameters
        FunctionResult result = new FunctionResult();
        ScriptParameter outputParameter = new ScriptParameter(FunctionScript.RESULT, FunctionResult.class.getName(),
                result);
        container.getOutputParameter().add(outputParameter);

        try {
            logger.info("Executing FunctionScript '" + script.getName() + "'");
            engine.execute(container);
            PropertyList returnedPropertyList = result.getPropertyList();

            if (returnedPropertyList != null) {
                returnedPropertyList.setName(function.getName().getValue());
                this.getContext().put(returnedPropertyList);
            }

            logger.info("FunctionScript '" + script.getName() + "' executed");
        } catch (ScriptRunnerException e) {
            scriptingLogger.error(e.getCause());
            throw new TestScriptException("Technical error while executing Function '"
                    + function.getName() + "': " + e.getMessage());
        } catch (ScriptExecutionException e) {
            throw new TestScriptException("Error while executing Function '"
                    + function.getName() + "': " + e.getMessage());
        } finally {
            logToResult(scriptingLogger, argument);
        }
        super.visit(function, argument);
    }

    private void logToResult(ScriptingLogger logger, TestScriptResult argument) {

        if (argument.getLogging() == null || argument.getLogging().getValue() == null) {
            argument.setLogging(logger.getInternalBuffer());
        } else {
            argument.setLogging(argument.getLogging().getValue() + logger.getInternalBuffer());
        }
    }

}
