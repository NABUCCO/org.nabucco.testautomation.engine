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
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.nabucco.framework.base.facade.datatype.Amount;
import org.nabucco.framework.base.facade.datatype.Flag;
import org.nabucco.framework.base.facade.datatype.date.Date;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.BooleanProperty;
import org.nabucco.testautomation.property.facade.datatype.DateProperty;
import org.nabucco.testautomation.property.facade.datatype.NumericProperty;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.base.PropertyReference;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Function;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.ConditionType;

/**
 * ConditionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ConditionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final String EMPTY_STRING = "";

    private static final String DATE_ISO_REVERSE = "yyyy-MM-dd";

    private static final String DATE_ISO = "dd-MM-yyyy";

    private static final String DATE_DIN_REVERSE = "yyyy.MM.dd";

    private static final String DATE_DIN = "dd.MM.yyyy";

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(ConditionVisitor.class);

    private boolean conditionFulfilled;

    /**
     * Constructs a new ConditionVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
    protected ConditionVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Condition condition, TestScriptResult argument) throws TestScriptException {
        getContext().setCurrentTestScriptElement(condition);

        ConditionType conditionType = condition.getConditionType();
        PropertyReference propertyName = null;
        String value = null;

        StringBuilder sb = new StringBuilder();
        sb.append("[Condition] property = ");

        if (condition.getPropertyRef() != null
                && condition.getPropertyRef().getValue() != null
                && !condition.getPropertyRef().getValue().equals(EMPTY_STRING)) {
            propertyName = condition.getPropertyRef();
            sb.append("'" + propertyName + "' ");
        } else {
            sb.append("null ");
        }

        sb.append(conditionType);

        switch (conditionType) {

        // conditions requiring a value
        case EQUALS:
        case NOT_EQUALS:
        case GT:
        case GTE:
        case LT:
        case LTE:
        case STARTS_WITH:
        case ENDS_WITH:
        case CONTAINS:
        case NOT_STARTS_WITH:
        case NOT_ENDS_WITH:
        case NOT_CONTAINS: {

            if (condition.getValueRef() != null
                    && condition.getValueRef().getValue() != null
                    && !condition.getValueRef().getValue().equals(EMPTY_STRING)) {
                PropertyReference valueRef = condition.getValueRef();
                sb.append(" valueref = '" + valueRef + "'");
                Property valueProp = getContext().getProperty(valueRef);

                if (valueProp == null) {
                    logger.warning("No Property found for valueref '" + valueRef + "'");
                } else {
                    value = PropertyHelper.toString(valueProp);
                }
            } else if (condition.getValue() != null) {
                value = condition.getValue().getValue();
                sb.append(" value = '" + value + "'");
            } else {
                sb.append(" no value defined");
            }
        }
        }

        logger.info(sb.toString());
        this.conditionFulfilled = checkCondition(propertyName, value, conditionType);

        if (this.conditionFulfilled) {
            super.visit(condition, argument);
        }
    }

    /**
     * Evaluates the given {@link Condition} whether it's true or false.
     * 
     * @param condition
     *            the Condition to be checked
     * @return true or false
     */
    protected boolean checkCondition(PropertyReference propertyName, String value, ConditionType conditionType) {

        Property prop = getContext().getProperty(propertyName);
        boolean conditionFulfilled = false;

        switch (conditionType) {
        case EQUALS:
            conditionFulfilled = checkEquals(prop, value);
            break;
        case FALSE:
            conditionFulfilled = checkBoolean(prop, Boolean.FALSE);
            break;
        case GT:
            conditionFulfilled = checkGreaterThan(prop, value);
            break;
        case GTE:
            conditionFulfilled = checkGreaterThanEquals(prop, value);
            break;
        case LT:
            conditionFulfilled = checkLowerThan(prop, value);
            break;
        case LTE:
            conditionFulfilled = checkLowerThanEquals(prop, value);
            break;
        case NOT_EQUALS:
            conditionFulfilled = checkNotEquals(prop, value);
            break;
        case TRUE:
            conditionFulfilled = checkBoolean(prop, Boolean.TRUE);
            break;
        case IS_EMPTY:
            conditionFulfilled = checkIsEmpty(prop);
            break;
        case IS_NULL:
            conditionFulfilled = checkIsNull(prop);
            break;
        case NOT_EMPTY:
            conditionFulfilled = checkNotEmpty(prop);
            break;
        case NOT_NULL:
            conditionFulfilled = checkNotNull(prop);
            break;
        case STARTS_WITH:
            conditionFulfilled = checkStartsWith(prop, value);
            break;
        case ENDS_WITH:
            conditionFulfilled = checkEndsWith(prop, value);
            break;
        case CONTAINS:
            conditionFulfilled = checkContains(prop, value);
            break;
        case NOT_STARTS_WITH:
            conditionFulfilled = !checkStartsWith(prop, value);
            break;
        case NOT_ENDS_WITH:
            conditionFulfilled = !checkEndsWith(prop, value);
            break;
        case NOT_CONTAINS:
            conditionFulfilled = !checkContains(prop, value);
            break;
        default:
            logger.error("ConditionType not supported: " + conditionType);
            break;
        }

        if (conditionFulfilled) {
            logger.info("Condition fulfilled: " + PropertyHelper.toString(prop) + " " + conditionType + " " + value);
        } else {
            logger.info("Condition NOT fulfilled: " + PropertyHelper.toString(prop) + " " + conditionType + " " + value);
        }
        return conditionFulfilled;
    }

    private boolean checkNotNull(Property prop) {
        return prop != null;
    }

    private boolean checkNotEmpty(Property prop) {
        String value = PropertyHelper.toString(prop);
        return value != null && !value.equals(EMPTY_STRING);
    }

    private boolean checkIsNull(Property prop) {
        return prop == null;
    }

    private boolean checkIsEmpty(Property prop) {
        String value = PropertyHelper.toString(prop);
        return value == null || value.equals(EMPTY_STRING);
    }

    private boolean checkEquals(Property property, String expected) {

        if (property == null) {
            if (expected == null) {
                return true;
            } else {
                return false;
            }
        }

        Object valueObject = PropertyHelper.getValue(property);
        if (valueObject == null) {
            if (expected == null) {
                return true;
            } else {
                return false;
            }
        }

        switch (property.getType()) {

        case BOOLEAN: {
            Flag value = ((BooleanProperty) property).getValue();
            return value.getValue().equals(Boolean.valueOf(expected));
        }
        case DATE: {
            Date value = ((DateProperty) property).getValue();
            return value.getValue().equals(this.parseDateSave(expected));
        }
        case NUMERIC: {
            Amount value = ((NumericProperty) property).getValue();
            return value.getValue().equals(new BigDecimal(expected));
        }
        default: {
            return String.valueOf(valueObject).equals(expected);
        }
        }

    }

    private boolean checkNotEquals(Property property, String expected) {
        return !this.checkEquals(property, expected);
    }

    /**
     * Try to parse the date for multiple patterns
     * <ul>
     * <li>dd.MM.yyyy</li>
     * <li>yyyy.MM.dd</li>
     * <li>dd-MM-yyyy</li>
     * <li>yyyy-MM-dd</li>
     * </ul>
     * 
     * @param value
     *            the date string to parse
     * 
     * @return the parsed date, or null if the date is not parsable
     */
    private java.util.Date parseDateSave(String value) {
        java.util.Date date = null;
        date = this.parseDate(value, DATE_DIN);
        if (date != null) {
            return date;
        }
        date = this.parseDate(value, DATE_DIN_REVERSE);
        if (date != null) {
            return date;
        }
        date = this.parseDate(value, DATE_ISO);
        if (date != null) {
            return date;
        }
        date = this.parseDate(value, DATE_ISO_REVERSE);
        if (date != null) {
            return date;
        }
        return date;
    }

    /**
     * Parse the date for the given pattern.
     * 
     * @param value
     *            the date value as string
     * @param pattern
     *            the date pattern to parse for
     * 
     * @return the parsed date
     */
    private java.util.Date parseDate(String value, String pattern) {
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean checkBoolean(Property prop, Boolean expected) {

        if (prop == null) {

            if (expected == null) {
                return true;
            } else {
                return false;
            }
        }

        switch (prop.getType()) {
        case BOOLEAN:
            return ((BooleanProperty) prop).getValue().getValue().equals(expected);
        case TEXT:
            return Boolean.valueOf(((TextProperty) prop).getValue().getValue()).equals(expected);
        default:
            logger.error("Cannot check 'false' for PropertyType " + prop.getType());
            return false;
        }
    }

    private boolean checkGreaterThan(Property prop, String expected) {

        if (prop == null) {
            return false;
        }
        switch (prop.getType()) {
        case DATE:
            long dateValue = ((DateProperty) prop).getValue().getValue().getTime();
            return dateValue > Long.parseLong(expected);
        case NUMERIC:
            Amount value = ((NumericProperty) prop).getValue();
            return value.getValue().compareTo(new BigDecimal(expected)) > 0;
        case LIST:
            int size = (Integer) PropertyHelper.getValue(prop);
            return size > Integer.parseInt(expected);
        default:
            logger.error("Cannot check 'GT' for PropertyType " + prop.getType());
            break;
        }
        return false;
    }

    private boolean checkGreaterThanEquals(Property prop, String expected) {

        if (prop == null) {
            if (expected == null) {
                return true;
            } else {
                return false;
            }
        }

        switch (prop.getType()) {
        case DATE:
            long dateValue = ((DateProperty) prop).getValue().getValue().getTime();
            return dateValue >= Long.parseLong(expected);
        case NUMERIC:
            Amount value = ((NumericProperty) prop).getValue();
            return value.getValue().compareTo(new BigDecimal(expected)) >= 0;
        case LIST:
            int size = (Integer) PropertyHelper.getValue(prop);
            return size >= Integer.parseInt(expected);
        default:
            logger.error("Cannot check 'GTE' for PropertyType " + prop.getType());
            break;
        }
        return false;
    }

    private boolean checkLowerThan(Property prop, String expected) {

        if (prop == null) {
            return false;
        }

        switch (prop.getType()) {
        case DATE:
            long dateValue = ((DateProperty) prop).getValue().getValue().getTime();
            return dateValue < Long.parseLong(expected);
        case NUMERIC:
            Amount value = ((NumericProperty) prop).getValue();
            return value.getValue().compareTo(new BigDecimal(expected)) < 0;
        case LIST:
            int size = (Integer) PropertyHelper.getValue(prop);
            return size < Integer.parseInt(expected);
        default:
            logger.error("Cannot check 'LT' for PropertyType " + prop.getType());
            break;
        }
        return false;
    }

    private boolean checkLowerThanEquals(Property prop, String expected) {

        if (prop == null) {
            if (expected == null) {
                return true;
            } else {
                return false;
            }
        }

        switch (prop.getType()) {
        case DATE:
            long dateValue = ((DateProperty) prop).getValue().getValue().getTime();
            return dateValue <= Long.parseLong(expected);
        case NUMERIC:
            Amount value = ((NumericProperty) prop).getValue();
            return value.getValue().compareTo(new BigDecimal(expected)) <= 0;
        case LIST:
            int size = (Integer) PropertyHelper.getValue(prop);
            return size <= Integer.parseInt(expected);
        default:
            logger.error("Cannot check 'LTE' for PropertyType " + prop.getType());
            break;
        }
        return false;
    }

    private boolean checkStartsWith(Property prop, String value) {

        if (prop == null || value == null) {
            return false;
        }

        return PropertyHelper.toString(prop).startsWith(value);
    }

    private boolean checkEndsWith(Property prop, String value) {

        if (prop == null || value == null) {
            return false;
        }

        return PropertyHelper.toString(prop).endsWith(value);
    }

    private boolean checkContains(Property prop, String value) {

        if (prop == null || value == null) {
            return false;
        }

        return PropertyHelper.toString(prop).contains(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Loop loop, TestScriptResult argument) throws TestScriptException {
        new LoopVisitor(getContext(), getTestScriptEngine()).visit(loop, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Logger logger, TestScriptResult argument) throws TestScriptException {
        new LoggerVisitor(getContext(), getTestScriptEngine()).visit(logger, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Execution execution, TestScriptResult argument) throws TestScriptException {
        new ExecutionVisitor(getContext(), getTestScriptEngine()).visit(execution, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Foreach foreach, TestScriptResult argument) throws TestScriptException {
        new ForeachVisitor(getContext(), getTestScriptEngine()).visit(foreach, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Function function, TestScriptResult argument) throws TestScriptException {
        new FunctionVisitor(getContext(), getTestScriptEngine()).visit(function, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Assertion assertion, TestScriptResult argument) throws TestScriptException {
        new AssertionVisitor(getContext(), getTestScriptEngine()).visit(assertion, argument);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(PropertyAction propertyAction, TestScriptResult argument) throws TestScriptException {
        new PropertyActionVisitor(getContext(), getTestScriptEngine()).visit(propertyAction, argument);
    }

    public boolean getResult() {
        return this.conditionFulfilled;
    }

}
