/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.stratos.autoscaler.message.receiver.health;

import javax.jms.TextMessage;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implements topology event queue.
 */
public class HealthEventQueue extends LinkedBlockingQueue<TextMessage>{

	private static final long serialVersionUID = 2556240855574421561L;
	private static volatile HealthEventQueue instance;

    private HealthEventQueue(){
    }

    public static HealthEventQueue getInstance() {
        if (instance == null) {
            synchronized (HealthEventQueue.class){
                if (instance == null) {
                    instance = new HealthEventQueue();
                }
            }
        }
        return instance;
    }
}
