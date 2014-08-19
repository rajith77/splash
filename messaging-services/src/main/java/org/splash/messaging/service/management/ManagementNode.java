/*
 *
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
 *
 */
package org.splash.messaging.service.management;

import org.splash.messaging.EventHandler;
import org.splash.messaging.InboundLink;
import org.splash.messaging.OutboundLink;
import org.splash.messaging.service.MessagingServiceException;

public interface ManagementNode extends EventHandler
{
    public static final class Factory
    {
        final static String className = System.getProperty("mgt_node.class",
                "org.splash.messaging.service.management.DefaultManagementNode");

        public static ManagementNode create() throws MessagingServiceException
        {
            try
            {
                Class<? extends ManagementNode> c = Class.forName(className).asSubclass(ManagementNode.class);
                return c.newInstance();
            }
            catch (Exception e)
            {
                throw new MessagingServiceException("Unable to instantiate the management node", e);
            }
        }
    }

    String getAddress();

    void init(InboundLink inLink, OutboundLink outLink, ManageableEntityLifecycleHandler lifeCycleHandler,
            ManagementEventHandler... handlers) throws MessagingServiceException;

    void registerType(Class<?> type, ManageableEntityFactory factory) throws MessagingServiceException;
}