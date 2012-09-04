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

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.nabucco.framework.base.facade.datatype.Data;
import org.nabucco.framework.base.facade.datatype.Identifier;
import org.nabucco.framework.base.facade.datatype.image.ImageData;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.engine.base.client.ClientInteraction;
import org.nabucco.testautomation.engine.base.client.ManualTestResultInput;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.base.util.TestResultHelper;
import org.nabucco.testautomation.engine.proxy.cache.DataCache;
import org.nabucco.testautomation.engine.proxy.cache.ImageCache;
import org.nabucco.testautomation.property.facade.datatype.base.HierarchyLevelType;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.result.facade.datatype.TestResult;
import org.nabucco.testautomation.result.facade.datatype.TestResultContainer;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualState;
import org.nabucco.testautomation.result.facade.datatype.manual.ManualTestResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;
import org.nabucco.testautomation.result.facade.datatype.trace.ActionTrace;
import org.nabucco.testautomation.result.facade.datatype.trace.FileTrace;
import org.nabucco.testautomation.result.facade.datatype.trace.MessageTrace;
import org.nabucco.testautomation.result.facade.datatype.trace.ScreenshotTrace;
import org.nabucco.testautomation.settings.facade.datatype.engine.ContextSnapshot;
import org.nabucco.testautomation.settings.facade.datatype.engine.TestConfigElementLink;
import org.nabucco.testautomation.settings.facade.exception.engine.TestEngineException;

