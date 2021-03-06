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

package org.apache.stratos.adc.mgt.manager;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.adc.mgt.client.CloudControllerServiceClient;
import org.apache.stratos.adc.mgt.connector.CartridgeSubscriptionConnector;
import org.apache.stratos.adc.mgt.connector.CartridgeSubscriptionConnectorFactory;
import org.apache.stratos.adc.mgt.dao.CartridgeSubscriptionInfo;
import org.apache.stratos.adc.mgt.dto.SubscriptionInfo;
import org.apache.stratos.adc.mgt.exception.*;
import org.apache.stratos.adc.mgt.payload.BasicPayloadData;
import org.apache.stratos.adc.mgt.payload.PayloadData;
import org.apache.stratos.adc.mgt.payload.PayloadFactory;
import org.apache.stratos.adc.mgt.publisher.ArtifactUpdatePublisher;
import org.apache.stratos.adc.mgt.repository.Repository;
import org.apache.stratos.adc.mgt.subscriber.Subscriber;
import org.apache.stratos.adc.mgt.subscription.CartridgeSubscription;
import org.apache.stratos.adc.mgt.subscription.factory.CartridgeSubscriptionFactory;
import org.apache.stratos.adc.mgt.subscription.tenancy.SubscriptionMultiTenantBehaviour;
import org.apache.stratos.adc.mgt.subscription.tenancy.SubscriptionSingleTenantBehaviour;
import org.apache.stratos.adc.mgt.subscription.tenancy.SubscriptionTenancyBehaviour;
import org.apache.stratos.adc.mgt.subscription.utils.CartridgeSubscriptionUtils;
import org.apache.stratos.adc.mgt.utils.ApplicationManagementUtil;
import org.apache.stratos.adc.mgt.utils.CartridgeConstants;
import org.apache.stratos.adc.mgt.utils.PersistenceManager;
import org.apache.stratos.cloud.controller.pojo.CartridgeInfo;
import org.apache.stratos.cloud.controller.pojo.Property;
import org.wso2.carbon.context.CarbonContext;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Manager class for the purpose of managing CartridgeSubscriptionInfo subscriptions, groupings, etc.
 */
public class CartridgeSubscriptionManager {

    private static Log log = LogFactory.getLog(CartridgeSubscriptionManager.class);

    /**
     * Subscribes to a cartridge
     *
     * @param cartridgeType Cartridge type
     * @param cartridgeAlias Cartridge alias
     * @param autoscalingPolicyName Autoscaling policy name
     * @param deploymentPolicyName Deployment Policy name
     * @param tenantDomain Subscriing tenant's domain
     * @param tenantId Subscribing tenant's Id
     * @param tenantAdminUsername Subscribing tenant's admin user name
     * @param repositoryType Type of repository
     * @param repositoryURL Repository URL
     * @param isPrivateRepository If a private or a public repository
     * @param repositoryUsername Repository username
     * @param repositoryPassword Repository password
     *
     * @return Subscribed CartridgeSubscriptionInfo object
     * @throws ADCException
     * @throws InvalidCartridgeAliasException
     * @throws DuplicateCartridgeAliasException
     * @throws PolicyException
     * @throws UnregisteredCartridgeException
     * @throws RepositoryRequiredException
     * @throws RepositoryCredentialsRequiredException
     * @throws RepositoryTransportException
     * @throws AlreadySubscribedException
     * @throws InvalidRepositoryException
     */
    public CartridgeSubscription subscribeToCartridge (String cartridgeType, String cartridgeAlias,
                                                  String autoscalingPolicyName, String deploymentPolicyName,
                                                  String tenantDomain, int tenantId,
                                                  String tenantAdminUsername, String repositoryType,
                                                  String repositoryURL, boolean isPrivateRepository,
                                                  String repositoryUsername, String repositoryPassword)

            throws ADCException, InvalidCartridgeAliasException, DuplicateCartridgeAliasException, PolicyException,
            UnregisteredCartridgeException, RepositoryRequiredException, RepositoryCredentialsRequiredException,
            RepositoryTransportException, AlreadySubscribedException, InvalidRepositoryException {

        return subscribeToCartridgeWithProperties(cartridgeType, cartridgeAlias, autoscalingPolicyName, 
                                                  deploymentPolicyName, tenantDomain, tenantId, tenantAdminUsername, 
                                                  repositoryType, repositoryURL, isPrivateRepository, repositoryUsername, 
                                                  repositoryPassword, null);
    }
    
