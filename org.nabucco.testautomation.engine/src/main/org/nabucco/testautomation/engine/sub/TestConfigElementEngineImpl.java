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
package org.nabucco.testautomation.engine.sub;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.config.facade.datatype.Dependency;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.exception.engine.TestEngineException;
import org.nabucco.testautomation.result.facade.datatype.ExecutionType;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestResultContainer;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.schema.facade.datatype.SchemaElement;
import org.nabucco.testautomation.schema.facade.datatype.ScriptContainerType;

/**
 * 
 * TestConfigElementEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestConfigElementEngineImpl implements TestConfigElementEngine {

	private static final long serialVersionUID = 1L;
	
	private static final String RESULT = "-Result";

    private static final NBCTestLogger logger = NBCTestLoggingFactory
            .getInstance().getLogger(TestConfigElementEngineImpl.class);
    
    private final TestConfigElementEngine manualEngine = new ManualTestConfigElementEngineImpl();
    
    private final TestScriptEngine testScriptEngine = new TestScriptEngineImpl();

    /**
     * Constructs a new TestConfigElementEngine instance.
     */
    public TestConfigElementEngineImpl() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult executeTestConfigElement(TestConfigElement testConfigElement, TestContext context,
            TestResult parentResult) throws TestEngineException {
        
        SchemaElement schema = testConfigElement.getSchemaElement();
        
        // A TestConfigElement must be defined by a SchemaElement
        if (schema == null) {
            String error = "No SchemaElement defined for TestConfigElement '"
                + testConfigElement.getIdentificationKey().getValue()
                + "'";
            parentResult.setErrorMessage(error);
            throw new TestEngineException(error);
        }

        // Put current Brand into Context
		if (testConfigElement.getBrandType() != null
				&& testConfigElement.getBrandType().getName() != null) {
			StringProperty brand = new StringProperty();
			brand.setName(TestContext.BRAND);
			brand.setValue(testConfigElement.getBrandType().getName()
					.getValue());
			context.put(brand);
		}
		
		TestResult result = TestResultHelper.createTestResult(schema);
        result.setTestConfigElementId(testConfigElement.getId());
        result.setTestConfigElementName(testConfigElement.getName());
        result.setTestConfigElementKey(testConfigElement.getIdentificationKey());
        result.setName(testConfigElement.getName().getValue() + RESULT);
        result.setLevel(schema.getLevel());
        result.setSchemaElementId(schema.getId());
        result.setBrandType(testConfigElement.getBrandType());
        
        // Check skipping
        if (schema.getSkipable() != null && schema.getSkipable().getValue()) {
           
        	if (testConfigElement.getSkip() != null && testConfigElement.getSkip().getValue()) {
                skip(testConfigElement, result);
                logger.info("TestConfigElement '" + testConfigElement.getIdentificationKey().getValue() + "' skipped");
                return result;
            }
        }
        
        // Check dependencies
        if (parentResult != null && schema.getHasDependencies() != null && schema.getHasDependencies().getValue()) {
            
        	if (schema.getDefaultDependency() == null || schema.getDefaultDependency().getValue().booleanValue()) {
        		defaultDependencyCheck(testConfigElement, parentResult.getTestResultList(), result);
        	} else {
                customDependencyCheck(testConfigElement, parentResult.getTestResultList(), result);
            }
        	
        	if (testConfigElement.getSkip() != null && testConfigElement.getSkip().getValue()) {
        		skip(testConfigElement, result);
        		// reset skip-attribute
        		testConfigElement.setSkip(Boolean.FALSE);
        		TestResultHelper.addTestResult(result, parentResult);
        		return result;
        	}
        }

        // Put Properties into Context
        if (schema.getPropertyContainer() != null && schema.getPropertyContainer().getValue()) {
            
        	if (testConfigElement.getPropertyList() != null) {
                context.put(testConfigElement.getPropertyList());
            }
        }
        
        // Add new result to parent result 
        TestResultHelper.addTestResult(result, parentResult);

        // Manual execution
		if (isManual(testConfigElement)) {
			result = this.manualEngine.executeTestConfigElement(testConfigElement, context, parentResult);
		}
		// Check and execute TestScripts
		else if (schema.getScriptsAllowed() != null && (schema.getScriptsAllowed() == ScriptContainerType.ONE 
        		|| schema.getScriptsAllowed() == ScriptContainerType.MANY)) {

            if (!testConfigElement.getTestScriptList().isEmpty()) {
	            this.testScriptEngine.executeTestScriptList(testConfigElement.getTestScriptList(), context,
	                    result, testConfigElement);
            }
        }
        return result;
    }
    
    /**
     * Checks, if all TestConfigElements succeeded a given TestConfigElement depends on.
     * If at least one of these TestConfigElements failed, the given TestConfigElement will
     * be marked to be skipped.
     * 
     * @param testConfigElement the TestConfigElement to check
     * @param parentResult the TestResult to get the depending TestResult from
     */
    private void customDependencyCheck(TestConfigElement testConfigElement, List<TestResultContainer> precedingResults,
            TestResult result) {

    	Map<Long, TestConfigElementStatusType> resultMap = new HashMap<Long, TestConfigElementStatusType>();
    	
    	for (TestResultContainer container : precedingResults) {
    		TestResult precedingResult = container.getResult();
    		
    		if (precedingResult.getTestConfigElementId() != null) {
    			resultMap.put(precedingResult.getTestConfigElementId().getValue(), precedingResult.getStatus());
    		}
    	}
    	
        for (Dependency dependencyContainer : testConfigElement.getDependencyList()) {

        	TestConfigElement dependency = dependencyContainer.getElement();
        	TestConfigElementStatusType status = resultMap.get(dependency.getId());
        	
        	if (status == null) {
        		logger.error("No Status found for TestConfigElement '" + dependency.getIdentificationKey().getValue() + "'");
        	}
        	
            if (status != TestConfigElementStatusType.PASSED) {
                    testConfigElement.setSkip(Boolean.TRUE);
                    result.setStatus(TestConfigElementStatusType.SKIPPED);
                    String message = "Dependency-check failed: '"
                            + testConfigElement.getIdentificationKey().getValue()
                            + "' SKIPPED because '"
                            + dependency.getIdentificationKey().getValue()
                            + "' " + status;
                    result.setMessage(message);
                    logger.info(message);
                    return;
            }
        }
    }
    
    /**
     * The default dependency-check checks, if the preceding TestConfigElements succeeded.
     * If the preceding TestConfigElement failed, the given TestConfigElement will
     * be marked to be skipped.
     * 
     * @param testConfigElement the TestConfigElement to check
     * @param precedingResult the preceding TestResults
     */
    private void defaultDependencyCheck(TestConfigElement testConfigElement, List<TestResultContainer> precedingResults,
            TestResult result) {

        if (precedingResults == null || precedingResults.isEmpty()) {
        	return;
        }
    	
        TestResult precedingResult = precedingResults.get(precedingResults.size() - 1).getResult();
        TestConfigElementStatusType status = precedingResult.getStatus();

		if (status != TestConfigElementStatusType.PASSED) {
            testConfigElement.setSkip(Boolean.TRUE);
            result.setStatus(TestConfigElementStatusType.SKIPPED);
            String message = "Dependency-check failed: '"
                    + testConfigElement.getIdentificationKey().getValue()
                    + "' SKIPPED because '"
                    + precedingResult.getTestConfigElementKey().getValue()
                    + "' " + status;
            result.setMessage(message);
            logger.info(message);
            return;
        }
    }
    
    /**
     * Skips the given TestConfigElement and adds a TestResult to the 
     * given TestResult.
     * 
     * @param testConfigElement the TestConfigElement to be skipped
     * @param result the TestResult
     */
    private void skip(TestConfigElement testConfigElement, TestResult result) {
        result.setStatus(TestConfigElementStatusType.SKIPPED);
    }
    
    /**
     * Checks whether the given {@link TestConfigElement} must be executed by
     * a ManualTestEngine or not.
     *  
     * @param testConfigElement the Element to check
     * @return true if manual execution is required, otherwise false
     */
    private boolean isManual(TestConfigElement testConfigElement) {
		return testConfigElement.getExecutionType() == ExecutionType.MANUAL
				&& testConfigElement.getTestConfigElementList().isEmpty();
	}
    
}
