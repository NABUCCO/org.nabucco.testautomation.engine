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
package org.nabucco.testautomation.engine.sub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.exception.InterruptionException;
import org.nabucco.testautomation.engine.base.exception.NBCTestConfigurationException;
import org.nabucco.testautomation.engine.proxy.ProxyEngine;
import org.nabucco.testautomation.engine.proxy.SubEngine;
import org.nabucco.testautomation.engine.proxy.SubEngineActionType;
import org.nabucco.testautomation.engine.proxy.SubEngineOperationType;
import org.nabucco.testautomation.engine.proxy.pool.ProxyEnginePool;
import org.nabucco.testautomation.engine.proxy.pool.ProxyPoolFactory;
import org.nabucco.testautomation.property.facade.datatype.PropertyList;
import org.nabucco.testautomation.result.facade.datatype.ActionResponse;
import org.nabucco.testautomation.script.facade.datatype.code.SubEngineActionCode;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;
import org.nabucco.testautomation.settings.facade.datatype.engine.SubEngineType;

/**
 * SubEngineInvoker
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class SubEngineInvoker {

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(SubEngineInvoker.class);

    private static final String OPERATION_NAME = "execute";

    private List<Metadata> metadata;

    private TestContext context;

    private PropertyList propertyList;

    private ActionResponse result;

    private SubEngine subEngine;

    private SubEngineActionType actionType;

    private SubEngineOperationType operationType;

    /**
     * Constructs a new instance of a SubEngineInvoker for a certain {@link Metadata} object (the
     * last in the list) and {@link SubEngineActionType}.
     * 
     * @param proxyPool
     *            the proxy pool with the configured SubEngine proxies
     * @param metadata
     *            the list of metadata. The last metadata is the element to be invoked.
     * @param context
     *            the context
     * @param properties
     *            a list of parameter for the operation call
     * @param actionType
     *            the type of action to be executed
     * @throws NBCTestConfigurationException
     *             thrown, if an validation error occurs during the setup
     */
    public SubEngineInvoker(Metadata metadata, TestContext context, PropertyList properties, SubEngineActionCode action)
            throws NBCTestConfigurationException {
        this.metadata = resolveMetadataList(metadata);
        this.context = context;
        this.propertyList = properties;

        if (this.propertyList == null) {
            this.propertyList = new PropertyList();
        }

        if (this.metadata == null || this.metadata.isEmpty()) {
            throw new NBCTestConfigurationException("No Metadata found for invocation");
        }
        if (action == null || action.getCode() == null || action.getCode().getValue() == null) {
            throw new NBCTestConfigurationException("No Action selected for invocation of Metadata '"
                    + metadata.getName().getValue() + "'");
        }

        // Get last Metadata for invoke
        Metadata invokeMetadata = this.metadata.get(this.metadata.size() - 1);

        if (invokeMetadata == null
                || invokeMetadata.getSubEngine() == null || invokeMetadata.getSubEngine().getCode() == null
                || invokeMetadata.getOperation() == null || invokeMetadata.getOperation().getCode() == null) {
            throw new NBCTestConfigurationException("Metadata not configured properly for execution of SubEngine");
        }

        // Get the ProxyEngine from the ProxyEnginePool
        SubEngineType subEngineType = getSubEngineType(invokeMetadata.getSubEngine().getCode().getValue());
        ProxyEnginePool proxyEnginePool = ProxyPoolFactory.getInstance().getProxyEnginePool();
        ProxyEngine proxyEngine = proxyEnginePool.getProxyEngine(subEngineType);

        // Get the SubEngine from the ProxyEngine
        this.subEngine = proxyEngine.getSubEngine();

        if (this.subEngine == null) {
            throw new NBCTestConfigurationException(
                    "No SubEngine received from ProxyEngine. Please check configuration of "
                            + proxyEngine.getSubEngineType() + "-Proxy");
        }

        this.actionType = getActionType(action.getCode().getValue());
        this.operationType = getOperationType(invokeMetadata.getOperation().getCode().getValue());
    }

    /**
     * Gets the result of the execution.
     * 
     * @return the execution result
     */
    public ActionResponse getResponse() {
        return result;
    }

    /**
     * Actually invokes the configured SubEngine-operation.
     * 
     * @throws NBCTestConfigurationException
     *             thrown, if an error occurs during the preparation or execution of the
     *             SubEngine-operation
     */
    public void invoke() throws NBCTestConfigurationException {
        logger.debug("SubEngine selected: ", subEngine.getClass().getName());
        Method method = getExecuteMethod(subEngine.getClass());

        try {
            logger.debug("invoking ", method.getName(), " for SubEngineOperationType " + operationType);

            Object resultObj = method
                    .invoke(subEngine, operationType, actionType, this.metadata, propertyList, context);

            if (resultObj == null) {
                throw new NBCTestConfigurationException("received ResultObject is null");
            } else if (resultObj instanceof ActionResponse) {
                this.result = (ActionResponse) resultObj;
                logger.debug("TestResult received: ", this.result.toString());
            } else {
                throw new NBCTestConfigurationException("No TestResult received ! received class: "
                        + resultObj.getClass().toString());
            }
        } catch (InterruptionException ex) {
            throw ex;
        } catch (InvocationTargetException ex) {

            Throwable t = ex.getTargetException();

            // Action was interrupted
            if (t != null && t instanceof InterruptionException) {
                throw (InterruptionException) t;
            }

            String msg = "Could not invoke operation '" + method.getName() + "'";

            if (t != null && t instanceof Exception) {
                msg += ": " + t.toString();
                logger.error(msg);
                logger.fatal((Exception) t);
            }

            throw new NBCTestConfigurationException(msg, ex);
        } catch (Exception ex) {
            throw new NBCTestConfigurationException("Could not invoke operation '" + method.getName() + "'", ex);
        }
    }

    private SubEngineType getSubEngineType(String subEngine) {

        if (subEngine == null) {
            return null;
        }
        return SubEngineType.valueOf(subEngine.toUpperCase());
    }

    /**
     * Gets the matching {@link SubEngineActionType} for a given string.
     * 
     * @param action
     *            action string
     * @return the matching SubEngineActionType
     */
    private SubEngineActionType getActionType(String action) throws NBCTestConfigurationException {
        SubEngineActionType actionType = this.subEngine.getActions().get(action.toUpperCase());

        if (actionType == null) {
            throw new NBCTestConfigurationException("No SubEngineActionType found for Code '" + action + "'");
        }
        return actionType;
    }

    /**
     * Gets the matching {@link SubEngineOperationType} for a given string.
     * 
     * @param operation
     *            operation string
     * @return the matching SubEngineOperationType
     */
    private SubEngineOperationType getOperationType(String operation) throws NBCTestConfigurationException {
        SubEngineOperationType operationType = this.subEngine.getOperations().get(operation);

        if (operationType == null) {
            throw new NBCTestConfigurationException("No SubEngineOperationType found for Code '" + operation + "'");
        }
        return operationType;
    }

    /**
     * Gets the execute method from the given class instance.
     * 
     * @param clazz
     *            the class
     * @return the method object
     * @throws NBCTestConfigurationException
     *             thrown, if no execution-method exists in the given class
     */
    private Method getExecuteMethod(Class<?> clazz) throws NBCTestConfigurationException {

        Method[] methods = clazz.getMethods();

        for (Method m : methods) {
            if (m.getName().equals(OPERATION_NAME)) {
                return m;
            }
        }
        throw new NBCTestConfigurationException("'" + OPERATION_NAME + "' operation not found in " + clazz.getName());
    }

    private List<Metadata> resolveMetadataList(Metadata metadata) {

        List<Metadata> list = new ArrayList<Metadata>();
        Metadata parent = metadata;

        while (parent != null) {
            list.add(parent);
            parent = parent.getParent();
        }
        Collections.reverse(list);
        return list;
    }

}
