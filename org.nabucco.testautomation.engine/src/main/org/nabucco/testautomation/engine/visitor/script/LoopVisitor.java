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
import org.nabucco.testautomation.engine.exception.BreakLoopException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.BreakLoop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Lock;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;

/**
 * 
 * LoopVisitor
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class LoopVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

	private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
			LoopVisitor.class);
	
	/**
     * Constructs a new LoopVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
	protected LoopVisitor(TestContext context, TestScriptEngine testScriptEngine) {
		super(context, testScriptEngine);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(Loop loop, TestScriptResult argument) throws TestScriptException {
	    
		if (loop.getName() == null || loop.getName().getValue() == null) {
			loop.setName("n/a");
		}
		getContext().setCurrentTestScriptElement(loop);
		
		Integer maxIteration = loop.getMaxIterations() != null ? loop.getMaxIterations().getValue() : null;
		Long maxDuration = loop.getMaxDuration() != null ? loop.getMaxDuration().getValue() : null;
		Long wait = loop.getWait() != null ? loop.getWait().getValue() : null;
		
		if (maxIteration != null && maxIteration.intValue() > 0) {
			performForLoop(loop, maxIteration, maxDuration, wait, argument);
		} else if (maxDuration != null && maxDuration.longValue() > 0) {
			performWhileLoop(loop, maxDuration, wait, argument);
		}		
	}
	
	private void performForLoop(Loop loop, int iterations, Long maxDuration, Long waitTime, TestScriptResult argument) throws TestScriptException {
		
		logger.info("Starting For-Loop: '", loop.getName().getValue(), "'");
		Long end = null;
		
		if (maxDuration != null && maxDuration.longValue() > 0) {
			 end = System.currentTimeMillis() + maxDuration;
		}
		
		// Index of loop
		IntegerProperty index = new IntegerProperty();

		if (loop.getIndexName() != null
				&& loop.getIndexName().getValue() != null
				&& !loop.getIndexName().getValue().equals("")) {
			index.setName(loop.getIndexName().getValue());
			getContext().put(index);
		}
		
		for (int i = 0; i < iterations; i++) {
			logger.info("Loop iteration " + i);
			index.setValue(i);
			
			try {
			    super.visit(loop, argument);
			} catch(BreakLoopException ex) {
			    break;
			}
			
			if (timeout(end)) {
				logger.info("Timeout: MaxDuration exceeded");
				break;
			}
			
			if (end != null && waitTime != null && System.currentTimeMillis() + waitTime > end) {
				waitTime = end - System.currentTimeMillis();
			}				
			wait(waitTime);
		}
		getContext().remove(index);
	}
	
	private void performWhileLoop(Loop loop, Long maxDuration, Long waitTime, TestScriptResult argument) throws TestScriptException {
		
		if (loop.getName() != null) {
			logger.info("Starting While-Loop: '", loop.getName().getValue(), "'");
		}
		Long end = System.currentTimeMillis() + maxDuration;
		int counter = 0;
		
		// Index of loop
		IntegerProperty index = new IntegerProperty();

		if (loop.getIndexName() != null
				&& loop.getIndexName().getValue() != null
				&& !loop.getIndexName().getValue().equals("")) {
			index.setName(loop.getIndexName().getValue());
			getContext().put(index);
		}
		
		while(!timeout(end) && counter <= 999) {
			logger.debug("Next while iteration");
			index.setValue(counter);
			
			try {
                super.visit(loop, argument);
            } catch(BreakLoopException ex) {
                break;
            }
			
			wait(waitTime);
			counter++;
		}
		getContext().remove(index);
		logger.debug("Timeout: MaxDuration exceeded");		
	}
	
	private void wait(Long wait) {
		
		if (wait != null) {
			logger.debug("Waiting for " + wait + " ms ...");
			getContext().getExecutionController().sleep(wait);
		}		
	}
	
	private boolean timeout(Long end) {
		
		if (end == null || end.longValue() == 0) {
			return false;
		}
		return end - System.currentTimeMillis() <= 0;
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
    public void visit(BreakLoop breakLoop, TestScriptResult argument) throws TestScriptException {
        new BreakLoopVisitor(getContext(), getTestScriptEngine()).visit(breakLoop, argument);
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
