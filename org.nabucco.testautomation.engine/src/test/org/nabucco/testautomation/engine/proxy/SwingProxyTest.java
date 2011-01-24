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
package org.nabucco.testautomation.engine.proxy;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.event.KeyEvent;

import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyReference;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.ConditionType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * 
 * SwingProxyTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class SwingProxyTest extends TestEngineSupport {

    @Before
    public void setUp() throws Exception {
    	super.setUp();
    }
    
    @Test
    public void testStartStopMultiApplications() {

    	TestConfiguration config = getDefaultTestConfiguration("SwingProxyTest");
        TestConfigElement testSheet = createTestSheet("SwingProxyTest");
        TestConfigElement testCase = createTestCase("MultiDemoTest");
        TestConfigElement testStep1 = createTestStep("Start and Stop SpotlightDemo");
        TestConfigElement testStep2 = createTestStep("Start and Stop SalesManagerDemo");
        
        add(createStartApplicationScript("SpotlightDemo"),testStep1);
        add(createStopApplicationScript("SpotlightDemo"),testStep1);
        
        add(createStartApplicationScript("SalesManagerDemo"),testStep2);
        add(createStopApplicationScript("SalesManagerDemo"),testStep2);
        
        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    @Test
    public void testSpotlightDemoSearch() {

    	TestConfiguration config = getDefaultTestConfiguration("SwingProxyTest");
        TestConfigElement testSheet = createTestSheet("SwingProxyTest");
        TestConfigElement testCase = createTestCase("SpotlightDemoTest");
        TestConfigElement testStep1 = createTestStep("Start SpotlightDemo");
        TestConfigElement testStep2 = createTestStep("Perform Search");
        TestConfigElement testStep3 = createTestStep("Stop SpotlightDemo");
        
        add(createStartApplicationScript("SpotlightDemo"),testStep1);
        add(createSearchScript(),testStep2);
        add(createStopApplicationScript("SpotlightDemo"),testStep3);
        
        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testStep3,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    @Test
    public void testSalesManagerDemo() {

    	TestConfiguration config = getDefaultTestConfiguration("SwingProxyTest");
        TestConfigElement testSheet = createTestSheet("SwingProxyTest");
        TestConfigElement testCase = createTestCase("SalesManagerDemoTest");
        TestConfigElement testStep1 = createTestStep("Start SalesManagerDemo");
        TestConfigElement testStep2 = createTestStep("Perform Search");
        TestConfigElement testStep3 = createTestStep("Stop SalesManangerDemo");
        
        add(createStartApplicationScript("SalesManagerDemo"),testStep1);
        add(createSalesManagerSearchScript(),testStep1);
        add(createStopApplicationScript("SalesManagerDemo"),testStep3);
        
        add(testCase,testSheet);
        add(testStep1,testCase);
        add(testStep2,testCase);
        add(testStep3,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    @Test
    public void testDemoApplet() {
    
    	TestConfiguration config = getDefaultTestConfiguration("SwingProxyTest");
        TestConfigElement testSheet = createTestSheet("SwingProxyTest");
        TestConfigElement testCase = createTestCase("AppletDemoTest");
        TestConfigElement testStep = createTestStep("Test DemoApplet");
        
        add(createStartAppletScript("Applet"),testStep);
        add(createDemoAppletScript(),testStep);
        add(createStopAppletScript("Applet"),testStep);
        
        add(testCase,testSheet);
        add(testStep,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    @Test
    public void testActionFailure() {
    
    	TestConfiguration config = getDefaultTestConfiguration("SwingProxyTest");
        TestConfigElement testSheet = createTestSheet("SwingProxyTest");
        TestConfigElement testCase = createTestCase("ActionFailureTest");
        TestConfigElement testStep = createTestStep("Perform invalid action");
        
        add(createStartApplicationScript("SpotlightDemo"),testStep);
        add(createActionFailureScript(),testStep);
        
        add(testCase,testSheet);
        add(testStep,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        
        TestResult testSheetResult = result.getTestResultList().get(0).getResult();
        assertNotNull("TestSheetResult was null", testSheetResult);
        assertTrue("TestSheetResult has no sub results", !testSheetResult.getTestResultList().isEmpty());
        TestResult testCaseResult = testSheetResult.getTestResultList().get(0).getResult();
        assertTrue("TestCaseResult has no sub-results", !testCaseResult.getTestResultList().isEmpty());
        TestResult testStepResult = testCaseResult.getTestResultList().get(0).getResult();
        assertTrue("TestStepResult does not contain 2 TestScriptResults", testStepResult.getTestScriptResultList().size() == 2);
        assertTrue("Status of TestScriptResult 1 is not PASSED", testStepResult.getTestScriptResultList().get(0).getStatus() == TestScriptStatusType.PASSED);
        assertTrue("Status of TestScriptResult 2 is not FAILED", testStepResult.getTestScriptResultList().get(1).getStatus() == TestScriptStatusType.FAILED);
        assertTrue("Status of TestStepResult is not FAILED", testStepResult.getStatus() == TestConfigElementStatusType.FAILED);
        assertTrue("Status of TestCaseResult is not FAILED", testCaseResult.getStatus() == TestConfigElementStatusType.FAILED);
        assertTrue("Status of TestSheetResult is not FAILED", testSheetResult.getStatus() == TestConfigElementStatusType.FAILED);
    }
    
    public TestScript createActionFailureScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("ActionFailureScript");
    	
    	IntegerProperty index = TestScriptElementFactory.createIntegerProperty("COMPONENT_INDEX", 0);
    	StringProperty tfPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,1,1");
    	StringProperty input = TestScriptElementFactory.createStringProperty("input", "Test input");
    	
    	Metadata frameMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_FRAME", index);
    	Metadata tfMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_TEXTINPUT", tfPath);
    	tfMetadata.setParent(frameMetadata);
    	
    	Action inputAction = TestScriptElementFactory.createAction("ENTER", tfMetadata, input);
    	
    	Execution execution = TestScriptElementFactory.createExecution(inputAction, inputAction);
    	
    	add(TestScriptElementFactory.createLogger("Start failing Action"),script);
        add(TestScriptElementFactory.createLoop(1, 1000L, null),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(1, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Failing Action finished"),script);
    	return script;
    }
    
    public TestScript createDemoAppletScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("AccessDemoApplet");

		IntegerProperty index = TestScriptElementFactory.createIntegerProperty("COMPONENT_INDEX", 0);
		StringProperty tfPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,0,0");
		StringProperty btnPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,0,2");
		BooleanProperty isAvailable = TestScriptElementFactory.createBooleanProperty("isAvailable", null);
		StringProperty tfContent = TestScriptElementFactory.createStringProperty("TextFieldContent", null);
		StringProperty input1 = TestScriptElementFactory.createStringProperty("param1", "Test input part 1");
		StringProperty input2 = TestScriptElementFactory.createStringProperty("param2", "Test input part 2");
		
		Metadata frameMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_FRAME", index);
		Metadata tfMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_TEXTINPUT", tfPath);
		Metadata btnMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_BUTTON", btnPath);
		
        Action availableAction = TestScriptElementFactory.createAction("IS_AVAILABLE", frameMetadata, isAvailable);
        Action readAction = TestScriptElementFactory.createAction("READ", tfMetadata, tfContent);
        Action input1Action = TestScriptElementFactory.createAction("ENTER", tfMetadata, input1);
        Action input2Action = TestScriptElementFactory.createAction("ENTER", tfMetadata, input2);
        Action clickAction = TestScriptElementFactory.createAction("LEFTCLICK", btnMetadata);
		
        Execution execution = TestScriptElementFactory.createExecution(availableAction);
        Execution execution2 = TestScriptElementFactory.createExecution(readAction);
        Execution execution3 = TestScriptElementFactory.createExecution(input1Action, clickAction);
        Execution execution4 = TestScriptElementFactory.createExecution(input2Action, clickAction);
        
        Condition condition = new Condition();
        condition.setConditionType(ConditionType.EQUALS);
        PropertyReference propertyRef = new PropertyReference();
        propertyRef.setValue("isAvailable");
		condition.setPropertyRef(propertyRef);
		condition.setValue("true");
		add(TestScriptElementFactory.createLogger("Applet found"),condition);
		
		Condition condition2 = new Condition();
        condition2.setConditionType(ConditionType.NOT_EQUALS);
		condition2.setPropertyRef(propertyRef);
		condition2.setValue("true");
		add(TestScriptElementFactory.createLogger("Applet NOT found"),condition2);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Check Availability of Applet"),script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(execution,script);
        add(condition,script);
        add(condition2,script);
        add(execution2,script);
        add(TestScriptElementFactory.createLogger("Content of TextField: ", "TextFieldContent"),script);
        add(execution3,script);
        add(TestScriptElementFactory.createLoop(2, 1000L, null),script);
        add(execution4,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
    	return script;
    }
    
    public TestScript createSearchScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Perform search in SpotlightDemo");
        
        StringProperty path = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,2,1");
        StringProperty searchString = TestScriptElementFactory.createStringProperty("searchString", "books");
        IntegerProperty key =  TestScriptElementFactory.createIntegerProperty("keyCode", KeyEvent.VK_ENTER);
        IntegerProperty index =  TestScriptElementFactory.createIntegerProperty("COMPONENT_INDEX", 0);
        
        Metadata frameMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_FRAME", index);
        Metadata textFieldMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING,frameMetadata, "SWING_TEXTINPUT", path);

		Action inputAction = TestScriptElementFactory.createAction("ENTER", textFieldMetadata, searchString);
		Action enterAction = TestScriptElementFactory.createAction("PRESS_KEY", textFieldMetadata, key);
		
        Execution execution = TestScriptElementFactory.createExecution(inputAction, enterAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Enter TextInput"),script);
        add(execution,script);
        add(TestScriptElementFactory.createLogger("TextInput entered"),script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
    	return script;
    }
    
    public TestScript createSalesManagerSearchScript() {

    	TestScript script = TestScriptElementFactory.createTestScript("Perform search in SalesManager");
        
        StringProperty viewSaleButtonPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,1,0,1,1");
        StringProperty searchString1 = TestScriptElementFactory.createStringProperty("searchString", "test");
        StringProperty searchString2 = TestScriptElementFactory.createStringProperty("searchString", "0000006973");
        StringProperty searchFieldPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,1,1,1,0,2");
        IntegerProperty index =  TestScriptElementFactory.createIntegerProperty("COMPONENT_INDEX", 0);
        StringProperty searchButtonPath = TestScriptElementFactory.createStringProperty("COMPONENT_PATH", "0,1,1,1,0,3");
        
        Metadata frameMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_FRAME", index);
        Metadata viewSaleButtonMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_BUTTON", viewSaleButtonPath);
        Metadata searchFieldMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_TEXTINPUT", searchFieldPath);
        Metadata searchButtonMetadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, frameMetadata, "SWING_BUTTON", searchButtonPath);

        Action viewSaleAction = TestScriptElementFactory.createAction("LEFTCLICK", viewSaleButtonMetadata);
		Action inputAction1 = TestScriptElementFactory.createAction("ENTER", searchFieldMetadata, searchString1);
		Action inputAction2 = TestScriptElementFactory.createAction("ENTER", searchFieldMetadata, searchString2);
        Action searchAction = TestScriptElementFactory.createAction("LEFTCLICK", searchButtonMetadata);
        
        Execution execution1 = TestScriptElementFactory.createExecution(viewSaleAction);
        Execution execution2 = TestScriptElementFactory.createExecution(inputAction1);
        Execution execution3 = TestScriptElementFactory.createExecution(inputAction2);
        Execution execution4 = TestScriptElementFactory.createExecution(searchAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Select ViewSale"),script);
        add(execution1,script);
        add(TestScriptElementFactory.createLogger("ViewSale selected"),script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(execution2,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(execution3,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(execution4,script);
        add(TestScriptElementFactory.createLoop(8, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Done"),script);
    	return script;
    }
    
    public TestScript createStartAppletScript(String appletName) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Start " + appletName);
        
    	StringProperty applicationNameProp = TestScriptElementFactory.createStringProperty("APPLICATION_NAME", appletName);
    	
    	Metadata metadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_APPLET", applicationNameProp);

    	Action startAction = TestScriptElementFactory.createAction("START", metadata);

        Execution execution = TestScriptElementFactory.createExecution(startAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Starting Applet " + appletName),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Applet started"),script);
    	return script;
    }
    
    public TestScript createStartApplicationScript(String applicationName) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Start " + applicationName);

    	StringProperty applicationNameProp = TestScriptElementFactory.createStringProperty("APPLICATION_NAME", applicationName);
        
        Metadata metadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_APPLICATION", applicationNameProp);
        
        Action startAction = TestScriptElementFactory.createAction("START", metadata);

        Execution execution = TestScriptElementFactory.createExecution(startAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Starting Application " + applicationName),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Application started"),script);
    	return script;
    }
    
    public TestScript createStopAppletScript(String appletName) {
        
        TestScript script = TestScriptElementFactory.createTestScript("Stop " + appletName);
    	
        Metadata metadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING,null, "SWING_APPLET");
		
        Action stopAction = TestScriptElementFactory.createAction("STOP", metadata);
        
        Execution execution = TestScriptElementFactory.createExecution(stopAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Stopping Applet " + appletName),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Applet stopped"),script);
    	return script;
    }
    
    public TestScript createStopApplicationScript(String applicationName) {
        
        TestScript script = TestScriptElementFactory.createTestScript("Stop " + applicationName);
    	
        Metadata metadata = TestScriptElementFactory.createMetadata(SubEngineType.SWING, null, "SWING_APPLICATION");
		
        Action stopAction = TestScriptElementFactory.createAction("STOP", metadata);
       
        Execution execution = TestScriptElementFactory.createExecution(stopAction);
        
        // Setup TestScript
        add(TestScriptElementFactory.createLogger("Stopping Application " + applicationName),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Application stopped"),script);
    	return script;
    }
    
}
