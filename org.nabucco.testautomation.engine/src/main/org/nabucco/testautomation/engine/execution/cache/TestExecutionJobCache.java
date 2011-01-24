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
package org.nabucco.testautomation.engine.execution.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nabucco.testautomation.engine.execution.job.TestExecutionJob;


/**
 * 
 * TestExecutionJobCache
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public final class TestExecutionJobCache {

	private final Map<Long, TestExecutionJob> cache = new HashMap<Long, TestExecutionJob>();

	public TestExecutionJobCache() {}
	
	/**
	 * Adds a TestRunnerJob to the cache.
	 * 
	 * @param job the job to be added
	 */
	public synchronized void addTestExecutionJob(TestExecutionJob job) {
		cache.put(job.getId(), job);
	}
	
	/**
	 * Gets a TestRunnerJob from cache identified by the given job id.
	 * 
	 * @param jobId the job id
	 * @return the TestRunnerJob or null, if no job was found
	 */
	public synchronized TestExecutionJob getTestExecutionJob(Long jobId) {
		return cache.get(jobId);
	}
	
	/**
	 * Removes a TestRunnerJob from the cache identified by the given job id.
	 * 
	 * @param jobId the job id
	 * @return the TestRunnerJob or null, if no job was found
	 */
	public synchronized TestExecutionJob removeTestExecutionJob(Long jobId) {
		return cache.remove(jobId);
	}
	
	/**
	 * Removes all Jobs from the Cache.
	 */
	public synchronized void clean() {
		cache.clear();
	}
	
	/**
	 * Returns all currently cached jobIds.
	 * 
	 * @return the jobIds
	 */
	public synchronized Set<Long> getCachedJobs() {
		return cache.keySet();
	}
	
}
