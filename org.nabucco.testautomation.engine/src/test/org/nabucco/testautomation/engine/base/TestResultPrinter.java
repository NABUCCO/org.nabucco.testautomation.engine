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
package org.nabucco.testautomation.engine.base;

import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestResultContainer;
import org.nabucco.testautomation.result.facade.datatype.TestScriptElementResult;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;

/**
 * 
 * TestResultPrinter
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestResultPrinter {

	public static String toString(TestConfigurationResult configResult) {
		
		StringBuilder str = new StringBuilder();
		str.append("\nName: " + configResult.getName());
		str.append("  [" + configResult.getStatus() + "] (" + (configResult.getDuration().getValue()) + " ms)\n");
		str.append("------------------------------------------------\n\n");
		for (TestResultContainer result : configResult.getTestResultList()) {
			str.append(toString(result.getResult()));
		}
		return str.toString();		
	}
	
	public static String toString(TestResult result) {
		
		StringBuilder str = new StringBuilder();
		int level = 0;
		
		switch (result.getLevel()) {
		case ONE:
			level = 1;
			break;
		case TWO:
			level = 2;
			break;
		case THREE:
			level = 3;
			break;
		case FOUR:
			level = 4;
			break;
		case FIVE:
			level = 5;
			break;
		}
		
		for (int i = 0; i < level; i++) {
			str.append("   ");
		}
		
		str.append(result.getName());
		str.append(": ");
		str.append(result.getTestConfigElementName().getValue());
		str.append(" [");
		str.append(result.getStatus());
		str.append("]");
		
		if (result.getEndTime() != null && result.getStartTime() != null) {
			str.append(" (");
			str.append(result.getDuration().getValue());
			str.append(" ms)");
		}
		
		if (result.getErrorMessage() != null) {
			str.append(" -> ");
			str.append(result.getErrorMessage().getValue());
		}
		str.append("\n");
		
		for (TestScriptResult scriptResult : result.getTestScriptResultList()) {
			str.append(toString(scriptResult, level));
		}
		
		for (TestResultContainer tr : result.getTestResultList()) {
			str.append(toString(tr.getResult()));
		}
		return str.toString();
	}
	
	public static String toString(TestScriptResult result, int level) {
		
		StringBuilder str = new StringBuilder();
		str.append("   ");
		
		for (int i = 0; i < level; i++) {
			str.append("   ");
		}

		str.append("TestScripResult: ");
		str.append(result.getTestScriptName());
		str.append(" [");
		str.append(result.getStatus());
		str.append("]");

		if (result.getEndTime() != null && result.getStartTime() != null) {
			str.append(" (");
			str.append(result.getDuration().getValue());
			str.append(" ms)");
		}
		
		if (result.getErrorMessage() != null) {
			str.append(" -> ");
			str.append(result.getErrorMessage().getValue());
		}
		str.append("\n");
		
		for (TestScriptElementResult elementResult : result.getElementResultList()) {
			if (elementResult instanceof TestScriptResult) {
				str.append(toString((TestScriptResult) elementResult, level + 1));
			}
		}
		return str.toString();
	}
	
}
