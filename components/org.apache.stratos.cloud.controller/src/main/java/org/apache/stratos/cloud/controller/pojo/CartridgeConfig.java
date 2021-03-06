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

package org.apache.stratos.cloud.controller.pojo;

import java.io.Serializable;

/**
 * This class is used as the pojo for supporting the service at CC,
 * which is called by the Rest API in SM to deploy a cartridge definition
 */
public class CartridgeConfig implements Serializable {

    private static final long serialVersionUID = 3455721779991902731L;

    private String type;

    private String hostName;

    private String provider;

    private String displayName;

    private String description;

    private String version;

    private boolean multiTenant;

    private String baseDir;

    private String[] deploymentDirs;

    private PortMapping[] portMappings;
    
    private String defaultAutoscalingPolicy;

    private Properties properties;

    private IaasConfig[] iaasConfigs;
    
    private LoadbalancerConfig lbConfig;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isMultiTenant() {
        return multiTenant;
    }

    public void setMultiTenant(boolean multiTenant) {
        this.multiTenant = multiTenant;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String[] getDeploymentDirs() {
        return deploymentDirs;
    }

    public void setDeploymentDirs(String[] deploymentDirs) {
        this.deploymentDirs = deploymentDirs;
    }

    public PortMapping[] getPortMappings() {
        return portMappings;
    }

    public void setPortMappings(PortMapping[] portMappings) {
        this.portMappings = portMappings;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public IaasConfig[] getIaasConfigs() {
        return iaasConfigs;
    }

    public void setIaasConfigs(IaasConfig[] iaasConfigs) {
        this.iaasConfigs = iaasConfigs;
    }

    public String toString () {

        return "Type: " + type + ", Provider: " + provider + ", Host: " + hostName + ", Display Name: " + displayName +
                ", Description: " + description +  ", Version: " + version + ", Multitenant " + multiTenant +
                "\n Deployment: " + getDeploymentDetails() + "\n PortMapping: " + getPortMappingDetails() +
                "\n IaaS: " +  getIaasConfigDetails() + "\n Properties: " + getPropertyDetails();
    }

    private String getDeploymentDetails () {

        StringBuilder deploymentDetailBuilder = new StringBuilder();
        deploymentDetailBuilder.append("Base direcotry: " + baseDir);
        if(deploymentDirs != null) {
            if(deploymentDirs.length > 0) {
                deploymentDetailBuilder.append(" Direcotries: ");
                for (String directory : deploymentDirs) {
                    deploymentDetailBuilder.append(directory + " | ");
                }
            }
        }

        return  deploymentDetailBuilder.toString();
    }

    private String getPortMappingDetails () {

        StringBuilder portMappingDetailBuilder = new StringBuilder();
        if(portMappings != null) {
            if(portMappings.length > 0) {
                for (PortMapping portMapping : portMappings) {
                    portMappingDetailBuilder.append(portMapping.toString() + " | ");
                }
            }
        }
        return portMappingDetailBuilder.toString();
    }

    private String getIaasConfigDetails () {

        StringBuilder iaasConfigDetailBuilder = new StringBuilder();
        if(iaasConfigs != null) {
            if(iaasConfigs.length > 0) {
                for (IaasConfig iaasConfig : iaasConfigs) {
                    iaasConfigDetailBuilder.append(iaasConfig.toString() + " | ");
                }
            }
        }
        return iaasConfigDetailBuilder.toString();
    }

    private String getPropertyDetails () {

        StringBuilder propertyDetailBuilder = new StringBuilder();
        if(properties != null) {
            Property[] propertyArray = properties.getProperties();
            if (propertyArray.length > 0) {
                for (Property property : propertyArray) {
                    propertyDetailBuilder.append(property.toString() + " | ");
                }
            }
        }
        return propertyDetailBuilder.toString();
    }

    public LoadbalancerConfig getLbConfig() {
        return lbConfig;
    }

    public void setLbConfig(LoadbalancerConfig lbConfig) {
        this.lbConfig = lbConfig;
    }

    public String getDefaultAutoscalingPolicy() {
        return defaultAutoscalingPolicy;
    }

    public void setDefaultAutoscalingPolicy(String defaultAutoscalingPolicy) {
        this.defaultAutoscalingPolicy = defaultAutoscalingPolicy;
    }
}
