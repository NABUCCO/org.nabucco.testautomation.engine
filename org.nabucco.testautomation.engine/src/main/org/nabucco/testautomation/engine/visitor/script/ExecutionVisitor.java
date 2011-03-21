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
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.EmbeddedTestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;

/**
 * ExecutionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ExecutionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

	/**
     * Constructs a new ExecutionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
	protected ExecutionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
		super(context, testScriptEngine);
	}

	/**
     * {@inheritDoc}
     */
	@Override
    public void visit(Execution execution, TestScriptResult argument) throws TestScriptException {
	    getContext().setCurrentTestScriptElement(execution);
        super.visit(execution, argument);
    }
	
	/**
     * {@inheritDoc}
     */
    @Override
    public void visit(Action action, TestScriptResult argument) throws TestScriptException {
        new ActionVisitor(getContext(), getTestScriptEngine()).visit(action,
                argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Logger logger, TestScriptResult argument) throws TestScriptException {
        new LoggerVisitor(getContext(), getTestScriptEngine()).visit(logger,
                argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(EmbeddedTestScript testScript, TestScriptResult argument) throws TestScriptException {
    	new EmbeddedTestScriptVisitor(getContext(), getTestScriptEngine()).visit(testScript, argument);
    }

}
