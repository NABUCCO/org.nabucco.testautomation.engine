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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestResultPrinter;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;
import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.client.ManualTestResultInput;
import org.nabucco.testautomation.engine.proxy.SwingProxyTest;
import org.nabucco.testautomation.engine.proxy.WebProxyTest;

import org.nabucco.testautomation.config.facade.datatype.Dependency;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.result.facade.datatype.ExecutionType;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualState;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualTestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * 
 * TestEngineTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class TestEngineTest extends TestEngineSupport {

	@Before
    public void setUp() throws Exception {
    	super.setUp();
    }
    
    @Test
    public void testDependencies() {
        
    	TestConfiguration config = getDefaultTestConfiguration("DependencyTest");

        TestConfigElement testSheet = createTestSheet("MyTestSheet");
        PropertyList testSheetProperties = new PropertyList();
        Property testSheetProp = TestScriptElementFactory.createStringProperty("TestSheetProperty", "My value");
        testSheetProperties.setName("testSheetProperties");
        add(testSheetProp, testSheetProperties);
        testSheet.setPropertyList(testSheetProperties);

        TestConfigElement testCase = createTestCase("MyTestCase");
        PropertyList testCaseProperties = new PropertyList();
        Property testCaseProp = TestScriptElementFactory.createIntegerProperty("TestCaseProperty", 4711);
        testCaseProperties.setName("testCaseProperties");
        add(testCaseProp, testCaseProperties);
        testCase.setPropertyList(testCaseProperties);

        TestConfigElement testStep1 = createTestStep("MyTestStep 1");
        testStep1.getTestScriptList().add(createTestScript("SuccessScript", false, 0));
        testStep1.getTestScriptList().add(createTestScript("FailureScript", true, 1));

        TestConfigElement testStep2 = createTestStep("MyTestStep 2");
        testStep2.getTestScriptList().add(createTestScript(0));
        Dependency container = new Dependency();
        container.setElement(testStep1);
        container.setOrderIndex(testStep2.getDependencyList().size());
		testStep2.getDependencyList().add(container);

        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testSheet, config);

        TestConfigurationResult result = execute(config); 
        
        TestResult testSheetResult = result.getTestResultList().get(0).getResult();
        assertNotNull("TestSheetResult was null", testSheetResult);
        assertTrue("TestSheetResult has no sub results", !testSheetResult.getTestResultList().isEmpty());
        TestResult testCaseResult = testSheetResult.getTestResultList().get(0).getResult();
        assertTrue("TestCaseResult has no sub-results", !testCaseResult.getTestResultList().isEmpty());
        TestResult testStep1Result = testCaseResult.getTestResultList().get(0).getResult();
        TestResult testStep2Result = testCaseResult.getTestResultList().get(1).getResult();
        assertTrue("Status of TestStep1Result is not FAILED", testStep1Result.getStatus() == TestConfigElementStatusType.FAILED);
        assertTrue("Status of TestStep2Result is not SKIPPED", testStep2Result.getStatus() == TestConfigElementStatusType.SKIPPED);
    }

    @Test
    public void testSimpleTestConfiguration() {

        TestConfiguration config = getDefaultTestConfiguration("SimpleTestConfiguration");

        TestConfigElement testSheet = createTestSheet("MyTestSheet");
        PropertyList testSheetProperties = new PropertyList();
        Property testSheetProp = TestScriptElementFactory.createStringProperty("TestSheetProperty", "My value");
        testSheetProperties.setName("testSheetProperties");
        add(testSheetProp, testSheetProperties);
        testSheet.setPropertyList(testSheetProperties);

        TestConfigElement testCase = createTestCase("MyTestCase");
        PropertyList testCaseProperties = new PropertyList();
        Property testCaseProp = TestScriptElementFactory.createIntegerProperty("TestCaseProperty", 4711);
        testCaseProperties.setName("testCaseProperties");
        add(testCaseProp,testCaseProperties);
        testCase.setPropertyList(testCaseProperties);

        TestConfigElement testStep1 = createTestStep("MyTestStep 1");
        testStep1.getTestScriptList().add(createTestScript(0));
        PropertyList testStepProperties = new PropertyList();
        Property testStepProp = TestScriptElementFactory.createStringProperty("TestStepProperty", "My value");
        testStepProperties.setName("testSheetProperties");
        add(testStepProp, testStepProperties);
        testStep1.setPropertyList(testStepProperties);

        TestConfigElement testStep2 = createTestStep("MyTestStep 2");
        testStep2.setPropertyList(testStepProperties);
        testStep2.getTestScriptList().add(createTestScript(0));

        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testSheet,config);

        TestConfigurationResult result = execute(config); 
        
        TestResult testSheetResult = result.getTestResultList().get(0).getResult();
        assertNotNull("TestSheetResult was null", testSheetResult);
        assertTrue("TestSheetResult has no sub results", !testSheetResult.getTestResultList().isEmpty());
        TestResult testCaseResult = testSheetResult.getTestResultList().get(0).getResult();
        assertTrue("TestCaseResult has no sub-results", !testCaseResult.getTestResultList().isEmpty());
        TestResult testStep1Result = testCaseResult.getTestResultList().get(0).getResult();
        TestResult testStep2Result = testCaseResult.getTestResultList().get(1).getResult();
        assertTrue("Status of TestStep1Result is not PASSED", testStep1Result.getStatus() == TestConfigElementStatusType.PASSED);
        assertTrue("Status of TestStep2Result is not PASSED", testStep2Result.getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    @Test
    public void testManualTestResult() {

        TestConfiguration config = getDefaultTestConfiguration("SimpleTestConfiguration");

        TestConfigElement testSheet = createTestSheet("MyTestSheet");
        PropertyList testSheetProperties = new PropertyList();
        Property testSheetProp = TestScriptElementFactory.createStringProperty("TestSheetProperty", "My value");
        testSheetProperties.setName("testSheetProperties");
        add(testSheetProp, testSheetProperties);
        testSheet.setPropertyList(testSheetProperties);

        TestConfigElement testCase = createTestCase("MyTestCase");
        PropertyList testCaseProperties = new PropertyList();
        Property testCaseProp = TestScriptElementFactory.createIntegerProperty("TestCaseProperty", 4711);
        testCaseProperties.setName("testCaseProperties");
        add(testCaseProp,testCaseProperties);
        testCase.setPropertyList(testCaseProperties);

        TestConfigElement testStep1 = createTestStep("MyTestStep 1");
        testStep1.setExecutionType(ExecutionType.MANUAL);
        
        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testSheet,config);

        try {
            TestExecutionInfo info = getEngine().executeTestConfiguration(config, createContext());

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
            
            TestConfigurationResult result = getEngine().getTestConfigurationResult(info);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.WAITING);
            
            ManualTestResult manualResult = (ManualTestResult) result.getTestResultList().get(0).getResult().getTestResultList().get(0).getResult().getTestResultList().get(0).getResult();
            manualResult.setErrorMessage("Shit");
            manualResult.setStatus(TestConfigElementStatusType.FAILED);
            manualResult.setState(ManualState.FINISHED);
            ClientInteraction userInput = new ManualTestResultInput(manualResult);
            getEngine().setClientInteraction(info, userInput);
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            
            result = getEngine().getTestConfigurationResult(info);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);            
            System.out.println(TestResultPrinter.toString(result));
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
    
    @Test
    public void testMultipleProxyAccess() {
    	TestConfiguration config = getDefaultTestConfiguration("MultipleProxyAccessTest");
        TestConfigElement testSheet = createTestSheet("MultipleProxyAccessTest");
        TestConfigElement testCase = createTestCase("WebAndSwingProxyTest");
        TestConfigElement testStep1 = createTestStep("Google Search");
        TestConfigElement testStep2 = createTestStep("Multi Swing Demo");
        TestConfigElement testStep3 = createTestStep("Second Google Search");
        TestConfigElement testStep4 = createTestStep("Second Swing Demo");
        
        WebProxyTest webProxyTest = new WebProxyTest();
        SwingProxyTest swingProxyTest = new SwingProxyTest();

        add(webProxyTest.createGoogleSearchScript("Prodyna"),testStep1);
        add(swingProxyTest.createStartApplicationScript("SpotlightDemo"),testStep2);
        add(swingProxyTest.createStopApplicationScript("SpotlightDemo"),testStep2);
        add(swingProxyTest.createStartApplicationScript("SalesManagerDemo"),testStep2);
        add(swingProxyTest.createStopApplicationScript("SalesManagerDemo"),testStep2);
        
        add(webProxyTest.createGoogleSearchScript("Steffen Schmidt"),testStep3);
        add(swingProxyTest.createStartApplicationScript("SpotlightDemo"),testStep4);
        add(swingProxyTest.createSearchScript(),testStep4);
        add(swingProxyTest.createStopApplicationScript("SpotlightDemo"),testStep4);
        
        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testStep3,testCase);
        add(testStep4,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    private TestScriptContainer createTestScript(int orderIndex) {
        TestScript script = TestScriptElementFactory.createTestScript("Demo TestScript");
        Loop loop = TestScriptElementFactory.createLoop(3, 1000L, null);
        Logger logger = TestScriptElementFactory.createLogger("TestMessage");
        add(logger, loop);
        add(loop,  script);
        TestScriptContainer container = new TestScriptContainer();
        container.setTestScript(script);
        container.setOrderIndex(orderIndex);
        return container;
    }
    
    private TestScriptContainer createTestScript(String name, boolean fail, int orderIndex) {
    	TestScript script = TestScriptElementFactory.createTestScript("Failure-Demo");
        Assertion assertion = new Assertion();
        assertion.setFail(fail);
        
        if (fail) {
            assertion.setMessage("Assertion set to fail");
        }
        add(assertion, script);
        TestScriptContainer container = new TestScriptContainer();
        container.setTestScript(script);
        container.setOrderIndex(orderIndex);
        return container;
    }

//    @Test
//    public void testWebServiceCall() throws Exception {
//
//         context.put(PropertyHelper.createStringProperty(TestContext.TEST_USERNAME, "sschmidt"));
//         context.put(PropertyHelper.createStringProperty("password", "geheim"));
//        		
//         TestSheetParser testSheetParser = new TestSheetParser();
//         TestCaseParser testCaseParser = new TestCaseParser();
//         TestStepParser testStepParser = new TestStepParser();
//                
//         TestSheet testSheet = testSheetParser.parseTestSheet(new
//         File("conf/test/sheets/Sample.xml"));
//         TestCase testCase = testCaseParser.parseTestCase(new
//         File("conf/test/cases/SampleCase.xml"));
//         TestStep testStep = testStepParser.parseTestStep(new
//         File("conf/test/steps/WebServiceTest.xml"));
//         testSheet.add(testCase);
//         testCase.add(testStep);
//                
//         try {
//         TestExecutionInfo info = engine.executeTest(testSheet, context);
//         assertNotNull(info);
//        
//         while (info.getTestStatus() != ExecutionStatusType.FINISHED) {
//         info = engine.getTestStatus(info);
//         System.out.println(info.getCurrentTestSheet());
//         System.out.println(info.getCurrentTestCase());
//         System.out.println(info.getCurrentTestStep());
//         try {
//         Thread.sleep(250);
//         } catch (InterruptedException e) {
//         }
//         }
//         TestResult result = engine.getTestResult(info);
//         assertNotNull(result);
//         assertTrue(result instanceof TestSheetResult);
//         TestSheetResult tsr = (TestSheetResult) result;
//         System.out.println(tsr.getMessage());
//         assertTrue(tsr.getStatus() == TestSheetStatusType.PASSED);
//         } catch (TestEngineException e) {
//         e.printStackTrace();
//         fail();
//         } catch (RemoteException e) {
//         e.printStackTrace();
//         fail();
//         }
//
//    }

//    @Test
//    public void testDatabaseQuery() throws Exception {
//
//         context.put(new StringProperty(TestContext.TEST_USERNAME, "sschmidt"));
//        		
//         TestSheetParser testSheetParser = new TestSheetParser();
//         TestCaseParser testCaseParser = new TestCaseParser();
//         TestStepParser testStepParser = new TestStepParser();
//                
//         TestSheet testSheet = testSheetParser.parseTestSheet(new
//         File("conf/test/sheets/Sample.xml"));
//         TestCase testCase = testCaseParser.parseTestCase(new
//         File("conf/test/cases/SampleCase.xml"));
//         TestStep testStep = testStepParser.parseTestStep(new
//         File("conf/test/steps/DatabaseTest.xml"));
//         testSheet.add(testCase);
//         testCase.add(testStep);
//        
//         try {
//         TestExecutionInfo info = engine.executeTest(testSheet, context);
//         assertNotNull(info);
//        
//         while (info.getTestStatus() != ExecutionStatusType.FINISHED) {
//         info = engine.getTestStatus(info);
//         }
//         TestResult result = engine.getTestResult(info);
//         assertNotNull(result);
//         assertTrue(result instanceof TestSheetResult);
//         TestSheetResult tsr = (TestSheetResult) result;
//         assertTrue(tsr.getStatus() == TestSheetStatusType.PASSED);
//         } catch (TestEngineException e) {
//         e.printStackTrace();
//         fail();
//         } catch (RemoteException e) {
//         e.printStackTrace();
//         fail();
//         }
//
//    }

}
