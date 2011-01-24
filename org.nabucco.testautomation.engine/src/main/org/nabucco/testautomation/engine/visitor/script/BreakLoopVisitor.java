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
package org.nabucco.testautomation.engine.visitor.script;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.exception.BreakLoopException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.BreakLoop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElement;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementContainer;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.OperatorType;

/**
 * BreakLoopVisitor
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class BreakLoopVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    /**
     * Constructs a new BreakConditionVisitor instance using the given {@link TestContext} and {@link TestScriptEngine}.
     * 
     * @param context the context
     * @param testScriptEngine the TestScriptEngine
     */
    protected BreakLoopVisitor(TestContext context, TestScriptEngine engine) {
        super(context, engine);
    }
    
    /**
     * Visits the given {@link BreakCondition}. After visiting all children, the result will be set.
     * 
     * @param condition the BreakCondition to be visited
     * @throws BreakLoopException thrown, if the break condition is true
     */
    public void visit(BreakLoop breakLoopElement, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(breakLoopElement);
        
        for (TestScriptElementContainer container : breakLoopElement.getTestScriptElementList()) {
        	TestScriptElement element = container.getElement();
        	
        	if (element.getType() == TestScriptElementType.CONDITION) {
                boolean breakLoop = visit((Condition) element);
                
                if (breakLoop) {
                    throw new BreakLoopException();
                }
            }
        }
    }
    
    /**
     * Visits an instance of a {@link Condition}.
     * 
     * @param condition the Condition instance
     */
    public boolean visit(Condition condition) throws TestScriptException {

    	if (condition.getOperator() == null || condition.getOperator() == OperatorType.NONE) {
    		ConditionVisitor visitor = new ConditionVisitor(getContext(), getTestScriptEngine());
            visitor.visit(condition, TestResultHelper.createTestScriptResult());
            return visitor.getResult();
    	} else if (condition.getOperator() == OperatorType.AND) {
            for (TestScriptElementContainer container : condition.getTestScriptElementList()) {
            	TestScriptElement element = container.getElement();
            	
                if (element.getType() == TestScriptElementType.CONDITION) {
                    ConditionVisitor visitor = new ConditionVisitor(getContext(), getTestScriptEngine());
                    visitor.visit(condition, TestResultHelper.createTestScriptResult());
                    if (!visitor.getResult()) {
                        return false;
                    }
                }
            }
            return true;
        } else if (condition.getOperator() == OperatorType.OR) {
        	for (TestScriptElementContainer container : condition.getTestScriptElementList()) {
            	TestScriptElement element = container.getElement();
            	
                if (element.getType() == TestScriptElementType.CONDITION) {
                    ConditionVisitor visitor = new ConditionVisitor(getContext(), getTestScriptEngine());
                    visitor.visit(condition, TestResultHelper.createTestScriptResult());
                    if (visitor.getResult()) {
                        return true;
                    }
                }
            }
            return false;
        } else {
        	return false;
        }
    }

}
