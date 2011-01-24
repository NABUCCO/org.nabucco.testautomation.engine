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
package org.nabucco.testautomation.engine.proxy;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nabucco.testautomation.engine.base.TestEngineSupport;
import org.nabucco.testautomation.engine.base.TestScriptElementFactory;

import org.nabucco.testautomation.config.facade.datatype.TestConfigElement;
import org.nabucco.testautomation.config.facade.datatype.TestConfiguration;
import org.nabucco.testautomation.facade.datatype.engine.SubEngineType;
import org.nabucco.testautomation.facade.datatype.property.FileProperty;
import org.nabucco.testautomation.facade.datatype.property.StringProperty;
import org.nabucco.testautomation.result.facade.datatype.TestConfigurationResult;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigElementStatusType;
import org.nabucco.testautomation.result.facade.datatype.status.TestConfigurationStatusType;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Action;
import org.nabucco.testautomation.script.facade.datatype.dictionary.Execution;
import org.nabucco.testautomation.script.facade.datatype.dictionary.TestScript;
import org.nabucco.testautomation.script.facade.datatype.metadata.Metadata;

/**
 * 
 * ProcessProxyTest
 * 
 * @author Steffen Schmidt, PRODYNA AG
 * 
 */
public class ProcessProxyTest extends TestEngineSupport {

    @Before
    public void setUp() throws Exception {
    	super.setUp();
    }
    
    @Test
    public void testLoginLogout() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("ProcesProxyTest");
        TestConfigElement testSheet = createTestSheet("FTPTest");
        TestConfigElement testCase = createTestCase("LoginLogoutTest");
        TestConfigElement login = createLoginTestStep();
        TestConfigElement logout = createLogoutTestStep();
        
