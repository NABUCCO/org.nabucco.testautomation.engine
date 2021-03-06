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

import org.nabucco.framework.base.facade.datatype.logger.NabuccoLogger;
import org.nabucco.framework.base.facade.datatype.logger.NabuccoLoggingFactory;
import org.nabucco.testautomation.engine.base.context.TestContext;
import org.nabucco.testautomation.engine.exception.TestScriptException;
import org.nabucco.testautomation.engine.sub.TestScriptEngine;
import org.nabucco.testautomation.property.facade.datatype.TextProperty;
import org.nabucco.testautomation.property.facade.datatype.base.Property;
import org.nabucco.testautomation.property.facade.datatype.util.PropertyHelper;
import org.nabucco.testautomation.result.facade.datatype.TestScriptResult;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TextMessage;

/**
 * LoggerVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class LoggerVisitor extends AbstractTestScriptVisitor<TestScriptResult> {

    private static final String SPACE = " ";

    private static final NabuccoLogger log = NabuccoLoggingFactory.getInstance().getLogger(LoggerVisitor.class);

    private StringBuilder logMessage;

    /**
     * Constructs a new LoggerVisitor instance using the given {@link TestContext} and
     * {@link TestScriptEngine}.
     * 
     * @param context
     *            the context
     * @param testScriptEngine
     *            the TestScriptEngine
     */
    protected LoggerVisitor(TestContext context, TestScriptEngine testScriptEngine) {
        super(context, testScriptEngine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(Logger logger, TestScriptResult argument) throws TestScriptException {
        
        getContext().setCurrentTestScriptElement(logger);
        this.logMessage = new StringBuilder();
        super.visit(logger, argument);
        TextProperty usernameProperty = (TextProperty) getContext().getProperty(TestContext.USERNAME);

        if (usernameProperty == null) {
            log.error("No username found in context");
            return;
        }

        String username = usernameProperty.getValue().getValue();
        String message = "#" + username + ": " + logMessage.toString();

        // Log into TestScriptResult
        String logging = "[" + logger.getLevel() + "] " +  message + "\r\n";

        if (argument.getLogging() == null || argument.getLogging().getValue() == null) {
            argument.setLogging(logging);
        } else {
            argument.setLogging(argument.getLogging().getValue() + logging);
        }

        // Log to NBCTestLogger
        switch (logger.getLevel()) {
        case INFO:
            log.info(message);
            break;
        case WARN:
            log.warning(message);
            break;
        case DEBUG:
            log.debug(message);
            break;
        case ERROR:
            log.error(message);
            break;
        case FATAL:
            log.fatal(message);
            break;
        default:
            log.info(message);
            break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visit(TextMessage message, TestScriptResult argument) {

        if (message.getText() != null) {
            logMessage.append(message.getText() + SPACE);
        }
        
        if (message.getPropertyRef() != null) {
            Property prop = getContext().getProperty(message.getPropertyRef());

            if (prop != null) {
                String str = PropertyHelper.toString(prop);

                if (str == null) {
                    str = "null";
                }
                logMessage.append(str + SPACE);
            }
        }
    }

}
