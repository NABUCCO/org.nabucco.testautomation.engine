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

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;


/**
 * DBProxyTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class DBProxyTest extends TestEngineSupport {

	@Before
    public void setUp() throws Exception {
    	super.setUp();
    }
	
	@Test
    public void testSelectStatement() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("DBProxyTest");
        TestConfigElement testSheet = createTestSheet("DatabaseTest");
        TestConfigElement testCase = createTestCase("SelectTest");
        TestConfigElement testStep = createTestStep("Simple Select");
        
        add(createSimpleSelectScript(), testStep);
        add(testCase,testSheet);
        add(testStep,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
	
	private TestScript createSimpleSelectScript() {
		
		TestScript script = TestScriptElementFactory.createTestScript("Select Script");
		
		StringProperty query = TestScriptElementFactory.createStringProperty("QUERY", "SELECT * FROM demo.user");
		
		Metadata serverMetadata = TestScriptElementFactory.createMetadata(SubEngineType.DB, null, "SQL_STATEMENT", query);
		
		Action selectAction = TestScriptElementFactory.createAction("SELECT", serverMetadata);
		selectAction.setDelay(1000L);
		
		Execution execution = TestScriptElementFactory.createExecution(selectAction);
		
		add(TestScriptElementFactory.createLogger("Execute Select-Statement"), script);
	    add(execution,script);
	    add(TestScriptElementFactory.createLogger("SQL-Result: ", selectAction.getName().getValue()),script);
		return script;
	}
    
}
