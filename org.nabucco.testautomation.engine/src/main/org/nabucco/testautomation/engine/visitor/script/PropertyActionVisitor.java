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
package org.nabucco.testautomation.engine.visitor.script;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.nabucco.framework.base.facade.datatype.file.TextFileContent;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.PropertyHelper;
import org.nabucco.testautomation.engine.exception.PropertyActionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.facade.datatype.base.BooleanValue;
import org.nabucco.testautomation.facade.datatype.base.DateValue;
import org.nabucco.testautomation.facade.datatype.base.DoubleValue;
import org.nabucco.testautomation.facade.datatype.base.IntegerValue;
import org.nabucco.testautomation.facade.datatype.base.LongValue;
import org.nabucco.testautomation.facade.datatype.base.SqlValue;
import org.nabucco.testautomation.facade.datatype.base.StringValue;
import org.nabucco.testautomation.facade.datatype.base.XPathValue;
import org.nabucco.testautomation.facade.datatype.base.XmlValue;
import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.DoubleProperty;
import org.nabucco.testautomation.facade.datatype.property.FileProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.LongProperty;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.SqlProperty;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.XPathProperty;
import org.nabucco.testautomation.facade.datatype.property.XmlProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyContainer;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyReference;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.PropertyActionType;

/**
 * PropertyActionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class PropertyActionVisitor extends
		AbstractTestScriptVisitor<TestScriptResult> {

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(PropertyActionVisitor.class);
	
	private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

	/**
	 * Constructs a new PropertyActionVisitor instance using the given
	 * {@link TestContext} and {@link TestScriptEngine}.
	 * 
	 * @param context
	 *            the context
	 * @param testScriptEngine
	 *            the TestScriptEngine
	 */
	protected PropertyActionVisitor(TestContext context,
			TestScriptEngine testScriptEngine) {
		super(context, testScriptEngine);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(PropertyAction propertyAction, TestScriptResult argument)
			throws TestScriptException {
		getContext().setCurrentTestScriptElement(propertyAction);
		super.visit(propertyAction, argument);

		PropertyActionType action = propertyAction.getAction();
		logger.debug("Performing PropertyAction " + action);

		switch (action) {
		case CLEAR:
			clear(propertyAction);
			break;
		case COPY:
			copy(propertyAction);
			break;
		case DELETE:
			delete(propertyAction);
			break;
		case SET:
			set(propertyAction);
			break;
		default:
			throw new PropertyActionException("Unsupported PropertyActionType: " + action);
		}
	}

	private void clear(PropertyAction propertyAction) throws PropertyActionException {

		PropertyReference propertyRef = propertyAction.getPropertyRef();

		if (propertyRef == null || propertyRef.getValue() == null) {
			throw new PropertyActionException("No PropertyRef defined in PropertyAction CLEAR");
		}

		logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
		Property property = getContext().getProperty(
				propertyRef.getValue());

		if (property == null) {
			throw new PropertyActionException("PropertyAction CLEAR: no Property found in TestContext for NameRef: '"
					+ propertyRef.getValue());
		}

		switch (property.getType()) {
		case BOOLEAN:
			((BooleanProperty) property).setValue((BooleanValue) null);
			break;
		case DATE:
			((DateProperty) property).setValue((DateValue) null);
			break;
		case DOUBLE:
			((DoubleProperty) property).setValue((DoubleValue) null);
			break;
		case INTEGER:
			((IntegerProperty) property).setValue((IntegerValue) null);
			break;
		case LIST:
			((PropertyList) property).getPropertyList().clear();
			break;
		case LONG:
			((LongProperty) property).setValue((LongValue) null);
			break;
		case STRING:
			((StringProperty) property).setValue((StringValue) null);
			break;
		case XML:
			((XmlProperty) property).setValue((XmlValue) null);
			break;
		case XPATH:
			((XPathProperty) property).setValue((XPathValue) null);
			break;
		case FILE:
			((FileProperty) property).setContent((TextFileContent) null);
			break;
		case SQL:
			((SqlProperty) property).setValue((SqlValue) null);
			break;
		default:
			throw new PropertyActionException("Unsupported PropertyType for PropertyAction CLEAR: " + property.getType());
		}
		logger.info("Property '" + property.getName().getValue() + "' cleared");
	}

	private void copy(PropertyAction propertyAction) throws PropertyActionException {

		PropertyReference propertyRef = propertyAction.getPropertyRef();
		PropertyReference targetRef = propertyAction.getTarget();

		if (propertyRef == null || propertyRef.getValue() == null) {
			throw new PropertyActionException("No PropertyRef defined in PropertyAction COPY");
		}

		if (targetRef == null || targetRef.getValue() == null) {
			throw new PropertyActionException("No TargetPropertyRef defined in PropertyAction COPY");
		}

		logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
		Property property = getContext().getProperty(
				propertyRef.getValue());
		Property target = getContext().getProperty(
				targetRef.getValue());

		if (property == null) {
			throw new PropertyActionException("PropertyAction COPY: no Property found in TestContext for NameRef: '"
					+ propertyRef.getValue());
		}

		// TargetProperty not found -> create new one an put into TestContext
		if (target == null || target.getType() != property.getType()) {
			target = PropertyHelper.createProperty(property.getType(),
					targetRef.getValue());
			getContext().put(target);
		}

		switch (target.getType()) {
		case BOOLEAN:
			((BooleanProperty) target).setValue(((BooleanProperty) property)
					.getValue());
			break;
		case DATE:
			((DateProperty) target).setValue(((DateProperty) property)
					.getValue());
			break;
		case DOUBLE:
			((DoubleProperty) target).setValue(((DoubleProperty) property)
					.getValue());
			break;
		case INTEGER:
			((IntegerProperty) target).setValue(((IntegerProperty) property)
					.getValue());
			break;
		case LIST:
			List<PropertyContainer> children = ((PropertyList) target).getPropertyList();
			children.addAll(((PropertyList) property).getPropertyList());
			break;
		case LONG:
			((LongProperty) target).setValue(((LongProperty) property)
					.getValue());
			break;
		case STRING:
			((StringProperty) target).setValue(((StringProperty) property)
					.getValue());
			break;
		case XML:
			((XmlProperty) target)
					.setValue(((XmlProperty) property).getValue());
			break;
		case XPATH:
			((XPathProperty) target).setValue(((XPathProperty) property)
					.getValue());
			break;
		case FILE:
			((FileProperty) target).setContent(((FileProperty) property)
					.getContent());
			break;
		case SQL:
			((SqlProperty) target).setValue(((SqlProperty) property)
					.getValue());
			break;
		default:
			throw new PropertyActionException("Unsupported PropertyType for PropertyAction COPY: " + target.getType());
		}
		logger.info("Value of Property '" + property.getName().getValue()
				+ "' copied to Property '" + target.getName().getValue() + "'");
	}

	private void delete(PropertyAction propertyAction) throws PropertyActionException {

		PropertyReference propertyRef = propertyAction.getPropertyRef();

		if (propertyRef == null || propertyRef.getValue() == null) {
			throw new PropertyActionException("No PropertyRef defined in PropertyAction DELETE");
		}

		logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
		Property property = getContext().getProperty(
				propertyRef.getValue());

		if (property == null) {
			throw new PropertyActionException("PropertyAction DELETE: no Property found in TestContext for NameRef: '"
					+ propertyRef.getValue());
		}
		getContext().remove(property);
		logger.info("Property '" + property.getName().getValue()
				+ "' removed from TestContext");
	}

	private void set(PropertyAction propertyAction) throws PropertyActionException {

		PropertyReference propertyRef = propertyAction.getPropertyRef();

		if (propertyRef == null || propertyRef.getValue() == null) {
			throw new PropertyActionException("No PropertyRef defined in PropertyAction SET");
		}

		logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
		Property property = getContext().getProperty(
				propertyRef.getValue());

		if (property == null) {
			throw new PropertyActionException("PropertyAction SET: no Property found in TestContext for NameRef: '"
					+ propertyRef.getValue());
		}

		String value = propertyAction.getValue() != null ? propertyAction.getValue().getValue() : "";

		if (value == null) {
			throw new PropertyActionException("No value to set defined in PropertyAction SET");
		}

		switch (property.getType()) {
		case BOOLEAN:
			((BooleanProperty) property).setValue(Boolean.parseBoolean(value));
			break;
		case DATE:
			try {
				((DateProperty) property).setValue(df.parse(value));
			} catch (ParseException e) {
				throw new PropertyActionException("Invalid date format in PropertyAction SET: " + value + " (expected format: dd.MM.yyyy)");
			}
			break;
		case DOUBLE:
			((DoubleProperty) property).setValue(Double.parseDouble(value));
			break;
		case INTEGER:
			((IntegerProperty) property).setValue(Integer.parseInt(value));
			break;
		case LONG:
			((LongProperty) property).setValue(Long.parseLong(value));
			break;
		case STRING:
			((StringProperty) property).setValue(value);
			break;
		case XML:
			((XmlProperty) property).setValue(value);
			break;
		case XPATH:
			((XPathProperty) property).setValue(value);
			break;
		case FILE:
			((FileProperty) property).setContent(value);
			break;
		case SQL:
			((SqlProperty) property).setValue(value);
			break;
		default:
			throw new PropertyActionException("Unsupported PropertyType for for PropertyAction SET: " + property.getType());
		}
		logger.info("Property '" + property.getName().getValue()
				+ "' resetted with value '" + value + "'");
	}

}
