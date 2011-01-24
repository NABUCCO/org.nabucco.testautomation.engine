package org.nabucco.testautomation.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestResultPrinter;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.engine.sub.TestScriptEngineImpl;
import org.nabucco.testautomation.engine.visitor.script.TestScriptVisitor;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyReference;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.BreakLoop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TextMessage;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.ConditionType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.LoggerLevelType;

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
public class TestScriptTest extends TestEngineSupport {

	private static final String TEST_PROP = "TestProp";

	@Before
	public void setup() throws Exception {
		super.setUp();
		BasicConfigurator.configure();
	}

	@Test
	public void testLogger() throws Exception {

		TestScriptEngine testScriptEngine = new TestScriptEngineImpl();
		TestContext context = createContext();
		TestScript script = createTestScript();
		add(createLogger(), script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				testScriptEngine);
		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertEquals(script.getName(), result.getTestScriptName());
			Assert.assertEquals(script.getId(), result.getElementId()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testLoop() {

		TestScript script = createTestScript();
		Loop loop = new Loop();
		loop.setMaxIterations(3);
		loop.setWait(1000L);
		add(createLogger(), loop);
		add(loop, script);
		TestScriptVisitor visitor = new TestScriptVisitor(createContext(),
				new TestScriptEngineImpl());
		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertEquals(script.getName(), result.getTestScriptName());
			Assert.assertEquals(script.getId(), result.getElementId()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testForeach() {

		TestContext context = createContext();
		TestScript script = createTestScript();
		Foreach loop = new Foreach();
		loop.setElementName("currentElement");
		PropertyReference ref = new PropertyReference();
		ref.setValue("PropertyList");
		loop.setIterableRef(ref);

		PropertyList list = new PropertyList();
		list.setName("PropertyList");
		StringProperty sp = new StringProperty();
		sp.setName("StringProperty");
		sp.setValue("string value");
		IntegerProperty ip = new IntegerProperty();
		ip.setName("IntegerProperty");
		ip.setValue(1);
		DateProperty dp = new DateProperty();
		dp.setName("DateProperty");
		dp.setValue(new Date());
		add(sp, list);
		add(ip, list);
		add(dp, list);
		context.put(list);

		Logger logger = new Logger();
		logger.setLevel(LoggerLevelType.INFO);
		TextMessage msg = new TextMessage();
		msg.setText("The value of currentElement is: ");
		PropertyReference propRef = new PropertyReference();
		propRef.setValue("currentElement");
		msg.setPropertyRef(propRef);
		add(msg, logger);
		add(logger, loop);
		add(loop, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertEquals(script.getName(), result.getTestScriptName());
			Assert.assertEquals(script.getId(), result.getElementId()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}

	}

	@Test
	public void testCondition() {

		TestContext context = createContext();
		String value = "12345";

		TestScript script = createTestScript();
		Condition c1 = new Condition();
		c1.setConditionType(ConditionType.EQUALS);
		PropertyReference r1 = new PropertyReference();
		r1.setValue(TEST_PROP);
		c1.setPropertyRef(r1);
		c1.setValue(value);
		add(createMessage("CONDITION OKAY"), c1);

		Condition c2 = new Condition();
		c2.setConditionType(ConditionType.NOT_EQUALS);
		PropertyReference r2 = new PropertyReference();
		r2.setValue(TEST_PROP);
		c2.setPropertyRef(r2);
		c2.setValue(value);
		add(createMessage("CONDITION FAILED"), c2);

		IntegerProperty ip = new IntegerProperty();
		ip.setName("IntegerProp");
		ip.setValue(11);
		context.put(ip);
		Condition c3 = new Condition();
		c3.setConditionType(ConditionType.GT);
		PropertyReference r3 = new PropertyReference();
		r3.setValue("IntegerProp");
		c3.setPropertyRef(r3);
		c3.setValue("10");
		add(createMessage("CONDITION OKAY"), c3);

		add(c1,script);
		add(c2,script);
		add(c3,script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertEquals(script.getName(), result.getTestScriptName());
			Assert.assertEquals(script.getId(), result.getElementId()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testBreakLoop() {

		TestContext context = createContext();
		TestScript script = createTestScript();
		Foreach loop = new Foreach();
		loop.setElementName("currentElement");
		PropertyReference ref = new PropertyReference();
		ref.setValue("PropertyList");
		loop.setIterableRef(ref);

		PropertyList list = new PropertyList();
		list.setName("PropertyList");
		StringProperty sp = new StringProperty();
		sp.setName("StringProperty");
		sp.setValue("string value");
		IntegerProperty ip = new IntegerProperty();
		ip.setName("IntegerProperty");
		ip.setValue(1);
		DateProperty dp = new DateProperty();
		dp.setName("DateProperty");
		dp.setValue(new Date());
		add(sp, list);
		add(ip, list);
		add(dp, list);
		context.put(list);

		Logger logger = new Logger();
		logger.setLevel(LoggerLevelType.INFO);
		TextMessage msg = new TextMessage();
		msg.setText("The value of currentElement is: ");
		PropertyReference propRef = new PropertyReference();
		propRef.setValue("currentElement");
		msg.setPropertyRef(propRef);
		add(msg, logger);

		BreakLoop breakLoop = new BreakLoop();
		Condition breakCondition = new Condition();
		breakCondition.setConditionType(ConditionType.EQUALS);
		PropertyReference currRef = new PropertyReference();
		currRef.setValue("currentElement");
		breakCondition.setPropertyRef(currRef);
		breakCondition.setValue("1");
		add(breakCondition, breakLoop);

		add(logger, loop);
		add(createMessage("Before BreakLoop"),loop);
		add(breakLoop,loop);
		add(createMessage("After BreakLoop"),loop);

		add(loop, script);

		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(script, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			Assert.assertEquals(script.getName(), result.getTestScriptName());
			Assert.assertEquals(script.getId(), result.getElementId()
					.getValue());
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void testSimpleSubTestScript() {
		
		TestContext context = createContext();
		TestScript parentScript = createTestScript();
		parentScript.setName("ParentScript");
		TestScript childScript = createTestScript();
		childScript.setName("ChildScript");
		
		add(TestScriptElementFactory.createLogger("ChildScript: executing ..."), childScript);
		
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript"),parentScript);
		add(childScript,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: after SubTestScript"),parentScript);
		
		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(parentScript, result);
			Assert.assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			System.out.println(TestResultPrinter.toString(result, 0));
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testMultiSubTestScript() {
		
		TestContext context = createContext();
		TestScript parentScript = createTestScript();
		parentScript.setName("ParentScript");
		
		TestScript childScript1 = createTestScript();
		TestScript childScript2 = createTestScript();
		TestScript childScript3 = createTestScript();
		childScript1.setName("ChildScript_1");
		childScript2.setName("ChildScript_2");
		childScript3.setName("ChildScript_3");
		
		TestScript childScript1_1 = createTestScript();
		TestScript childScript2_1 = createTestScript();
		TestScript childScript3_1 = createTestScript();
		childScript1_1.setName("ChildScript_1_1");
		childScript2_1.setName("ChildScript_2_1");
		childScript3_1.setName("ChildScript_3_1");
		
		add(TestScriptElementFactory.createLogger("ChildScript_1: executing ..."),childScript1);
		add(childScript1_1, childScript1);
		add(TestScriptElementFactory.createLogger("ChildScript_2: executing ..."),childScript2);
		add(childScript2_1,childScript2);
		add(TestScriptElementFactory.createLogger("ChildScript_3: executing ..."),childScript3);
		add(childScript3_1,childScript3);
		
		add(TestScriptElementFactory.createLogger("ChildScript_1_1: executing ..."),childScript1_1);
		add(TestScriptElementFactory.createLogger("ChildScript_2_1: executing ..."),childScript2_1);
		add(TestScriptElementFactory.createLogger("ChildScript_3_1: executing ..."),childScript3_1);
		
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript1"),parentScript);
		add(childScript1,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript 2"),parentScript);
		add(childScript2,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript 3"),parentScript);
		add(childScript3,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: after SubTestScript"),parentScript);
		
		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		try {
			TestScriptResult result = TestResultHelper.createTestScriptResult();
			visitor.visit(parentScript, result);
			assertEquals(TestScriptStatusType.PASSED, result.getStatus());
			assertTrue(result.getElementResultList().size() == 3);
			
			assertTrue(result.getElementResultList().get(0) instanceof TestScriptResult);
			assertTrue(result.getElementResultList().get(0).getElementStatus() == TestScriptElementStatusType.EXECUTED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(0)).getStatus() == TestScriptStatusType.PASSED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(0)).getElementResultList().size() == 1);
			
			assertTrue(result.getElementResultList().get(1) instanceof TestScriptResult);
			assertTrue(result.getElementResultList().get(1).getElementStatus() == TestScriptElementStatusType.EXECUTED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(1)).getStatus() == TestScriptStatusType.PASSED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(1)).getElementResultList().size() == 1);
			
			assertTrue(result.getElementResultList().get(2) instanceof TestScriptResult);
			assertTrue(result.getElementResultList().get(2).getElementStatus() == TestScriptElementStatusType.EXECUTED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(2)).getStatus() == TestScriptStatusType.PASSED);
			assertTrue(((TestScriptResult) result.getElementResultList().get(2)).getElementResultList().size() == 1);
			
			System.out.println(TestResultPrinter.toString(result, 0));
		} catch (TestScriptException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void testSubTestScriptFailure() {
		
		TestContext context = createContext();
		TestScript parentScript = createTestScript();
		parentScript.setName("ParentScript");
		
		TestScript childScript1 = createTestScript();
		TestScript childScript2 = createTestScript();
		TestScript childScript3 = createTestScript();
		childScript1.setName("ChildScript_1");
		childScript2.setName("ChildScript_2");
		childScript3.setName("ChildScript_3");
		
		TestScript childScript1_1 = createTestScript();
		TestScript childScript2_1 = createTestScript();
		TestScript childScript3_1 = createTestScript();
		childScript1_1.setName("ChildScript_1_1");
		childScript2_1.setName("ChildScript_2_1");
		childScript3_1.setName("ChildScript_3_1");
		
		add(TestScriptElementFactory.createLogger("ChildScript_1: executing ..."),childScript1);
		add(childScript1_1,childScript1);
		add(TestScriptElementFactory.createLogger("ChildScript_2: executing ..."),childScript2);
		Assertion assertion = new Assertion();
		assertion.setFail(Boolean.TRUE);
		assertion.setMessage("TestScript set to fail");
		add(TestScriptElementFactory.createLogger("ChildScript_2: executing ..."),childScript2);
		add(childScript2_1,childScript2);
		add(assertion,childScript2);
		add(TestScriptElementFactory.createLogger("ChildScript_3: executing ..."),childScript3);
		add(childScript3_1,childScript3);
		
		add(TestScriptElementFactory.createLogger("ChildScript_1_1: executing ..."),childScript1_1);
		add(TestScriptElementFactory.createLogger("ChildScript_2_1: executing ..."),childScript2_1);
		add(TestScriptElementFactory.createLogger("ChildScript_3_1: executing ..."),childScript3_1);
		
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript1"),parentScript);
		add(childScript1,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript 2"),parentScript);
		add(childScript2,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: before SubTestScript 3"),parentScript);
		add(childScript3,parentScript);
		add(TestScriptElementFactory.createLogger("ParentScript: after SubTestScript"),parentScript);
		
		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		TestScriptResult result = TestResultHelper.createTestScriptResult();
		try {
			visitor.visit(parentScript, result);
			Assert.fail("Exception should have been thrown");
		} catch(TestScriptException ex) {
			System.out.println(TestResultPrinter.toString(result, 0));
		} catch(Exception ex) {
			Assert.fail("Exception should have been thrown");
		}
	}
	
	@Test
	public void testSuccessAssertion() {
		
		TestContext context = createContext();
		TestScript script = TestScriptElementFactory.createTestScript("testSuccessAssertion");
		
		Assertion assertion = new Assertion();
		assertion.setName("org.nabucco.testautomation.engine.assertion.DummySuccessAssertion");
		PropertyReference ref = new PropertyReference();
		ref.setValue(TEST_PROP);
		assertion.setPropertyRef(ref);
		add(assertion,script);
		
		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		TestScriptResult result = TestResultHelper.createTestScriptResult();
		try {
			visitor.visit(script, result);
		} catch(Exception ex) {
			ex.printStackTrace();
			Assert.fail("Exception should not have been thrown");
		}
	}
	
	@Test
	public void testFailAsserion() {
		
		TestContext context = createContext();
		TestScript script = TestScriptElementFactory.createTestScript("testSuccessAssertion");
		
		Assertion assertion = new Assertion();
		assertion.setName("org.nabucco.testautomation.engine.assertion.DummyFailAssertion");
		PropertyReference ref = new PropertyReference();
		ref.setValue(TEST_PROP);
		assertion.setPropertyRef(ref);
		add(assertion,script);
		
		TestScriptVisitor visitor = new TestScriptVisitor(context,
				new TestScriptEngineImpl());

		TestScriptResult result = TestResultHelper.createTestScriptResult();
		try {
			visitor.visit(script, result);
			Assert.fail("Exception should not have been thrown");
		} catch(AssertionException ex) {
			TestResultPrinter.toString(result, 0);
		} catch (Exception ex) {
			Assert.fail("Other Exception should not have been thrown");
		}
	}
	
	private Logger createLogger() {

		Logger logger = new Logger();
		logger.setLevel(LoggerLevelType.INFO);
		TextMessage msg = new TextMessage();
		msg.setText("The value of " + TEST_PROP + " is: ");
		PropertyReference ref = new PropertyReference();
		ref.setValue(TEST_PROP);
		msg.setPropertyRef(ref);
		add(msg, logger);
		return logger;
	}

	private Logger createMessage(String msg) {

		Logger logger = new Logger();
		logger.setLevel(LoggerLevelType.INFO);
		TextMessage message = new TextMessage();
		message.setText(msg);
		add(message,logger);
		return logger;
	}

}
