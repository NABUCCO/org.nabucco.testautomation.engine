package org.nabucco.testautomation.engine.base;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.nabucco.framework.base.facade.datatype.DatatypeState;
import org.nabucco.framework.base.facade.datatype.Identifier;
import org.nabucco.testautomation.engine.TestEngine;
import org.nabucco.testautomation.engine.base.context.TestContext;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElementContainer;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.facade.datatype.base.HierarchyLevelType;
import org.nabucco.testautomation.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.facade.datatype.engine.proxy.ConfigurationProperty;
import org.nabucco.testautomation.facade.datatype.engine.proxy.ProxyConfiguration;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyContainer;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;
import org.nabucco.testautomation.schema.facade.datatype.SchemaConfig;
import org.nabucco.testautomation.schema.facade.datatype.SchemaElement;
import org.nabucco.testautomation.schema.facade.datatype.ScriptContainerType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptComposite;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElement;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementContainer;

/**
 * 
 * TestEngineSupport.
 * 
 * Base class for all TestEngine-RuntimeTests
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestEngineSupport {
	
	private TestEngine engine;

    private TestContext context = new TestContext();
    
    protected TestEngine getEngine() {
		return engine;
	}
    
    protected TestContext getContext() {
		return context;
	}
    
	protected void setUp() throws Exception {
        engine = (TestEngine) LocateRegistry.getRegistry(1099).lookup("TestEngine");

        Properties props = loadProperties("conf/test/db.properties");
        ProxyConfiguration config = createProxyConfiguration(SubEngineType.DB, props);
        context.addProxyConfiguration(config);
        
        props = loadProperties("conf/test/process.properties");
        config = createProxyConfiguration(SubEngineType.PROCESS, props);
        context.addProxyConfiguration(config);

        props = loadProperties("conf/test/swing.properties");
        config = createProxyConfiguration(SubEngineType.SWING, props);
        context.addProxyConfiguration(config);

        props = loadProperties("conf/test/web.properties");
        config = createProxyConfiguration(SubEngineType.WEB, props);
        context.addProxyConfiguration(config);

        props = loadProperties("conf/test/ws.properties");
        config = createProxyConfiguration(SubEngineType.WS, props);
        context.addProxyConfiguration(config);
    }
	
	private Properties loadProperties(String file) throws IOException { 
    	Properties props = new Properties();
        props.load(new FileReader(file));
        return props;
    }

    private ProxyConfiguration createProxyConfiguration(SubEngineType type, Properties props) {
    	ProxyConfiguration config = new ProxyConfiguration();
    	config.setSubEngineType(type);
    	
    	for (Object key : props.keySet()) {
        	ConfigurationProperty configProp = new ConfigurationProperty();
			config.getConfigurationProperties().add(configProp);
        	configProp.setName((String) key);
        	configProp.setValue(props.getProperty((String) key));
        }
        return config;
    }
	
	private SchemaElement testSheetSchema;
	
	private SchemaElement testCaseSchema;
	
	private SchemaElement testStepSchema;
	
	protected SchemaElement getTestSheetSchema() {
		
		if (testSheetSchema == null) {
			testSheetSchema = new SchemaElement();
	        testSheetSchema.setName("TestSheet");
	        testSheetSchema.setLevel(HierarchyLevelType.ONE);
	        testSheetSchema.setHasDependencies(Boolean.FALSE);
	        testSheetSchema.setPropertyContainer(Boolean.TRUE);
	        testSheetSchema.setScriptsAllowed(ScriptContainerType.NONE);
	        testSheetSchema.setSkipable(Boolean.FALSE);
	        testSheetSchema.setDefaultDependency(Boolean.FALSE);
		}
		return testSheetSchema;
	}
	
	protected SchemaElement getTestCaseSchema() {
		
		if (testCaseSchema == null) {
			testCaseSchema = new SchemaElement();
	        testCaseSchema.setName("TestCase");
	        testCaseSchema.setLevel(HierarchyLevelType.TWO);
	        testCaseSchema.setHasDependencies(Boolean.FALSE);
	        testCaseSchema.setPropertyContainer(Boolean.TRUE);
	        testCaseSchema.setScriptsAllowed(ScriptContainerType.NONE);
	        testCaseSchema.setSkipable(Boolean.FALSE);
	        testCaseSchema.setDefaultDependency(Boolean.FALSE);
		}
		return testCaseSchema;
	}

	protected SchemaElement getTestStepSchema() {
		
		if (testStepSchema == null) {
			testStepSchema = new SchemaElement();
	        testStepSchema.setName("TestStep");
	        testStepSchema.setLevel(HierarchyLevelType.THREE);
	        testStepSchema.setHasDependencies(Boolean.TRUE);
	        testStepSchema.setPropertyContainer(Boolean.TRUE);
	        testStepSchema.setScriptsAllowed(ScriptContainerType.MANY);
	        testStepSchema.setSkipable(Boolean.TRUE);
	        testStepSchema.setDefaultDependency(Boolean.FALSE);
		}
		return testStepSchema;
	}

	protected TestConfiguration getDefaultTestConfiguration(String name) {
    	TestConfiguration config = new TestConfiguration();
        config.setName(name);

        SchemaConfig schemaConfig = new SchemaConfig();
        schemaConfig.setName("ProdynaConfig");
        schemaConfig.setDescription("Default SchemaConfig for Prodyna");

        SchemaElement testSheetSchema = getTestSheetSchema();
        SchemaElement testCaseSchema = getTestCaseSchema();
        testSheetSchema.getSchemaElementList().add(testCaseSchema);

        SchemaElement testStepSchema = getTestStepSchema();
        testCaseSchema.getSchemaElementList().add(testStepSchema);

        schemaConfig.getSchemaElementList().add(testSheetSchema);
        config.setSchemaConfig(schemaConfig);
        return config;
    }
	
	protected void addTestScript(TestConfigElement element, TestScript script) {
		
		TestScriptContainer container = new TestScriptContainer();
		container.setTestScript(script);
		element.getTestScriptList().add(container);
	}
	
	protected String printMap(Map<HierarchyLevelType, TestConfigElement> map) {
        StringBuilder sb = new StringBuilder();
        
        for (HierarchyLevelType level : map.keySet()) {
            TestConfigElement testConfigElement = map.get(level);
            sb.append(level);
            sb.append(": (");
            sb.append(testConfigElement.getSchemaElement().getName().getValue());
            sb.append(") ");
            sb.append(testConfigElement.getName().getValue());
            sb.append(" [");
            sb.append(testConfigElement.getId());
            sb.append("]; ");
        }
        return sb.toString();
    }

	protected TestContext createContext() {
        StringProperty user = new StringProperty();
        user.setName(TestContext.USERNAME);
        user.setValue("sschmidt");
        StringProperty testProp = new StringProperty();
        testProp.setName("TEST_PROP");
        testProp.setValue("12345");
        context.put(user);
        context.put(testProp);
        return context;
    }
	
	protected TestScript createTestScript() {
		TestScript script = new TestScript();
		script.setId(4711L);
		script.setName("TestScriptName");
		script.setTestScriptKey("TSC-1");
		return script;
	}
	
	private static long id = 1L;
	
	protected TestConfigElement createTestSheet(String name) {
		TestConfigElement testSheet = new TestConfigElement();
        testSheet.setSchemaElement(getTestSheetSchema());
        testSheet.setName(name);
        testSheet.setId(id++);
        return testSheet;
	}
	
	protected TestConfigElement createTestCase(String name) {
		TestConfigElement testCase = new TestConfigElement();
        testCase.setSchemaElement(getTestCaseSchema());
        testCase.setName(name);
        testCase.setId(id++);
        return testCase;
	}
	
	protected TestConfigElement createTestStep(String name) {
		TestConfigElement testStep = new TestConfigElement();
        testStep.setSchemaElement(getTestStepSchema());
        testStep.setName(name);
        testStep.setId(id++);
        testStep.setSkip(Boolean.FALSE);
        return testStep;
	}

    protected TestConfigurationResult execute(TestConfiguration config) {
    	try {
            TestExecutionInfo info = getEngine().executeTestConfiguration(config, createContext());
            Assert.assertNotNull(info);

            while (info.getTestStatus() != ExecutionStatusType.FINISHED) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                info = getEngine().getTestStatus(info);
                // TODO Map<HierarchyLevelType, TestConfigElement> currentTestConfigElementMap = info.getCurrentTestConfigElementMap();
                Identifier currentTestScriptId = info.getCurrentTestScriptId();
                Identifier currentTestScriptElementId = info.getCurrentTestScriptElementId();
                
//                if (currentTestConfigElementMap != null) {
//                    System.out.println("CurrentTestConfigElement: " + printMap(currentTestConfigElementMap));
//                }
                if (currentTestScriptId != null) {
                    System.out.println("CurrentTestScript: " + currentTestScriptId.getValue());
                }
                if (currentTestScriptElementId != null) {
                    System.out.println("CurrentTestScriptElement: " + currentTestScriptElementId.getValue());
                }
            }
            TestConfigurationResult result = getEngine().getTestConfigurationResult(info);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
            System.out.println(TestResultPrinter.toString(result));
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
        return null;
    }
    
    protected void add(TestConfigElement element, TestConfigElement container) {
    	TestConfigElementContainer wrapper = new TestConfigElementContainer();
    	wrapper.setElement(element);
    	wrapper.setOrderIndex(container.getTestConfigElementList().size());
    	container.getTestConfigElementList().add(wrapper);
    }
    
    protected void add(TestScriptElement element, TestScriptComposite container) {
    	TestScriptElementContainer wrapper = new TestScriptElementContainer();
    	wrapper.setElement(element);
    	wrapper.setOrderIndex(container.getTestScriptElementList().size());
    	container.getTestScriptElementList().add(wrapper);
    }
    
    protected void add(TestConfigElement element, TestConfiguration config) {
    	TestConfigElementContainer wrapper = new TestConfigElementContainer();
    	wrapper.setElement(element);
    	wrapper.setOrderIndex(config.getTestConfigElementList().size());
    	config.getTestConfigElementList().add(wrapper);
    }
    
    protected void add(TestScript script, TestConfigElement container) {
    	TestScriptContainer wrapper = new TestScriptContainer();
    	wrapper.setTestScript(script);
    	wrapper.setOrderIndex(container.getTestConfigElementList().size());
    	container.getTestScriptList().add(wrapper);
    }
    
    protected void add(Property property, PropertyList list) {
    	PropertyContainer container = new PropertyContainer();
    	container.setDatatypeState(DatatypeState.INITIALIZED);
    	container.setOrderIndex(list.getPropertyList().size());
    	container.setProperty(property);
    	list.getPropertyList().add(container);
    }
	
}
