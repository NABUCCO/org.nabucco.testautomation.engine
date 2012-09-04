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

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.ActionStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.result.facade.datatype.trace.ActionTrace;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;

/**
 * ActionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class ActionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(ActionVisitor.class);

    /**
     * Constructs a new TestActionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     * @param testScriptEngine
     */
    protected ActionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Action action, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(action);
        super.visit(action, argument);
        ActionResponse response = getTestScriptEngine().executeAction(action, getContext());
        argument.setElementStatus(TestScriptElementStatusType.EXECUTED);

        if (response == null) {
            argument.setStatus(TestScriptStatusType.FAILED);
            String msg = "No ActionResponse received for Action '"
                    + action.getName().getValue() + "' " + action.getIdentificationKey().getValue();
            argument.setErrorMessage(msg);
            logger.error(msg);
            return;
        } else {
            response.setElementStatus(TestScriptElementStatusType.EXECUTED);
            argument.getElementResultList().add(response);
        }

        ActionTrace actionTrace = response.getActionTrace();

        if (actionTrace != null) {

            if (actionTrace.getName() == null || actionTrace.getName().getValue() == null) {
                actionTrace.setName(action.getName());
            }
            actionTrace.setActionId(action.getIdentificationKey());
            argument.getActionTraceList().add(actionTrace);
        }

        if (response.getActionStatus() == ActionStatusType.FAILED) {
            argument.setStatus(TestScriptStatusType.FAILED);

            if (response.getErrorMessage() != null) {
                argument.setErrorMessage(response.getErrorMessage());
                throw new TestScriptException(response.getErrorMessage().getValue());
            } else {
                String msg = "Action " + action.getId() + " failed with no error message. Please check the log.";
                argument.setErrorMessage(msg);
                throw new TestScriptException(msg);
            }
        }

        PropertyList returnProperties = response.getReturnProperties();

        if (returnProperties != null && !returnProperties.getPropertyList().isEmpty()) {
            returnProperties.setName(action.getName().getValue());
            getContext().put(returnProperties);
        }
    }

}
