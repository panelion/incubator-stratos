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

package org.apache.stratos.adc.mgt.deploy.service;

import org.apache.stratos.adc.mgt.exception.ADCException;
import org.apache.stratos.adc.mgt.exception.UnregisteredCartridgeException;
import org.apache.stratos.adc.mgt.payload.PayloadData;
import org.apache.stratos.adc.mgt.subscription.utils.CartridgeSubscriptionUtils;
import org.apache.stratos.cloud.controller.pojo.CartridgeInfo;

import java.io.Serializable;

public abstract class Service implements Serializable {

    private String type;
    private String autoscalingPolicyName;
    private String deploymentPolicyName;
    private String tenantRange;
    private String clusterId;
    private String hostName;
    private int tenantId;
    private String subscriptionKey;
    private CartridgeInfo cartridgeInfo;
    private PayloadData payloadData;

    public Service (String type, String autoscalingPolicyName, String deploymentPolicyName, int tenantId, CartridgeInfo cartridgeInfo,
    		String tenantRange) {

        this.type = type;
        this.autoscalingPolicyName = autoscalingPolicyName;
        this.deploymentPolicyName = deploymentPolicyName;
        this.tenantId = tenantId;
        this.cartridgeInfo = cartridgeInfo;
        this.tenantRange = tenantRange;
        this.subscriptionKey = CartridgeSubscriptionUtils.generateSubscriptionKey();
    }

    public abstract void deploy () throws ADCException, UnregisteredCartridgeException;

    public abstract void undeploy (String clusterId) throws ADCException;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAutoscalingPolicyName() {
        return autoscalingPolicyName;
    }

    public void setAutoscalingPolicyName(String autoscalingPolicyName) {
        this.autoscalingPolicyName = autoscalingPolicyName;
    }

    public String getDeploymentPolicyName() {
        return deploymentPolicyName;
    }

    public void setDeploymentPolicyName(String deploymentPolicyName) {
        this.deploymentPolicyName = deploymentPolicyName;
    }

    public String getTenantRange() {
        return tenantRange;
    }

    public void setTenantRange(String tenantRange) {
        this.tenantRange = tenantRange;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public CartridgeInfo getCartridgeInfo() {
        return cartridgeInfo;
    }

    public void setCartridgeInfo(CartridgeInfo cartridgeInfo) {
        this.cartridgeInfo = cartridgeInfo;
    }

    public String getSubscriptionKey() {
        return subscriptionKey;
    }

    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }

    public PayloadData getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(PayloadData payloadData) {
        this.payloadData = payloadData;
    }
}
