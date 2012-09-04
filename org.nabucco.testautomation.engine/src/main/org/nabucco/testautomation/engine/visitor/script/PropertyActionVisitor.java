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
package org.nabucco.testautomation.engine.visitor.script;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.nabucco.framework.base.facade.datatype.Amount;
import org.nabucco.framework.base.facade.datatype.Flag;
import org.nabucco.framework.base.facade.datatype.Name;
import org.nabucco.framework.base.facade.datatype.date.Date;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.framework.base.facade.datatype.text.TextContent;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.PropertyActionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.BooleanProperty;
import org.nabucco.testautomation.property.facade.datatype.DateProperty;
import org.nabucco.testautomation.property.facade.datatype.FileProperty;
import org.nabucco.testautomation.property.facade.datatype.NumericProperty;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.property.facade.datatype.SqlProperty;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.XPathProperty;
import org.nabucco.testautomation.property.facade.datatype.XmlProperty;
import org.nabucco.testautomation.property.facade.datatype.base.LongText;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyContainer;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyReference;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyType;
import org.nabucco.testautomation.property.facade.datatype.base.Text;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.PropertyActionType;

/**
 * PropertyActionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class PropertyActionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final String EMPTY_STRING = "";

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            PropertyActionVisitor.class);

    private static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    /**
     * Constructs a new PropertyActionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
    protected PropertyActionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(PropertyAction propertyAction, TestScriptResult argument) throws TestScriptException {
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
        case SIZE_OF:
            sizeOf(propertyAction);
            break;
        case CONCAT:
            concat(propertyAction);
            break;
        case ADD:
            add(propertyAction);
            break;
        case SUBTRACT:
            subtract(propertyAction);
            break;
        case LENGTH:
            length(propertyAction);
            break;
        case MULTIPLY:
        	multiply(propertyAction);
        	break;
        case DIVIDE:
        	divide(propertyAction);
        	break;
        case MODULO:
        	modulo(propertyAction);
        	break;
        case TRIM:
        	trim(propertyAction);
        	break;
        case SUBSTRING:
        	substring(propertyAction);
        	break;
        case SPLIT:
        	split(propertyAction);
        	break;
        default:
            throw new PropertyActionException("Unsupported PropertyActionType: " + action);
        }
    }

	/**
     * 
     * @param propertyAction
     * @throws PropertyActionException
     */
    private void length(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();
        PropertyReference targetRef = propertyAction.getTarget();

        if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No Target defined in PropertyAction '" + propertyAction + "'");
        }

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No Property Reference defined in PropertyAction '"
                    + propertyAction + "'");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction LENGTH: no Property found in TestContext for NameRef: '"
                    + propertyRef.getValue());
        }

        int length = PropertyHelper.toString(property).length();
        Property resultProp = PropertyHelper.createNumericProperty(targetRef.getValue(), length);
        this.getContext().put(resultProp);
    }

    /**
     * 
     * @param propertyAction
     * @throws PropertyActionException
     */
    private void add(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();
        PropertyReference targetRef = propertyAction.getTarget();

        if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No Target defined in PropertyAction '" + propertyAction + "'");
        }

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No Property Reference defined in PropertyAction '"
                    + propertyAction + "'");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef.getValue() + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException(
                    "PropertyAction SUBTRACT: no Property found in TestContext for NameRef: '" + propertyRef);
        }

        if (propertyAction.getValue() == null || propertyAction.getValue().getValue() == null) {
            throw new PropertyActionException("No Value defined in PropertyAction '" + propertyAction + "'");
        }

        try {
            BigDecimal a = new BigDecimal(PropertyHelper.toString(property));
            BigDecimal b = null;
            Property valueProp = this.getContext().getProperty(new Name(propertyAction.getValue().getValue()));

            if (valueProp == null) {
                b = new BigDecimal(propertyAction.getValue().getValue());
            } else {
                b = new BigDecimal(PropertyHelper.toString(valueProp));
            }

            BigDecimal result = a.add(b);
            logger.info("Addition: " + a.toString() + " + " + b.toString() + " = " + result.toString());
            Property resultProp = null;
            resultProp = PropertyHelper.createNumericProperty(targetRef.getValue(), result);

            this.getContext().put(resultProp);
        } catch (NumberFormatException ex) {
            throw new PropertyActionException("NumberFormatException in PropertyAction '"
                    + propertyAction.getName().getValue() + "'");
        }
    }

    /**
     * 
     * @param propertyAction
     * @throws PropertyActionException
     */
    private void subtract(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();
        PropertyReference targetRef = propertyAction.getTarget();

        if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No Target defined in PropertyAction '" + propertyAction + "'");
        }

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No Property Reference defined in PropertyAction '"
                    + propertyAction + "'");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException(
                    "PropertyAction SUBTRACT: no Property found in TestContext for NameRef: '" + propertyRef + "'");
        }

        if (propertyAction.getValue() == null || propertyAction.getValue().getValue() == null) {
            throw new PropertyActionException("No Value defined in PropertyAction '" + propertyAction + "'");
        }

        try {
            BigDecimal a = new BigDecimal(PropertyHelper.toString(property));
            BigDecimal b = null;
            Property valueProp = this.getContext().getProperty(new Name(propertyAction.getValue().getValue()));

            if (valueProp == null) {
                b = new BigDecimal(propertyAction.getValue().getValue());
            } else {
                b = new BigDecimal(PropertyHelper.toString(valueProp));
            }

            BigDecimal result = a.subtract(b);
            logger.info("Subtraction: " + a.toString() + " + " + b.toString() + " = " + result.toString());
            Property resultProp = null;

            if (result.scale() == 0) {
                resultProp = PropertyHelper.createNumericProperty(targetRef.getValue(), result);
            } else {
                resultProp = PropertyHelper.createNumericProperty(targetRef.getValue(), result);
            }

            this.getContext().put(resultProp);
        } catch (NumberFormatException ex) {
            throw new PropertyActionException("NumberFormatException in PropertyAction '"
                    + propertyAction.getName().getValue() + "'");
        }
    }

    /**
     * Concatenates the StringValue of the Property referenced by Property Reference and the
     * StringValue of the Property referenced by Value or just Value as string, if no Property is
     * found.
     * 
     * @param propertyAction
     * @throws PropertyActionException
     */
    private void concat(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();
        PropertyReference targetRef = propertyAction.getTarget();

        if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No Target defined in PropertyAction '" + propertyAction + "'");
        }

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No Property Reference defined in PropertyAction '"
                    + propertyAction + "'");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction CONCAT: no Property found in TestContext for NameRef: '"
                    + propertyRef + "'");
        }

        if (propertyAction.getValue() == null || propertyAction.getValue().getValue() == null) {
            throw new PropertyActionException("No Value defined in PropertyAction '" + propertyAction + "'");
        }

        String arg1 = PropertyHelper.toString(property);
        String arg2 = EMPTY_STRING;
        Property valueProp = this.getContext().getProperty(new Name(propertyAction.getValue().getValue()));

        if (valueProp == null) {
            arg2 = propertyAction.getValue().getValue();
        } else {
            arg2 = PropertyHelper.toString(valueProp);
        }

        if (arg1 == null) {
            arg1 = EMPTY_STRING;
        }

        if (arg2 == null) {
            arg2 = EMPTY_STRING;
        }

        String concatenation = arg1.concat(arg2);
        Property targetProp = getContext().getProperty(targetRef);

        if (targetProp == null) {
            this.getContext().put(PropertyHelper.createTextProperty(targetRef.getValue(), concatenation));
        } else {

            switch (targetProp.getType()) {
            case TEXT:
                ((TextProperty) targetProp).setValue(concatenation);
                break;
            case XML:
                ((XmlProperty) targetProp).setValue(concatenation);
                break;
            case XPATH:
                ((XPathProperty) targetProp).setValue(concatenation);
                break;
            case SQL:
                ((SqlProperty) targetProp).setValue(concatenation);
                break;
            default:
                throw new PropertyActionException("Illegal PropertyType for PropertyAction CONCAT: "
                        + targetProp.getType());
            }
        }
    }

    /**
     * Determines the Property referenced by Property Reference. If List or XPath, the number of
     * children is set in an NumericProperty with the name defined in Target, otherwise 1 or 0, if
     * the referenced Property is null.
     * 
     * @param propertyAction
     * @throws PropertyActionException
     */
    private void sizeOf(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();
        PropertyReference targetRef = propertyAction.getTarget();

        if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No Target defined in PropertyAction '" + propertyAction + "'");
        }

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No Property Reference defined in PropertyAction '"
                    + propertyAction + "'");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            getContext().put(PropertyHelper.createNumericProperty(targetRef.getValue(), BigDecimal.ZERO));
            return;
        }

        switch (property.getType()) {
        case LIST: {
            int size = ((PropertyList) property).getPropertyList().size();
            logger.info("Size of '" + property.getName().getValue() + "': " + size);
            getContext().put(PropertyHelper.createNumericProperty(targetRef.getValue(), size));
            break;
        }
        case XPATH: {
            int size = ((XPathProperty) property).getPropertyList().size();
            logger.info("Size of '" + property.getName().getValue() + "': " + size);
            getContext().put(PropertyHelper.createNumericProperty(targetRef.getValue(), size));
            break;
        }
        default: {
            int size = 1;
            logger.info("Size of '" + property.getName().getValue() + "': " + size);
            getContext().put(PropertyHelper.createNumericProperty(targetRef.getValue(), size));
            break;
        }
        }
    }

    private void clear(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No PropertyRef defined in PropertyAction CLEAR");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction CLEAR: no Property found in TestContext for NameRef: '"
                    + propertyRef.getValue());
        }

        switch (property.getType()) {
        case BOOLEAN:
            ((BooleanProperty) property).setValue((Flag) null);
            break;
        case DATE:
            ((DateProperty) property).setValue((Date) null);
            break;
        case NUMERIC:
            ((NumericProperty) property).setValue((Amount) null);
            break;
        case LIST:
            ((PropertyList) property).getPropertyList().clear();
            break;
        case TEXT:
            ((TextProperty) property).setValue((Text) null);
            break;
        case XML:
            ((XmlProperty) property).setValue((LongText) null);
            break;
        case XPATH:
            ((XPathProperty) property).setValue((LongText) null);
            break;
        case FILE:
            ((FileProperty) property).setContent((TextContent) null);
            break;
        case SQL:
            ((SqlProperty) property).setValue((LongText) null);
            break;
        default:
            throw new PropertyActionException("Unsupported PropertyType for PropertyAction CLEAR: "
                    + property.getType());
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

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);
        Property target = getContext().getProperty(targetRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction COPY: no Property found in TestContext for NameRef: '"
                    + propertyRef.getValue());
        }

        // TargetProperty not found -> create new one an put into TestContext
        if (target == null || target.getType() != property.getType()) {
            target = PropertyHelper.createProperty(property.getType(), targetRef.getValue());
            getContext().put(target);
        }

        switch (target.getType()) {
        case BOOLEAN:
            ((BooleanProperty) target).setValue(((BooleanProperty) property).getValue());
            break;
        case DATE:
            ((DateProperty) target).setValue(((DateProperty) property).getValue());
            break;
        case NUMERIC:
            ((NumericProperty) target).setValue(((NumericProperty) property).getValue());
            break;
        case LIST:
            List<PropertyContainer> children = ((PropertyList) target).getPropertyList();
            children.addAll(((PropertyList) property).getPropertyList());
            break;
        case TEXT:
            ((TextProperty) target).setValue(((TextProperty) property).getValue());
            break;
        case XML:
            ((XmlProperty) target).setValue(((XmlProperty) property).getValue());
            break;
        case XPATH:
            ((XPathProperty) target).setValue(((XPathProperty) property).getValue());
            break;
        case FILE:
            ((FileProperty) target).setContent(((FileProperty) property).getContent());
            break;
        case SQL:
            ((SqlProperty) target).setValue(((SqlProperty) property).getValue());
            break;
        default:
            throw new PropertyActionException("Unsupported PropertyType for PropertyAction COPY: " + target.getType());
        }
        logger.info("Value of Property '"
                + property.getName().getValue() + "' copied to Property '" + target.getName().getValue() + "'");
    }

    private void delete(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No PropertyRef defined in PropertyAction DELETE");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction DELETE: no Property found in TestContext for NameRef: '"
                    + propertyRef.getValue());
        }
        getContext().remove(property);
        logger.info("Property '" + property.getName().getValue() + "' removed from TestContext");
    }

    private void set(PropertyAction propertyAction) throws PropertyActionException {

        PropertyReference propertyRef = propertyAction.getPropertyRef();

        if (propertyRef == null || propertyRef.getValue() == null) {
            throw new PropertyActionException("No PropertyRef defined in PropertyAction SET");
        }

        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);

        if (property == null) {
            throw new PropertyActionException("PropertyAction SET: no Property found in TestContext for NameRef: '"
                    + propertyRef.getValue());
        }

        String value = propertyAction.getValue() != null ? propertyAction.getValue().getValue() : EMPTY_STRING;

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
                throw new PropertyActionException("Invalid date format in PropertyAction SET: "
                        + value + " (expected format: dd.MM.yyyy)");
            }
            break;
        case NUMERIC:
            ((NumericProperty) property).setValue(new BigDecimal(value));
            break;
        case TEXT:
            ((TextProperty) property).setValue(value);
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
            throw new PropertyActionException("Unsupported PropertyType for for PropertyAction SET: "
                    + property.getType());
        }
        logger.info("Property '" + property.getName().getValue() + "' resetted with value '" + value + "'");
    }
    
    private void split(PropertyAction propertyAction) throws PropertyActionException {
    	String source = PropertyHelper.toString(getSourceProperty(propertyAction));
        String value = (String)getValue(propertyAction, false, false);
        PropertyList target = (PropertyList) getTargetProperty(propertyAction, PropertyType.LIST);
        
        String[] split = source.split(value);
        for(int i = 0; i < split.length; i++) {
        	TextProperty prop = PropertyHelper.createTextProperty(String.valueOf(i+1), split[i]);
        	PropertyHelper.add(prop, target);
        }
	}

	private void modulo(PropertyAction propertyAction) throws PropertyActionException {
    	Property sourceProperty = getSourceProperty(propertyAction);
    	double source;
    	if(sourceProperty instanceof NumericProperty) {
    		NumericProperty num = (NumericProperty) sourceProperty;
    		source = num.getValue().getValue().doubleValue();
    	} else {
    		source = Double.valueOf(PropertyHelper.toString(sourceProperty)).doubleValue();
    	}
        double value = Double.valueOf(getValue(propertyAction, false, true));
        NumericProperty target = (NumericProperty) getTargetProperty(propertyAction, PropertyType.NUMERIC);
        target.setValue(new BigDecimal(source % value));
	}

	private void substring(PropertyAction propertyAction) throws PropertyActionException {
    	String source = PropertyHelper.toString(getSourceProperty(propertyAction));
        String value = (String)getValue(propertyAction, false, false);
        TextProperty target = (TextProperty) getTargetProperty(propertyAction, PropertyType.TEXT);
        String[] split = value.split("\\,");
        if(split.length == 2) {
        	int start = Integer.parseInt(split[0]);
        	int end = Integer.parseInt(split[1]);
        	if(start < 0) start = source.length() + start;
        	if(end < 0) end = source.length() + end;
        	String substring = source.substring(start, end);
        	target.setValue(substring);
        } else {
        	int start = Integer.parseInt(value);
        	if(start < 0) start = source.length() + start;
        	String substring = source.substring(start);
        	target.setValue(substring);
        }
		
	}

	private void trim(PropertyAction propertyAction) throws PropertyActionException {
		String source = PropertyHelper.toString(getSourceProperty(propertyAction));
        TextProperty target = (TextProperty) getTargetProperty(propertyAction, PropertyType.TEXT);
        target.setValue(source.trim());
	}

	private void divide(PropertyAction propertyAction) throws PropertyActionException {
		Property sourceProperty = getSourceProperty(propertyAction);
    	double source;
    	if(sourceProperty instanceof NumericProperty) {
    		NumericProperty num = (NumericProperty) sourceProperty;
    		source = num.getValue().getValue().doubleValue();
    	} else {
    		source = Double.valueOf(PropertyHelper.toString(sourceProperty)).doubleValue();
    	}
        double value = Double.valueOf(getValue(propertyAction, false, true));
        NumericProperty target = (NumericProperty) getTargetProperty(propertyAction, PropertyType.NUMERIC);
        target.setValue(new BigDecimal(source / value));
	}

	private void multiply(PropertyAction propertyAction) throws PropertyActionException {
		Property sourceProperty = getSourceProperty(propertyAction);
    	double source;
    	if(sourceProperty instanceof NumericProperty) {
    		NumericProperty num = (NumericProperty) sourceProperty;
    		source = num.getValue().getValue().doubleValue();
    	} else {
    		source = Double.valueOf(PropertyHelper.toString(sourceProperty)).doubleValue();
    	}
        double value = Double.valueOf(getValue(propertyAction, false, true));
        NumericProperty target = (NumericProperty) getTargetProperty(propertyAction, PropertyType.NUMERIC);
        target.setValue(new BigDecimal(source * value));
	}
	
	private Property getSourceProperty(PropertyAction propertyAction) throws PropertyActionException {
		PropertyReference propertyRef = propertyAction.getPropertyRef();
        if (propertyRef == null || propertyRef.getValue() == null) {
        	throw new PropertyActionException("No PropertyRef defined in PropertyAction " + propertyAction);
        }
        logger.debug("Resolving PropertyRef '" + propertyRef + "'");
        Property property = getContext().getProperty(propertyRef);
        if (property == null) {
        	throw new PropertyActionException("PropertyAction " + propertyAction + ": no Property found in TestContext for NameRef: '"
        			+ propertyRef.getValue());
        }
        return property;
	}
	
	private Property getTargetProperty(PropertyAction propertyAction, PropertyType type) throws PropertyActionException {
		PropertyReference targetRef = propertyAction.getTarget();
		if (targetRef == null || targetRef.getValue() == null) {
            throw new PropertyActionException("No TargetPropertyRef defined in PropertyAction " + propertyAction);
        }
        logger.debug("Resolving PropertyRef '" + targetRef + "'");
        Property target = getContext().getProperty(targetRef);

        if (target == null || target.getType() != type) {
        	String[] targetStrings = targetRef.getValue().split("\\.");
        	if(targetStrings.length > 1) {
        		PropertyList propertyList = PropertyHelper.createPropertyList(targetStrings[0]);
        		PropertyList prev = propertyList;
        		for(int i = 1; i < targetStrings.length - 1; i++) {
        			PropertyList pl = PropertyHelper.createPropertyList(targetStrings[i]);
        			PropertyHelper.add(pl, prev);
        			prev = pl;
        		}
        		target = PropertyHelper.createProperty(type, targetStrings[targetStrings.length - 1]);
        		PropertyHelper.add(target, prev);
        		getContext().merge(propertyList);
        	} else {
        		target = PropertyHelper.createProperty(type, targetRef.getValue());
        		getContext().put(target);
        	}
        }
		return target;
	}
	
	private String getValue(PropertyAction propertyAction, boolean canBeEmpty, boolean getReference) throws PropertyActionException {
		Text value = propertyAction.getValue(); 
        if (value == null || value.getValue() == null || (!canBeEmpty && value.getValue().equals(""))) {
        	throw new PropertyActionException("No Value defined in PropertyAction " + propertyAction);
        }
        if(getReference) {
        	Property val = getContext().getProperty(new Name(value.getValue()));
        	if(val != null) {
        		return PropertyHelper.toString(val);
        	}
        }
        return value.getValue();
	}

}
