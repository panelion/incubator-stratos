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

package org.apache.stratos.adc.mgt.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.stratos.adc.mgt.dao.CartridgeSubscriptionInfo;
import org.apache.stratos.adc.mgt.internal.DataHolder;
import org.apache.stratos.adc.mgt.utils.PersistenceManager;
import org.apache.stratos.messaging.broker.publish.EventPublisher;
import org.apache.stratos.messaging.domain.tenant.Tenant;
import org.apache.stratos.messaging.event.tenant.CompleteTenantEvent;
import org.apache.stratos.messaging.util.Constants;
import org.wso2.carbon.ntask.core.Task;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tenant synchronizer task for publishing complete tenant event periodically
 * to message broker.
 */
public class TenantSynzhronizerTask implements Task {

    private static final Log log = LogFactory.getLog(TenantSynzhronizerTask.class);

    @Override
    public void init() {
    }

    @Override
    public void execute() {
        try {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Publishing complete tenant event"));
            }
            Tenant tenant;
            List<Tenant> tenants = new ArrayList<Tenant>();
            TenantManager tenantManager = DataHolder.getRealmService().getTenantManager();
            org.wso2.carbon.user.api.Tenant[] carbonTenants = tenantManager.getAllTenants();
            for (org.wso2.carbon.user.api.Tenant carbonTenant : carbonTenants) {
                // Create tenant
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Tenant found: [tenant-id] %d [tenant-domain] %s", carbonTenant.getId(), carbonTenant.getDomain()));
                }
                tenant = new Tenant(carbonTenant.getId(), carbonTenant.getDomain());
                // Add subscriptions
                List<CartridgeSubscriptionInfo> subscriptions = PersistenceManager.getSubscriptionsForTenant(tenant.getTenantId());
                for (CartridgeSubscriptionInfo subscription : subscriptions) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Tenant subscription found: [tenant-id] %d [tenant-domain] %s [service] %s",
                                   carbonTenant.getId(), carbonTenant.getDomain(), subscription.getCartridge()));
                    }
                    tenant.addServiceSubscription(subscription.getCartridge());
                }
                tenants.add(tenant);
            }
            CompleteTenantEvent event = new CompleteTenantEvent(tenants);
            EventPublisher eventPublisher = new EventPublisher(Constants.TENANT_TOPIC);
            eventPublisher.publish(event);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Could not publish complete tenant event", e);
            }
        }
    }

    @Override
    public void setProperties(Map<String, String> stringStringMap) {
    }
}
