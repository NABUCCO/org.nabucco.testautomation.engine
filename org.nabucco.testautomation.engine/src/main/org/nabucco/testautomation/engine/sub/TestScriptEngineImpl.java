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
package org.nabucco.testautomation.engine.sub;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.nabucco.framework.base.facade.datatype.Duration;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.base.exception.PropertyException;
import org.nabucco.testautomation.engine.base.logging.NBCTestLogger;
import org.nabucco.testautomation.engine.base.logging.NBCTestLoggingFactory;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.exception.AssertionException;
import org.nabucco.testautomation.engine.exception.SynchronizationException;
import org.nabucco.testautomation.engine.exception.TestExecutionAssertionException;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.engine.visitor.result.TestResultStatusVisitor;
import org.nabucco.testautomation.engine.visitor.script.TestScriptVisitor;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestScriptContainer;
import org.nabucco.testautomation.config.facade.datatype.comparator.TestScriptSorter;
import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.DoubleProperty;
import org.nabucco.testautomation.facade.datatype.property.FileProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.LongProperty;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.XPathProperty;
import org.nabucco.testautomation.facade.datatype.property.XmlProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyContainer;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.status.ActionStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestScriptStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;

/**
 * TestScriptEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class TestScriptEngineImpl implements TestScriptEngine {

	private static final long serialVersionUID = 1L;

	private static final NBCTestLogger logger = NBCTestLoggingFactory
			.getInstance().getLogger(TestScriptEngineImpl.class);
	
	private final Lock lock = new ReentrantLock();
	
	private final Condition delay = lock.newCondition();


	/**
	 * Constructs a new TestScriptEngine instance using the given
	 * {@link ProxyEnginePoolEntry} for SubEngine-calls.
	 */
	public TestScriptEngineImpl() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeTestScript(TestScript testScript, TestContext context, TestResult testResult) throws TestScriptException {
		context.setCurrentTestScript(testScript);
	    
		TestScriptResult testScriptResult = TestResultHelper.createTestScriptResult();
	    testScriptResult.setTestScriptName(testScript.getName());
	    testScriptResult.setTestScriptKey(testScript.getIdentificationKey());
	    testScriptResult.setElementId(testScript.getId());
	    testResult.getTestScriptResultList().add(testScriptResult);
	    
	    // Add PropertyList of TestScript to Context
	    PropertyList testScriptProperties = testScript.getPropertyList();
	    
		if (testScriptProperties != null) {
	    	context.put(testScriptProperties.cloneObject());
	    }
	    
	    long startTime = 0;
	    long endTime = 0;
	    
	    try {
	    	TestScriptVisitor visitor = new TestScriptVisitor(context, this);
	    	startTime = System.currentTimeMillis();
			testScriptResult.setStartTime(new Date(startTime));
			visitor.visit(testScript, testScriptResult);
			endTime = System.currentTimeMillis();
			testScriptResult.setElementStatus(TestScriptElementStatusType.EXECUTED);
			testScriptResult.setEndTime(new Date(endTime));
			testScriptResult.setDuration(endTime - startTime);
		} catch (TestScriptException ex) {
			endTime = System.currentTimeMillis();
			testScriptResult.setEndTime(new Date(endTime));
			testScriptResult.setDuration(endTime - startTime);
			testScriptResult.setErrorMessage(ex.getMessage());
			testScriptResult.setStatus(TestScriptStatusType.FAILED);
			throw ex;
		} catch (InterruptionException ex) {
			endTime = System.currentTimeMillis();
			testScriptResult.setEndTime(new Date(endTime));
			testScriptResult.setDuration(endTime - startTime);
			testScriptResult.setErrorMessage("Execution of TestScript aborted");
			testScriptResult.setStatus(TestScriptStatusType.ABORTED);
			throw ex;
		} catch (RuntimeException ex) {
			endTime = System.currentTimeMillis();
			testScriptResult.setEndTime(new Date(endTime));
			testScriptResult.setDuration(endTime - startTime);
			testScriptResult.setErrorMessage("Fatal error: " + ex.toString());
			testScriptResult.setStatus(TestScriptStatusType.FAILED);
			throw ex;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeTestScriptList(List<TestScriptContainer> testScripts, TestContext context, TestResult testResult,
            TestConfigElement parentElement) {
		Collections.sort(testScripts, new TestScriptSorter());
		
		// loop over all TestScripts of one TestStep
		scriptLoop: for (TestScriptContainer testScriptContainer : testScripts) {
			TestScript testScript = testScriptContainer.getTestScript();

			try {
				executeTestScript(testScript, context, testResult);
				TestResultStatusVisitor visitor = new TestResultStatusVisitor();
				visitor.visit(testResult);
				
				if (testResult.getStatus() == TestConfigElementStatusType.FAILED) {
					logger.info("TestScript '", testScript.getIdentificationKey().getValue(), "' failed");
					break scriptLoop;
				}
			} catch (TestExecutionAssertionException ex) {
				// break assertion failed
				testResult.setErrorMessage(ex.getMessage());
				testResult.setStatus(TestConfigElementStatusType.FAILED);
				logger.info("TestExecutionAssertion: TestResult set to FAILED", " - " , ex.getMessage());
				break scriptLoop;
			} catch (AssertionException ex) {
                // Assertion failed
                testResult.setErrorMessage(ex.getMessage());
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.info("Assertion: TestResult set to FAILED", " - " , ex.getMessage());
                break scriptLoop;
            } catch (SynchronizationException ex) {
				// Lock could not be acquired
			    testResult.setErrorMessage(ex.getMessage());
                testResult.setStatus(TestConfigElementStatusType.FAILED);
				logger.error(ex.getMessage());
				break scriptLoop;
			} catch (TestScriptException ex) {
			    // Error in TestScript
				String error = "Execution of TestScript '"
						+ testScript.getIdentificationKey().getValue() + "' failed. Cause: " + ex.getMessage();
				testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.error(error);
                break scriptLoop;
            } catch (InterruptionException ex) {
            	// TestScript interrupted
            	String error = "Execution of TestScript '"
						+ testScript.getIdentificationKey().getValue() + "' aborted";
            	testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.error(error);
                throw ex;
            } catch (Exception ex) {
			    // Unexpected error
				String error = "Execution of TestScript '"
						+ testScript.getIdentificationKey().getValue() + "' failed. Cause: " + ex.toString();
				testResult.setErrorMessage(error);
                testResult.setStatus(TestConfigElementStatusType.FAILED);
                logger.fatal(ex, error);
                break scriptLoop;
            }
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ActionResponse executeAction(Action action, TestContext context) {
	    
		ActionResponse response = null;
		boolean trace = action.getTrace() != null
				&& action.getTrace().getValue() != null
				&& action.getTrace().getValue().booleanValue();
		context.setTracingEnabled(trace);
	    PropertyList actionProperties = action.getPropertyList();
	    
		// Invoke SubEngine
		try {
			resolvePropertyRefs(actionProperties, context);
			SubEngineInvoker subEngineInvoker = new SubEngineInvoker(action.getMetadata(), context, actionProperties,
					action.getActionCode());
			subEngineInvoker.invoke();
			response = subEngineInvoker.getResponse();
			response.setElementId(action.getId());
			
			// Delay of Action
			delayAction(action);
			
		} catch (InterruptionException ex) {
            throw ex;
        } catch (PropertyException ex) {
			response = TestResultHelper.createActionResponse();
			response.setActionStatus(ActionStatusType.FAILED);
			response.setErrorMessage("PropertyException before invoking SubEngine: " + ex.getMessage());
			logger.error(ex);
		} catch (NBCTestConfigurationException ex) {
			response = TestResultHelper.createActionResponse();
			response.setActionStatus(ActionStatusType.FAILED);
			response.setErrorMessage("ConfigurationError while invoking SubEngine: " + ex.getMessage());
			logger.error(ex);
		} catch (Exception ex) {
			response = TestResultHelper.createActionResponse();
			response.setActionStatus(ActionStatusType.FAILED);
			response.setErrorMessage("Unexpected error while invoking SubEngine: " + ex.toString());
			logger.fatal(ex);
		}
		return response;
	}
	
	private void resolvePropertyRefs(PropertyList props, TestContext ctx) throws PropertyException {
		
		if (props == null) {
			return;
		}
		
		for (PropertyContainer container : props.getPropertyList()) {
			Property prop = container.getProperty();

			if (prop != null && prop.getReference() != null
					&& prop.getReference().getValue() != null
					&& !prop.getReference().getValue().equals("")) {
				
				String nameRef = prop.getReference().getValue();
				Property refProperty = ctx.getProperty(nameRef);
				
				if (refProperty == null) {
					throw new PropertyException("PropertyReference '" + nameRef + "' could not be resolved");
				}
				
				if (prop.getType() != refProperty.getType()) {
					throw new PropertyException("Type mismatch of '"
							+ prop.getName().getValue() + "' and '"
							+ refProperty.getName().getValue() + "': "
							+ prop.getType() + " != " + refProperty.getType());
				}

				try {
					switch (prop.getType()) {
					case BOOLEAN:
						BooleanProperty booleanProp = (BooleanProperty) prop;
						booleanProp.setValue(((BooleanProperty) refProperty).getValue());
						break;
					case DATE:
						DateProperty dateProp = (DateProperty) prop;
						dateProp.setValue(((DateProperty) refProperty).getValue());
						break;
					case DOUBLE:
						DoubleProperty doubleProp = (DoubleProperty) prop;
						doubleProp.setValue(((DoubleProperty) refProperty).getValue());
						break;
					case INTEGER:
						IntegerProperty integerProp = (IntegerProperty) prop;
						integerProp.setValue(((IntegerProperty) refProperty).getValue());
						break;
					case LONG:
						LongProperty longProp = (LongProperty) prop;
						longProp.setValue(((LongProperty) refProperty).getValue());
						break;
					case STRING:
						StringProperty stringProp = (StringProperty) prop;
						stringProp.setValue(((StringProperty) refProperty).getValue());
						break;
					case XML:
						XmlProperty xmlProp = (XmlProperty) prop;
						xmlProp.setValue(((XmlProperty) refProperty).getValue());
						break;
					case XPATH:
						XPathProperty xpathProp = (XPathProperty) prop;
						xpathProp.setValue(((XPathProperty) refProperty).getValue());
						break;
					case FILE:
						FileProperty fileProp = (FileProperty) prop;
						fileProp.setContent(((FileProperty) refProperty).getContent());
						break;
					case LIST:
						PropertyList list = (PropertyList) prop;
						list.getPropertyList().clear();
						list.getPropertyList().addAll(((PropertyList) refProperty).getPropertyList());
						break;
					default:
						throw new PropertyException("Unsupported PropertyType referenced in resolvePropertyRefs: "
								+ prop.getType());
					}
				} catch (ClassCastException ex) {
					throw new PropertyException("Invalid PropertyType found in resolvePropertyRefs: "
							+ (prop != null ? prop.getClass() : "null")
							+ " -> "
							+ (refProperty != null ? refProperty.getClass()
									: "null"));
				}

			}
		}
	}
	
	private void delayAction(Action action) {
		
		Duration actionDelay = action.getDelay();
		
		if (actionDelay != null && actionDelay.getValue() != null) {
			try {
				lock.lock();
				this.delay.await(actionDelay.getValue(), TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new InterruptionException("Delay was unexpectedly interrupted");
			} finally {
				lock.unlock();
			}
		}
	}
	
}
