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
package org.nabucco.testautomation.engine.assertion;

import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.property.facade.datatype.base.Property;

/**
 * Assertion
 *
 * @author Steffen Schmidt, PRODYNA AG
 */
public interface Assertion {

	public void executeAssertion(Property property) throws AssertionException;
	
}
