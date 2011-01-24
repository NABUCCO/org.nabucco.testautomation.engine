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
package org.nabucco.testautomation.engine.visitor.config;

import java.util.Date;

import org.nabucco.framework.base.facade.datatype.visitor.VisitorException;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;
import org.nabucco.testautomation.engine.visitor.result.TestResultStatusVisitor;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.exception.engine.TestEngineException;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;

/**
 * TestConfigElementVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestConfigElementVisitor extends AbstractTestConfigElementVisitor<TestResult> {

    private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
            TestConfigElementVisitor.class);
    
    private final TestResultStatusVisitor resultVisitor = new TestResultStatusVisitor();

    protected TestConfigElementVisitor(TestContext context,
            TestConfigElementEngine testConfigElementEngine) {
        super(context, testConfigElementEngine);
    }

    @Override
    public void visit(TestConfiguration testConfiguration, TestResult argument) {
        throw new UnsupportedOperationException(
                "Should not visit a TestConfiguration from within a TestConfigElementVisitor");
    }

    @Override
    public void visit(TestConfigElement testConfigElement, TestResult argument) {
        
    	logger.debug("Visting TestConfigElement '" + testConfigElement.getName().getValue() + "'");
        long startTime = System.currentTimeMillis();
        
        try {
        	// Set Brand in TestResult
        	argument.setBrandType(testConfigElement.getBrandType());
        	
        	// Execute TestConfigElement
            TestResult result = getTestConfigElementEngine().executeTestConfigElement(
                    testConfigElement, getContext(), argument);
            
            // Do not execute children if skipped
            if (result.getStatus() != TestConfigElementStatusType.SKIPPED) {
            	super.visit(testConfigElement, result);
            }
            
            long endTime = System.currentTimeMillis();
            result.setStartTime(new Date(startTime));
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);
            
            // Determine Status of TestResult
            TestResultStatusVisitor visitor = new TestResultStatusVisitor();
            visitor.visit(result);
        } catch (InterruptionException ex) {
        	finish(argument, startTime);
        	argument.setStatus(TestConfigElementStatusType.FAILED);
        	argument.setErrorMessage("Execution of '" + testConfigElement.getName().getValue() + "' aborted");
        	throw ex;
        } catch (TestEngineException ex) {
        	finish(argument, startTime);
        	argument.setErrorMessage(ex.getMessage());
            logger.error(ex.getMessage());
        } catch (VisitorException ex) {
        	finish(argument, startTime);
        	argument.setErrorMessage(ex.getMessage());
            logger.error(ex.getMessage());
		}
    }
    
    public void visit(TestConfigElement testConfigElement, TestConfigurationResult testConfigurationResult) {
        
    	logger.debug("Visting TestConfigElement '" + testConfigElement.getName().getValue() + "'");
    	long startTime = System.currentTimeMillis();
    	
        try {
        	// Execute TestConfigElement
            TestResult result = getTestConfigElementEngine().executeTestConfigElement(
                    testConfigElement, getContext(), null);
            
            // Do not execute children if skipped
            if (result.getStatus() != TestConfigElementStatusType.SKIPPED) {
            	TestResultHelper.addTestResult(result, testConfigurationResult);
            	super.visit(testConfigElement, result);
            }
            
            long endTime = System.currentTimeMillis();
            result.setStartTime(new Date(startTime));
            result.setEndTime(new Date(endTime));
            result.setDuration(endTime - startTime);
            
            // Determine Status of TestResult
            resultVisitor.visit(result);
        } catch (InterruptionException ex) {
        	finish(testConfigurationResult, startTime);
        	throw ex;
        } catch (TestEngineException ex) {
        	finish(testConfigurationResult, startTime);
            logger.error(ex.getMessage());
        } catch (VisitorException ex) {
        	finish(testConfigurationResult, startTime);
            logger.error(ex.getMessage());
		}
    }
    
    private void finish(TestResult argument, long startTime) {
    	
    	long endTime = System.currentTimeMillis();
    	
    	if (!argument.getTestResultList().isEmpty()) {
    		TestResult result = argument.getTestResultList().get(argument.getTestResultList().size() - 1).getResult();
    		result.setEndTime(new Date(endTime));
    		result.setDuration(endTime - startTime);
    	}
    }
    
    private void finish(TestConfigurationResult argument, long startTime) {
    	
    	long endTime = System.currentTimeMillis();
    	
    	if (!argument.getTestResultList().isEmpty()) {
    		TestResult result = argument.getTestResultList().get(argument.getTestResultList().size() - 1).getResult();
    		result.setEndTime(new Date(endTime));
    		result.setDuration(endTime - startTime);
    	}
    }
    
}
