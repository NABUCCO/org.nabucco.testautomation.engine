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
import org.nabucco.testautomation.engine.exception.SynchronizationException;
import org.nabucco.testautomation.engine.exception.SynchronizationSemaphoreException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.semaphore.SynchronizationSemaphoreMap;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
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
 * LockVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class LockVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance()
            .getLogger(LockVisitor.class);

    protected LockVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Lock lock, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(lock);
        SynchronizationSemaphoreMap semaphoreMap = SynchronizationSemaphoreMap.getInstance();
        boolean locked = false;
        
        try {
            logger.warning("Try to aquire lock for blockName = '", lock.getBlockName().getValue(), "', (Timeout="
                    + lock.getTimeout(), " ms)");
            semaphoreMap.accuireLock(lock.getBlockName().getValue(), lock.getTimeout().getValue());
            locked = true;
            logger.debug("Lock acquired. Executing synchronized block");
            super.visit(lock.getTestScriptElementList(), argument);
            logger.debug("Execution of synchronized block finished");
        } catch (SynchronizationSemaphoreException ex) {
            String error = "Synchronization failed: " + ex.getMessage();
            argument.setErrorMessage(error);
            argument.setStatus(TestScriptStatusType.FAILED);
            logger.warning(error);
            throw new SynchronizationException(error);
        } finally {
            if (locked) {
                semaphoreMap.releaseLock(lock.getBlockName().getValue());
            }
        }
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestScript testScript, TestScriptResult argument, boolean subTestScript) throws TestScriptException {
    	new SubTestScriptVisitor(getContext(), getTestScriptEngine()).visit(testScript, argument);
    }

}
