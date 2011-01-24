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
package org.nabucco.testautomation.engine.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.proxy.ProxyEngine;
import org.nabucco.testautomation.engine.proxy.deploy.ProxyEngineDeployer;
import org.nabucco.testautomation.engine.proxy.deploy.ProxyEngineLocator;
import org.nabucco.testautomation.engine.proxy.exception.ProxyDeploymentException;
import org.nabucco.testautomation.engine.proxy.pool.ProxyEnginePool;
import org.nabucco.testautomation.engine.proxy.pool.ProxyPoolFactory;


/**
 * Deployer
 * 
 * @author Steffen Schmidt, PRODYNA AG
 *
 */
public class Deployer {

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(Deployer.class);

	private File deployPath;

	private final List<ProxyEngineDeployer> deployerList = new ArrayList<ProxyEngineDeployer>();

	public Deployer(String deployPath) {
		this.deployPath = new File(deployPath);
	}

	public Deployer(File deployPath) {
		this.deployPath = deployPath;
	}
	
	public void deploy() {
		deployProxyEngines();
	}
	
	public void undeploy() {
		undeployProxyEngines();
	}

	private void deployProxyEngines() {
		logger.info("Deploying proxies ...");
		ProxyEngineLocator locator = new ProxyEngineLocator();
		List<File> proxyJars = locator.locateProxies(deployPath);
		logger.info(proxyJars.size() + " proxies found for deployment");
		ProxyEnginePool proxyPool = ProxyPoolFactory.getInstance().getProxyEnginePool();

		for (File jar : proxyJars) {
			try {
				ProxyEngineDeployer deployer = new ProxyEngineDeployer(jar);
				ProxyEngine proxy = deployer.deploy();
				proxyPool.addProxyEngine(proxy);
				deployerList.add(deployer);
				logger.info(deployer.getProxyEngineName() + " deployed");
			} catch (ProxyDeploymentException e) {
				logger.error("Error while deploying proxy " + jar.getName()
						+ ": " + e.getMessage());
			}
		}
	}
	
	private void undeployProxyEngines() {
		
		for (ProxyEngineDeployer deployer : deployerList) {
			try {
				deployer.undeploy();
			} catch (ProxyDeploymentException e) {
				logger.error(e, "Error while undeploying "
						+ deployer.getProxyEngineName());
			}
		}
	}

}
