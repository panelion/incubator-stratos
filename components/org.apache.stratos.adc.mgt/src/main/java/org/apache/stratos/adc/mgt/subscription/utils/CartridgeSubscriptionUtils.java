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

package org.apache.stratos.adc.mgt.subscription.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.adc.mgt.deploy.service.Service;
import org.apache.stratos.adc.mgt.payload.BasicPayloadData;
import org.apache.stratos.adc.mgt.subscription.CartridgeSubscription;
import org.apache.stratos.cloud.controller.pojo.CartridgeInfo;
import org.apache.stratos.messaging.broker.publish.EventPublisher;
import org.apache.stratos.messaging.event.tenant.TenantSubscribedEvent;
import org.apache.stratos.messaging.event.tenant.TenantUnSubscribedEvent;
import org.apache.stratos.messaging.util.Constants;

public class CartridgeSubscriptionUtils {

    private static Log log = LogFactory.getLog(CartridgeSubscriptionUtils.class);

    public static BasicPayloadData createBasicPayload (CartridgeSubscription cartridgeSubscription) {

        BasicPayloadData basicPayloadData = new BasicPayloadData();
        basicPayloadData.setApplicationPath(cartridgeSubscription.getCartridgeInfo().getBaseDir());
        basicPayloadData.setSubscriptionKey(cartridgeSubscription.getSubscriptionKey());
        basicPayloadData.setClusterId(cartridgeSubscription.getClusterDomain());
        basicPayloadData.setDeployment("default");//currently hard coded to default
        if(cartridgeSubscription.getRepository() != null) {
            basicPayloadData.setGitRepositoryUrl(cartridgeSubscription.getRepository().getUrl());
        }
        basicPayloadData.setHostName(cartridgeSubscription.getHostName());
        basicPayloadData.setMultitenant(String.valueOf(cartridgeSubscription.getCartridgeInfo().getMultiTenant()));
        basicPayloadData.setPortMappings(createPortMappingPayloadString(cartridgeSubscription.getCartridgeInfo()));
        basicPayloadData.setServiceName(cartridgeSubscription.getCartridgeInfo().getType());
        basicPayloadData.setSubscriptionAlias(cartridgeSubscription.getAlias());
        basicPayloadData.setTenantId(cartridgeSubscription.getSubscriber().getTenantId());
        //TODO:remove. we do not want to know about the tenant rance in subscription!
        if(cartridgeSubscription.getCartridgeInfo().getMultiTenant() ||
                cartridgeSubscription.getSubscriber().getTenantId() == -1234) {  //TODO: fix properly
            basicPayloadData.setTenantRange("*");
        } else {
            basicPayloadData.setTenantRange(String.valueOf(cartridgeSubscription.getSubscriber().getTenantId()));
        }

        return basicPayloadData;
    }

    public static BasicPayloadData createBasicPayload (Service service) {

        BasicPayloadData basicPayloadData = new BasicPayloadData();
        basicPayloadData.setApplicationPath(service.getCartridgeInfo().getBaseDir());
        basicPayloadData.setSubscriptionKey(service.getSubscriptionKey());
        basicPayloadData.setClusterId(service.getClusterId());
        basicPayloadData.setDeployment("default");//currently hard coded to default
        basicPayloadData.setHostName(service.getHostName());
        basicPayloadData.setMultitenant(String.valueOf(service.getCartridgeInfo().getMultiTenant()));
        basicPayloadData.setPortMappings(createPortMappingPayloadString(service.getCartridgeInfo()));
        basicPayloadData.setServiceName(service.getType());
        basicPayloadData.setTenantId(service.getTenantId());
        basicPayloadData.setTenantRange(service.getTenantRange());

        return basicPayloadData;
    }

    private static String createPortMappingPayloadString (CartridgeInfo cartridgeInfo) {

        // port mappings
        StringBuilder portMapBuilder = new StringBuilder();
        org.apache.stratos.cloud.controller.pojo.PortMapping[] portMappings = cartridgeInfo.getPortMappings();
        for (org.apache.stratos.cloud.controller.pojo.PortMapping portMapping : portMappings) {
            String port = portMapping.getPort();
            portMapBuilder.append(port).append("|");
        }

        // remove last "|" character
        String portMappingString = portMapBuilder.toString().replaceAll("\\|$", "");

        return portMappingString;
    }

    public static String generateSubscriptionKey() {
        String key = RandomStringUtils.randomAlphanumeric(16);
        log.info("Generated key  : " + key); // TODO -- remove the log
        return key;
    }

    public static void publishTenantSubscribedEvent(int tenantId, String serviceName) {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Publishing tenant subscribed event: [tenant-id] %d [service] %s", tenantId, serviceName));
            }
            TenantSubscribedEvent subscribedEvent = new TenantSubscribedEvent(tenantId, serviceName);
            EventPublisher eventPublisher = new EventPublisher(Constants.TENANT_TOPIC);
            eventPublisher.publish(subscribedEvent);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Could not publish tenant subscribed event: [tenant-id] %d [service] %s", tenantId, serviceName), e);
            }
        }
    }

    public static void publishTenantUnSubscribedEvent(int tenantId, String serviceName) {
        try {
            if(log.isInfoEnabled()) {
                log.info(String.format("Publishing tenant un-subscribed event: [tenant-id] %d [service] %s", tenantId, serviceName));
            }
            TenantUnSubscribedEvent event = new TenantUnSubscribedEvent(tenantId, serviceName);
            EventPublisher eventPublisher = new EventPublisher(Constants.TENANT_TOPIC);
            eventPublisher.publish(event);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(String.format("Could not publish tenant un-subscribed event: [tenant-id] %d [service] %s", tenantId, serviceName), e);
            }
        }
    }
}
