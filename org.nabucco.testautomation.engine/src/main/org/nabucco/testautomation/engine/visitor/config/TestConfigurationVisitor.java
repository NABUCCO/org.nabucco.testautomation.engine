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
package org.nabucco.testautomation.engine.visitor.config;

import java.util.Collections;
import java.util.List;

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElementContainer;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.config.facade.datatype.comparator.TestConfigElementSorter;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;

/**
 * TestConfigurationVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestConfigurationVisitor extends
        AbstractTestConfigElementVisitor<TestConfigurationResult> {

    public TestConfigurationVisitor(TestConfigElementEngine testStepEngine) {
        super(testStepEngine);
    }

    /**
     * 
     * @param testConfiguration
     * @param testResult
     */
    @Override
    public void visit(TestConfiguration testConfiguration, TestContext context, 
            TestConfigurationResult testConfigurationResult) {
    	
    	if (testConfiguration.getEnvironmentType() != null
				&& testConfiguration.getEnvironmentType().getName() != null) {
			
    		// Set Environment in TestConfigurationResult
    		testConfigurationResult.setEnvironmentType(testConfiguration.getEnvironmentType());
    		
    		// Put into context
    		StringProperty environment = new StringProperty();
			environment.setName(TestContext.ENVIRONMENT);
			environment.setValue(testConfiguration.getEnvironmentType().getName()
					.getValue());
			context.put(environment);
		}
    	
    	if (testConfiguration.getReleaseType() != null
				&& testConfiguration.getReleaseType().getName() != null) {
    		
    		// Set Release in TestConfigurationResult
    		testConfigurationResult.setReleaseType(testConfiguration.getReleaseType());
    		
    		// Put into context
    		StringProperty release = new StringProperty();
			release.setName(TestContext.RELEASE);
			release.setValue(testConfiguration.getReleaseType().getName()
					.getValue());
			context.put(release);
		}
    	
    	List<TestConfigElementContainer> testConfigElementList = testConfiguration.getTestConfigElementList();
    	Collections.sort(testConfigElementList, new TestConfigElementSorter());
    	
    	for (TestConfigElementContainer testConfigElement : testConfigElementList) {
    		visit(testConfigElement.getElement(), context, testConfigurationResult);
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestConfigElement testConfigElement, TestContext context, 
            TestConfigurationResult testConfigurationResult) {
        new TestConfigElementVisitor(getTestConfigElementEngine()).visit(
                testConfigElement, context, testConfigurationResult);
    }

}
