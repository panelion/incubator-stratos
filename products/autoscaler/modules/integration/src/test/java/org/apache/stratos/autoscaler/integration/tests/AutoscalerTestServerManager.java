/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.autoscaler.integration.tests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.JMSBrokerController;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfiguration;
import org.wso2.carbon.automation.core.utils.jmsbrokerutils.controller.config.JMSBrokerConfigurationProvider;
import org.wso2.carbon.integration.framework.TestServerManager;

import java.io.File;
import java.io.IOException;


/**
 * Prepares the Autoscaler for test runs, starts JMS Broker, starts the server, and stops the
 * server after test runs
 */
public class AutoscalerTestServerManager extends TestServerManager {
	private static final Log log = LogFactory.getLog(AutoscalerTestServerManager.class);

	private EnvironmentBuilder builder = null;
	private JMSBrokerController activeMqBroker = null;

	@Override
	@BeforeSuite(timeOut = 300000)
	public String startServer() throws IOException {
		
		builder = new EnvironmentBuilder();

		if (builder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled()) {
			// starting jms broker
			activeMqBroker = new JMSBrokerController("localhost",getJMSBrokerConfiguration());
			if (!JMSBrokerController.isBrokerStarted()) {
				Assert.assertTrue(activeMqBroker.start(),"JMS Broker stating failed");
			}
		}

		String carbonHome = super.startServer();
		System.setProperty("carbon.home", carbonHome);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		return carbonHome;
	}

	@Override
	@AfterSuite(timeOut = 60000)
	public void stopServer() throws Exception {
		super.stopServer();
		 if (builder.getFrameworkSettings().getEnvironmentSettings().is_builderEnabled()) {  
	         if (activeMqBroker != null) {  
	           Assert.assertTrue(activeMqBroker.stop(), "JMS Broker Stopping failed");  
	         }  
		 }
	}

	protected void copyArtifacts(String carbonHome) throws IOException {
		Assert.assertNotNull(carbonHome, "carbon home cannot be null");
		String resourceLocation = System.getProperty("framework.resource.location", 
				System.getProperty("basedir") + "src" + File.separator + "test" + File.separator + "resources" + File.separator );
		String libDir = carbonHome + File.separator + "repository"+ File.separator + "components"+ File.separator + "lib";
		String confDir = carbonHome + File.separator + "repository"+ File.separator + "conf";
		log.info("Copying jndi.properties file....");
		FileUtils.copyFile(new File(resourceLocation,"jndi.properties"),new File(confDir,"jndi.properties"));
		log.info("Copying ActiveMQ dependencies....");
		FileUtils.copyDirectory(new File(resourceLocation + File.separator + "artifacts"+ File.separator + "jar"), new File(libDir));
	}

	private JMSBrokerConfiguration getJMSBrokerConfiguration() {
		return JMSBrokerConfigurationProvider.getInstance()
				.getBrokerConfiguration();
	}

}