    public CartridgeSubscription subscribeToCartridgeWithProperties(String cartridgeType, String cartridgeAlias,
        String autoscalingPolicyName, String deploymentPolicyName, String tenantDomain,
        int tenantId, String tenantAdminUsername, String repositoryType, String repositoryURL,
        boolean isPrivateRepository, String repositoryUsername, String repositoryPassword, Property[] props)

    throws ADCException,
        InvalidCartridgeAliasException,
        DuplicateCartridgeAliasException,
        PolicyException,
        UnregisteredCartridgeException,
        RepositoryRequiredException,
        RepositoryCredentialsRequiredException,
        RepositoryTransportException,
        AlreadySubscribedException,
        InvalidRepositoryException {

        // validate cartridge alias
        ApplicationManagementUtil.validateCartridgeAlias(cartridgeAlias, cartridgeType);

        CartridgeInfo cartridgeInfo;
        try {
            cartridgeInfo =
                            CloudControllerServiceClient.getServiceClient().getCartridgeInfo(cartridgeType);
            if (props != null) {
                cartridgeInfo.setProperties(props);
            }

        } catch (UnregisteredCartridgeException e) {
            String message =
                             cartridgeType +
                                     " is not a valid cartridgeSubscription type. Please try again with a valid cartridgeSubscription type.";
            log.error(message);
            throw e;

        } catch (Exception e) {
            String message = "Error getting info for " + cartridgeType;
            log.error(message, e);
            throw new ADCException(message, e);
        }

        //Decide tenancy behaviour
        SubscriptionTenancyBehaviour tenancyBehaviour;
        if(cartridgeInfo.getMultiTenant()) {
            tenancyBehaviour = new SubscriptionMultiTenantBehaviour();
        } else {
            tenancyBehaviour = new SubscriptionSingleTenantBehaviour();
        }

        //Create the CartridgeSubscription instance
        CartridgeSubscription cartridgeSubscription = CartridgeSubscriptionFactory.
                getCartridgeSubscriptionInstance(cartridgeInfo, tenancyBehaviour);

        //Create repository
        Repository repository = cartridgeSubscription.manageRepository(repositoryURL,
                                                                       repositoryUsername,
                                                                       repositoryPassword,
                                                                       isPrivateRepository,
                                                                       cartridgeAlias,
                                                                       cartridgeInfo, tenantDomain);

        //Create subscriber
        Subscriber subscriber = new Subscriber(tenantAdminUsername, tenantId, tenantDomain);

        //create subscription
        cartridgeSubscription.createSubscription(subscriber, cartridgeAlias, autoscalingPolicyName,
                                                 deploymentPolicyName, repository);
        cartridgeSubscription.setSubscriptionKey(CartridgeSubscriptionUtils.generateSubscriptionKey()); // TODO ---- fix
                                                                             // properly

        log.info("Tenant [" + tenantId + "] with username [" + tenantAdminUsername +
                 " subscribed to " + "] Cartridge Alias " + cartridgeAlias + ", Cartridge Type: " +
                 cartridgeType + ", Repo URL: " + repositoryURL + ", Policy: " +
                 autoscalingPolicyName);

        //Create the payload
        BasicPayloadData basicPayloadData = CartridgeSubscriptionUtils.createBasicPayload(cartridgeSubscription);
        //Populate the basic payload details
        basicPayloadData.populatePayload();

        PayloadData payloadData = PayloadFactory.getPayloadDataInstance(cartridgeInfo.getProvider(),
                cartridgeInfo.getType(), basicPayloadData);

        // get the payload parameters defined in the cartridge definition file for this cartridge type
        if (cartridgeInfo.getProperties() != null && cartridgeInfo.getProperties().length != 0) {

            for (Property property : cartridgeInfo.getProperties()) {
                // check if a property is related to the payload. Currently this is done by checking if the
                // property name starts with 'payload_parameter.' suffix. If so the payload param name will
                // be taken as the substring from the index of '.' to the end of the property name.
                if (property.getName()
                            .startsWith(CartridgeConstants.CUSTOM_PAYLOAD_PARAM_NAME_PREFIX)) {
                    String payloadParamName = property.getName();
                    payloadData.add(payloadParamName.substring(payloadParamName.indexOf(".") + 1), property.getValue());
                }
            }
        }

        //check if there are any custom payload entries defined
        if (cartridgeSubscription.getCustomPayloadEntries() != null) {
            //add them to the payload
            Map<String, String> customPayloadEntries = cartridgeSubscription.getCustomPayloadEntries();
            Set<Map.Entry<String,String>> entrySet = customPayloadEntries.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                payloadData.add(entry.getKey(), entry.getValue());
            }
        }