/**
 * ManualTestConfigElementEngineImpl
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class ManualTestConfigElementEngineImpl implements TestConfigElementEngine {

    private static final long serialVersionUID = 1L;

    private static final NabuccoLogger logger = NabuccoLoggingFactory.getInstance().getLogger(
            ManualTestConfigElementEngineImpl.class);

    /**
     * Constructs a new TestConfigElementEngine instance.
     */
    ManualTestConfigElementEngineImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestResult executeTestConfigElement(TestConfigElement testConfigElement, TestContext context,
            TestResult parentResult) throws TestEngineException {

        List<TestResultContainer> parentTestResultList = parentResult.getTestResultList();

        if (parentTestResultList.isEmpty()) {
            throw new TestEngineException("TestResultList is empty");
        }

        // Create ManualResult
        TestResult orgResult = parentTestResultList.remove(parentTestResultList.size() - 1).getResult();
        ManualTestResult manualResult = TestResultHelper.createManualTestResult(orgResult);

        // Apply runtime information to ManualResult
        manualResult.setPropertyList(this.getCurrentProperties(context));
        manualResult.setContextSnapshot(this.createContextSnapshot(context));
        attachActionTraces(manualResult, parentResult);

        TestResultHelper.addTestResult(manualResult, parentResult);

        // wait for user input
        logger.info("Waiting for ManualTestResult ...");
        long start = System.currentTimeMillis();
        ClientInteraction userInput = context.getExecutionController().receiveClientInteraction();
        long end = System.currentTimeMillis();

        if (userInput != null && userInput instanceof ManualTestResultInput) {

            // Complete ManualTestResult
            ManualTestResult receivedResult = ((ManualTestResultInput) userInput).getResult();

            if (receivedResult == null) {
                String errorMessage = "No ManualTestResult received";
                manualResult.setStatus(TestConfigElementStatusType.FAILED);
                manualResult.setUserErrorMessage("No UserInput received");
                logger.error(errorMessage);
                return manualResult;
            } else if (receivedResult.getState() == ManualState.FINISHED) {
                logger.info("Input for ManualTestResult received");
            } else if (receivedResult.getState() == ManualState.ABORTED) {
                receivedResult.setStatus(TestConfigElementStatusType.FAILED);
                receivedResult.setUserErrorMessage("'"
                        + testConfigElement.getIdentificationKey() + "' aborted by user");
                context.getTestConfigurationResult().setStatus(TestConfigurationStatusType.CANCELLED);
                finalizeResult(parentResult, manualResult, start, end, receivedResult);
                logger.info("Input of ManualTestResult aborted");
                context.getExecutionController().tryInterruption();
            } else {
                receivedResult.setStatus(TestConfigElementStatusType.FAILED);
                String errorMessage = "Invalid state of ManualTestResult: " + receivedResult.getState();
                receivedResult.setErrorMessage(errorMessage);
                logger.error(errorMessage);
            }

            // Update Properties
            updateCurrentProperties(receivedResult.getPropertyList(), context);

            finalizeResult(parentResult, manualResult, start, end, receivedResult);
        } else {
            manualResult.setStatus(TestConfigElementStatusType.FAILED);
            manualResult.setUserErrorMessage("No UserInput received");
            logger.error("No UserInput received");
        }
        return manualResult;
    }

    /**
     * @param parentResult
     * @param orgResult
     * @param start
     * @param end
     * @param receivedResult
     */
    private void finalizeResult(TestResult parentResult, ManualTestResult orgResult, long start, long end,
            ManualTestResult receivedResult) {

        receivedResult.setStartTime(new Date(start));
        receivedResult.setEndTime(new Date(end));
        receivedResult.setDuration(end - start);

        // Put large data into Cache
        for (ActionTrace trace : receivedResult.getActionTraceList()) {

            if (trace instanceof ScreenshotTrace) {
                ScreenshotTrace screenshot = (ScreenshotTrace) trace;
                Identifier imageId = new Identifier(UUID.randomUUID().getMostSignificantBits());
                ImageCache.getInstance().put(imageId, screenshot.getScreenshot());
                screenshot.setImageId(imageId);
                screenshot.setScreenshot((ImageData) null);
            } else if (trace instanceof FileTrace) {
                FileTrace file = (FileTrace) trace;
                Identifier fileId = new Identifier(UUID.randomUUID().getMostSignificantBits());
                DataCache.getInstance().put(fileId, file.getFileContent());
                file.setFileId(fileId);
                file.setFileContent((Data) null);
            }
        }

        TestResultHelper.removeTestResult(orgResult, parentResult);
        TestResultHelper.addTestResult(receivedResult, parentResult);
    }

    /**
     * @param context
     * @return
     */
    private ContextSnapshot createContextSnapshot(TestContext context) {
        ContextSnapshot snapshot = new ContextSnapshot();

        // Add all Properties to Snapshot
        snapshot.getPropertyList().addAll(context.getAll());

        // Add current TestConfigElements to Snapshot
        TestConfigElement currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.ONE);

        if (currentElement != null) {
            snapshot.getCurrentTestConfigElementList().add(createTestConfigElementLink(currentElement));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.TWO);

        if (currentElement != null) {
            snapshot.getCurrentTestConfigElementList().add(createTestConfigElementLink(currentElement));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.THREE);

        if (currentElement != null) {
            snapshot.getCurrentTestConfigElementList().add(createTestConfigElementLink(currentElement));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.FOUR);

        if (currentElement != null) {
            snapshot.getCurrentTestConfigElementList().add(createTestConfigElementLink(currentElement));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.FIVE);

        if (currentElement != null) {
            snapshot.getCurrentTestConfigElementList().add(createTestConfigElementLink(currentElement));
        }

        return snapshot;
    }

    /**
     * 
     * @param context
     * @return
     */
    private ContextSnapshot getCurrentProperties(TestContext context) {

        if (context == null) {
            return null;
        }

        ContextSnapshot snapshot = new ContextSnapshot();
        TestConfigElement currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.ONE);

        if (currentElement != null && currentElement.getPropertyList() != null) {
            snapshot.getPropertyList().add(context.getProperty(currentElement.getPropertyList().getName()));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.TWO);

        if (currentElement != null && currentElement.getPropertyList() != null) {
            snapshot.getPropertyList().add(context.getProperty(currentElement.getPropertyList().getName()));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.THREE);

        if (currentElement != null && currentElement.getPropertyList() != null) {
            snapshot.getPropertyList().add(context.getProperty(currentElement.getPropertyList().getName()));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.FOUR);

        if (currentElement != null && currentElement.getPropertyList() != null) {
            snapshot.getPropertyList().add(context.getProperty(currentElement.getPropertyList().getName()));
        }

        currentElement = context.getCurrentTestConfigElement(HierarchyLevelType.FIVE);

        if (currentElement != null && currentElement.getPropertyList() != null) {
            snapshot.getPropertyList().add(context.getProperty(currentElement.getPropertyList().getName()));
        }

        return snapshot;
    }

    /**
     * 
     * @param element
     * @return
     */
    private TestConfigElementLink createTestConfigElementLink(TestConfigElement element) {

        TestConfigElementLink link = new TestConfigElementLink();
        link.setElementId(element.getId());
        link.setElementKey(element.getIdentificationKey());
        link.setElementName(element.getName());
        return link;
    }

    /**
     * 
     * @param propertyList
     * @param context
     */
    private void updateCurrentProperties(ContextSnapshot propertyList, TestContext context) {

        if (propertyList == null || propertyList.getPropertyList() == null) {
            return;
        }

        for (Property property : propertyList.getPropertyList()) {
            context.put(property);
        }
    }

    /**
     * @param manualResult
     * @param parentResult
     */
    private void attachActionTraces(ManualTestResult manualResult, TestResult parentResult) {

        // Check ManualTestResults
        if (parentResult instanceof ManualTestResult) {
            for (ActionTrace trace : ((ManualTestResult) parentResult).getActionTraceList()) {
                addActionTrace(manualResult, trace);
            }
        }

        // Search for Traces in TestScriptResults
        for (TestScriptResult scriptResult : parentResult.getTestScriptResultList()) {
            for (ActionTrace trace : scriptResult.getActionTraceList()) {
                addActionTrace(manualResult, trace);
            }
        }

        // Search for Traces in SubResults
        for (TestResultContainer container : parentResult.getTestResultList()) {
            attachActionTraces(manualResult, container.getResult());
        }
    }

    /**
     * 
     * @param manualResult
     * @param trace
     */
    private void addActionTrace(ManualTestResult manualResult, ActionTrace trace) {

        if (trace instanceof ScreenshotTrace) {
            ScreenshotTrace screenshot = (ScreenshotTrace) trace.cloneObject();
            ImageData image = ImageCache.getInstance().get(screenshot.getImageId());
            screenshot.setScreenshot(image);
            manualResult.getScreenshots().add(screenshot);
        } else if (trace instanceof FileTrace) {
            FileTrace file = (FileTrace) trace.cloneObject();
            Data fileData = DataCache.getInstance().get(file.getFileId());
            file.setFileContent(fileData);
            manualResult.getFiles().add(file);
        } else if (trace instanceof MessageTrace) {
            manualResult.getMessages().add((MessageTrace) trace);
        }
    }

}
