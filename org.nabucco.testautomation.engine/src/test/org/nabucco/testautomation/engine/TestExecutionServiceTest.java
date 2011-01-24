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

import junit.framework.Assert;

import org.junit.Test;
import org.nabucco.testautomation.engine.exception.JobRejectionException;
import org.nabucco.testautomation.engine.execution.TestExecutionService;
import org.nabucco.testautomation.engine.execution.TestExecutionServiceFactory;
import org.nabucco.testautomation.engine.execution.job.TestConfigurationExecutionJob;

import org.nabucco.testautomation.facade.datatype.engine.ExecutionStatusType;


/**
 * TestExecutionServiceTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestExecutionServiceTest {

	@Test
	public void simpleThreadTest() throws Exception {
		TestExecutionService service = TestExecutionServiceFactory.getInstance().createTestExecutionService();
		long jobId = service.startExecution(new DummyTestExecutionJob(1000L));
		System.out.println("JobId=" + jobId);
		while(service.getJobStatus(jobId) != ExecutionStatusType.FINISHED) {
			Thread.sleep(500);
		}
		System.out.println("JobId=" + jobId + " finished");
	}
	
	@Test
	public void multiThreadTest() throws Exception {
		TestExecutionService service = TestExecutionServiceFactory.getInstance().createTestExecutionService();
		long jobId1 = 0;
		try {
			jobId1 = service.startExecution(new DummyTestExecutionJob(1000L));
		} catch (JobRejectionException e1) {
			Assert.fail();
		}
		try {
			service.startExecution(new DummyTestExecutionJob(1000L));
		} catch (Exception e1) {
			System.out.println(e1);
			Assert.assertTrue(e1 instanceof JobRejectionException);
		}
		
		System.out.println("JobId1=" + jobId1);
		boolean keepWaiting = true;
		
		while(keepWaiting) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				System.out.println("Main-Thread interrupted");
			}
			keepWaiting = !(service.getJobStatus(jobId1) == ExecutionStatusType.FINISHED);
		}
		service.getTestConfigurationResult(jobId1);
		System.out.println("JobId1=" + jobId1 + " finished");
		long jobId3 = 0;
		try {
			jobId3 = service.startExecution(new DummyTestExecutionJob(1000L));
		} catch (JobRejectionException e1) {
			Assert.fail();
		}
		try {
			service.startExecution(new DummyTestExecutionJob(1000L));
		} catch (Exception e1) {
			System.out.println(e1);
			Assert.assertTrue(e1 instanceof JobRejectionException);
		}	
		keepWaiting = true;
		while(keepWaiting) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				System.out.println("Main-Thread interrupted");
			}
			keepWaiting = !(service.getJobStatus(jobId3) == ExecutionStatusType.FINISHED);
		}
		service.getTestConfigurationResult(jobId3);
		System.out.println("jobId3=" + jobId3 + " finished");
	}
	
	@Test
	public void stopJobTest() throws Exception {
		TestExecutionService service = TestExecutionServiceFactory.getInstance().createTestExecutionService();
		long jobId = service.startExecution(new DummyTestExecutionJob(1000L));
		System.out.println("JobId=" + jobId);
		
		Thread.sleep(3000);
		service.stopExecution(jobId);
		System.out.println("JobId=" + jobId + " stopped");
		while(service.getJobStatus(jobId) != ExecutionStatusType.FINISHED && service.getJobStatus(jobId) != ExecutionStatusType.INTERRUPTED) {
			Thread.sleep(500);
		}
		System.out.println("JobId=" + jobId + " finished/interrupted");
		jobId = service.startExecution(new DummyTestExecutionJob(1000L));
		System.out.println("JobId=" + jobId);
		while(service.getJobStatus(jobId) != ExecutionStatusType.FINISHED) {
			Thread.sleep(500);
		}
		System.out.println("JobId=" + jobId + " finished");
	}
	
	@Test
	public void pauseJobTest() throws Exception {
		TestExecutionService service = TestExecutionServiceFactory.getInstance().createTestExecutionService();
		long jobId = service.startExecution(new DummyTestExecutionJob(1000L));
		System.out.println("JobId=" + jobId);
		
		Thread.sleep(3000);
		service.pauseExecution(jobId);
		System.out.println("JobId=" + jobId + " paused");
		Thread.sleep(6000);
		service.resumeExecution(jobId);
		System.out.println("JobId=" + jobId + " resumed");
		
		while(service.getJobStatus(jobId) != ExecutionStatusType.FINISHED) {
			Thread.sleep(500);
		}
		System.out.println("JobId=" + jobId + " finished");
	}

}

class DummyTestExecutionJob extends TestConfigurationExecutionJob {

	private long sleeping;
	
	public DummyTestExecutionJob(long sleeping) {
		this.sleeping = sleeping;
	}
	
	@Override
	protected void execute() {
		for (int i = 0; i < 10; i++) {
			if (isInterrupted()) {
				tryInterruption();
			}
			if (isPaused()) {
				tryPause();
			}
			System.out.println("[ID:" + getId() + "] Execute " + i);
			try {
				Thread.sleep(sleeping);
			} catch (InterruptedException e) {
				System.out.println("[ID:" + getId() + "] Sleeping interrupted");
			}
		}
	}

	@Override
	protected void finalizeExecution() {
		super.finalizeExecution();
		System.out.println("[ID:" + getId() + "] finalizeExecution() called");		
	}

	@Override
	protected void prepareExecution() {
		super.prepareExecution();
		System.out.println("[ID:" + getId() + "] prepareExecution() called");
		
	}
	
}
