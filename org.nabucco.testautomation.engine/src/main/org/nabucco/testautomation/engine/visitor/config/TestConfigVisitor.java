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

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.engine.base.context.TestContext;

/**
 * TestConfigVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface TestConfigVisitor<A> {

    /**
     * Visits the given TestConfiguration.
     * 
     * @param testConfiguration
     * @param argument
     */
    void visit(TestConfiguration testConfiguration, TestContext context, A argument);
    
    /**
     * Visits the given TestConfigElement.
     * 
     * @param testConfigElement the TestConfigElement
     * @param argument an argument
     */
    void visit(TestConfigElement testConfigElement, TestContext context, A argument);
    
}
