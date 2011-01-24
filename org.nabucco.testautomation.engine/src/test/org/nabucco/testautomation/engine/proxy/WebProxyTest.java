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

import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * 
 * WebProxyTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class WebProxyTest extends TestEngineSupport {

    @Before
    public void setUp() throws Exception {
    	super.setUp();
    }

    @Test
    public void testSimpleGoogleSearch() {

    	TestConfiguration config = getDefaultTestConfiguration("WebProxyTest");
        TestConfigElement testSheet = createTestSheet("WebProxyTest");
        TestConfigElement testCase = createTestCase("GoogleTest");
        TestConfigElement testStep = createTestStep("Google Search");
        
        add(createGoogleSearchScript("Prodyna"),testStep);
        add(testCase,testSheet);
        add(testStep,testCase);
        add(testSheet,config);
        
        execute(config);
    }
    
    public TestScript createGoogleSearchScript(String searchString) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Simple Google Search");
    	
    	StringProperty url = TestScriptElementFactory.createStringProperty("URL", "http://www.google.de");
    	StringProperty input = TestScriptElementFactory.createStringProperty("input", searchString);
    	StringProperty searchFieldPath = TestScriptElementFactory.createStringProperty("XPATH", "html/body/span/center/span[1]/center/form/table/tbody/tr/td[2]/div/input");
    	StringProperty searchButtonPath = TestScriptElementFactory.createStringProperty("XPATH", "html/body/span/center/span[1]/center/form/table/tbody/tr/td[2]/span[1]/span/input");
    	
    	Metadata frameMetadata = TestScriptElementFactory.createMetadata(SubEngineType.WEB, null, "WEB_PAGE", url);
    	Metadata textInputMetadata = TestScriptElementFactory.createMetadata(SubEngineType.WEB, frameMetadata, "WEB_TEXTINPUT", searchFieldPath);
    	Metadata buttonMetadata = TestScriptElementFactory.createMetadata(SubEngineType.WEB, frameMetadata, "WEB_BUTTON", searchButtonPath);
    	
    	Action googleAction = TestScriptElementFactory.createAction("ENTER", frameMetadata, url);
    	Action inputAction = TestScriptElementFactory.createAction("ENTER", textInputMetadata, input);
    	Action buttonAction = TestScriptElementFactory.createAction("LEFTCLICK", buttonMetadata);
    	
    	googleAction.setDelay(2000L);
    	inputAction.setDelay(2000L);
    	buttonAction.setDelay(2000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(googleAction, inputAction, buttonAction);
    	
    	add(TestScriptElementFactory.createLogger("Start failing Action"),script);
    	add(TestScriptElementFactory.createLoop(3, 1000L, null),script);
        add(execution,script);
        add(TestScriptElementFactory.createLoop(5, 1000L, null),script);
        add(TestScriptElementFactory.createLogger("Failing Action finished"),script);
    	return script;
    }
    
}
