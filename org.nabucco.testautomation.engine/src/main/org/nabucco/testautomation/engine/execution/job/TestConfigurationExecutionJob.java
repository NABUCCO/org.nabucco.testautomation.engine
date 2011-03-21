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
package org.nabucco.testautomation.engine.execution.job;

import java.util.Collection;
import java.util.Date;

import org.nabucco.framework.base.facade.datatype.visitor.VisitorException;
import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.proxy.ProxyEngine;
import org.nabucco.testautomation.engine.proxy.exception.ProxyConfigurationException;
import org.nabucco.testautomation.engine.proxy.pool.ProxyEnginePool;
import org.nabucco.testautomation.engine.proxy.pool.ProxyPoolFactory;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngineImpl;
import org.nabucco.testautomation.engine.visitor.config.TestConfigurationVisitor;
import org.nabucco.testautomation.engine.visitor.result.TestResultFinalizationVisitor;

import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.datatype.engine.TestExecutionInfo;
import org.nabucco.testautomation.facade.datatype.engine.proxy.ProxyConfiguration;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;

/**
 * TestConfigurationExecutionJob
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class TestConfigurationExecutionJob extends TestExecutionJob {

	/**
	 * 
	 */
	private static final String RESULT_SUFFIX = "-Result";

	private static final long serialVersionUID = 1L;

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(TestConfigurationExecutionJob.class);

	private TestConfiguration testConfiguration;

	private TestContext context;

	private TestConfigurationResult testConfigurationResult;
	
	private ClientInteraction clientInteraction;

	/**
	 * Constructs a new instance of a TestSheetExecutionJob.
	 */
	public TestConfigurationExecutionJob() {
	}

	/**
	 * Gets the TestConfiguration contained by the TestRunnerJob.
	 * 
	 * @return the TestSheet
	 */
	public TestConfiguration getTestConfiguration() {
		return testConfiguration;
	}

	/**
	 * Sets the given TestConfiguration to the TestRunnerJob.
	 * 
	 * @param testSheet
	 */
	public void setTestConfiguration(TestConfiguration testConfiguration) {
		this.testConfiguration = testConfiguration;
	}

	/**
	 * Sets the context.
	 * 
	 * @param context
	 */
	public void setContext(TestContext context) {
		this.context = context;
	}

	/**
	 * Sets the {@link ClientInteraction} and resumes the job waiting
	 * for it.
	 * 
	 * @param clientInteraction the ClientInteraction to set
	 */
	public void setClientInteraction(ClientInteraction clientInteraction) {
		lock.lock();
		try {
			this.clientInteraction = clientInteraction;
			this.resume();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClientInteraction receiveClientInteraction() {
		lock.lock();
		try {
			this.testConfigurationResult.setStatus(TestConfigurationStatusType.WAITING);
			this.tryPause();
			ClientInteraction receivedClientInteraction = this.clientInteraction;
			this.clientInteraction = null;
			return receivedClientInteraction;
		} finally {
			this.testConfigurationResult.setStatus(TestConfigurationStatusType.RUNNING);
			lock.unlock();
		}
	}

	/**
	 * Gets the TestConfigurationResult.
	 * 
	 * @return the TestConfigurationResult
	 */
	public TestConfigurationResult getTestConfigurationResult() {
		lock.lock();
		try {
			if (this.testConfigurationResult.getStatus() == TestConfigurationStatusType.FINISHED
					|| this.testConfigurationResult.getStatus() == TestConfigurationStatusType.CANCELLED) {
				TestResultFinalizationVisitor visitor = new TestResultFinalizationVisitor();
				try {
					this.testConfigurationResult.accept(visitor);
				} catch (VisitorException ex) {
					logger.error(ex, "Could not finalize TestConfigurationResult");
				}
			}
			return this.testConfigurationResult;
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
    public TestExecutionInfo getTestExecutionInfo() {
		lock.lock();
		try {
			TestExecutionInfo info = super.getTestExecutionInfo();
	        
	        if (context.getCurrentTestScript() != null) {
	        	info.setCurrentTestScriptId(context.getCurrentTestScript().getId());
	        }
	        
	        if (context.getCurrentTestScriptElement() != null) {
	        	info.setCurrentTestScriptElementId(context.getCurrentTestScriptElement().getId());
	        }
	        return info;
		} finally {
			lock.unlock();
		}
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void prepareExecution() {

		// initialize and prepare the TestConfigurationResult
		String resultName = testConfiguration.getName().getValue() + RESULT_SUFFIX;
		testConfigurationResult = TestResultHelper.createTestConfigurationResult();
		testConfigurationResult.setName(resultName);
		testConfigurationResult.setTestConfigurationId(testConfiguration.getId());
		testConfigurationResult.setTestConfigurationName(testConfiguration.getName());
		logger.debug("TestConfigurationResult initialized and prepared. Name: ", resultName);
		
		// initialize TestContext
		context.setExecutionController(this);
		context.setTestConfigurationResult(testConfigurationResult);
		logger.debug("TestContext initialized");
		
		ProxyEnginePool proxyPool = ProxyPoolFactory.getInstance()
				.getProxyEnginePool();
		Collection<ProxyEngine> proxies = proxyPool.getProxyEngines();

		// configure and start all proxies with configurations from TestContext
		logger.debug("Configuring and starting all ProxyEngines");
		
		for (ProxyEngine proxyEngine : proxies) {
			ProxyConfiguration config = context
					.getProxyConfiguration(proxyEngine.getSubEngineType());

			if (config == null) {
				logger.warning("No ProxyConfiguration found in TestContext for SubEngineType ",
						proxyEngine.getSubEngineType().toString(), ". Proxy DISABLED !");
				continue;
			}
			try {
				proxyEngine.configureProxy(config);
				proxyEngine.startProxy();
			} catch (ProxyConfigurationException ex) {
				logger.error(ex, "Could not configure and start "
						+ proxyEngine.getSubEngineType());
			}
		}
		logger.debug("ProxyEngines configured and started");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute() throws InterruptionException {
		long start = 0;
		long end = 0;
		
		try {
			// Start the execution of the TestConfiguration
			logger.debug("Starting execution of TestConfiguration ", testConfiguration.getName().getValue());
			TestConfigurationVisitor visitor = new TestConfigurationVisitor(new TestConfigElementEngineImpl());
			testConfigurationResult.setStatus(TestConfigurationStatusType.RUNNING);
			start = System.currentTimeMillis();
			testConfigurationResult.setStartTime(new Date(start));
			visitor.visit(testConfiguration, context, testConfigurationResult);
			end = System.currentTimeMillis();
			testConfigurationResult.setEndTime(new Date(end));
			testConfigurationResult.setDuration(end - start);
			testConfigurationResult.setStatus(TestConfigurationStatusType.FINISHED);
			logger.debug("Finished execution of TestConfiguration ", testConfiguration.getName().getValue());
		} catch (InterruptionException ex) {
			end = System.currentTimeMillis();
			testConfigurationResult.setEndTime(new Date(end));
			testConfigurationResult.setDuration(end - start);
			testConfigurationResult.setStatus(TestConfigurationStatusType.CANCELLED);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void finalizeExecution() {
		logger.debug("Finalizing execution of TestConfiguration ", testConfiguration.getName().getValue());
		ProxyEnginePool proxyPool = ProxyPoolFactory.getInstance()
				.getProxyEnginePool();
		Collection<ProxyEngine> proxies = proxyPool.getProxyEngines();

		// stop and unconfigure all proxies
		logger.debug("Stopping and unconfiguring all ProxyEngines");
		
		for (ProxyEngine proxyEngine : proxies) {
			try {
				proxyEngine.stopProxy();
				proxyEngine.unConfigureProxy();
			} catch (ProxyConfigurationException ex) {
				logger.error(ex, "Could not unconfigure and stop "
						+ proxyEngine.getSubEngineType());
			}
		}
		logger.debug("ProxyEngines stopped and unconfigured");
	}

}