        add(testCase,testSheet);
        add(login,testCase);
        add(logout,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    @Test
    public void testListFiles() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("ProcesProxyTest");
        TestConfigElement testSheet = createTestSheet("FTPTest");
        TestConfigElement testCase = createTestCase("ListFilesTest");
        TestConfigElement login = createLoginTestStep();
        TestConfigElement logout = createLogoutTestStep();
        TestConfigElement testStep = createTestStep("ListFiles");
        
        add(createListFilesScript(), testStep);
        add(testCase,testSheet);
        add(login,testCase);
        add(testStep,testCase);
        add(logout,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    @Test
    public void testChangeDirectory() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("ProcesProxyTest");
        TestConfigElement testSheet = createTestSheet("FTPTest");
        TestConfigElement testCase = createTestCase("ChangeDirectoryTest");
        TestConfigElement login = createLoginTestStep();
        TestConfigElement logout = createLogoutTestStep();
        TestConfigElement testStep = createTestStep("ChangeDir");
        TestConfigElement testStep2 = createTestStep("List files");
        
        add(createChangeDirScript("/htdocs/private"), testStep);
        add(createListFilesScript(), testStep2);
        add(testCase,testSheet);
        add(login,testCase);
        add(testStep,testCase);
        add(testStep2,testCase);
        add(logout,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    @Test
    public void testDownloadFile() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("ProcesProxyTest");
        TestConfigElement testSheet = createTestSheet("FTPTest");
        TestConfigElement testCase = createTestCase("ChangeDirectoryTest");
        TestConfigElement login = createLoginTestStep();
        TestConfigElement logout = createLogoutTestStep();
        TestConfigElement testStep = createTestStep("ChangeDir");
        TestConfigElement testStep2 = createTestStep("Download File");
        
        add(createChangeDirScript("/htdocs/private"), testStep);
        add(createDownloadScript(), testStep2);
        add(testCase,testSheet);
        add(login,testCase);
        add(testStep,testCase);
        add(testStep2,testCase);
        add(logout,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    @Test
    public void testUploadFile() {
    	
    	TestConfiguration config = getDefaultTestConfiguration("ProcesProxyTest");
        TestConfigElement testSheet = createTestSheet("FTPTest");
        TestConfigElement testCase = createTestCase("ChangeDirectoryTest");
        TestConfigElement login = createLoginTestStep();
        TestConfigElement logout = createLogoutTestStep();
        TestConfigElement testStep = createTestStep("ChangeDir");
        TestConfigElement testStep2 = createTestStep("MakeDir");
        TestConfigElement testStep3 = createTestStep("Upload File");
        TestConfigElement testStep4 = createTestStep("CleanUp");
        
        add(createChangeDirScript("/htdocs/private"), testStep);
        add(createMakeDirScript("testpath"), testStep2);
        add(createChangeDirScript("./testpath"), testStep3);
        add(createUploadScript("test.txt"), testStep3);
        add(createListFilesScript(), testStep3);
        add(deleteFileScript("test.txt"), testStep4);
        add(moveUpScript(), testStep4);
        add(removeDirScript("testpath"), testStep4);
        add(testCase,testSheet);
        add(login,testCase);
        add(testStep,testCase);
        add(testStep2,testCase);
        add(testStep3,testCase);
        add(testStep4,testCase);
        add(logout,testCase);
        add(testSheet,config);
        
        TestConfigurationResult result = execute(config);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getStatus() == TestConfigurationStatusType.FINISHED);
        Assert.assertTrue(result.getTestResultList().size() == 1);
        Assert.assertTrue(result.getTestResultList().get(0).getResult().getStatus() == TestConfigElementStatusType.PASSED);
    }
    
    private TestScript moveUpScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("MoveUp Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_DIRECTORY");
    	
    	Action mkDirAction = TestScriptElementFactory.createAction("MOVE_UP", dirMetadata);
    	
    	mkDirAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(mkDirAction);
    	
    	add(TestScriptElementFactory.createLogger("Move Up"), script);
    	add(execution,script);
        return script;
    }
    
    private TestScript deleteFileScript(String filename) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Delete File Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_FILE");
    	
    	StringProperty pathname = TestScriptElementFactory.createStringProperty("FILENAME", filename);
    	
    	Action mkDirAction = TestScriptElementFactory.createAction("DELETE_FILE", dirMetadata, pathname);
    	
    	mkDirAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(mkDirAction);
    	
    	add(TestScriptElementFactory.createLogger("Delete File"), script);
    	add(execution,script);
        return script;
    }
 
    private TestScript removeDirScript(String path) {
 	
	 	TestScript script = TestScriptElementFactory.createTestScript("MakeDir Script");
	 	
	 	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_DIRECTORY");
	 	
	 	StringProperty pathname = TestScriptElementFactory.createStringProperty("PATHNAME", path);
	 	
	 	Action mkDirAction = TestScriptElementFactory.createAction("REMOVE_DIR", dirMetadata, pathname);
	 	
	 	mkDirAction.setDelay(1000L);
	 	
	 	Execution execution = TestScriptElementFactory.createExecution(mkDirAction);
	 	
	 	add(TestScriptElementFactory.createLogger("Remove Dir"), script);
	 	add(execution,script);
	     return script;
	}
    
    private TestScript createMakeDirScript(String path) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("MakeDir Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_DIRECTORY");
    	
    	StringProperty pathname = TestScriptElementFactory.createStringProperty("PATHNAME", path);
    	
    	Action mkDirAction = TestScriptElementFactory.createAction("MAKE_DIR", dirMetadata, pathname);
    	
    	mkDirAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(mkDirAction);
    	
    	add(TestScriptElementFactory.createLogger("Make Dir"), script);
    	add(execution,script);
        return script;
    }
    
    private TestScript createUploadScript(String filename) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Upload Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_FILE");
    	
    	FileProperty file = TestScriptElementFactory.createFileProperty(filename, "a sample text file for the FTP-Test");
    	
    	Action uploadAction = TestScriptElementFactory.createAction("UPLOAD", dirMetadata, file);
    	
    	uploadAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(uploadAction);
    	
