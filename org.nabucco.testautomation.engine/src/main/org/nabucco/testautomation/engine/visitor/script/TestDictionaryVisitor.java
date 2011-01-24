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

import org.nabucco.testautomation.engine.exception.TestScriptException;

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

/**
 * TestDictionaryVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface TestDictionaryVisitor<A> {
    
	/**
	 * Visits a {@link TestScript}.
	 * 
	 * @param script
	 * @param argument
	 * @param subTestScript
	 * @throws TestScriptException
	 */
    public void visit(TestScript script, A argument, boolean subTestScript) throws TestScriptException;
    
    /**
	 * Visits a {@link TestScript}.
	 * 
	 * @param script
	 * @param argument
	 * @throws TestScriptException
	 */
    public void visit(TestScript script, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Logger}.
     * 
     * @param logger
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Logger logger, A argument) throws TestScriptException;

    /**
     * Visits a {@link TextMessage}.
     * 
     * @param message
     * @param argument
     * @throws TestScriptException
     */
    public void visit(TextMessage message, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Condition}.
     * 
     * @param condition
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Condition condition, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Execution}.
     * 
     * @param execution
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Execution execution, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Action}.
     * 
     * @param action
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Action action, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Loop}.
     * 
     * @param loop
     * @param argmument
     * @throws TestScriptException
     */
    public void visit(Loop loop, A argmument) throws TestScriptException;
    
    /**
     * Visits a {@link Lock}.
     * 
     * @param lock
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Lock lock, A argument) throws TestScriptException;

    /**
     * Visits a {@link Assertion}.
     * 
     * @param assertion
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Assertion assertion, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link Foreach}.
     * 
     * @param foreach
     * @param argument
     * @throws TestScriptException
     */
    public void visit(Foreach foreach, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link BreakLoop}.
     * 
     * @param breakLoop
     * @param argument
     * @throws TestScriptException
     */
    public void visit(BreakLoop breakLoop, A argument) throws TestScriptException;
    
    /**
     * Visits a {@link PropertyAction}.
     * 
     * @param propertyAction
     * @param argument
     * @throws TestScriptException
     */
    public void visit(PropertyAction propertyAction, A argument) throws TestScriptException;
    
}
