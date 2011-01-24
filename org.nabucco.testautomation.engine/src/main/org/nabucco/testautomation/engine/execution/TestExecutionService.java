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
package org.nabucco.testautomation.engine.execution;

import java.util.Set;

import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.exception.ExecutionServiceException;
import org.nabucco.testautomation.engine.execution.job.TestExecutionJob;

import org.nabucco.testautomation.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;

/**
 * TestExecutionService
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public interface TestExecutionService {

	/**
	 * 
	 * @param job
	 * @return
	 * @throws ExecutionServiceException
	 */
	public long startExecution(TestExecutionJob job) throws ExecutionServiceException;
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws ExecutionServiceException
	 */
	public ExecutionStatusType getJobStatus(long jobId) throws ExecutionServiceException;
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws ExecutionServiceException
	 */
	public TestExecutionInfo getTestExecutionInfo(long jobId) throws ExecutionServiceException;
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws ExecutionServiceException
	 */
	public TestConfigurationResult getTestConfigurationResult(long jobId) throws ExecutionServiceException;

	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws ExecutionServiceException
	 */
	public void setClientInteraction(long jobId, ClientInteraction clientInteraction) throws ExecutionServiceException;
	
	/**
	 * 
	 * @param jobId
	 * @throws ExecutionServiceException
	 */
	public void stopExecution(long jobId) throws ExecutionServiceException;
	
	/**
	 * 
	 * @param jobId
	 * @throws ExecutionServiceException
	 */
	public void pauseExecution(long jobId) throws ExecutionServiceException;

	/**
	 * 
	 * @param jobId
	 * @throws ExecutionServiceException
	 */
	public void resumeExecution(long jobId) throws ExecutionServiceException;
	
	/**
	 * 
	 * @return
	 * @throws ExecutionServiceException
	 */
	public Set<Long> getRunningJobs() throws ExecutionServiceException;
	
}
