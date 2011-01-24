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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.exception.ExecutionServiceException;
import org.nabucco.testautomation.engine.exception.JobNotFoundException;
import org.nabucco.testautomation.engine.exception.JobRejectionException;
import org.nabucco.testautomation.engine.execution.cache.TestConfigurationResultCache;
import org.nabucco.testautomation.engine.execution.cache.TestExecutionJobCache;
import org.nabucco.testautomation.engine.execution.job.JobIdFactory;
import org.nabucco.testautomation.engine.execution.job.TestConfigurationExecutionJob;
import org.nabucco.testautomation.engine.execution.job.TestExecutionJob;

import org.nabucco.testautomation.facade.datatype.engine.ExecutionStatusType;
import org.nabucco.testautomation.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;

/**
 * TestExecutionServiceImpl
 *
 * @author Steffen Schmidt, PRODYNA AG
 */
public final class TestExecutionServiceImpl implements TestExecutionService, TestExecutionListener {

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(TestExecutionServiceImpl.class);
	
	private final Lock lock = new ReentrantLock();
	
	private final TestExecutionJobCache jobCache;
	
	private final TestConfigurationResultCache resultCache;
	
	private ExecutorService executor;

	private boolean busy = false;
	
	private final int threadPoolSize;
	
	TestExecutionServiceImpl(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		initExecutorService();
		jobCache = new TestExecutionJobCache();
		resultCache = new TestConfigurationResultCache();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public long startExecution(TestExecutionJob job) throws ExecutionServiceException {
		lock.lock();
		try {
			if (busy) {
				throw new JobRejectionException();
			}			
			
			busy = true;
			final long jobId = JobIdFactory.getInstance().createNewId();
			job.setId(jobId);
			job.addTestExecutionListener(this);
			jobCache.clean();
			jobCache.addTestExecutionJob(job);
			resultCache.clean();
			
			try {
				executor.execute(job);
			} catch (RejectedExecutionException ex) {
				job.rejected();
				jobCache.removeTestExecutionJob(jobId);
				String msg = "Job rejected: TestExecutionService is busy";
				logger.warning(msg);
				throw new JobRejectionException(msg, ex);
			}
			return jobId;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stopExecution(long jobId) throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			job.interrupt();
			executor.shutdownNow();
			initExecutorService();
		} finally {
			busy = false;
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void pauseExecution(long jobId) throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			job.pause();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resumeExecution(long jobId) throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			job.resume();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionStatusType getJobStatus(long jobId)
			throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			return job.getStatus();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestConfigurationResult getTestConfigurationResult(long jobId)
			throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			
			if (job instanceof TestConfigurationExecutionJob) {
				TestConfigurationResult result = resultCache.getTestConfigurationResult(jobId); 
				
				if (result == null) {
					result = ((TestConfigurationExecutionJob) job).getTestConfigurationResult();
				}
				
				if (result == null) {
					throw new ExecutionServiceException("No TestConfigurationResult found for JobId " + jobId);
				} else {
					return result;
				}
			} else {
				throw new ExecutionServiceException("Invalid type of job requested: " + job.getClass().getName());
			}
		} finally {			
			lock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TestExecutionInfo getTestExecutionInfo(long jobId)
			throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			return job.getTestExecutionInfo();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Long> getRunningJobs() throws ExecutionServiceException {
		lock.lock();
		try {
			return jobCache.getCachedJobs();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setClientInteraction(long jobId, ClientInteraction clientInteraction)
			throws ExecutionServiceException {
		lock.lock();
		try {
			TestExecutionJob job = getJob(jobId);
			
			if (job instanceof TestConfigurationExecutionJob) {
				((TestConfigurationExecutionJob) job).setClientInteraction(clientInteraction);
			} else {
				throw new ExecutionServiceException("Invalid type of job requested: " + job.getClass().getName());
			}
		} finally {			
			lock.unlock();
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void testExecutionFinished(TestExecutionEvent e) {
		lock.lock();
		try {
			TestExecutionJob job = getJob(e.getJobId());
			
			// Put TestResult into Cache
			if (job instanceof TestConfigurationExecutionJob) {
				TestConfigurationResult result = ((TestConfigurationExecutionJob) job).getTestConfigurationResult();
				resultCache.addTestConfigurationResult(job.getId(), result);
			}
		} catch (JobNotFoundException ex) {
			logger.warning(ex);
		} finally {			
			busy = false;
			lock.unlock();
		}
	}

	/**
	 * 
	 */
	private void initExecutorService() {
		
		if (threadPoolSize <= 1) {
			this.executor = Executors.newSingleThreadExecutor();
		} else {
			this.executor = Executors.newFixedThreadPool(threadPoolSize);
		}
	}
	
	/**
	 * 
	 * @param jobId
	 * @return
	 * @throws JobNotFoundException
	 */
	private TestExecutionJob getJob(long jobId) throws JobNotFoundException {
		TestExecutionJob job = jobCache.getTestExecutionJob(jobId);
		
		if (job == null) {
			throw new JobNotFoundException("Job with Id '" + jobId + " not found");
		}
		return job;
	}

}
