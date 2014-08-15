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
package org.splash.messaging.services;

import java.util.UUID;

import org.splash.messaging.AbstractEventHandler;
import org.splash.messaging.Connection;
import org.splash.messaging.ConnectionSettings;
import org.splash.messaging.CreditMode;
import org.splash.messaging.InboundLink;
import org.splash.messaging.InboundLinkMode;
import org.splash.messaging.Message;
import org.splash.messaging.Messaging;
import org.splash.messaging.MessagingException;
import org.splash.messaging.NetworkException;
import org.splash.messaging.OutboundLink;
import org.splash.messaging.OutboundLinkMode;
import org.splash.messaging.Session;
import org.splash.messaging.Tracker;

public class RouterNode extends AbstractEventHandler
{
    private Connection _conn;

    private String _routerMgtAddress;

    private Router _router;

    private Session _ssn;

    private OutboundLink _outLink;

    private InboundLink _mgtLink;

    private int _capacity = 0;

    private String _id = null;

    RouterNode(ConnectionSettings settings, String routerAddress, Router router, int capacity) throws NetworkException,
            MessagingException
    {
        _conn = Messaging.connect(settings, this);
        _routerMgtAddress = routerAddress;
        _router = router;
        _capacity = capacity;
        _id = Router.class.getSimpleName() + "-" + UUID.randomUUID().toString();
        setup();
    }

    void setup() throws MessagingException
    {
        _ssn = _conn.createSession();
        _mgtLink = _ssn.createInboundLink(_routerMgtAddress, InboundLinkMode.AT_LEAST_ONCE, CreditMode.AUTO);
        _mgtLink.setCredits(_capacity);

        _outLink = _ssn.createOutboundLink(_id, OutboundLinkMode.AT_LEAST_ONCE);
    }

    @Override
    public void onSettled(Tracker tracker)
    {
        // TODO Auto-generated method stub
        super.onSettled(tracker);
    }

    @Override
    public void onMessage(InboundLink link, Message msg)
    {
        // TODO Auto-generated method stub
        super.onMessage(link, msg);
    }
    
    public static void main(String[] args) throws Exception
    {
        String host = System.getProperty("peer.host", "localhost");
        int port = Integer.getInteger("peer.port", 5672);
        String routerMgtAddress = System.getProperty("router.mgt.address", "DEFAULT_ROUTER");
        String routerClass = System.getProperty("router.class", "org.splash.messaging.service.DefaultRouter");
        int capacity = Integer.getInteger("router.capacity", 5000);

        ConnectionSettings settings = new ConnectionSettings();
        settings.setHost(host);
        settings.setPort(port);

        RouterNode node = new RouterNode(settings, routerMgtAddress, Router.Factory.create(routerClass), capacity);
    }
}