        cartridgeSubscription.setPayloadData(payloadData);

        // Publish tenant subscribed event to message broker
        CartridgeSubscriptionUtils.publishTenantSubscribedEvent(cartridgeSubscription.getSubscriber().getTenantId(),
                cartridgeSubscription.getCartridgeInfo().getType());
        
        // publish artifact-deployment event for multi-tenant subscription
        //CartridgeSubscriptionInfo subscription = PersistenceManager.getSubscriptionFromClusterId(clusterId);
        
        if(cartridgeInfo.getMultiTenant()) {
        	log.info(" Multitenant --> Publishing Artifact update event -- ");
            ArtifactUpdatePublisher publisher = new ArtifactUpdatePublisher(cartridgeSubscription.getRepository(),
            		cartridgeSubscription.getClusterDomain(), // clusterId 
            		String.valueOf(cartridgeSubscription.getSubscriber().getTenantId()));
            publisher.publish();
        } 

        return cartridgeSubscription;
    }

    /**
     * Connects / groups cartridges
     *
     * @param tenantDomain Tenant's domain
     * @param cartridgeSubscription CartridgeSubscription instance to which the CartridgeSubscription denoted by
     *                          connectingSubscriptionAlias will be connected to
     * @param connectingSubscriptionAlias Alias of the connecting cartridge
     *
     * @throws ADCException
     * @throws NotSubscribedException
     * @throws AxisFault
     */
    public void connectCartridges (String tenantDomain, CartridgeSubscription cartridgeSubscription,
                                   String connectingSubscriptionAlias)
            throws ADCException, NotSubscribedException, AxisFault {

        //TODO: retrieve from the cache and connect. For now, new objects are created

        CartridgeSubscription connectingCartridgeSubscription = getCartridgeSubscription(tenantDomain,
                connectingSubscriptionAlias);

        if(cartridgeSubscription == null) {
            String errorMsg = "No cartridge subscription found in cache for tenant " + tenantDomain + "  connecting aborted";
            log.error(errorMsg);
            return;
        }

        if(connectingCartridgeSubscription == null) {
            String errorMsg = "No cartridge subscription found in cache for tenant " + tenantDomain + ", alias " +
                    connectingSubscriptionAlias + ",  connecting aborted";
            log.error(errorMsg);
            return;
        }

        CartridgeSubscriptionConnector cartridgeSubscriptionConnector = CartridgeSubscriptionConnectorFactory.
                getCartridgeInstanceConnector(connectingCartridgeSubscription.getType());

        cartridgeSubscription.connect(connectingSubscriptionAlias);

        //PayloadArg payloadArg = cartridgeSubscription.createPayloadParameters();

        //get additional payload params for connecting cartridges
        Properties payloadProperties = cartridgeSubscriptionConnector.createConnection(cartridgeSubscription,
                connectingCartridgeSubscription);
        StringBuilder connectionParamsBuilder = new StringBuilder();
        Set<Map.Entry<Object,Object>> payloadParamEntries = payloadProperties.entrySet();

        for (Map.Entry<Object, Object> payloadParamEntry : payloadParamEntries) {
            connectionParamsBuilder.append(",");
            connectionParamsBuilder.append(payloadParamEntry.getKey().toString());
            connectionParamsBuilder.append("=");
            connectionParamsBuilder.append(payloadParamEntry.getValue().toString());
        }

        //add connection relates parameters to the payload
        if(cartridgeSubscription.getPayloadData() != null) {
            //cartridgeSubscription.getPayloadData().populatePayload(connectionParamsBuilder.toString());
        } else {
            //no existing payload
            /*Payload payload = PayloadFactory.getPayloadDataInstance(cartridgeSubscription.getCartridgeInfo().getProvider(),
                    cartridgeSubscription.getType(), "/tmp/" + tenantDomain + "-" + cartridgeSubscription.getAlias() +
                    ".zip");
            payload.populatePayload(connectionParamsBuilder.toString());
            cartridgeSubscription.setPayloadData(payload);*/
        }

    }

    /**
     * Registers the cartridge subscription for the given CartridgeSubscriptionInfo object
     *
     * @param cartridgeSubscription CartridgeSubscription subscription
     *
     * @return SubscriptionInfo object populated with relevant information
     * @throws ADCException
     * @throws UnregisteredCartridgeException
     */
    public SubscriptionInfo registerCartridgeSubscription(CartridgeSubscription cartridgeSubscription)
            throws ADCException, UnregisteredCartridgeException {

        CartridgeSubscriptionInfo cartridgeSubscriptionInfo = cartridgeSubscription.registerSubscription(null);

        int subscriptionId;
        try {
            subscriptionId = PersistenceManager.persistSubscription(cartridgeSubscriptionInfo);

        } catch (Exception e) {
            String errorMsg = "Error saving subscription for tenant " +
                    cartridgeSubscription.getSubscriber().getTenantDomain() + ", alias " + cartridgeSubscription.getType();
            log.error(errorMsg);
            throw new ADCException(errorMsg, e);
        }

        cartridgeSubscription.setSubscriptionId(subscriptionId);
        ApplicationManagementUtil.addDNSEntry(cartridgeSubscriptionInfo.getAlias(), cartridgeSubscription.getType());

        log.info("Successful Subscription: "+cartridgeSubscription.toString());
        return ApplicationManagementUtil.
                createSubscriptionResponse(cartridgeSubscriptionInfo, cartridgeSubscription.getRepository());
    }

    /**
     * Unsubscribe from a Cartridge
     *
     * @param tenantDomain Tenant's domain
     * @param alias Alias given at subscription time
     * @throws ADCException
     * @throws NotSubscribedException
     */
    public void unsubscribeFromCartridge (String tenantDomain, String alias)
            throws ADCException, NotSubscribedException {

        //TODO: retrieve from the cache and connect. For now, new objects are created

        CartridgeSubscription cartridgeSubscription = getCartridgeSubscription(tenantDomain, alias);

        if(cartridgeSubscription != null) {
            cartridgeSubscription.removeSubscription();

            // Publish tenant un-subscribed event to message broker
            CartridgeSubscriptionUtils.publishTenantUnSubscribedEvent(cartridgeSubscription.getSubscriber().getTenantId(),
                    cartridgeSubscription.getCartridgeInfo().getType());
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug("No cartridge subscription found with alias " + alias + " for tenant " + tenantDomain);
            }
        }
    }

    /**
     * Creates and returns a CartridgeSubscription object
     *
     * @param tenantDomain Tenant's domain
     * @param alias alias given at subscription
     *
     * @return CartridgeSubscription object populated with relevant information
     * @throws ADCException
     * @throws NotSubscribedException
     */
    public CartridgeSubscription getCartridgeSubscription(String tenantDomain, String alias)
            throws ADCException, NotSubscribedException {

        CartridgeSubscriptionInfo cartridgeSubscriptionInfo = getCartridgeSubscriptionInfo(tenantDomain, alias);

        CartridgeInfo cartridgeInfo;
        try {
            cartridgeInfo = CloudControllerServiceClient.getServiceClient().
                    getCartridgeInfo(cartridgeSubscriptionInfo.getCartridge());
        } catch (Exception e) {
            throw new ADCException(e.getMessage(), e);
        }

        return populateCartridgeSubscriptionInformation(cartridgeInfo, cartridgeSubscriptionInfo);
    }

    //TODO: remove
    /*public CartridgeSubscription deployMultitenantService(String cartridgeType, String cartridgeAlias,
            String autoscalingPolicyName, String deploymentPolicyName,
            String tenantDomain, int tenantId,
            String tenantAdminUsername,
            String clusterDomain, String clusterSubdomain,
            String repositoryURL, boolean isPrivateRepository,
            String repositoryUsername, String repositoryPassword, String tenantRange) throws ADCException, InvalidCartridgeAliasException, DuplicateCartridgeAliasException, PolicyException,
            UnregisteredCartridgeException, RepositoryRequiredException, RepositoryCredentialsRequiredException,
            RepositoryTransportException, AlreadySubscribedException, InvalidRepositoryException {
    	
    	log.info(" ---- in deploy multitenant service ---- ");
    	//validate cartridge alias
        ApplicationManagementUtil.validateCartridgeAlias(cartridgeAlias, cartridgeType);

        CartridgeInfo cartridgeInfo;
        try {
            cartridgeInfo = CloudControllerServiceClient.getServiceClient().getCartridgeInfo(cartridgeType);

        } catch (UnregisteredCartridgeException e) {
            String message = cartridgeType
                    + " is not a valid cartridgeSubscription type. Please try again with a valid cartridgeSubscription type.";
            log.error(message);
            throw e;

        } catch (Exception e) {
            String message = "Error getting info for " + cartridgeType;
            log.error(message, e);
            throw new ADCException(message, e);
        }

        Subscriber subscriber = new Subscriber(tenantAdminUsername, tenantId, tenantDomain);

        CartridgeSubscription cartridgeSubscription = new FrameworkCartridgeSubscription(cartridgeInfo,true);
        log.info("-- Cartridge subscription is created --- ");
        Repository repository = cartridgeSubscription.manageRepository(repositoryURL, repositoryUsername,
                repositoryPassword, isPrivateRepository, cartridgeAlias, cartridgeInfo, tenantDomain);
        log.info("-- Repository creation is done --- ");
        cartridgeSubscription.createSubscription(subscriber, cartridgeAlias, autoscalingPolicyName, deploymentPolicyName, repository);
        cartridgeSubscription.setSubscriptionKey(generateSubscriptionKey());
        
        //cartridgeSubscription.setClusterDomain(clusterDomain);
        //cartridgeSubscription.setClusterSubDomain(clusterDomain);        
        
        log.info("-- subscription key is generated --- ");
        log.info("Tenant [" + tenantId + "] with username [" + tenantAdminUsername +
                " subscribed to " + "] Cartridge Alias " + cartridgeAlias + ", Cartridge Type: " + cartridgeType +
                ", Repo URL: " + repositoryURL + ", Policy: " + autoscalingPolicyName);
        
        // TODO -- payload would need some additional params - like Puppet master IP .. etc
        
        Payload payload = PayloadFactory.getPayloadDataInstance(cartridgeInfo.getProvider(), cartridgeType,
                "/tmp/" + tenantDomain + "-" + cartridgeAlias + ".zip");
        PayloadArg payloadArg = cartridgeSubscription.createPayloadParameters();

        if (payloadArg != null) {
            //populate the payload
            payload.populatePayload(payloadArg);
            
            // populate payload from UI here
            payloadArg.setTenantRange(tenantRange);
            //payloadArg.setDeployment("default");   
            payloadArg.setServiceDomain(cartridgeAlias+"."+cartridgeInfo.getHostName()+".domain"); // This is cluster id
            cartridgeSubscription.setPayloadData(payload);
        }

        //get the payload parameters defined in the cartridge definition file for this cartridge type
        if (cartridgeInfo.getProperties() != null && cartridgeInfo.getProperties().length != 0) {

            StringBuilder customPayloadParamsBuilder = new StringBuilder();
            for(Property property : cartridgeInfo.getProperties()) {
                //check if a property is related to the payload. Currently this is done by checking if the
                //property name starts with 'payload_parameter.' suffix. If so the payload param name will
                //be taken as the substring from the index of '.' to the end of the property name.
                if(property.getName().startsWith(CartridgeConstants.CUSTOM_PAYLOAD_PARAM_NAME_PREFIX)) {
                    String payloadParamName = property.getName();
                    customPayloadParamsBuilder.append(",");
                    customPayloadParamsBuilder.append(payloadParamName.
                            substring(payloadParamName.indexOf(".") + 1));
                    customPayloadParamsBuilder.append("=");
                    customPayloadParamsBuilder.append(property.getValue());
                }
            }
            //if valid payload related parameters are found in the cartridge definition file, add them to the payload
            String customPayloadParamString = customPayloadParamsBuilder.toString();
            if(!customPayloadParamString.isEmpty()) {
                payload.populatePayload(customPayloadParamString);
                cartridgeSubscription.setPayloadData(payload);
            }
        }
        
        return cartridgeSubscription;
    	
    }*/

    private CartridgeSubscriptionInfo getCartridgeSubscriptionInfo(String tenantDomain, String alias)
            throws ADCException, NotSubscribedException {

        CartridgeSubscriptionInfo subscription;
        try {
            subscription = PersistenceManager.getSubscription(tenantDomain, alias);

        } catch (Exception e) {
            String msg = "Failed to get subscriptions for " + tenantDomain;
            log.error(msg, e);
            throw new ADCException(msg, e);
        }

        if (subscription == null) {
            String msg = "Tenant " + tenantDomain + " has not subscribed for cartridges";
            log.error(msg);
            throw new NotSubscribedException(msg, msg);
        }

        return subscription;

    }

    private List<CartridgeSubscriptionInfo> getCartridgeSubscriptions (int tenantId) throws ADCException, NotSubscribedException {

        List<CartridgeSubscriptionInfo> subscriptions;
        try {
            subscriptions = PersistenceManager.getSubscriptionsForTenant(tenantId);

        } catch (Exception e) {
            String msg = "Failed to get subscriptions for " + tenantId;
            log.error(msg, e);
            throw new ADCException(msg, e);
        }

        if (subscriptions == null) {
            String msg = "Tenant " + tenantId + " has not subscribed for cartridges";
            log.error(msg);
            throw new NotSubscribedException(msg, msg);
        }

        return subscriptions;
    }

    private CartridgeSubscription populateCartridgeSubscriptionInformation(CartridgeInfo cartridgeInfo,
                                                                           CartridgeSubscriptionInfo cartridgeSubscriptionInfo)
            throws ADCException {

        SubscriptionTenancyBehaviour tenancyBehaviour;
        if(cartridgeInfo.getMultiTenant()) {
            tenancyBehaviour = new SubscriptionMultiTenantBehaviour();
        } else {
            tenancyBehaviour = new SubscriptionSingleTenantBehaviour();
        }

        CartridgeSubscription cartridgeSubscription = CartridgeSubscriptionFactory.
                getCartridgeSubscriptionInstance(cartridgeInfo, tenancyBehaviour);

        cartridgeSubscription.setSubscriptionId(cartridgeSubscriptionInfo.getSubscriptionId());
        cartridgeSubscription.setAlias(cartridgeSubscriptionInfo.getAlias());
        cartridgeSubscription.setHostName(cartridgeSubscriptionInfo.getHostName());
        cartridgeSubscription.setClusterDomain(cartridgeSubscriptionInfo.getClusterDomain());
        cartridgeSubscription.setClusterSubDomain(cartridgeSubscriptionInfo.getClusterSubdomain());
        cartridgeSubscription.setMgtClusterDomain(cartridgeSubscriptionInfo.getMgtClusterDomain());
        cartridgeSubscription.setMgtClusterSubDomain(cartridgeSubscriptionInfo.getMgtClusterSubDomain());
        cartridgeSubscription.setAutoscalingPolicyName(cartridgeSubscriptionInfo.getPolicy());
        Subscriber subscriber = new Subscriber(CarbonContext.getThreadLocalCarbonContext().getUsername(),
                cartridgeSubscriptionInfo.getTenantId(), cartridgeSubscriptionInfo.getTenantDomain());
        cartridgeSubscription.setSubscriber(subscriber);
        cartridgeSubscription.setRepository(cartridgeSubscriptionInfo.getRepository());

        return cartridgeSubscription;
    }
}
