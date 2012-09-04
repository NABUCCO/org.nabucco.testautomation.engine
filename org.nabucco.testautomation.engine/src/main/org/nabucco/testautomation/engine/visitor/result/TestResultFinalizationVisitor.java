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
package org.nabucco.testautomation.engine.visitor.result;

import org.nabucco.framework.base.facade.datatype.Data;
import org.nabucco.framework.base.facade.datatype.Identifier;
import org.nabucco.framework.base.facade.datatype.image.ImageData;
import org.nabucco.testautomation.engine.proxy.cache.DataCache;
import org.nabucco.testautomation.engine.proxy.cache.ImageCache;
import org.nabucco.testautomation.result.facade.datatype.trace.FileTrace;
import org.nabucco.testautomation.result.facade.datatype.trace.ScreenshotTrace;
import org.nabucco.testautomation.result.facade.datatype.visitor.TestResultVisitor;

/**
 * TestResultFinalizationVisitor
 * 
 * @author Steffen Schmidt, PRODYNA AG
 */
public class TestResultFinalizationVisitor extends TestResultVisitor {

	@Override
	protected void visit(ScreenshotTrace datatype) {
		
		if (datatype != null && datatype.getImageId() != null) {
			ImageData image = ImageCache.getInstance().remove(datatype.getImageId());
			datatype.setScreenshot(image);
			datatype.setImageId((Identifier) null);
		}		
		super.visit(datatype);
	}
	
	@Override
	protected void visit(FileTrace datatype) {
		
		if (datatype != null && datatype.getFileId() != null) {
			Data data = DataCache.getInstance().remove(datatype.getFileId());
			datatype.setFileContent(data);
			datatype.setFileId((Identifier) null);
		}		
		super.visit(datatype);
	}

}
