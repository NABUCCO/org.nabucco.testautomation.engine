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
package org.nabucco.testautomation.engine.launcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.nabucco.testautomation.engine.TestEngine;
import org.nabucco.testautomation.engine.TestEngineFactory;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.net.TestEngineConnectionFactory;
import org.nabucco.testautomation.engine.deploy.Deployer;
import org.nabucco.testautomation.engine.execution.TestExecutionService;
import org.nabucco.testautomation.engine.execution.TestExecutionServiceFactory;
import org.nabucco.testautomation.engine.launcher.exception.LaunchingException;

import org.nabucco.testautomation.facade.exception.engine.TestEngineException;

/**
 * TestEngineLauncher
 * 
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class TestEngineLauncher {

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(TestEngineLauncher.class);

	private static final String DEPLOY_PATH = "./deploy";

	private int port;

	private String bindingName;
	
	private Registry registry;

	private File deployPath;

	private TestEngine testEngine;
	
	private Deployer deployer;
	
	private TestExecutionService service;

	public TestEngineLauncher(int port, String bindingName, File deployPath) {
		this.port = port;
		this.bindingName = bindingName;
		this.deployPath = deployPath;
	}

	public void start() throws LaunchingException {
		
		// Start Engine-Server
		logger.info("Starting TestEngine ...");
		try {
			this.service = TestExecutionServiceFactory.getInstance().createTestExecutionService();
			logger.info("TestExecutionService initialized with thread pool size: " + TestExecutionServiceFactory.THREAD_POOL_SIZE);
			this.testEngine = TestEngineFactory.getInstance().createTestEngine(this.service);
		} catch (TestEngineException ex) {
			throw new LaunchingException(
					"Could not create TestEngine-instance", ex);
		}
		exportTestEngine(this.testEngine, this.port, this.bindingName);

		this.deployer = new Deployer(deployPath);
		this.deployer.deploy();

		logger.info("TestEngine started");
	}

	public void stop() {
		logger.info("Stopping TestEngine ...");
		this.deployer.undeploy();
		
		try {
			this.registry.unbind(bindingName);
			logger.info(bindingName + " unbound");
		} catch(NotBoundException ex) {
		} catch (Exception ex) {
			logger.error(ex);
		}
		logger.info("TestEngine stopped");
	}
	
	public void pause() {
		logger.info("Pausing current Jobs ...");
		
		try {
			for (long jobId : this.service.getRunningJobs()) {
				this.service.pauseExecution(jobId);
			}
			logger.info("All jobs paused");
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public void resume() {
		logger.info("Resuming current Jobs ...");
		
		try {
			for (long jobId : this.service.getRunningJobs()) {
				this.service.resumeExecution(jobId);
			}
			logger.info("All jobs resumed");
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void kill() {
		logger.info("Stopping current Jobs ...");
		
		try {
			for (long jobId : this.service.getRunningJobs()) {
				this.service.stopExecution(jobId);
			}
			logger.info("All jobs stopped");
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/*
	 * exports the TestEngine interface, so it available via RMI under the
	 * defined name in bindingName
	 */
	private void exportTestEngine(TestEngine testEngine, int port,
			String bindingName) throws LaunchingException {

		try {
			TestEngineConnectionFactory connectionFactory = new TestEngineConnectionFactory();
			this.registry = LocateRegistry.createRegistry(port, connectionFactory, connectionFactory);
			logger.info("Registry created on port " + port);
			this.registry.rebind(bindingName, testEngine);
			logger.info("TestEngine bound on name '" + bindingName + "'");
		} catch (Exception ex) {
			throw new LaunchingException("Could not export TestEngine", ex);
		}
	}

	public static void main(String[] args) {

		// check args
		if (args.length != 2) {
			printUsage();
		}
		String arg = args[0];
		int port = 0;

		try {
			port = Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			logger.fatal("Could not parse port: " + arg);
			printUsage();
		}
		String bindingName = args[1];
		File deployPath = new File(DEPLOY_PATH);

		if (!deployPath.exists()) {
			deployPath.mkdir();
		}

		// Launch TestEngine
		TestEngineLauncher launcher = new TestEngineLauncher(port, bindingName,
				deployPath);
		try {
			launcher.start();
			Runtime.getRuntime().addShutdownHook(
					new ShutdownTestEngineHook(launcher));
		} catch (LaunchingException ex) {
			logger.fatal(ex, "Could not launch TestEngine");
			System.exit(0);
		}

		// Get commands
		LineNumberReader in = new LineNumberReader(new InputStreamReader(
				System.in));
		String command = null;
		
		try {
			while (true) {
				command = in.readLine();
				
				if (command.equals("stop")) {
					break;
				} else if (command.equals("pause")) {
					launcher.pause();
				} else if (command.equals("kill")) {
					launcher.kill();
				} else if (command.equals("resume")) {
					launcher.resume();
				} else {
					logger.warning("Unknown command");
				}
			}
		} catch (IOException ex) {
			logger.warning(ex, "Could not read command");
		}
		System.exit(0);
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("TestEngineLauncher <port> <bindingName>");
		System.out.println();
		System.out.println("Arguments:");
		System.out.println("<port> = the RMI-Port");
		System.out.println("<bindingName> = the jndi-name of the bound TestEngine");
		System.out.println();
		System.exit(0);
	}

	static class ShutdownTestEngineHook extends Thread {

		private TestEngineLauncher launcher;

		/**
		 * Constructs a new ShutdownTestEngineHook-Thread for the specified
		 * {@link TestEngineLauncher}
		 * 
		 * @param server
		 *            the server application which will be stopped
		 * 
		 */
		public ShutdownTestEngineHook(TestEngineLauncher launcher) {
			super("ShutdownTestEngineHook");
			this.launcher = launcher;
		}

		/**
		 * Stops the TestEngine
		 */
		public void run() {

			if (this.launcher != null) {
				this.launcher.stop();
			}
		}

	}

}
