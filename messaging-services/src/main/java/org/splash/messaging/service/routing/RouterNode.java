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
package org.splash.messaging.service.routing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.splash.logging.Logger;
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
import org.splash.messaging.proton.InboundMessage;
import org.splash.messaging.service.MessagingServiceException;
import org.splash.messaging.service.management.ManageableEntity;
import org.splash.messaging.service.management.ManageableEntityFactory;
import org.splash.messaging.service.management.ManageableEntityLifecycleHandler;
import org.splash.messaging.service.management.ManagementNode;

public class RouterNode extends AbstractEventHandler implements ManageableEntityLifecycleHandler
{
    private static final Logger _logger = Logger.get(RouterNode.class);

    private Connection _conn;

    private String _routerMgtAddress;

    private String _DLQAddress;

    private Session _ssn;

    private OutboundLink _outLink;

    private InboundLink _mgtLink;

    private int _capacity = 0;

    private String _id = null;

    private ManagementNode _mgtNode;

    final private Map<String, Router> _routersByAddress = new HashMap<String, Router>();

    final private Map<String, Router> _routersById = new HashMap<String, Router>();

    RouterNode(ConnectionSettings settings, String mgtAddress, String dlqAddress, int capacity)
            throws NetworkException, MessagingException
    {
        super();
        _conn = Messaging.connect(settings, this);
        _routerMgtAddress = mgtAddress;
        _DLQAddress = dlqAddress;
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
        _mgtNode = ManagementNode.Factory.create();
        _mgtNode.init(_mgtLink, _outLink, this);

        // Registering standard routers
        registerRouterType(DirectMatchRouter.class, DirectMatchRouter.Factory.get());
        registerRouterType(WildcardRouter.class, WildcardRouter.Factory.get());

        // Registering standard route types
        registerRouteType(DirectMatchRoute.class, DirectMatchRoute.Factory.get());
        registerRouteType(WildcardRoute.class, WildcardRoute.Factory.get());
    }

    public void registerRouterType(Class<? extends Router> router, ManageableEntityFactory factory)
            throws MessagingServiceException
    {
        _mgtNode.registerType(router, factory);
    }

    public void registerRouteType(Class<? extends Route> route, ManageableEntityFactory factory)
            throws MessagingServiceException
    {
        _mgtNode.registerType(route, factory);
    }

    @Override
    public void onSettled(Tracker tracker)
    {
        Message msg = (Message) tracker.get("INBOUND_MSG");
        try
        {
            _ssn.settle(msg);
        }
        catch (NetworkException e)
        {
            _logger.warn(e, "Network error when settling message");
        }
        catch (MessagingException e)
        {
            _logger.warn(e, "Exception when settling message");
        }
    }

    @Override
    public void onMessage(InboundLink link, Message msg)
    {
        if (link.equals(_mgtLink))
        {
            super.onMessage(link, msg);
        }
        else
        {
            if (_routersByAddress.containsKey(msg.getAddress()))
            {
                List<String> addrList = _routersByAddress.get(msg.getAddress()).route(msg);
                for (String addr : addrList)
                {
                    send(addr, msg);
                }
            }
            else
            {
                send(_DLQAddress, msg);
            }
        }
    }

    void send(String address, Message msg)
    {
        msg.setAddress(address);
        boolean preSettled = ((InboundMessage) msg).isPreSettled();
        try
        {
            Tracker tracker = _outLink.send(msg);
            if (!preSettled)
            {
                tracker.put("INBOUND_MSG", msg);
            }

        }
        catch (NetworkException e)
        {
            // TODO handle network exception and retry the delivery
            _logger.warn(e, "Network error when sending message for %s", address);
        }
        catch (MessagingException e)
        {
            _logger.warn(e, "Exception when sending message for %s", address);
        }
    }

    // Sets up a link with the router network for receiving messages
    InboundLink createLink(String address)
    {
        InboundLink rcv = null;
        try
        {
            rcv = _ssn.createInboundLink(address, InboundLinkMode.AT_LEAST_ONCE, CreditMode.AUTO);
            rcv.setCredits(_capacity);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rcv;
    }

    @Override
    public void entityCreated(ManageableEntity entity)
    {
        if (entity instanceof Router)
        {
            Router router = (Router) entity;
            router.setLink(createLink(router.getAddress()));
            _routersByAddress.put(router.getAddress(), router);
            _routersById.put(router.getID(), router);
            _logger.info("Added new router %s", router);
        }
        else
        {
            Route route = (Route) entity;
            Router router = _routersById.get(route.getRouterId());
            route.setRouter(router);
            router.addRoute(route);
            _logger.info("Added new route %s, for router %s", route, route.getRouter());
        }

    }

    @Override
    public void entityUpdated(ManageableEntity entity)
    {
    }

    @Override
    public void entityDeleted(ManageableEntity entity)
    {
        if (entity instanceof Router)
        {
            _routersByAddress.remove(((Router) entity).getAddress());
            // TODO remove all routes associated with this router
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws Exception
    {
        String host = System.getProperty("router.peer_host", "localhost");
        int port = Integer.getInteger("router.peer_port", 5672);
        String routerMgtAddress = System.getProperty("router.mgt_address", "SPLASH_ROUTER");
        String routerDlqAddress = System.getProperty("router.dlq_address", "SPLASH_DLQ");
        int capacity = Integer.getInteger("router.capacity", 5000);

        ConnectionSettings settings = new ConnectionSettings();
        settings.setHost(host);
        settings.setPort(port);

        RouterNode node = new RouterNode(settings, routerMgtAddress, routerDlqAddress, capacity);
    }
}