    	add(TestScriptElementFactory.createLogger("Upload file"), script);
    	add(execution,script);
        return script;
    }
    
    private TestScript createDownloadScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("Download Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_FILE");
    	
    	StringProperty filename = TestScriptElementFactory.createStringProperty("FILENAME", "test.txt");
    	
    	Action downloadAction = TestScriptElementFactory.createAction("DOWNLOAD", dirMetadata, filename);
    	
    	downloadAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(downloadAction);
    	
    	add(TestScriptElementFactory.createLogger("Changing directory"), script);
    	add(execution,script);
    	add(TestScriptElementFactory.createLogger("Files", downloadAction.getName().getValue()), script);
        return script;
    }
    
    private TestScript createChangeDirScript(String path) {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("FTP ChangeDir Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_DIRECTORY");
    	
    	StringProperty pathname = TestScriptElementFactory.createStringProperty("PATHNAME", path);
    	
    	Action changeDirAction = TestScriptElementFactory.createAction("CHANGE_DIR", dirMetadata, pathname);
    	
    	changeDirAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(changeDirAction);
    	
    	add(TestScriptElementFactory.createLogger("Changing directory"), script);
        add(execution,script);
        return script;
    }
    
    private TestScript createListFilesScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("FTP ListFiles Script");
    	
    	Metadata dirMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_DIRECTORY");
    	
    	Action listFileAction = TestScriptElementFactory.createAction("LIST_FILES", dirMetadata);

    	listFileAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(listFileAction);
    	
    	add(TestScriptElementFactory.createLogger("ListingFiles of current directory"), script);
        add(execution,script);
        add(TestScriptElementFactory.createLogger("FileList: ", listFileAction.getName().getValue()), script);
        return script;
    }
    
    private TestConfigElement createLoginTestStep() {
    
    	TestConfigElement login = createTestStep("Login");
    	add(createConnectScript(), login);
    	add(createLoginScript(), login);
    	return login;
    }
    
    private TestConfigElement createLogoutTestStep() {
        
    	TestConfigElement login = createTestStep("Logout");
    	add(createLogoutScript(), login);
    	add(createDisconnectScript(), login);
    	return login;
    }
    
    private TestScript createConnectScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("FTP Connect Script");
    	
    	StringProperty server = TestScriptElementFactory.createStringProperty("SERVERNAME", "ftp.testserver.de");
    	
    	Metadata serverMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_SERVER", server);
    	
    	Action connectAction = TestScriptElementFactory.createAction("CONNECT", serverMetadata);
    	connectAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(connectAction);
    	
    	add(TestScriptElementFactory.createLogger("Try connection to FTP-Server"), script);
        add(execution,script);
        add(TestScriptElementFactory.createLogger("FTP-Connection established"),script);
        return script;
    }
    
    private TestScript createLoginScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("FTP Login Script");
    	
    	StringProperty username = TestScriptElementFactory.createStringProperty("USERNAME", "sschmidt");
    	StringProperty password = TestScriptElementFactory.createStringProperty("PASSWORD", "geheim");
    	
    	Metadata serverMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_SERVER", username, password);
    	
    	Action loginAction = TestScriptElementFactory.createAction("LOGIN", serverMetadata);
    	loginAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(loginAction);
    	
    	add(TestScriptElementFactory.createLogger("Try login to FTP-Server"), script);
        add(execution,script);
        add(TestScriptElementFactory.createLogger("Login successful"),script);
    	return script;
    }
    
    private TestScript createLogoutScript() {
    	
    	TestScript script = TestScriptElementFactory.createTestScript("FTP Logout Script");
    	
    	Metadata serverMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_SERVER");
    	
    	Action logoutAction = TestScriptElementFactory.createAction("LOGOUT", serverMetadata);
    	logoutAction.setDelay(1000L);
    	
    	Execution execution = TestScriptElementFactory.createExecution(logoutAction);
    	
    	add(TestScriptElementFactory.createLogger("Try logout from FTP-Server"), script);
        add(execution,script);
        add(TestScriptElementFactory.createLogger("Logout successful"),script);
    	return script;
    }

	private TestScript createDisconnectScript() {
	
		TestScript script = TestScriptElementFactory.createTestScript("FTP Disconnect Script");
		
		Metadata serverMetadata = TestScriptElementFactory.createMetadata(SubEngineType.PROCESS, null, "FTP_SERVER");
		
		Action disconnectAction = TestScriptElementFactory.createAction("DISCONNECT", serverMetadata);
		disconnectAction.setDelay(1000L);
		
		Execution execution = TestScriptElementFactory.createExecution(disconnectAction);
		
		add(TestScriptElementFactory.createLogger("Try to disconnect from FTP-Server"), script);
	    add(execution,script);
	    add(TestScriptElementFactory.createLogger("FTP-Connection closed"),script);
		return script;
	}
	    
}
