/*
 * Copyright 2012 PRODYNA AG
 *
 * Licensed under the Eclipse Public License (EPL), Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/eclipse-1.0.php or
 * http://www.nabucco.org/License.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.nabucco.testautomation.engine.execution.info;

import org.nabucco.testautomation.settings.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.settings.facade.datatype.engine.TestExecutionInfo;

/**
 * 
 * TestExecutionInfoFactory
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestExecutionInfoFactory {

	private static TestExecutionInfoFactory instance;
	
	private TestExecutionInfoFactory() {}
	
	/**
	 * 
	 * @return
	 */
	public static synchronized TestExecutionInfoFactory getInstance() {
		
		if (instance == null) {
			instance = new TestExecutionInfoFactory();
		}
		return instance;
	}
	
	/**
	 * 
	 * @param jobId
	 * @param testStatus
	 * @param startTime
	 * @param stopTime
	 * @return
	 */
	public TestExecutionInfo createTestExecutionInfo(long jobId,
			ExecutionStatusType testStatus, Long startTime, Long stopTime) {
		TestExecutionInfo info = new TestExecutionInfo();
		info.setJobId(jobId);
		info.setTestStatus(testStatus);
		info.setStartTime(startTime);
		info.setStopTime(stopTime);
		return info;
	}
	
}
