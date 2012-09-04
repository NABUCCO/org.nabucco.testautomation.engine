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
package org.nabucco.testautomation.engine.execution.cache;

import java.util.HashMap;
import java.util.Map;

import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;

/**
 * 
 * TestResultCache
 *
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class TestConfigurationResultCache {
	
	private final Map<Long, TestConfigurationResult> cache = new HashMap<Long, TestConfigurationResult>();

	public TestConfigurationResultCache() {}
	
	/**
	 * Adds a TestResult to the cache.
	 * 
	 * @param result the TestResult to be added
	 */
	public synchronized void addTestConfigurationResult(Long resultId, TestConfigurationResult result) {
		cache.put(resultId, result);
	}
	
	/**
	 * Gets a TestResult from cache identified by the given job id.
	 * 
	 * @param resultId the result id
	 * @return the TestResult or null, if no job was found
	 */
	public synchronized TestConfigurationResult getTestConfigurationResult(Long resultId) {
		return cache.get(resultId);
	}
	
	/**
	 * Removes a TestResult from the cache identified by the given job id.
	 * 
	 * @param resultId the result id
	 * @return the TestResult or null, if no job was found
	 */
	public synchronized TestConfigurationResult removeTestConfigurationResult(Long resultId) {
		return cache.remove(resultId);
	}
	
	/**
	 * Removes all TestConfigurationResults from the Cache.
	 */
	public synchronized void clean() {
		cache.clear();
	}
	
}
