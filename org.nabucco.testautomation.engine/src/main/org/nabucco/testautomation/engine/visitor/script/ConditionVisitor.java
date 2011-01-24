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

import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.PropertyHelper;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;

import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.LongProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Assertion;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Condition;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Foreach;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Lock;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.PropertyAction;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.ConditionType;

/**
 * ConditionVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ConditionVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

	private static final String EMPTY_STRING = "";

	private static final NBCTestLogger logger = NBCTestLoggingFactory.getInstance().getLogger(
			ConditionVisitor.class);
	
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
        String propertyName = null;
        String value = null;
        
        StringBuilder sb = new StringBuilder();
        sb.append("[Condition] property = ");
        
		if (condition.getPropertyRef() != null
				&& condition.getPropertyRef().getValue() != null
				&& !condition.getPropertyRef().getValue().equals(EMPTY_STRING)) {
			propertyName = condition.getPropertyRef().getValue();
			sb.append("'" + propertyName + "' ");
		} else {
			sb.append("null ");
		}
        
		sb.append(conditionType);
        
        switch (conditionType) {
        	
        case EQUALS:
        case NOT_EQUALS:
        case GT:
        case GTE:
        case LT:
        case LTE: { 
        	
			if (condition.getValueRef() != null
					&& condition.getValueRef().getValue() != null
					&& !condition.getValueRef().getValue().equals(EMPTY_STRING)) {
				String valueRef = condition.getValueRef().getValue();
				sb.append(" valueref = '" + valueRef + "'");
				Property valueProp = getContext().getProperty(valueRef);
	
				if (valueProp == null) {
					logger.warning("No Property found for valueref '" + valueRef
							+ "'");
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
        conditionFulfilled = checkCondition(propertyName, value, conditionType);

        if (conditionFulfilled) {
            super.visit(condition, argument);
        }
    }
	
    /**
     * Evaluates the given {@link Condition} whether it's true or false.
     * 
     * @param condition the Condition to be checked
     * @return true or false
     */
    protected boolean checkCondition(String propertyName, String value, ConditionType conditionType) {
        
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
        default:
            logger.error("ConditionType not supported: " + conditionType);
            break;
        }

        if (conditionFulfilled) {
            logger.info("Condition fulfilled: "
                    + PropertyHelper.toString(prop)
                    + " "
                    + conditionType
                    + " "
                    + value);
        } else {
            logger.info("Condition NOT fulfilled: "
                    + PropertyHelper.toString(prop)
                    + " "
                    + conditionType
                    + " "
                    + value);
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

	private boolean checkEquals(Property prop, String expected) {
	    
	    if (prop == null) {
	        if (expected == null) {
	            return true;
	        } else {
	            return false;
	        }
	    }	    
		Object value = PropertyHelper.getValue(prop);
		
		if (value == null) {
			if (expected == null) {
	            return true;
	        } else {
	            return false;
	        }
		}
		return value.toString().equals(expected);
	}
	
	private boolean checkNotEquals(Property prop, String expected) {
		
	    if (prop == null) {
            if (expected == null) {
                return false;
            } else {
                return true;
            }
        }	    
	    Object value = PropertyHelper.getValue(prop);
	    
	    if (value == null) {
	    	if (expected == null) {
                return false;
            } else {
                return true;
            }
	    }
		return !value.toString().equals(expected);
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
		case INTEGER:
			int intValue = ((IntegerProperty) prop).getValue().getValue();
			return intValue > Integer.parseInt(expected);
		case LONG:
			long longValue = ((LongProperty) prop).getValue().getValue();
			return longValue > Long.parseLong(expected);
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
		case INTEGER:
			int intValue = ((IntegerProperty) prop).getValue().getValue();
			return intValue >= Integer.parseInt(expected);
		case LONG:
			long longValue = ((LongProperty) prop).getValue().getValue();
			return longValue >= Long.parseLong(expected);
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
		case INTEGER:
			int intValue = ((IntegerProperty) prop).getValue().getValue();
			return intValue < Integer.parseInt(expected);
		case LONG:
			long longValue = ((LongProperty) prop).getValue().getValue();
			return longValue < Long.parseLong(expected);
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
		case INTEGER:
			int intValue = ((IntegerProperty) prop).getValue().getValue();
			return intValue <= Integer.parseInt(expected);
		case LONG:
			long longValue = ((LongProperty) prop).getValue().getValue();
			return longValue <= Long.parseLong(expected);
		case LIST:
			int size = (Integer) PropertyHelper.getValue(prop);
			return size <= Integer.parseInt(expected);
		default:
			logger.error("Cannot check 'LTE' for PropertyType " + prop.getType());
			break;
		}
		return false;
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
    public void visit(Lock lock, TestScriptResult argument) throws TestScriptException {
        new LockVisitor(getContext(), getTestScriptEngine()).visit(lock, argument);
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TestScript testScript, TestScriptResult argument, boolean subTestScript) throws TestScriptException {
    	new SubTestScriptVisitor(getContext(), getTestScriptEngine()).visit(testScript, argument);
    }
    
    public boolean getResult() {
        return this.conditionFulfilled;
    }
	
}
