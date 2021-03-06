/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.load.balancer.algorithm;

/**
 * Algorithm context is used for identifying the cluster and its current member for executing load balancing algorithms.
 */
public class AlgorithmContext {
    private String serviceName;
    private String clusterId;
    private int currentMemberIndex;

    public AlgorithmContext(String serviceName, String clusterId) {
        this.serviceName = serviceName;
        this.clusterId = clusterId;
        this.currentMemberIndex = 0;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public int getCurrentMemberIndex() {
        return currentMemberIndex;
    }

    public void setCurrentMemberIndex(int currentMemberIndex) {
        this.currentMemberIndex = currentMemberIndex;
    }
}
