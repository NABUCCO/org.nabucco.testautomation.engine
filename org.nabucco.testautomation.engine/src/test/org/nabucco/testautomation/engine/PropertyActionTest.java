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
package org.nabucco.testautomation.engine;

import java.text.SimpleDateFormat;

import junit.framework.Assert;

import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngineImpl;
import org.nabucco.testautomation.engine.visitor.script.TestScriptVisitor;
import org.nabucco.testautomation.facade.datatype.base.StringValue;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyReference;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyType;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.PropertyActionType;


/**
 * PropertyActionTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class PropertyActionTest extends TestEngineSupport {

	@Test
	public void testCopyPropertyAction() {

		// Test COPY
		TestContext context = createContext();
		TestScript script = createTestScript();

		PropertyAction copyAction = new PropertyAction();
		copyAction.setAction(PropertyActionType.COPY);
		Property copyFrom = TestScriptElementFactory.createStringProperty(
				"copyFrom", "value of copyFrom");
		Property copyTo = TestScriptElementFactory.createStringProperty(
				"copyTo", "value of copyTo");
		PropertyReference copyFromRef = TestScriptElementFactory
				.createPropertyReference("copyFrom");
		PropertyReference copyToRef = TestScriptElementFactory
				.createPropertyReference("copyTo");
		copyAction.setPropertyRef(copyFromRef);
		copyAction.setTarget(copyToRef);
		context.put(copyFrom);
		context.put(copyTo);
		add(copyAction, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertNotNull(context.getProperty("copyFrom"));
			Property targetProp = context.getProperty("copyTo");
			Assert.assertNotNull(targetProp);
			Assert.assertTrue(targetProp.getType() == PropertyType.STRING);
			Assert.assertEquals(((StringProperty) copyFrom).getValue()
					.getValue(), ((StringProperty) targetProp).getValue()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testClearPropertyAction() {

		// Test CLEAR
		TestContext context = createContext();
		TestScript script = createTestScript();

		PropertyAction clearAction = new PropertyAction();
		clearAction.setAction(PropertyActionType.CLEAR);
		Property clearTestProperty = TestScriptElementFactory
				.createStringProperty("clearTestProperty",
						"value of clearTestProperty");
		context.put(clearTestProperty);
		PropertyReference clearTestRef = TestScriptElementFactory
				.createPropertyReference("clearTestProperty");
		clearAction.setPropertyRef(clearTestRef);
		add(clearAction,script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertNotNull(context.getProperty("clearTestProperty"));
			Property clearedProp = context.getProperty("clearTestProperty");
			Assert.assertNotNull(clearedProp);
			Assert.assertTrue(clearedProp.getType() == PropertyType.STRING);
			StringValue value = ((StringProperty) clearedProp).getValue();
			Assert.assertNull(value);
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testDeletePropertyAction() {

		// Test DELETE
		TestContext context = createContext();
		TestScript script = createTestScript();

		PropertyAction deleteAction = new PropertyAction();
		deleteAction.setAction(PropertyActionType.DELETE);
		Property deleteProp = TestScriptElementFactory.createStringProperty(
				"deleteProp", "deletePropValue");
		context.put(deleteProp);
		PropertyReference ref = TestScriptElementFactory.createPropertyReference("deleteProp");
		deleteAction.setPropertyRef(ref);
		add(deleteAction, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Property deletedProp = context.getProperty("deleteProp");
			Assert.assertNull("Property was not deleted from TestContext",
					deletedProp);
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSetPropertyAction() {

		// Test SET
		TestContext context = createContext();
		TestScript script = createTestScript();

		PropertyAction setAction = new PropertyAction();
		setAction.setAction(PropertyActionType.SET);
		StringProperty setTestProp = TestScriptElementFactory
				.createStringProperty("setTestProp", "org value");
		context.put(setTestProp);
		StringProperty setValueProp = TestScriptElementFactory
				.createStringProperty("setValueProp", "new value");
		PropertyReference ref = TestScriptElementFactory.createPropertyReference("setTestProp");
		setAction.setPropertyRef(ref);
		setAction.setValue(setValueProp.getValue().getValue());
		add(setAction, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			StringProperty resettedProp = (StringProperty) context
					.getProperty("setTestProp");
			Assert.assertNotNull("Property was not found in TestContext",
					resettedProp);
			Assert.assertEquals(setValueProp.getValue(),
					resettedProp.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSetDateProperty() {

		// Test SET
		TestContext context = createContext();
		TestScript script = createTestScript();

		PropertyAction setAction = new PropertyAction();
		setAction.setAction(PropertyActionType.SET);
		
		DateProperty setTestProp = TestScriptElementFactory
				.createDateProperty("setTestProp", "dd.MM.yyyy", "12.12.2012");
		context.put(setTestProp);
		PropertyReference ref = TestScriptElementFactory.createPropertyReference("setTestProp");
		setAction.setPropertyRef(ref);
		setAction.setValue("13.12.2012");
		add(setAction, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			DateProperty resettedProp = (DateProperty) context
					.getProperty("setTestProp");
			Assert.assertNotNull("Property was not found in TestContext",
					resettedProp);
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			Assert.assertEquals("13.12.2012",
					df.format(resettedProp.getValue().getValue()));
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
