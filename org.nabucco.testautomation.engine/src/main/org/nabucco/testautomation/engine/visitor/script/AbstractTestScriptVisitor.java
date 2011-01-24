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

import java.util.Collections;
import java.util.List;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.engine.ExecutionController;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.script.facade.datatype.comparator.TestScriptElementSorter;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.BreakLoop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Lock;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TextMessage;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElement;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementContainer;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementType;

/**
 * AbstractTestScriptVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public abstract class AbstractTestScriptVisitor<A> implements TestDictionaryVisitor<A> {

    private static final NBCTestLogger log = NBCTestLoggingFactory.getInstance().getLogger(
            AbstractTestScriptVisitor.class);

    private TestContext context;

    private TestScriptEngine testScriptEngine;

    /**
     * Constructs a new DefaultTestScriptVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
    protected AbstractTestScriptVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        this.context = context;
        this.testScriptEngine = testScriptEngine;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Logger logger, A argument) throws TestScriptException {
    	checkExecutionController();
        visit(logger.getTestScriptElementList(), argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TextMessage message, A argument) {
    	checkExecutionController();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Condition condition, A argument) throws TestScriptException {
    	checkExecutionController();
        visit(condition.getTestScriptElementList(), argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Lock lock, A argument) throws TestScriptException {
    	checkExecutionController();
        visit(lock.getTestScriptElementList(), argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Execution execution, A argument) throws TestScriptException {
    	checkExecutionController();
        visit(execution.getTestScriptElementList(), argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Action action, A argument) throws TestScriptException {
    	checkExecutionController();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Assertion assertion, A argument) throws TestScriptException {
        checkExecutionController();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestScript script, A argument, boolean subTestScript) throws TestScriptException {
    	checkExecutionController();
        log.info("Visiting TestScript '", script.getTestScriptKey().getValue(), "'");
        visit(script.getTestScriptElementList(), argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestScript script, A argument) throws TestScriptException {
    	checkExecutionController();
        log.info("Visiting TestScript '", script.getTestScriptKey().getValue(), "'");
        visit(script.getTestScriptElementList(), argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Loop loop, A argument) throws TestScriptException {
    	checkExecutionController();
        visit(loop.getTestScriptElementList(), argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Foreach foreach, A argument) throws TestScriptException {
        checkExecutionController();
        visit(foreach.getTestScriptElementList(), argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(BreakLoop breakLoop, A argument) throws TestScriptException {
        checkExecutionController();
        visit(breakLoop.getTestScriptElementList(), argument);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(PropertyAction propertyAction, A argument) throws TestScriptException {
        checkExecutionController();
    }

    /**
     * Visits all member of the given list of {@link TestScriptElement}.
     * 
     * @param list the list of TestScriptElement
     * @param argument an generic argument
     * @throws TestScriptException thrown, if an error occurs during the visit
     */
    public void visit(List<TestScriptElementContainer> list, A argument) throws TestScriptException {
    	Collections.sort(list, new TestScriptElementSorter());

    	for (TestScriptElementContainer e : list) {
            visit(e.getElement(), argument);
        }
    }

    /**
     * Determines its concrete type and then visits the TestScriptElement. 
     * 
     * @param element the TestScriptElement
     * @param argument an generic argument
     * @throws TestScriptException thrown, if an error occurs during the visit
     */
    public void visit(TestScriptElement element, A argument) throws TestScriptException {
        TestScriptElementType type = element.getType();
        switch (type) {
            case SCRIPT: {
                visit((TestScript) element, argument, true);
                break;
            }
            case LOGGER: {
                visit((Logger) element, argument);
                break;
            }
            case TEXT_MESSAGE: {
                visit((TextMessage) element, argument);
                break;
            }
            case EXECUTION: {
                visit((Execution) element, argument);
                break;
            }
            case CONDITION: {
                visit((Condition) element, argument);
                break;
            }
            case LOOP: {
                visit((Loop) element, argument);
                break;
            }
            case ACTION: {
                visit((Action) element, argument);
                break;
            }
            case ASSERTION: {
                visit((Assertion) element, argument);
                break;
            }
            case LOCK: {
                visit((Lock) element, argument);
                break;
            }
            case FOREACH: {
                visit((Foreach) element, argument);
                break;
            }
            case BREAK_LOOP: {
                visit((BreakLoop) element, argument);
                break;
            }
            case PROPERTY_ACTION: {
            	visit((PropertyAction) element, argument);
                break;
            }
        }
    }

    /**
     * Gets the TestContext.
     * 
     * @return the context
     */
    protected TestContext getContext() {
        return context;
    }

    /**
     * Gets the TestScriptEngine.
     * 
     * @return the TestScriptEngine
     */
    protected TestScriptEngine getTestScriptEngine() {
        return testScriptEngine;
    }
    
    protected void checkExecutionController() {
		ExecutionController executionController = context.getExecutionController();

		if (executionController != null) {
			if (executionController.isPaused()) {
				executionController.tryPause();
			}
			if (executionController.isInterrupted()) {
				executionController.tryInterruption();
			}
		}
	}
    
}
