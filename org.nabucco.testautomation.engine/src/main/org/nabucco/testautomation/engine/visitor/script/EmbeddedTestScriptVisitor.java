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
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * EmbeddedTestScriptVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class EmbeddedTestScriptVisitor extends TestScriptVisitor {

    /**
     * Constructs a new SubTestScriptVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
	public EmbeddedTestScriptVisitor(TestContext context, TestScriptEngine testScriptEngine) {
		super(context, testScriptEngine);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(TestScript script, TestScriptResult argument)
			throws TestScriptException {
		TestScriptResult subResult = TestResultHelper.createTestScriptResult();
		subResult.setElementId(script.getId());
		subResult.setTestScriptName(script.getName());
		subResult.setTestScriptKey(script.getIdentificationKey());
		
		try {
			super.visit(script, argument);
			subResult.setElementStatus(TestScriptElementStatusType.EXECUTED);
			argument.getElementResultList().add(subResult);
		} catch (Exception ex) {
			throw new TestScriptException("EmbeddedTestScript '"
					+ script.getIdentificationKey().getValue() + "' failed. Cause: "
					+ ex.getMessage());
		}

		if (subResult.getStatus() == TestScriptStatusType.FAILED) {
			throw new TestScriptException("EmbeddedTestScript '"
					+ script.getIdentificationKey().getValue() + "' failed. Cause: "
					+ subResult.getErrorMessage().getValue());
		}
	}
	
}
