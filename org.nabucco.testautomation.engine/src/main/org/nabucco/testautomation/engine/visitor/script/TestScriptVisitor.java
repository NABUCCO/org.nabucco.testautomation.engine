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
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Lock;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * TestScriptVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestScriptVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

	private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
			TestScriptVisitor.class);
	
    /**
     * Constructs a new TestScriptVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
	public TestScriptVisitor(TestContext context, TestScriptEngine testScriptEngine) {
		super(context, testScriptEngine);
	}
	
	/**
     * {@inheritDoc}
     */
	public void visit(TestScript script, TestScriptResult argument) throws TestScriptException {
		logger.info("Visiting TestScript '", script.getIdentificationKey().getValue(), "'");
		super.visit(script, argument);
		logger.info("Finished visiting TestScript '", script.getIdentificationKey().getValue(), "'");
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void visit(Condition condition, TestScriptResult argument) throws TestScriptException {
		new ConditionVisitor(getContext(), getTestScriptEngine()).visit(condition, argument);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void visit(Logger logger, TestScriptResult argument) throws TestScriptException {
		new LoggerVisitor(getContext(), getTestScriptEngine()).visit(logger, argument);
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void visit(Execution execution, TestScriptResult argument) throws TestScriptException {
		new ExecutionVisitor(getContext(), getTestScriptEngine()).visit(execution, argument);
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void visit(Loop loop, TestScriptResult argument) throws TestScriptException {
		new LoopVisitor(getContext(), getTestScriptEngine()).visit(loop, argument);
	}
	
	/**
     * {@inheritDoc}
     */
    @Override
    public void visit(Lock lock, TestScriptResult argument) throws TestScriptException {
        new LockVisitor(getContext(), getTestScriptEngine()).visit(lock, argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Foreach foreach, TestScriptResult argument) throws TestScriptException {
        new ForeachVisitor(getContext(), getTestScriptEngine()).visit(foreach, argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Assertion assertion, TestScriptResult argument) throws TestScriptException {
        new AssertionVisitor(getContext(), getTestScriptEngine()).visit(assertion, argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(PropertyAction propertyAction, TestScriptResult argument) throws TestScriptException {
    	new PropertyActionVisitor(getContext(), getTestScriptEngine()).visit(propertyAction, argument);
    }
    
}
