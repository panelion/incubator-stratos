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
 * Holds runtime data of a Cluster.
 * @author nirmal
 *
 */
public class ClusterContext implements Serializable{

    private static final long serialVersionUID = 4830667953031229223L;
    // cluster id
    private String clusterId;
    // cartridge type
    private String cartridgeType;
    // payload as a String
    private String payload;
    private String hostName;
    
    public ClusterContext(String clusterId, String cartridgeType, String payload) {
        this.clusterId = clusterId;
        this.cartridgeType = cartridgeType;
        this.payload = payload;
    }
    
    public ClusterContext(String clusterId, String cartridgeType, String payload, String hostName) {
        this.clusterId = clusterId;
        this.cartridgeType = cartridgeType;
        this.payload = payload;
        this.setHostName(hostName);
    }
    
    public String getClusterId() {
        return clusterId;
    }
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
    public String getCartridgeType() {
        return cartridgeType;
    }
    public void setCartridgeType(String cartridgeType) {
        this.cartridgeType = cartridgeType;
    }
    public String getPayload() {
        return payload;
    }
    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
}
