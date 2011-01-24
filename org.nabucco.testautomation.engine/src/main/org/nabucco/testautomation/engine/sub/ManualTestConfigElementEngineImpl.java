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

import java.util.Date;
import java.util.List;

import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.client.ManualTestResultInput;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.facade.exception.engine.TestEngineException;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestResultContainer;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualState;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualTestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;

/**
 * ManualTestConfigElementEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ManualTestConfigElementEngineImpl implements
		TestConfigElementEngine {

	private static final long serialVersionUID = 1L;
	
	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(ManualTestConfigElementEngineImpl.class);
	
	/**
     * Constructs a new TestConfigElementEngine instance.
     */
    public ManualTestConfigElementEngineImpl() {
    }
	
	@Override
	public TestResult executeTestConfigElement(
			TestConfigElement testConfigElement, TestContext context,
			TestResult parentResult) throws TestEngineException {
		
		List<TestResultContainer> parentTestResultList = parentResult.getTestResultList();
		
		if (parentTestResultList.isEmpty()) {
			throw new TestEngineException("TestResultList is empty");
		}
		
		// Create ManualResult
		TestResult orgResult = parentTestResultList.remove(parentTestResultList.size() - 1).getResult();
		ManualTestResult manualResult = TestResultHelper.createManualTestResult(orgResult);
		TestResultHelper.addTestResult(manualResult, parentResult);

		// wait for user input
		logger.info("Waiting for ManualTestResult ...");
		long start = System.currentTimeMillis();
		ClientInteraction userInput = context.getExecutionController().receiveClientInteraction();
		long end = System.currentTimeMillis();
		
		if (userInput != null && userInput instanceof ManualTestResultInput) { 
			
			// Complete ManualTestResult
			ManualTestResult userResult = ((ManualTestResultInput) userInput).getResult();
			
			if (userResult == null) {
				String errorMessage = "No ManualTestResult received";
				manualResult.setStatus(TestConfigElementStatusType.FAILED);
				manualResult.setUserErrorMessage("No UserInput received");
				logger.error(errorMessage);
				return manualResult;
			} else if (userResult.getState() == ManualState.FINISHED) {		
				logger.info("Input for ManualTestResult received");
			} else if (userResult.getState() == ManualState.ABORTED) {
				userResult.setStatus(TestConfigElementStatusType.FAILED);
				userResult.setUserErrorMessage("'" + testConfigElement.getElementKey().getValue() + "' aborted by user");
				context.getTestConfigurationResult().setStatus(TestConfigurationStatusType.CANCELLED);
				logger.info("Input of ManualTestResult aborted");
			} else {
				userResult.setStatus(TestConfigElementStatusType.FAILED);
				String errorMessage = "Invalid state of ManualTestResult: " + userResult.getState();
				userResult.setErrorMessage(errorMessage);
				logger.error(errorMessage);
			}

			userResult.setStartTime(new Date(start));
			userResult.setEndTime(new Date(end));
			userResult.setDuration(end - start);
			TestResultHelper.removeTestResult(manualResult, parentResult);
			TestResultHelper.addTestResult(userResult, parentResult);
		} else {
			manualResult.setStatus(TestConfigElementStatusType.FAILED);
			manualResult.setUserErrorMessage("No UserInput received");
			logger.error("No UserInput received");
		}
		return manualResult;
	}

}
