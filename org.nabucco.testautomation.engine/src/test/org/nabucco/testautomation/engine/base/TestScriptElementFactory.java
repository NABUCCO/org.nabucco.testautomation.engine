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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.nabucco.framework.base.facade.datatype.DatatypeState;

import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.BooleanProperty;
import org.nabucco.testautomation.facade.datatype.property.DateProperty;
import org.nabucco.testautomation.facade.datatype.property.FileProperty;
import org.nabucco.testautomation.facade.datatype.property.IntegerProperty;
import org.nabucco.testautomation.facade.datatype.property.LongProperty;
import org.nabucco.testautomation.facade.datatype.property.PropertyList;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.facade.datatype.property.XPathProperty;
import org.nabucco.testautomation.facade.datatype.property.XmlProperty;
import org.nabucco.testautomation.facade.datatype.property.base.Property;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyContainer;
import org.nabucco.testautomation.facade.datatype.property.base.PropertyReference;
import org.nabucco.testautomation.script.facade.datatype.code.SubEngineActionCode;
import org.nabucco.testautomation.script.facade.datatype.code.SubEngineCode;
import org.nabucco.testautomation.script.facade.datatype.code.SubEngineOperationCode;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Logger;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Loop;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TextMessage;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptComposite;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElement;
import org.nabucco.testautomation.script.facade.datatype.dictionary.base.TestScriptElementContainer;
import org.nabucco.testautomation.script.facade.datatype.dictionary.type.LoggerLevelType;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * 
 * TestScriptElementFactory
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestScriptElementFactory {

	private static long element_id = 1;
	
	private static long script_id = 1; 
	
	public static Logger createLogger(String message) {
		Logger logger = new Logger();
        logger.setId(element_id++);
        logger.setLevel(LoggerLevelType.INFO);
        TextMessage msg = new TextMessage();
        msg.setId(element_id++);
        msg.setText(message);
        add(msg, logger);
        return logger;
	}
	
	public static Logger createLogger(String message, String propertyName) {
		Logger logger = new Logger();
        logger.setId(element_id++);
        logger.setLevel(LoggerLevelType.INFO);
        TextMessage msg = new TextMessage();
        msg.setId(element_id++);
        msg.setText(message);
        PropertyReference ref = new PropertyReference();
        ref.setValue(propertyName);
		msg.setPropertyRef(ref);
        add(msg, logger);
        return logger;
	}
	
	public static Loop createLoop(Integer iterations, Long waitTime, Long maxDuration) {
		Loop loop = new Loop();
		loop.setId(element_id++);
        loop.setMaxIterations(iterations);
        loop.setWait(waitTime);
        loop.setMaxDuration(maxDuration);
        return loop;
	}
	
	public static TestScript createTestScript(String name) {
		TestScript script = new TestScript();
        script.setId(script_id++);
        script.setName(name);
        return script;
	}
	
	public static Metadata createMetadata(SubEngineType subEngineType, Metadata parent, String operation, Property ... props) {
		
		Metadata metadata = new Metadata();
		metadata.setName("Metadata " + element_id);
		metadata.setId(element_id++);
		SubEngineCode subEngine = new SubEngineCode(); 
		subEngine.setCode(subEngineType.toString());
		subEngine.setName(subEngineType.toString());
		metadata.setSubEngine(subEngine);
		metadata.setParent(parent);
		SubEngineOperationCode operationCode = new SubEngineOperationCode();
		operationCode.setCode(operation);
		operationCode.setName(operation);
		metadata.setOperation(operationCode);
		metadata.setPropertyList(new PropertyList());
		
		for (Property property : props) {
			PropertyContainer container = new PropertyContainer();
			container.setDatatypeState(DatatypeState.INITIALIZED);
			container.setOrderIndex(metadata.getPropertyList().getPropertyList().size());
			container.setProperty(property);
			metadata.getPropertyList().getPropertyList().add(container);
		}
		return metadata;
	}
	
	public static PropertyReference createPropertyReference(String nameRef) {
		PropertyReference ref = new PropertyReference();
		ref.setValue(nameRef);
		return ref;
	}
	
	public static StringProperty createStringProperty(String name, String value) {
		StringProperty prop = new StringProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}

	public static DateProperty createDateProperty(String name, String format, String value) {
		DateProperty prop = new DateProperty();
		prop.setId(element_id++);
		prop.setName(name);
		SimpleDateFormat df = new SimpleDateFormat(format);
		try {
			prop.setValue(df.parse(value));
		} catch (ParseException ex) {
			System.err.println(ex);
		}
		return prop;
	}
	
	public static FileProperty createFileProperty(String name, String content) {
		FileProperty prop = new FileProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setContent(content);
		return prop;
	}

	public static XPathProperty createXPathProperty(String name, String value) {
		XPathProperty prop = new XPathProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}

	public static XmlProperty createXmlProperty(String name, String value) {
		XmlProperty prop = new XmlProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}
	
	public static StringProperty createStringPropertyRef(String refName) {
		StringProperty prop = new StringProperty();
		prop.setId(element_id++);
		PropertyReference reference = new PropertyReference();
		reference.setValue(refName);
		prop.setReference(reference);
		return prop;
	}
	
	public static BooleanProperty createBooleanProperty(String name, Boolean value) {
		BooleanProperty prop = new BooleanProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}
	
	public static IntegerProperty createIntegerProperty(String name, Integer value) {
		IntegerProperty prop = new IntegerProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}

	public static LongProperty createLongProperty(String name, Long value) {
		LongProperty prop = new LongProperty();
		prop.setId(element_id++);
		prop.setName(name);
		prop.setValue(value);
		return prop;
	}
	
	public static Action createAction(String action, Metadata metadata, Property ... props) {
		Action actionElement = new Action();
		actionElement.setName("Action " + element_id);
		actionElement.setId(element_id++);
		SubEngineActionCode actionCode = new SubEngineActionCode();
		actionCode.setCode(action);
		actionCode.setName(action);
		actionElement.setAction(actionCode);
		actionElement.setMetadata(metadata);
		actionElement.setPropertyList(new PropertyList());
		
		for (Property property : props) {
			if (property != null) {
				PropertyContainer container = new PropertyContainer();
				container.setDatatypeState(DatatypeState.INITIALIZED);
				container.setOrderIndex(actionElement.getPropertyList().getPropertyList().size());
				container.setProperty(property);
				actionElement.getPropertyList().getPropertyList().add(container);
			}
		}
		return actionElement;
	}
	
	public static Execution createExecution(Action ... actions) {
		Execution execution = new Execution();
		execution.setName("Execution " + element_id);
		execution.setId(element_id++);
		
        for (Action action : actions) {
        	if (action != null) {
        		add(action, execution);
        	}
		}
        return execution;
	}
	
	public static void add(TestScriptElement element, TestScriptComposite container) {
    	TestScriptElementContainer wrapper = new TestScriptElementContainer();
    	wrapper.setElement(element);
    	wrapper.setOrderIndex(container.getTestScriptElementList().size());
    	container.getTestScriptElementList().add(wrapper);
    }
	
}
