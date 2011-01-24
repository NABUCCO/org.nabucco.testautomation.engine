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
import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;

/**
 * AssertionVisitor
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class AssertionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

//    private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
//            AssertionVisitor.class);

    /**
     * Constructs a new AssertionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     * @param testScriptEngine
     */
    protected AssertionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
	public void visit(Assertion assertion, TestScriptResult argument)
			throws TestScriptException {
		getContext().setCurrentTestScriptElement(assertion);

		if (assertion.getFail() != null
				&& assertion.getFail().getValue() != null
				&& assertion.getFail().getValue()) {
			String message = null;

			if (assertion.getMessage() != null) {
				message = assertion.getMessage().getValue();
			} else {
				message = "Assertion '" + assertion.getName().getValue() + "' failed";
			}

			argument.setStatus(TestScriptStatusType.FAILED);
			argument.setErrorMessage(message);
			throw new AssertionException(message);
		}

		if (assertion.getClassName() != null
				&& assertion.getClassName().getValue() != null
				&& assertion.getPropertyRef() != null
				&& assertion.getPropertyRef().getValue() != null) {

			String assertionClass = assertion.getName().getValue();
			String propertyRef = assertion.getPropertyRef().getValue();
			Property property = getContext().getProperty(propertyRef);

			if (property == null) {
				throw new AssertionException(
						"No Property found for reference '" + propertyRef + "'");
			}
			try {
				@SuppressWarnings("unchecked")
				Class<? extends org.nabucco.testautomation.engine.assertion.Assertion> clazz = (Class<? extends org.nabucco.testautomation.engine.assertion.Assertion>) Class
						.forName(assertionClass);
				org.nabucco.testautomation.engine.assertion.Assertion instance = clazz
						.newInstance();
				instance.executeAssertion(property);
			} catch (ClassNotFoundException e) {
				throw new AssertionException("AssertionClass '"
						+ assertionClass + "' not found");
			} catch (InstantiationException e) {
				throw new AssertionException("AssertionClass '"
						+ assertionClass + "' could not be instantiated: "
						+ e.getMessage());
			} catch (IllegalAccessException e) {
				throw new AssertionException("AssertionClass '"
						+ assertionClass + "' could not be accessed: "
						+ e.getMessage());
			}
		}
		super.visit(assertion, argument);
	}
    
}
