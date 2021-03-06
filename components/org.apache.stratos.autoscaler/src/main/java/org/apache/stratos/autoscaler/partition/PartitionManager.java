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

package org.apache.stratos.autoscaler.partition;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.autoscaler.NetworkPartitionContext;
import org.apache.stratos.autoscaler.client.cloud.controller.CloudControllerClient;
import org.apache.stratos.autoscaler.deployment.policy.DeploymentPolicy;
import org.apache.stratos.autoscaler.exception.AutoScalerException;
import org.apache.stratos.autoscaler.exception.InvalidPartitionException;
import org.apache.stratos.autoscaler.exception.PartitionValidationException;
import org.apache.stratos.autoscaler.registry.RegistryManager;
import org.apache.stratos.cloud.controller.deployment.partition.Partition;

import java.util.HashMap;
import java.util.Map;

/**
 * The model class for managing Partitions.
 */
public class PartitionManager {

private static final Log log = LogFactory.getLog(PartitionManager.class);
	
	// Partitions against partitionID
	private static Map<String,Partition> partitions = new HashMap<String, Partition>();
	
	/*
	 * Key - network partition id
	 * Value - reference to NetworkPartition
	 */
	private Map<String, NetworkPartitionContext> networkPartitionContexts;

	private static class Holder {
        static final PartitionManager INSTANCE = new PartitionManager();
    }
	
	public static PartitionManager getInstance(){
	    return Holder.INSTANCE;
	}
	
	private PartitionManager(){
        networkPartitionContexts = new HashMap<String, NetworkPartitionContext>();
	}
	
	
	public boolean partitionExist(String partitionId){
		return partitions.containsKey(partitionId);
	}
	
	/*
	 * Deploy a new partition to Auto Scaler.
	 */
	public boolean addNewPartition(Partition partition) throws AutoScalerException, InvalidPartitionException {
        try {
            if(this.partitionExist(partition.getId())) {
                throw new AutoScalerException(String.format("Partition already exist in partition manager: [id] %s", partition.getId()));
            }
            if(null == partition.getProvider()) {
                throw new InvalidPartitionException("Mandatory field provider has not be set for partition "+ partition.getId());
            }

        	validatePartitionViaCloudController(partition);
            RegistryManager.getInstance().persistPartition(partition);
			addPartitionToInformationModel(partition);
            if(log.isInfoEnabled()) {
                log.info(String.format("Partition is deployed successfully: [id] %s", partition.getId()));
            }
            return true;
		} catch(Exception e){
			throw new AutoScalerException(e);
		}
	}
	
	
	public void addPartitionToInformationModel(Partition partition) {
		partitions.put(partition.getId(), partition);
	}

	public NetworkPartitionContext getNetworkPartition(String networkPartitionId) {
	    return this.networkPartitionContexts.get(networkPartitionId);
	}

    public Partition getPartitionById(String partitionId){
		if(partitionExist(partitionId))
			return partitions.get(partitionId);
		else
			return null;
	}
	
	public Partition[] getAllPartitions(){
		return partitions.values().toArray(new Partition[0]);
		
	}
	
	public boolean validatePartitionViaCloudController(Partition partition) throws PartitionValidationException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Validating partition via cloud controller: [id] %s", partition.getId()));
        }
		return CloudControllerClient.getInstance().validatePartition(partition);
	}

    public void deployNewNetworkPartitions(DeploymentPolicy depPolicy) {
        for(PartitionGroup partitionGroup: depPolicy.getPartitionGroups()){
            String id = partitionGroup.getId();
            if (!networkPartitionContexts.containsKey(id)) {
                NetworkPartitionContext networkPartitionContext =
                        new NetworkPartitionContext(
                                id, partitionGroup.getPartitionAlgo(), partitionGroup.getPartitions());
                addNetworkPartitionContext(networkPartitionContext);
                RegistryManager.getInstance().persistNetworkPartition(networkPartitionContext);
            }

        }
    }
    
    public void undeployNetworkPartitions(DeploymentPolicy depPolicy) {
        for(PartitionGroup partitionGroup: depPolicy.getPartitionGroups()){
            String id = partitionGroup.getId();
            if (networkPartitionContexts.containsKey(id)) {                
                NetworkPartitionContext netPartCtx = this.getNetworkPartition(id);
                // remove from information model
                this.removeNetworkPartitionContext(netPartCtx);
                //remove from the registry
                RegistryManager.getInstance().removeNetworkPartition(this.getNetworkPartition(id).getId());
            }else{
            	String errMsg = "Network partition context not found for policy " + depPolicy;
            	log.error(errMsg);
            	throw new AutoScalerException(errMsg);
            }

        }
    }
    
    private void removeNetworkPartitionContext(NetworkPartitionContext netPartCtx) {
    	 networkPartitionContexts.remove(netPartCtx.getId());		
	}

	public void addNetworkPartitionContext(NetworkPartitionContext ctxt) {
        networkPartitionContexts.put(ctxt.getId(), ctxt);
    }

}
