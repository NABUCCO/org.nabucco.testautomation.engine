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
import org.nabucco.testautomation.engine.base.engine.ExecutionController;
import org.nabucco.testautomation.engine.sub.TestConfigElementEngine;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElementContainer;
import org.nabucco.testautomation.config.facade.datatype.comparator.TestConfigElementSorter;
import org.nabucco.testautomation.result.facade.datatype.TestResult;

/**
 * AbstractTestConfigElementVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public abstract class AbstractTestConfigElementVisitor<A> implements TestConfigVisitor<A> {

	private static final TestConfigElementSorter sorter = new TestConfigElementSorter();
	
    private TestConfigElementEngine testConfigElementEngine;

    /**
     * Constructs a AbstractTestConfigElementVisitor using the given TestContext and TestConfigElementEngine.
     * 
     * @param context the TestContext
     * @param testStepEngine the TestConfigElementEngine
     */
    AbstractTestConfigElementVisitor(TestConfigElementEngine testStepEngine) {
    	this.testConfigElementEngine = testStepEngine;
    }
    
    public void visit(TestConfigElement testConfigElement, TestContext context, TestResult argument) {
    	
        List<TestConfigElementContainer> testConfigElementList = testConfigElement.getTestConfigElementList();
        Collections.sort(testConfigElementList, sorter);
        
		for (TestConfigElementContainer child : testConfigElementList) {
            visit(child.getElement(), context, argument);
        }
    }
    
    /**
     * Gets the TestConfigElementEngine used by this visitor.
     * 
     * @return the TestStepEngine
     */
	protected TestConfigElementEngine getTestConfigElementEngine() {
		return testConfigElementEngine;
	}
	
	/**
	 * 
	 */
	protected void checkExecutionController(TestContext context) {
		ExecutionController executionController = context.getExecutionController();

		if (executionController != null) {
			if (executionController.isPaused()) {
				executionController.tryPause();
			}
			if (executionController.isInterrupted()) {
				executionController.tryInterruption();
			}
		}
	}

}
