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
package org.nabucco.testautomation.engine.visitor.result;

import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestResultContainer;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.result.facade.datatype.visitor.TestResultVisitor;

/**
 * TestResultStatusVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestResultStatusVisitor extends TestResultVisitor {

	@Override
    protected void visit(TestResult testResult) {

        if (testResult.getStatus() == null) {
            testResult.setStatus(TestConfigElementStatusType.PASSED);
        }

        for (TestScriptResult testScriptResult : testResult.getTestScriptResultList()) {
            visit(testScriptResult, testResult);
        }

        for (TestResultContainer subResultContainer : testResult.getTestResultList()) {
            TestResult subResult = subResultContainer.getResult();
        	this.visit(subResult);

            if (subResult.getStatus() == TestConfigElementStatusType.FAILED) {
                testResult.setStatus(TestConfigElementStatusType.FAILED);
            }
        }
    }

	protected void visit(TestScriptResult testScriptResult, TestResult parentResult) {

        if (testScriptResult.getStatus() == TestScriptStatusType.FAILED
                || testScriptResult.getStatus() == TestScriptStatusType.ABORTED) {
            parentResult.setStatus(TestConfigElementStatusType.FAILED);
        }
    }

}
