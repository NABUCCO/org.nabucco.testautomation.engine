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

import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nabucco.framework.base.facade.datatype.Order;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngineImpl;
import org.nabucco.testautomation.engine.visitor.config.TestConfigurationVisitor;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElementContainer;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.facade.datatype.base.HierarchyLevelType;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.schema.facade.datatype.SchemaConfig;
import org.nabucco.testautomation.schema.facade.datatype.SchemaElement;
import org.nabucco.testautomation.schema.facade.datatype.ScriptContainerType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TextMessage;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.LoggerLevelType;

/**
 * 
 * TestConfigurationTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestConfigurationTest extends TestEngineSupport {

    org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestConfigurationTest.class);
    
    @Before
    public void setup() {
        BasicConfigurator.configure();
    }
    
    @Test
    public void testTestConfiguration() {
        
        TestConfiguration config = new TestConfiguration();
        config.setName("RuntimeTestConfiguration");
        config.setDescription("My TestConfiguration");
        
        
        SchemaConfig schemaConfig = new SchemaConfig();
        schemaConfig.setName("ProdynaConfig");
        schemaConfig.setDescription("Default SchemaConfig for Prodyna");
        
        SchemaElement testSheetSchema = new SchemaElement();
        testSheetSchema.setName("TestSheet");
        testSheetSchema.setLevel(HierarchyLevelType.ONE);
        testSheetSchema.setHasDependencies(Boolean.FALSE);
        testSheetSchema.setPropertyContainer(Boolean.TRUE);
        testSheetSchema.setScriptsAllowed(ScriptContainerType.NONE);
        testSheetSchema.setSkipable(Boolean.FALSE);
        
        SchemaElement testCaseSchema = new SchemaElement();
        testCaseSchema.setName("TestCase");
        testCaseSchema.setLevel(HierarchyLevelType.TWO);
        testCaseSchema.setHasDependencies(Boolean.FALSE);
        testCaseSchema.setPropertyContainer(Boolean.TRUE);
        testCaseSchema.setScriptsAllowed(ScriptContainerType.NONE);
        testCaseSchema.setSkipable(Boolean.FALSE);
        testSheetSchema.getSchemaElementList().add(testCaseSchema);
        
        SchemaElement testStepSchema = new SchemaElement();
        testStepSchema.setName("TestStep");
        testStepSchema.setLevel(HierarchyLevelType.THREE);
        testStepSchema.setHasDependencies(Boolean.TRUE);
        testStepSchema.setPropertyContainer(Boolean.TRUE);
        testStepSchema.setScriptsAllowed(ScriptContainerType.MANY);
        testStepSchema.setSkipable(Boolean.TRUE);
        testCaseSchema.getSchemaElementList().add(testStepSchema);
        
        schemaConfig.getSchemaElementList().add(testSheetSchema);
        config.setSchemaConfig(schemaConfig);
        
        TestConfigElement testSheet = new TestConfigElement();
        testSheet.setSchemaElement(testSheetSchema);
        testSheet.setName("MyTestSheet");
        PropertyList testSheetProperties = new PropertyList();
        Property testSheetProp = createProperty("TestSheetProperty", "My value");
        testSheetProperties.setName("testSheetProperties");
        
        add(testSheetProp, testSheetProperties);
        testSheet.setPropertyList(testSheetProperties);
        
        TestConfigElement testCase = new TestConfigElement();
        testCase.setSchemaElement(testCaseSchema);
        testCase.setName("MyTestCase");
        PropertyList testCaseProperties = new PropertyList();
        Property testCaseProp = createProperty("TestCaseProperty", 4711);
        testCaseProperties.setName("testCaseProperties");
       add(testCaseProp, testCaseProperties);
        testCase.setPropertyList(testCaseProperties);
        
        TestConfigElement testStep1 = new TestConfigElement();
        testStep1.setSchemaElement(testStepSchema);
        testStep1.setName("MyTestStep 1");
        testStep1.setSkip(Boolean.TRUE);
        TestScriptContainer testScriptContainer = new TestScriptContainer();
        testScriptContainer.setTestScript(createTestScript());
        testStep1.getTestScriptList().add(testScriptContainer);
        PropertyList testStepProperties = new PropertyList();
        Property testStepProp = createProperty("TestStepProperty", "My value");
        testStepProperties.setName("testSheetProperties");
        add(testStepProp, testStepProperties);
        testStep1.setPropertyList(testStepProperties);
        
        TestConfigElement testStep2 = new TestConfigElement();
        testStep2.setSchemaElement(testStepSchema);
        testStep2.setName("MyTestStep 2");
        testStep2.setSkip(Boolean.FALSE);
        testStep2.setPropertyList(testStepProperties);
        TestScriptContainer testScriptContainer2 = new TestScriptContainer();
        testScriptContainer2.setTestScript(createTestScript());
        testStep2.getTestScriptList().add(testScriptContainer2);
        
        TestConfigElementContainer c1 = new TestConfigElementContainer();
        TestConfigElementContainer c2 = new TestConfigElementContainer();
        TestConfigElementContainer c3 = new TestConfigElementContainer();
        TestConfigElementContainer c4 = new TestConfigElementContainer();
        c1.setElement(testCase);
        c1.setOrderIndex(new Order(0));
        c2.setElement(testStep1);
        c2.setOrderIndex(new Order(0));
        c3.setElement(testStep2);
        c3.setOrderIndex(new Order(1));
        c4.setElement(testSheet);
        c4.setOrderIndex(new Order(0));
        testSheet.getTestConfigElementList().add(c1);
        testCase.getTestConfigElementList().add(c2);
        testCase.getTestConfigElementList().add(c3);
        config.getTestConfigElementList().add(c4);
        
        try {
            TestConfigurationResult testConfigurationResult = TestResultHelper.createTestConfigurationResult();
            new TestConfigurationVisitor(createContext(), new TestConfigElementEngineImpl()).visit(config, testConfigurationResult);
            System.out.println(testConfigurationResult);
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
        
    }
    
    private Property createProperty(String name, String value) {
        StringProperty prop = new StringProperty();
        prop.setName(name);
        prop.setValue(value);
        return prop;
    }

    private Property createProperty(String name, Integer value) {
        IntegerProperty prop = new IntegerProperty();
        prop.setName(name);
        prop.setValue(value);
        return prop;
    }
    
    protected TestScript createTestScript() {
        TestScript script = super.createTestScript();
        Logger logger = new Logger();
        logger.setLevel(LoggerLevelType.INFO);
        TextMessage message = new TextMessage();
        message.setText("TestMessage");
        add(message, logger);
        add(logger, script);
        return script;
    }
    
}
